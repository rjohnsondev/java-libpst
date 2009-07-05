/**
 * 
 */
package com.pff;

import java.util.*;


/**
 * Table class manages the parsing of table data binary blobs into manageable objects
 * @author toweruser
 */
class PSTTableBC extends PSTTable {
	
	private HashMap<Integer, PSTTableBCItem> items = new HashMap<Integer, PSTTableBCItem>();
	
	PSTTableBC(byte[] data)
		throws PSTException
	{
		super(data);
		

		// go through each of the entries.
		numberOfItems = (tableEntriesEnd - tableEntriesStart) / (sizeOfItemRecord+sizeOfItemValue);

		description += ("Number of entries: "+numberOfItems+"\n");

		// now the specific stuff...
		if (tableTypeByte != 0xffffffbc)
		{
			System.out.println(Long.toHexString(this.tableTypeByte));
			throw new PSTException("unable to create PSTTableBC, table does not appear to be a bc!");
		}
		int offset = tableEntriesStart;
		
		for (int x = 0; x < numberOfItems; x++) {
			
			PSTTableBCItem item = new PSTTableBCItem();
			item.itemIndex = x;
			
			item.entryType =(int)PSTObject.convertLittleEndianBytesToLong(data, offset+0, offset+2);
			item.entryValueType = (int)PSTObject.convertLittleEndianBytesToLong(data, offset+2, offset+4);
			item.entryValueReference = (int)PSTObject.convertLittleEndianBytesToLong(data, offset+4, offset+8);

			int end = 0;
			int start = 0;
			// we need to have no higher order bits set and no lower 4 bits set.  This doesn't seem to work to well....
			if ((item.entryValueReference & 0xf) == 0)
			{
				item.entryValueReferenceAsOffset = tableIndexOffset + (item.entryValueReference>>4) +2;
				if (item.entryValueReferenceAsOffset+2 <= data.length) {
					int entryInternalValueRef = 0;
					entryInternalValueRef = (int)PSTObject.convertLittleEndianBytesToLong(data, item.entryValueReferenceAsOffset, item.entryValueReferenceAsOffset+2);
					//description += " - "+entryInternalValueRef+"\n";
					
					// get the value...
					start = entryInternalValueRef;
					for (int y = 1; y < indexes.length; y++) {
						if (entryInternalValueRef == indexes[y-1]) {
							end = indexes[y];
							break;
						}
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
			
			offset = offset + 8;
						
			items.put(item.entryType, item);
			description += item.toString()+"\n\n";
		}
	}
	
	
	/**
	 * get the items parsed out of this table.
	 * @return
	 */
	public HashMap<Integer, PSTTableBCItem> getItems() {
		return this.items;
	}
	
	public String toString() {
		return this.description;
	}
}

