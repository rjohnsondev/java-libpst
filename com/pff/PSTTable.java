/**
 * 
 */
package com.pff;


/**
 * @author toweruser
 *
 */
class PSTTable {

	protected int tableIndexOffset;
	protected String tableType;
	protected byte tableTypeByte;
	protected int tableValueReference;
	
	// info from the b5 header
	protected int sizeOfItemRecord;
	protected int sizeOfItemValue;
	protected int tableEntriesReference;
	protected int tableEntriesReferenceAsOffset;
	protected int tableEntriesStart;
	protected int tableEntriesEnd;
	protected int numberOfItems = 0;
	
	protected int[] indexes;

	protected String description = "";
	
	protected PSTTable(byte[] data)
		throws PSTException
	{
		this(data, new int[0]);
	}
	
	protected PSTTable(byte[] data, int[] arrayBlocks)
		throws PSTException
	{
		
		tableIndexOffset = (int)PSTObject.convertLittleEndianBytesToLong(data, 0, 2);
		
		// the next two bytes should be the table type.
		if ((int)data[2] != 0xffffffec) {
			throw new PSTException("Unable to parse table, bad table type...");
		}
		boolean valid = false;
		switch ((int)data[3]) {
			case 0x7c:
				tableType = "7c";
				valid = true;
				break;
//			case 0x9c:
//				tableType = "9c";
//				valid = true;
//				break;
//			case 0xa5:
//				tableType = "a5";
//				valid = true;
//				break;
//			case 0xac:
//				tableType = "ac";
//				valid = true;
//				break;
			case 0xffffffbc:
				tableType = "bc";
				valid = true;
				break;
		}
		if (!valid) {
			throw new PSTException("Unable to parse table, bad table type.  Unknown identifier: 0x"+Long.toHexString(data[3]));
		} else {
			tableTypeByte = data[3];
		}
		
		tableValueReference = (int)PSTObject.convertLittleEndianBytesToLong(data, 4, 8);
		
		
		// process the table index.
		int numberOfIndexItems = (int)PSTObject.convertLittleEndianBytesToLong(data, tableIndexOffset, tableIndexOffset+2);
		description += "Number of items: \n"+numberOfIndexItems;
		indexes = new int[numberOfIndexItems+3];
		description += "Indexes:\n";
		for (int x = 1; x < numberOfIndexItems+3; x++) {
			indexes[x] = (int)PSTObject.convertLittleEndianBytesToLong(data, tableIndexOffset+(x*2), tableIndexOffset+(x*2)+2);
			description += "   index"+x+": "+ indexes[x]+" ("+Long.toHexString(indexes[x])+")";
			if (x > 1) {
				description += " "+(indexes[x] - indexes[x-1]);
			}
			description += "\n";
		}
		

		
		int offset = 12;
		// all tables should have a b5 header to start with...
		if ((int)(data[offset] & 0xff) != 0xb5) {
			throw new PSTException("Unable to parse table, does not appear to contain b5 header information: "+Long.toHexString(data[offset]));
		}
		
		sizeOfItemRecord = (int)PSTObject.convertLittleEndianBytesToLong(data, offset+1, offset+2);
		sizeOfItemValue = (int)PSTObject.convertLittleEndianBytesToLong(data, offset+2, offset+3);
		tableEntriesReference = (int)PSTObject.convertLittleEndianBytesToLong(data, offset+4, offset+8);
		
		description += "Table ("+tableType+")\n"+
			"Table Index Offset: "+tableIndexOffset+" - 0x"+Long.toHexString(tableIndexOffset)+"\n"+
			"Table Value Reference: "+tableValueReference+" - 0x"+Long.toHexString(tableValueReference)+"\n"+
			"Size Of Item Record: "+sizeOfItemRecord+" - 0x"+Long.toHexString(sizeOfItemRecord)+"\n"+
			"Size Of Item Value: "+sizeOfItemValue+" - 0x"+Long.toHexString(sizeOfItemValue)+"\n"+
			"Table Entries Reference: "+tableEntriesReference+" - 0x"+Long.toHexString(tableEntriesReference)+"\n";

		
		tableEntriesReferenceAsOffset = (tableEntriesReference >> 4);
		description += "tableEntriesReferenceAsOffset: "+tableEntriesReferenceAsOffset+"\n";
		tableEntriesReferenceAsOffset += tableIndexOffset;
		description += "tableEntriesReferenceAsOffset: "+tableEntriesReferenceAsOffset+"\n";

//		System.out.println(description);
//		PSTObject.printHexFormatted(data, true, indexes);
//		System.exit(0);

		
//		System.out.println(description);
		
		if ((tableTypeByte & 0xff) == 0xbc)
		{
			tableEntriesReferenceAsOffset += 2; // why is this not for 7c????!!!
		}
		if (tableEntriesReference > 0x10000 && arrayBlocks.length > 0) {
			// just kinda feeling my way around here....
			int tableEntriesReferenceAsOffset2 = ((tableEntriesReference & 65535)>>4)+2; // ahh, +2 shouldn't be here for 7c tables...
			int whichBlock = (tableEntriesReference >> 16);
			int blockStart = arrayBlocks[whichBlock-1];
			
			// get the index offset of the applicable block
			int tableIndexOffset2 = (int)PSTObject.convertLittleEndianBytesToLong(data, blockStart+0, blockStart+2)+blockStart;
			
			// get the location of the values block.
			tableEntriesStart =
				(int)
				PSTObject.convertLittleEndianBytesToLong(
					data,
					tableIndexOffset2+tableEntriesReferenceAsOffset2,
					tableIndexOffset2+tableEntriesReferenceAsOffset2+2
				)+blockStart;
			tableEntriesEnd =
				(int)
				PSTObject.convertLittleEndianBytesToLong(
						data,
						tableIndexOffset2+tableEntriesReferenceAsOffset2+2,
						tableIndexOffset2+tableEntriesReferenceAsOffset2+4
				)+blockStart;
			// confused yet?
//			System.out.println("start: "+tableEntriesStart+" - end: "+tableEntriesEnd);
		}
		else
		{
			tableEntriesStart = (int)PSTObject.convertLittleEndianBytesToLong(data, tableEntriesReferenceAsOffset, tableEntriesReferenceAsOffset+2);

			description += ("tableEntriesStart: "+tableEntriesStart+"\n");
			tableEntriesEnd = tableEntriesStart;
			for (int x = 0; x < indexes.length; x++) {
				if (indexes[x] > tableEntriesStart) {
					tableEntriesEnd = indexes[x];
					break;
				}
			}
			tableEntriesEnd = data.length;
		}
	}


	/**
	 * get the number of items stored in this table.
	 * @return
	 */
	public int getItemCount() {
		return this.numberOfItems;
	}
	
	
}
