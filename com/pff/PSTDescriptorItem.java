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

		if (pstFile.getPSTFileType() == PSTFile.PST_TYPE_ANSI) {
			descriptorIdentifier = (int)PSTObject.convertLittleEndianBytesToLong(data, offset, offset+4);
			offsetIndexIdentifier = ((int)PSTObject.convertLittleEndianBytesToLong(data, offset+4, offset+8))
										& 0xfffffffe;
			subNodeOffsetIndexIdentifier = (int)PSTObject.convertLittleEndianBytesToLong(data, offset+8, offset+12)
										& 0xfffffffe;
		} else {
			descriptorIdentifier = (int)PSTObject.convertLittleEndianBytesToLong(data, offset, offset+4);
			offsetIndexIdentifier = ((int)PSTObject.convertLittleEndianBytesToLong(data, offset+8, offset+16))
										& 0xfffffffe;
			subNodeOffsetIndexIdentifier = (int)PSTObject.convertLittleEndianBytesToLong(data, offset+16, offset+24)
										& 0xfffffffe;
		}
	}

	public byte[] getData()
		throws IOException, PSTException
	{
		if ( dataBlockData != null ) {
			return dataBlockData;
		}

		PSTNodeInputStream in = pstFile.readLeaf(offsetIndexIdentifier);
		byte[] out = new byte[(int)in.length()];
		in.read(out);
		dataBlockData = out;
		return dataBlockData;
	}
	
	public int[] getBlockOffsets()
		throws IOException, PSTException
	{
		if ( dataBlockOffsets != null ) {

			return dataBlockOffsets;
		}
		Long[] offsets = pstFile.readLeaf(offsetIndexIdentifier).getBlockOffsets();
		int[] offsetsOut = new int[offsets.length];
		for (int x = 0; x < offsets.length; x++) {
			offsetsOut[x] = offsets[x].intValue();
		}
		return offsetsOut;
	}
	
	public int getDataSize()
		throws IOException, PSTException
	{
		return pstFile.getLeafSize(offsetIndexIdentifier);
	}
	
	public HashMap<Integer, PSTDescriptorItem> getSubNodeDescriptorItems() {
		return subNodeDescriptorItems;
	}

	public PSTNodeInputStream getSubNodeData()
		throws IOException, PSTException
	{
		if (subNodeOffsetIndexIdentifier != 0) {
			return pstFile.readLeaf(subNodeOffsetIndexIdentifier);
			/*
			PSTFile.PSTFileBlock subNodeDataBlock = pstFile.readLeaf(subNodeOffsetIndexIdentifier);
			if ( subNodeDataBlock.blockOffsets != null &&
					subNodeDataBlock.blockOffsets.length > 1 ) {
				System.out.printf("SubNode (0x%08X) has more than one data block!", subNodeOffsetIndexIdentifier);
			}
			return subNodeDataBlock.data;
			 *
			 */
		}
		
		return null;
	}

	// Public data
	int descriptorIdentifier;
	int offsetIndexIdentifier;
	int subNodeOffsetIndexIdentifier;
	private HashMap<Integer, PSTDescriptorItem> subNodeDescriptorItems;

	// These are private to ensure that getData()/getBlockOffets() are used 
	//private PSTFile.PSTFileBlock dataBlock = null;
	byte[] dataBlockData = null;
	int[] dataBlockOffsets = null;
	private PSTFile pstFile;

	@Override
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
