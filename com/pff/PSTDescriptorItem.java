package com.pff;

import java.util.*;

/**
 * The descriptor items contain information that describes a PST object.
 * This is like extended table entries, usually when the data cannot fit in a traditional table item.
 * @author Richard Johnson
 */
class PSTDescriptorItem extends PSTTableItem
{
	int descriptorIdentifier;
	int offsetIndexIdentifier;
	int subNodeOffsetIndexIdentifier;
	
	HashMap<Integer, PSTDescriptorItem> subNodeDescriptorItems;
	
	public String toString() {
		return 
			"PSTDescriptorItem\n"+
			"   descriptorIdentifier: "+descriptorIdentifier+"\n"+
			"   offsetIndexIdentifier: "+offsetIndexIdentifier+"\n"+
			"   subNodeOffsetIndexIdentifier: "+subNodeOffsetIndexIdentifier+"\n";
			
		
	}
}
