/**
 * 
 */
package com.pff;

import java.util.*;

/**
 * Specific functions for the 7c table type.
 * This is used for attachments.
 * @author Richard Johnson
 */
class PSTTable7C extends PSTTable {

	private LinkedList<HashMap<Integer, PSTTable7CItem>> items = new LinkedList<HashMap<Integer, PSTTable7CItem>>();
	private int numberOfDataSets = 1;
	
	
	
	protected PSTTable7C(byte[] data, byte[] valuesArray)
		throws PSTException
	{
		super(data);
		
		// add on the b5 header length
		int offset = 20;
		
		// get the 7c header information
		int numberOfEntryDefinitions = (int)PSTObject.convertLittleEndianBytesToLong(data, offset+1, offset+2);
		int valuesArraySize = (int)PSTObject.convertLittleEndianBytesToLong(data, offset+8, offset+10);
		int b5tableIndexReference = (int)PSTObject.convertLittleEndianBytesToLong(data, offset+10, offset+14);
		int valuesArrayIndexRef = (int)PSTObject.convertLittleEndianBytesToLong(data, offset+14, offset+18);// was 18

		int valuesArrayIndexRefAsOffset = 0;
		int valueArrayStart = -1;
		int valueArrayEnd = 0;

		tableEntriesStart += 22;
		// go through each of the entries.
		numberOfItems = (tableEntriesEnd - tableEntriesStart) / (sizeOfItemRecord+sizeOfItemValue);
		
		if ((valuesArrayIndexRef & 0xf) != 0) {
			// what is this? so weird
			// just assume the values table starts at the end of the table.

			valueArrayStart = tableEntriesEnd;
			for (int z = 1; z < indexes.length; z++) {
				if (valueArrayStart == indexes[z-1]) {
					valueArrayEnd = indexes[z];
					break;
				}
			} 
			
			// find out how many attachments we have based on the external values
			numberOfDataSets = valuesArray.length / valuesArraySize;
			valuesArraySize = 8;
			
		} else {
			valuesArrayIndexRefAsOffset = tableIndexOffset + ((valuesArrayIndexRef)>>>4)+2;
			valueArrayStart = (int)PSTObject.convertLittleEndianBytesToLong(data, valuesArrayIndexRefAsOffset, valuesArrayIndexRefAsOffset+2);
			valueArrayEnd = (int)PSTObject.convertLittleEndianBytesToLong(data, valuesArrayIndexRefAsOffset+2, valuesArrayIndexRefAsOffset+4);
			// find out how many attachments we have
			numberOfDataSets = (valueArrayEnd - valueArrayStart) / valuesArraySize;
		}

		description += 
			"Data length: "+data.length+"\n"+
			"Number of entries: "+numberOfItems+"\n"+
			"numberOfEntryDefinitions: "+numberOfEntryDefinitions+"\n"+
			"valuesArraySize: "+valuesArraySize+"\n"+
			"b5tableIndexReference: "+b5tableIndexReference+"\n"+
			"valuesArrayIndexRef: "+valuesArrayIndexRef+"\n"+
			"valuesArrayIndexRefAsOffset: "+valuesArrayIndexRefAsOffset+"\n"+
			"valueArrayStart: "+valueArrayStart+"\n"+
			"valueArrayEnd: "+valueArrayEnd+"\n"+
			(valuesArray == null ? "" : "valueArray actual Size: "+valuesArray.length+"\n")+
			"numberOfDataSets: "+numberOfDataSets+"\n";
		
		// now the specific stuff...
		if (tableTypeByte != 0x7c)
		{
			System.out.println(Long.toHexString(this.tableTypeByte));
			throw new PSTException("unable to create PSTTable7C, table does not appear to be a 7c!");
		}

		// repeat the reading process for every dataset
		for (int dataSetNumber = 0; dataSetNumber < numberOfDataSets; dataSetNumber++)
		{
			offset = tableEntriesStart;
			HashMap<Integer, PSTTable7CItem> currentItem = new HashMap<Integer, PSTTable7CItem>();
		
			for (int x = 0; x < numberOfItems; x++)
			{
				PSTTable7CItem item = new PSTTable7CItem();
				item.itemIndex = x;
				
				item.entryValueType =(int)PSTObject.convertLittleEndianBytesToLong(data, offset+0, offset+2);
				item.entryType = (int)PSTObject.convertLittleEndianBytesToLong(data, offset+2, offset+4);
				item.valuesArrayEntryOffset = (int)PSTObject.convertLittleEndianBytesToLong(data, offset+4, offset+6);
				item.valuesArrayEntrySize = (int)PSTObject.convertLittleEndianBytesToLong(data, offset+6, offset+7);
				item.valuesArrayEntryNumber = (int)PSTObject.convertLittleEndianBytesToLong(data, offset+7, offset+8);
				
				// the offset may point to a value, or an actual offset
				int dataAsLong = item.valuesArrayEntryOffset;
				
				// if we have a value array, we look it up in there
				if (valueArrayStart > -1 ) {
					// see if we can't get a value out...
					item.data = new byte[item.valuesArrayEntrySize];
					int currentValueArrayStart = valueArrayStart + (dataSetNumber*valuesArraySize);
//					try {
						System.arraycopy(data, (currentValueArrayStart)+item.valuesArrayEntryOffset, item.data, 0, item.valuesArrayEntrySize);
//					} catch (Exception err) {
//						err.printStackTrace();
//						System.out.println(currentValueArrayStart+"+"+item.valuesArrayEntryOffset);
//						System.out.println(item.valuesArrayEntrySize);
//						System.out.println(description);
//						System.exit(1);
//					}
					dataAsLong = (int)PSTObject.convertLittleEndianBytesToLong(item.data);
					description += "valueArrayStart+item.valuesArrayEntryOffset = "+currentValueArrayStart+item.valuesArrayEntryOffset;
					description += "tableEntriesEnd+item.valuesArrayEntryOffset = "+tableEntriesEnd+item.valuesArrayEntryOffset+"\n";
				}
				
				int end = 0;
				int start = 0;				
				int entryValueReferenceAsOffset = tableIndexOffset + (dataAsLong>>>4) +2;
				if (item.valuesArrayEntrySize > 0 &&
					dataAsLong > 0 &&
					entryValueReferenceAsOffset+2 < data.length &&
					(dataAsLong & 0xf) == 0) // we need to have no higher order bits set and no lower 4 bits set.
				{
				
					int entryInternalValueRef = 0;
					entryInternalValueRef = (int)PSTObject.convertLittleEndianBytesToLong(data, entryValueReferenceAsOffset, entryValueReferenceAsOffset+2);
					description += " - "+entryInternalValueRef+"\n";
					
					// get the value...
					start = entryInternalValueRef;
					for (int z = 1; z < indexes.length; z++) {
						if (entryInternalValueRef == indexes[z-1]) {
							end = indexes[z];
							break;
						}
					} 
				}
				
				if (end > 0) {
					item.data = new byte[end-start];
					System.arraycopy(data, start, item.data, 0, end-start);
				}
				else
				{
					item.isExternalValueReference = true;
				}
				
				currentItem.put(item.entryType, item);
				
				description += item.toString()+"\n\n";
	
				offset = offset + 8;
				
			}
			items.add(currentItem);
		}
		
//		System.out.println(items.size());
		
		description += "Offset after items: "+offset;
		
//		System.out.println(description);
		
	}
	
	public int getItemCount() {
		return this.numberOfDataSets;
	}

	/**
	 * get the items parsed out of this table.
	 * @return
	 */
	public LinkedList<HashMap<Integer, PSTTable7CItem>> getItems() {
		return this.items;
	}
	
	public String toString() {
		return this.description;
	}
	
}
