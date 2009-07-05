package com.pff;

import java.util.*;

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
