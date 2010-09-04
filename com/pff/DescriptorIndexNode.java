/**
 * 
 */
package com.pff;

import java.io.IOException;

/**
 * DescriptorIndexNode is a leaf item from the Descriptor index b-tree
 * It is like a pointer to an element in the PST file, everything has one...
 * @author Richard Johnson
 */
public class DescriptorIndexNode {
	public int descriptorIdentifier;
	public long dataOffsetIndexIdentifier;
	public long localDescriptorsOffsetIndexIdentifier;
	public int parentDescriptorIndexIdentifier;
	public int itemType;

	//PSTFile.PSTFileBlock dataBlock = null;
	
	/**
	 * parse the data out into something meaningful
	 * @param data
	 */
	DescriptorIndexNode(byte[] data, int pstFileType) {
		// parse it out
		// first 4 bytes
		if (pstFileType == PSTFile.PST_TYPE_ANSI) {
			descriptorIdentifier = (int)PSTObject.convertLittleEndianBytesToLong(data, 0, 4);
			dataOffsetIndexIdentifier = (int)PSTObject.convertLittleEndianBytesToLong(data, 4, 8);
			localDescriptorsOffsetIndexIdentifier = (int)PSTObject.convertLittleEndianBytesToLong(data, 8, 12);
			parentDescriptorIndexIdentifier = (int)PSTObject.convertLittleEndianBytesToLong(data, 12, 16);
			//itemType = (int)PSTObject.convertLittleEndianBytesToLong(data, 28, 32);
		} else {
			descriptorIdentifier = (int)PSTObject.convertLittleEndianBytesToLong(data, 0, 4);
			dataOffsetIndexIdentifier = (int)PSTObject.convertLittleEndianBytesToLong(data, 8, 16);
			localDescriptorsOffsetIndexIdentifier = (int)PSTObject.convertLittleEndianBytesToLong(data, 16, 24);
			parentDescriptorIndexIdentifier = (int)PSTObject.convertLittleEndianBytesToLong(data, 24, 28);
			itemType = (int)PSTObject.convertLittleEndianBytesToLong(data, 28, 32);
		}
	}

	/*
	void readData(PSTFile file)
		throws IOException, PSTException
	{
		if ( dataBlock == null ) {
			dataBlock = file.readLeaf(dataOffsetIndexIdentifier);
		}
	}
	 *
	 */

	PSTNodeInputStream getNodeInputStream(PSTFile pstFile)
			throws IOException, PSTException
	{
		return new PSTNodeInputStream(pstFile,pstFile.getOffsetIndexNode(dataOffsetIndexIdentifier));
	}
	
	public String toString() {
		
		return "DescriptorIndexNode\n" +
			"Descriptor Identifier: "+descriptorIdentifier+" (0x"+Long.toHexString(descriptorIdentifier)+")\n"+
			"Data offset identifier: "+dataOffsetIndexIdentifier+" (0x"+Long.toHexString(dataOffsetIndexIdentifier)+")\n"+
			"Local descriptors offset index identifier: "+localDescriptorsOffsetIndexIdentifier+" (0x"+Long.toHexString(localDescriptorsOffsetIndexIdentifier)+")\n"+
			"Parent Descriptor Index Identifier: "+parentDescriptorIndexIdentifier+" (0x"+Long.toHexString(parentDescriptorIndexIdentifier)+")\n"+
			"Item Type: "+itemType+" (0x"+Long.toHexString(itemType)+")";
	}
}
