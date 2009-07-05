/**
 * 
 */
package com.pff;

/**
 * DescriptorIndexNode is a leaf item from the Descriptor index b-tree
 * @author toweruser
 */
public class DescriptorIndexNode {
	public int descriptorIdentifier;
	public long dataOffsetIndexIdentifier;
	public long localDescriptorsOffsetIndexIdentifier;
	public int parentDescriptorIndexIdentifier;
	public int itemType;
	
	/**
	 * parse the data out into something meaningful
	 * @param data
	 */
	DescriptorIndexNode(byte[] data) {
		// parse it out
		// first 4 bytes
		
		descriptorIdentifier = (int)PSTObject.convertLittleEndianBytesToLong(data, 0, 4);		
		dataOffsetIndexIdentifier = (int)PSTObject.convertLittleEndianBytesToLong(data, 8, 16);
		localDescriptorsOffsetIndexIdentifier = (int)PSTObject.convertLittleEndianBytesToLong(data, 16, 24);
		parentDescriptorIndexIdentifier = (int)PSTObject.convertLittleEndianBytesToLong(data, 24, 28);
		itemType = (int)PSTObject.convertLittleEndianBytesToLong(data, 28, 32);
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
