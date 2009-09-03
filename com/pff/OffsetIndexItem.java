/**
 * 
 */
package com.pff;

/**
 * OffsetIndexItem is a leaf item from the Offset index b-tree
 * Only really used internally to get the file offset for items
 * @author Richard Johnson
 */
class OffsetIndexItem {
	long indexIdentifier;
	long fileOffset;
	int size;
	long unknown;
	
	OffsetIndexItem(byte[] data) {
		indexIdentifier = PSTObject.convertLittleEndianBytesToLong(data, 0, 8);
		fileOffset = PSTObject.convertLittleEndianBytesToLong(data, 8, 16);
		size = (int)PSTObject.convertLittleEndianBytesToLong(data, 16, 18);
		unknown = (int)PSTObject.convertLittleEndianBytesToLong(data, 16, 18);
		//System.out.println("Data size: "+data.length);
		
	}
	
	public String toString() {
		return "OffsetIndexItem\n"+
			"Index Identifier: "+indexIdentifier+" (0x"+Long.toHexString(indexIdentifier)+")\n"+
			"File Offset: "+fileOffset+" (0x"+Long.toHexString(fileOffset)+")\n"+
			"unknown: "+unknown+" (0x"+Long.toHexString(unknown)+" bin:"+Long.toBinaryString(unknown)+")\n"+
			"Size: "+size+" (0x"+Long.toHexString(size)+")";
	}
}