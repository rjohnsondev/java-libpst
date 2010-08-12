package com.pff;

import java.io.IOException;
import java.util.*;

/**
 * The descriptor items contain information that describes a PST object.
 * This is like extended table entries, usually when the data cannot fit in a traditional table item.
 * @author Richard Johnson
 */
class PSTDescriptorItem
{
	PSTDescriptorItem(byte[] data, int offset, PSTFile pstFile)
	{
		this.pstFile = pstFile;

		descriptorIdentifier = (int)PSTObject.convertLittleEndianBytesToLong(data, offset, offset+4);
		offsetIndexIdentifier = ((int)PSTObject.convertLittleEndianBytesToLong(data, offset+8, offset+16))
									& 0xfffffffe;
		subNodeOffsetIndexIdentifier = (int)PSTObject.convertLittleEndianBytesToLong(data, offset+16, offset+24)
									& 0xfffffffe;
	}

	public byte[] getData()
		throws IOException, PSTException
	{
		if ( dataBlock != null ) {
			return dataBlock.data;
		}

		dataBlock = pstFile.readLeaf(offsetIndexIdentifier);
		return dataBlock.data;
	}
	
	public int[] getBlockOffsets()
		throws IOException, PSTException
	{
		if ( dataBlock != null ) {
			return dataBlock.blockOffsets;
		}
		dataBlock = pstFile.readLeaf(offsetIndexIdentifier);
		return dataBlock.blockOffsets;
	}
	
	public int getDataSize()
		throws IOException, PSTException
	{
		return pstFile.getLeafSize(offsetIndexIdentifier);
	}
	
	public HashMap<Integer, PSTDescriptorItem> getSubNodeDescriptorItems() {
		return subNodeDescriptorItems;
	}

	public byte[] getSubNodeData()
		throws IOException, PSTException
	{
		if (subNodeOffsetIndexIdentifier != 0) {
			PSTFile.PSTFileBlock subNodeDataBlock = pstFile.readLeaf(subNodeOffsetIndexIdentifier);
			if ( subNodeDataBlock.blockOffsets != null &&
					subNodeDataBlock.blockOffsets.length > 1 ) {
				System.out.printf("SubNode (0x%08X) has more than one data block!", subNodeOffsetIndexIdentifier);
			}
			return subNodeDataBlock.data;
		}
		
		return null;
	}

	// Public data
	int descriptorIdentifier;
	int offsetIndexIdentifier;
	int subNodeOffsetIndexIdentifier;
	private HashMap<Integer, PSTDescriptorItem> subNodeDescriptorItems;

	// These are private to ensure that getData()/getBlockOffets() are used 
	private PSTFile.PSTFileBlock dataBlock = null;
	private PSTFile pstFile;

	public String toString() {
		return 
			"PSTDescriptorItem\n"+
			"   descriptorIdentifier: "+descriptorIdentifier+"\n"+
			"   offsetIndexIdentifier: "+offsetIndexIdentifier+"\n"+
			"   subNodeOffsetIndexIdentifier: "+subNodeOffsetIndexIdentifier+"\n";
			
		
	}

	// For use by PSTDescriptor
	void setSubNodeDescriptorItems(
			HashMap<Integer, PSTDescriptorItem> subNodeDescriptorItems) {
		this.subNodeDescriptorItems = subNodeDescriptorItems;
	}

}
