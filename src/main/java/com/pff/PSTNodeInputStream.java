/*
 * Copyright 2010 Richard Johnson & Orin Eman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ---
 *
 * This file is part of java-libpst.
 *
 * java-libpst is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * java-libpst is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with java-libpst. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.pff;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

/**
 * this input stream basically "maps" an input stream on top of the random
 * access file
 * 
 * @author richard
 */
public class PSTNodeInputStream extends InputStream {

    private PSTFileContent in;
    private PSTFile pstFile;
    private final LinkedList<Long> skipPoints = new LinkedList<>();
    private final LinkedList<OffsetIndexItem> indexItems = new LinkedList<>();
    private int currentBlock = 0;
    private long currentLocation = 0;

    private byte[] allData = null;

    private long length = 0;

    private boolean encrypted = false;

    PSTNodeInputStream(final PSTFile pstFile, final byte[] attachmentData) throws PSTException {
        this.allData = attachmentData;
        this.length = this.allData.length;
        this.encrypted = pstFile.getEncryptionType() == PSTFile.ENCRYPTION_TYPE_COMPRESSIBLE;
        this.currentBlock = 0;
        this.currentLocation = 0;
        this.detectZlib();
    }

    PSTNodeInputStream(final PSTFile pstFile, final byte[] attachmentData, final boolean encrypted)
        throws PSTException {
        this.allData = attachmentData;
        this.encrypted = encrypted;
        this.length = this.allData.length;
        this.currentBlock = 0;
        this.currentLocation = 0;
        this.detectZlib();
    }

    PSTNodeInputStream(final PSTFile pstFile, final PSTDescriptorItem descriptorItem) throws IOException, PSTException {
        this.in = pstFile.getContentHandle();
        this.pstFile = pstFile;
        this.encrypted = pstFile.getEncryptionType() == PSTFile.ENCRYPTION_TYPE_COMPRESSIBLE;

        // we want to get the first block of data and see what we are dealing
        // with
        final OffsetIndexItem offsetItem = pstFile.getOffsetIndexNode(descriptorItem.offsetIndexIdentifier);
        this.loadFromOffsetItem(offsetItem);
        this.currentBlock = 0;
        this.currentLocation = 0;
        this.detectZlib();
    }

    PSTNodeInputStream(final PSTFile pstFile, final OffsetIndexItem offsetItem) throws IOException, PSTException {
        this.in = pstFile.getContentHandle();
        this.pstFile = pstFile;
        this.encrypted = pstFile.getEncryptionType() == PSTFile.ENCRYPTION_TYPE_COMPRESSIBLE;
        // this.encrypted = true;
        this.loadFromOffsetItem(offsetItem);
        this.currentBlock = 0;
        this.currentLocation = 0;
        this.detectZlib();
    }

    private final boolean isZlib = false;

    private void detectZlib() throws PSTException {
        // not really sure how this is meant to work, kind of going by feel
        // here.
        if (this.length < 4) {
            return;
        }
        try {
            if (this.read() == 0x78 && this.read() == 0x9c) {
                boolean multiStreams = false;
                if (this.indexItems.size() > 1) {
                    final OffsetIndexItem i = this.indexItems.get(1);
                    this.in.seek(i.fileOffset);
                    multiStreams = (this.in.read() == 0x78 && this.in.read() == 0x9c);
                }
                // we are a compressed block, decompress the whole thing into a
                // buffer
                // and replace our contents with that.
                // firstly, if we have blocks, use that as the length
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int) this.length);
                if (multiStreams) {
                    int y = 0;
                    for (final OffsetIndexItem i : this.indexItems) {
                        final byte[] inData = new byte[i.size];
                        this.in.seek(i.fileOffset);
                        this.in.readCompletely(inData);
                        final InflaterOutputStream inflaterStream = new InflaterOutputStream(outputStream);
                        //try {
                            inflaterStream.write(inData);
                            inflaterStream.close();
                        //} catch (Exception err) {
                        //    System.out.println("Y: " + y);
                        //    System.out.println(err);
                        //    PSTObject.printHexFormatted(inData, true);
                        //    System.exit(0);
                        //}
                        y++;
                    }
                    this.indexItems.clear();
                    this.skipPoints.clear();
                } else {
                    int compressedLength = (int) this.length;
                    if (this.indexItems.size() > 0) {
                        compressedLength = 0;
                        for (final OffsetIndexItem i : this.indexItems) {
                            //System.out.println(i);
                            compressedLength += i.size;
                        }
                    }
                    final byte[] inData = new byte[compressedLength];
                    this.seek(0);
                    this.readCompletely(inData);

                    final InflaterOutputStream inflaterStream = new InflaterOutputStream(outputStream);
                    inflaterStream.write(inData);
                    inflaterStream.close();
                }
                outputStream.close();
                final byte[] output = outputStream.toByteArray();
                this.allData = output;
                this.currentLocation = 0;
                this.currentBlock = 0;
                this.length = this.allData.length;
            }
            this.seek(0);
        } catch (final IOException err) {
            throw new PSTException("Unable to decompress reportedly compressed block", err);
        }
    }

    private void loadFromOffsetItem(final OffsetIndexItem offsetItem) throws IOException, PSTException {
        boolean bInternal = (offsetItem.indexIdentifier & 0x02) != 0;

        this.in.seek(offsetItem.fileOffset);
        final byte[] data = new byte[offsetItem.size];
        this.in.readCompletely(data);
        // PSTObject.printHexFormatted(data, true);

        if (bInternal) {
            // All internal blocks are at least 8 bytes long...
            if (offsetItem.size < 8) {
                throw new PSTException("Invalid internal block size");
            }

            if (data[0] == 0x1) {
                bInternal = false;
                // we are a xblock, or xxblock
                this.length = PSTObject.convertLittleEndianBytesToLong(data, 4, 8);
                // go through all of the blocks and create skip points.
                this.getBlockSkipPoints(data);
                return;
            }
        }

        // (Internal blocks aren't compressed)
        if (bInternal) {
            this.encrypted = false;
        }
        this.allData = data;
        this.length = this.allData.length;

    }

    public boolean isEncrypted() {
        return this.encrypted;
    }

    private void getBlockSkipPoints(final byte[] data) throws IOException, PSTException {
        if (data[0] != 0x1) {
            throw new PSTException("Unable to process XBlock, incorrect identifier");
        }

        final int numberOfEntries = (int) PSTObject.convertLittleEndianBytesToLong(data, 2, 4);

        int arraySize = 8;
        if (this.pstFile.getPSTFileType() == PSTFile.PST_TYPE_ANSI) {
            arraySize = 4;
        }
        if (data[1] == 0x2) {
            // XXBlock
            int offset = 8;
            for (int x = 0; x < numberOfEntries; x++) {
                long bid = PSTObject.convertLittleEndianBytesToLong(data, offset, offset + arraySize);
                bid &= 0xfffffffe;
                // get the details in this block and
                final OffsetIndexItem offsetItem = this.pstFile.getOffsetIndexNode(bid);
                this.in.seek(offsetItem.fileOffset);
                final byte[] blockData = new byte[offsetItem.size];
                this.in.readCompletely(blockData);
                this.getBlockSkipPoints(blockData);
                offset += arraySize;
            }
        } else if (data[1] == 0x1) {
            // normal XBlock
            int offset = 8;
            for (int x = 0; x < numberOfEntries; x++) {
                long bid = PSTObject.convertLittleEndianBytesToLong(data, offset, offset + arraySize);
                bid &= 0xfffffffe;
                // get the details in this block and add it to the list
                final OffsetIndexItem offsetItem = this.pstFile.getOffsetIndexNode(bid);
                this.indexItems.add(offsetItem);
                this.skipPoints.add(this.currentLocation);
                this.currentLocation += offsetItem.size;
                offset += arraySize;
            }
        }
    }

    public long length() {
        return this.length;
    }

    @Override
    public int read() throws IOException {

        // first deal with items < 8K and we have all the data already
        if (this.allData != null) {
            if (this.currentLocation == this.length) {
                // EOF
                return -1;
            }
            int value = this.allData[(int) this.currentLocation] & 0xFF;
            this.currentLocation++;
            if (this.encrypted) {
                value = PSTObject.compEnc[value];
            }
            return value;
        }

        OffsetIndexItem item = this.indexItems.get(this.currentBlock);
        long skipPoint = this.skipPoints.get(this.currentBlock);
        if (this.currentLocation + 1 > skipPoint + item.size) {
            // got to move to the next block
            this.currentBlock++;

            if (this.currentBlock >= this.indexItems.size()) {
                return -1;
            }

            item = this.indexItems.get(this.currentBlock);
            skipPoint = this.skipPoints.get(this.currentBlock);
        }

        // get the next byte.
        final long pos = (item.fileOffset + (this.currentLocation - skipPoint));
        if (this.in.getFilePointer() != pos) {
            this.in.seek(pos);
        }

        int output = this.in.read();
        if (output < 0) {
            return -1;
        }
        if (this.encrypted) {
            output = PSTObject.compEnc[output];
        }

        this.currentLocation++;

        return output;
    }

    private int totalLoopCount = 0;

    /**
     * Read a block from the input stream, ensuring buffer is completely filled.
     * Recommended block size = 8176 (size used internally by PSTs)
     * 
     * @param target buffer to fill
     * @throws IOException
     */
    public void readCompletely(final byte[] target) throws IOException {
        int offset = 0;
        int numRead = 0;
        while (offset < target.length) {
            numRead = this.read(target, offset, target.length - offset);
            if (numRead == -1) {
                throw new IOException("unexpected EOF encountered attempting to read from PSTInputStream");
            }
            offset += numRead;
        }
    }

    /**
     * Read a block from the input stream.
     * Recommended block size = 8176 (size used internally by PSTs)
     * 
     * @param output
     * @return
     * @throws IOException
     */
    @Override
    public int read(final byte[] output) throws IOException {
        // this method is implemented in an attempt to make things a bit faster
        // than the byte-by-byte read() crap above.
        // it's tricky 'cause we have to copy blocks from a few different areas.

        if (this.currentLocation == this.length) {
            // EOF
            return -1;
        }

        // first deal with the small stuff
        if (this.allData != null) {
            final int bytesRemaining = (int) (this.length - this.currentLocation);
            if (output.length >= bytesRemaining) {
                System.arraycopy(this.allData, (int) this.currentLocation, output, 0, bytesRemaining);
                if (this.encrypted) {
                    PSTObject.decode(output);
                }
                this.currentLocation += bytesRemaining; // should be = to
                                                        // this.length
                return bytesRemaining;
            } else {
                System.arraycopy(this.allData, (int) this.currentLocation, output, 0, output.length);
                if (this.encrypted) {
                    PSTObject.decode(output);
                }
                this.currentLocation += output.length;
                return output.length;
            }
        }

        boolean filled = false;
        int totalBytesFilled = 0;
        // while we still need to fill the array
        while (!filled) {

            // fill up the output from where we are
            // get the current block, either to the end, or until the length of
            // the output
            final OffsetIndexItem offset = this.indexItems.get(this.currentBlock);
            final long skipPoint = this.skipPoints.get(this.currentBlock);
            final int currentPosInBlock = (int) (this.currentLocation - skipPoint);
            this.in.seek(offset.fileOffset + currentPosInBlock);

            final long nextSkipPoint = skipPoint + offset.size;
            int bytesRemaining = (output.length - totalBytesFilled);
            // if the total bytes remaining if going to take us past our size
            if (bytesRemaining > ((int) (this.length - this.currentLocation))) {
                // we only have so much to give
                bytesRemaining = (int) (this.length - this.currentLocation);
            }

            if (nextSkipPoint >= this.currentLocation + bytesRemaining) {
                // we can fill the output with the rest of our current block!
                final byte[] chunk = new byte[bytesRemaining];
                this.in.readCompletely(chunk);

                System.arraycopy(chunk, 0, output, totalBytesFilled, bytesRemaining);
                totalBytesFilled += bytesRemaining;
                // we are done!
                filled = true;
                this.currentLocation += bytesRemaining;
            } else {
                // we need to read out a whole chunk and keep going
                final int bytesToRead = offset.size - currentPosInBlock;
                final byte[] chunk = new byte[bytesToRead];
                this.in.readCompletely(chunk);
                System.arraycopy(chunk, 0, output, totalBytesFilled, bytesToRead);
                totalBytesFilled += bytesToRead;
                this.currentBlock++;
                this.currentLocation += bytesToRead;
            }
            this.totalLoopCount++;
        }

        // decode the array if required
        if (this.encrypted) {
            PSTObject.decode(output);
        }

        // fill up our chunk
        // move to the next chunk
        return totalBytesFilled;
    }

    @Override
    public int read(final byte[] output, final int offset, int length) throws IOException {
        if (this.currentLocation == this.length) {
            // EOF
            return -1;
        }

        if (output.length < length) {
            length = output.length;
        }

        final byte[] buf = new byte[length];
        final int lengthRead = this.read(buf);

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

    /**
     * Get the offsets (block positions) used in the array
     * 
     * @return
     */
    public Long[] getBlockOffsets() {
        if (this.skipPoints.size() == 0) {
            final Long[] output = new Long[1];
            output[0] = this.length;
            return output;
        } else {
            final Long[] output = new Long[this.skipPoints.size()];
            for (int x = 0; x < output.length; x++) {
                output[x] = new Long(this.skipPoints.get(x) + this.indexItems.get(x).size);
            }
            return output;
        }
    }

    /*
     * public int[] getBlockOffsetsInts() {
     * int[] out = new int[this.skipPoints.size()];
     * for (int x = 0; x < this.skipPoints.size(); x++) {
     * out[x] = this.skipPoints.get(x).intValue();
     * }
     * return out;
     * }
     *
     */

    public void seek(final long location) throws IOException, PSTException {
        // not past the end!
        if (location > this.length) {
            throw new PSTException(
                "Unable to seek past end of item! size = " + this.length + ", seeking to:" + location);
        }

        // are we already there?
        if (this.currentLocation == location) {
            return;
        }

        // get us to the right block
        long skipPoint = 0;
        this.currentBlock = 0;
        if (this.allData == null) {
            skipPoint = this.skipPoints.get(this.currentBlock + 1);
            while (location >= skipPoint) {
                this.currentBlock++;
                // is this the last block?
                if (this.currentBlock == this.skipPoints.size() - 1) {
                    // that's all folks
                    break;
                } else {
                    skipPoint = this.skipPoints.get(this.currentBlock + 1);
                }
            }
        }

        // now move us to the right position in there
        this.currentLocation = location;

        if (this.allData == null) {
            long blockStart = this.indexItems.get(this.currentBlock).fileOffset;
            final long newFilePos = blockStart + (location - skipPoint);
            this.in.seek(newFilePos);
        }

    }

    public long seekAndReadLong(final long location, final int bytes) throws IOException, PSTException {
        this.seek(location);
        final byte[] buffer = new byte[bytes];
        this.readCompletely(buffer);
        return PSTObject.convertLittleEndianBytesToLong(buffer);
    }

    public PSTFile getPSTFile() {
        return this.pstFile;
    }

}
