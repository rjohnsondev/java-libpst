/**
 * 
 */
package com.pff;

import java.io.*;
import java.util.*;


/**
 * @author toweruser
 *
 */
public class PSTAttachment extends PSTObject {
	
	PSTAttachment(PSTFile theFile, PSTTableBC table, HashMap<Integer, PSTDescriptorItem> localDescriptorItems) {
		super(theFile, null, table, localDescriptorItems);
	}
	
	public int getSize() {
		return this.getIntItem(0x0e20);
	}

	public Date getCreationTime() {
		return this.getDateItem(0x3007);
	}

	public Date getModificationTime() {
		return this.getDateItem(0x3008);
	}
	
	public byte[] getFileContents()
		throws IOException, PSTException
	{
		if (this.getFilesize() == 0) {
			throw new PSTException("Attachment is empty!");
		}
		
		PSTTableBCItem attachmentDataObject = items.get(0x3701);
		
		PSTDescriptorItem descriptorItemNested = this.localDescriptorItems.get(attachmentDataObject.entryValueReference); 
		OffsetIndexItem attachmentOffsetNested = PSTObject.getOffsetIndexNode(this.pstFile.getFileHandle(), descriptorItemNested.offsetIndexIdentifier);
		
		RandomAccessFile in = this.pstFile.getFileHandle();
		in.seek(attachmentOffsetNested.fileOffset);
		byte[] attachmentData = new byte[attachmentOffsetNested.size];
		in.read(attachmentData);

		// we could be an array, so check and process if required.
		if (PSTObject.isPSTArray(attachmentData))
		{
			attachmentData = PSTObject.processArray(in, attachmentData);
		}
		if (this.pstFile.getEncryptionType() == PSTFile.ENCRYPTION_TYPE_COMPRESSIBLE) {
			attachmentData = PSTObject.decode(attachmentData);
		}

		return attachmentData;
	}
	
	public int getFilesize()
		throws PSTException, IOException
	{
		PSTTableBCItem attachmentDataObject = items.get(0x3701);
		PSTDescriptorItem descriptorItemNested = this.localDescriptorItems.get(attachmentDataObject.entryValueReference); 
		if (descriptorItemNested == null) {
			return 0;
		}
		OffsetIndexItem attachmentOffsetNested = PSTObject.getOffsetIndexNode(this.pstFile.getFileHandle(), descriptorItemNested.offsetIndexIdentifier);
		
		RandomAccessFile in = this.pstFile.getFileHandle();
		in.seek(attachmentOffsetNested.fileOffset);
		// we only need the first 8 bytes, just in case on an array...
		byte[] attachmentData = new byte[8];
		in.read(attachmentData);
		
		// we could be an array, so check and process if required.
		if (PSTObject.isPSTArray(attachmentData))
		{
			// we are an array, get the sum of the sizes...
			int dataSize = (int)PSTObject.convertLittleEndianBytesToLong(attachmentData, 4, 8);
			return dataSize;
		}
		else
		{
			return attachmentOffsetNested.size;
		}
	}

	
	// attachment properties
	
	/**
	 * Attachment (short) filename ASCII or Unicode string
	 */
	public String getAttachFilename() {
		return this.getStringItem(0x3704);
	}
	/**
	 * Attachment method Integer 32-bit signed 0 => None (No attachment) 1 => By value 2 => By reference 3 => By reference resolve 4 => By reference only 5 => Embedded message 6 => OLE
	 */
	public int getAttachMethod() {
		return this.getIntItem(0x3705);
	}
	/**
	 * Attachment long filename ASCII or Unicode string
	 */
	public String getAttachLongFilename() {
		return this.getStringItem(0x3707);
	}
	/**
	 * Attachment (short) pathname ASCII or Unicode string
	 */
	public String getAttachPathname() {
		return this.getStringItem(0x3708);
	}
	/**
	 * Attachment Position Integer 32-bit signed
	 */
	public int getRenderingPosition() {
		return this.getIntItem(0x370b);
	}
	/**
	 * Attachment long pathname ASCII or Unicode string
	 */
	public String getAttachLongPathname() {
		return this.getStringItem(0x370d);
	}
	/**
	 * Attachment mime type ASCII or Unicode string
	 */
	public String getAttachMimeTag() {
		return this.getStringItem(0x370e);
	}
	/**
	 * Attachment mime sequence
	 */
	public int getAttachMimeSequence() {
		return this.getIntItem(0x3710);
	}


	
}
