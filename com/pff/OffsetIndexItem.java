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
	long cRef;
	
	OffsetIndexItem(byte[] data, int pstFileType) {
		if (pstFileType == PSTFile.PST_TYPE_ANSI) {
			indexIdentifier = PSTObject.convertLittleEndianBytesToLong(data, 0, 4);
			fileOffset = PSTObject.convertLittleEndianBytesToLong(data, 4, 8);
			size = (int)PSTObject.convertLittleEndianBytesToLong(data, 8, 10);
			cRef = (int)PSTObject.convertLittleEndianBytesToLong(data, 10, 12);
		} else {
			indexIdentifier = PSTObject.convertLittleEndianBytesToLong(data, 0, 8);
			fileOffset = PSTObject.convertLittleEndianBytesToLong(data, 8, 16);
			size = (int)PSTObject.convertLittleEndianBytesToLong(data, 16, 18);
			cRef = (int)PSTObject.convertLittleEndianBytesToLong(data, 16, 18);
		}
		//System.out.println("Data size: "+data.length);
		
	}

	@Override
	public String toString() {
		return "OffsetIndexItem\n"+
			"Index Identifier: "+indexIdentifier+" (0x"+Long.toHexString(indexIdentifier)+")\n"+
			"File Offset: "+fileOffset+" (0x"+Long.toHexString(fileOffset)+")\n"+
			"cRef: "+cRef+" (0x"+Long.toHexString(cRef)+" bin:"+Long.toBinaryString(cRef)+")\n"+
			"Size: "+size+" (0x"+Long.toHexString(size)+")";
	}
}