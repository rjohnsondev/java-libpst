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

	public String getFilename() {
		return this.getStringItem(0x3704);
	}

	public String getLongFilename() {
		return this.getStringItem(0x3707);
	}
	
	public String getMimeType() {
		return this.getStringItem(0x370e);
	}
	
}
