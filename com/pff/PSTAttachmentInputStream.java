/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.pff;

import java.io.*;
import java.util.*;

/**
 * this input stream basically "maps" an input stream on top of the random access file
 * @author richard
 */
public class PSTAttachmentInputStream extends InputStream {

	private RandomAccessFile in;
	private PSTFile pstFile;
	private LinkedList<Long> skipPoints = new LinkedList<Long>();
	private LinkedList<OffsetIndexItem> indexItems = new LinkedList<OffsetIndexItem>();
	private int currentBlock = 0;
	private long currentLocation = 0;

	private byte[] allData = null;

	private long length = 0;

	PSTAttachmentInputStream(PSTFile pstFile, byte[] attachmentData) {
		this.allData = attachmentData;
		this.length = this.allData.length;
	}

	PSTAttachmentInputStream(PSTFile pstFile, PSTDescriptorItem descriptorItem)
			throws IOException, PSTException
	{
		this.in = pstFile.getFileHandle();
		this.pstFile = pstFile;

		// we want to get the first block of data and see what we are dealing with
		OffsetIndexItem offsetItem = PSTObject.getOffsetIndexNode(in, descriptorItem.offsetIndexIdentifier);
		in.seek(offsetItem.fileOffset);
		byte[] data = new byte[offsetItem.size];
		in.read(data);

		// get the total size
		if (data[0] == 0x1) {
			// we are a block, or xxblock
			length = PSTObject.convertLittleEndianBytesToLong(data, 4, 8);
			// go through all of the blocks and create skip points.
			this.getBlockSkipPoints(data);
		} else {
			this.allData = descriptorItem.getData();
			this.length = this.allData.length;
		}
		this.currentLocation = 0;

	}

	private void getBlockSkipPoints(byte[] data)
			throws IOException, PSTException
	{
		if (data[0] != 0x1) {
			throw new PSTException("Unable to process XBlock, incorrect identifier");
		}

		int numberOfEntries = (int)PSTObject.convertLittleEndianBytesToLong(data, 2, 4);

		if (data[1] == 0x2) {
			// XXBlock
			int offset = 8;
			for (int x = 0; x < numberOfEntries; x++) {
				long bid = PSTObject.convertLittleEndianBytesToLong(data, offset, offset+8);
				bid &= 0xfffffffe;
				// get the details in this block and
				OffsetIndexItem offsetItem = PSTObject.getOffsetIndexNode(in, bid);
				in.seek(offsetItem.fileOffset);
				byte[] blockData = new byte[offsetItem.size];
				in.read(blockData);
				this.getBlockSkipPoints(blockData);
				offset += 8;
			}
		} else if (data[1] == 0x1) {
			// normal XBlock
			int offset = 8;
			for (int x = 0; x < numberOfEntries; x++) {
				long bid = PSTObject.convertLittleEndianBytesToLong(data, offset, offset+8);
				bid &= 0xfffffffe;
				// get the details in this block and add it to the list
				OffsetIndexItem offsetItem = PSTObject.getOffsetIndexNode(in, bid);
				this.indexItems.add(offsetItem);
				this.skipPoints.add(this.currentLocation);
				this.currentLocation += offsetItem.size;
				offset += 8;
			}
		}
	}

	public long length() {
		return this.length;
	}

	@Override
	public int read()
			throws IOException
	{

		// first deal with items < 8K and we have all the data already
		if (this.allData != null) {
			if (this.currentLocation == this.length) {
				// EOF
				return -1;
			}
			int value = this.allData[(int)this.currentLocation];
			this.currentLocation++;
			return value;
		}

		OffsetIndexItem item = this.indexItems.get(this.currentBlock);
		long skipPoint = this.skipPoints.get(currentBlock);
		if (this.currentLocation+1 > skipPoint+item.size) {
			// got to move to the next block
			this.currentBlock++;

			if (this.currentBlock >= this.indexItems.size()) {
				return -1;
			}

			item = this.indexItems.get(this.currentBlock);
			skipPoint = this.skipPoints.get(currentBlock);
		}

		// get the next byte.
		long pos = (item.fileOffset + (this.currentLocation - skipPoint));
		if (in.getFilePointer()  != pos) {
			in.seek(pos);
		}

		int output = in.read();
		if (output < 0) {
			return -1;
		}
		if (this.pstFile.getEncryptionType() == PSTFile.ENCRYPTION_TYPE_COMPRESSIBLE) {
			output = PSTObject.compEnc[output];
		}

		this.currentLocation++;

		return output;
	}

	private int totalLoopCount = 0;

	/**
	 * Read a block from the input stream.
	 * Recommended block size = 8176 (size used internally by PSTs)
	 * @param output
	 * @return
	 * @throws IOException
	 */
	@Override
	public int read(byte[] output)
			throws IOException
	{
		// this method is implemented in an attempt to make things a bit faster than the byte-by-byte read() crap above.
		// it's tricky 'cause we have to copy blocks from a few different areas.

		if (this.currentLocation == this.length) {
			// EOF
			return -1;
		}

		// first deal with the small stuff
		if (this.allData != null) {
			int bytesRemaining = (int)(this.length - this.currentLocation);
			if (output.length >= bytesRemaining) {
				System.arraycopy(this.allData, (int)this.currentLocation, output, 0, bytesRemaining);
				this.currentLocation += bytesRemaining; // should be = to this.length
				return bytesRemaining;
			} else {
				System.arraycopy(this.allData, (int)this.currentLocation, output, 0, output.length);
				this.currentLocation += output.length;
				return output.length;
			}
		}

		boolean filled = false;
		int totalBytesFilled = 0;
		// while we still need to fill the array
		while (!filled) {

			// fill up the output from where we are
			// get the current block, either to the end, or until the length of the output
			OffsetIndexItem offset = this.indexItems.get(this.currentBlock);
			long skipPoint = this.skipPoints.get(currentBlock);
			int currentPosInBlock = (int)(this.currentLocation - skipPoint);
			in.seek(offset.fileOffset + currentPosInBlock);

			long nextSkipPoint = skipPoint + offset.size;
			int bytesRemaining = (output.length - totalBytesFilled);
			// if the total bytes remaining if going to take us past our size
			if (bytesRemaining > ((int)(this.length - this.currentLocation))) {
				// we only have so much to give
				bytesRemaining = (int)(this.length - this.currentLocation);
			}

			if (nextSkipPoint >= this.currentLocation + bytesRemaining) {
				// we can fill the output with the rest of our current block!
				byte[] chunk = new byte[bytesRemaining];
				in.read(chunk);

				System.arraycopy(chunk, 0, output, totalBytesFilled, bytesRemaining);
				totalBytesFilled += bytesRemaining;
				// we are done!
				filled = true;
				this.currentLocation += bytesRemaining;
			} else {
				// we need to read out a whole chunk and keep going
				int bytesToRead = offset.size - currentPosInBlock;
				byte[] chunk = new byte[bytesToRead];
				in.read(chunk);
				System.arraycopy(chunk, 0, output, totalBytesFilled, bytesToRead);
				totalBytesFilled += bytesToRead;
				this.currentBlock++;
				this.currentLocation += bytesToRead;
			}
			totalLoopCount++;
		}

		// decode the array if required
		if (this.pstFile.getEncryptionType() == PSTFile.ENCRYPTION_TYPE_COMPRESSIBLE) {
			PSTObject.decode(output);
		}

		// fill up our chunk
		// move to the next chunk
		return totalBytesFilled;
	}

	@Override
	public int read(byte[] output, int offset, int length)
			throws IOException
	{
		if (this.currentLocation == this.length) {
			// EOF
			return -1;
		}

		if (output.length < length) {
			length = output.length;
		}

		byte[] buf = new byte[length];
		int lengthRead = this.read(buf);

		System.arraycopy(buf, 0, output, offset, lengthRead);

		return lengthRead;
	}


	@Override
	public void reset() {
		this.currentBlock = 0;
		this.currentLocation = 0;
	}

	@Override
	public boolean markSupported() {
		return false;
	}

}
