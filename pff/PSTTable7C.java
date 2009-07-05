/**
 * 
 */
package com.pff;

import java.util.*;

/**
 * @author toweruser
 *
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
			valuesArrayIndexRefAsOffset = tableIndexOffset + ((valuesArrayIndexRef)>>4)+2;
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
				int entryValueReferenceAsOffset = tableIndexOffset + (dataAsLong>>4) +2;
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


/*
 * kids: [33567, 1718, 33599, 33631, 33663, 32933, 1682, 1649]
PSTDescriptorItem
descriptorIdentifier: 1649
offsetIndexIdentifier: 12468
subNodeOffsetIndexIdentifier: 0

---
1802 ec7c 4000 0000 0000 0000								0 - 12
					  b504 0400  ..ì.........µ...	12-20
6000 0000
  7c1c 7400 7400 7600 7a00 2000  ......t.t.v.z...	20 - 266
0000 8000 0000 0000 0000+0300 200e 0c00  ................
0403 1f00 0130 3000 040c 1f00 0337 2c00  .....00......7..
040b 1f00 0437 1400 0405 0300 0537 1000  .....7.......7..
0404 1f00 0737 2800 040a 1f00 0837 2400  .....7.......7..
0409 0201 0a37 1c00 0407 0300 0b37 0800  .....7.......7..
0402 1f00 0d37 2000 0408 1f00 0e37 4400  .....7.......7D.
0411 0201 0f37 3400 040d 1f00 1137 3800  .....74......78.
040e 1f00 1237 4000 0410 1f00 1337 3c00  .....7.......7..
040f 0300 1437 1800 0406 1f00 1937 6c00  .....7.......7l.
041a 1f00 1a37 7000 041b 0300 f267 0000  .....7p.....òg..
0400 0300 f367 0400 0401 0300 0969 4800  ....óg.......iH.
0412 4000 f97f 6400 0819 0300 fa7f 6000  ....ù.d.....ú...
0416 4000 fb7f 5000 0814 4000 fc7f 5800  ....û.P.....ü.X.
0815 0300 fd7f 4c00 0413 0b00 fe7f 7400  ....ý.L.....þ.t.
0117 0b00 ff7f 7500 0118
				 a580 0000 0000  ....ÿ.u.........	266 - 274
0000
a580 0000 1e06 0000 ffff ffff 206c  ..........ÿÿÿÿ.l	274 - 396
0a00 0100 0000 a000 0000 0000 0000 0000  ................
0000 0000 0000 0000 0000 c000 0000 e000  ..........À...à.
0000 0001 0000 0000 0000 0000 0000 0000  ................
0000 0000 0000 0000 0000 0000 0000 0000  ................
0000 0040 dda3 5745 b30c 0040 dda3 5745  ....Ý.WE....Ý.WE
b30c 0000 0000 0000 0000 0000 0000 0000  ................	
0000 0000 0000 0000 fe38 1f80			 ........þ8..
					  4c00 4900  			 L.I.	396 - 418
4200 5000 4600 4600 7e00 3100 2e00 4700  B.P.F.F...1...G.
5a00								 	 Z.
6c00 6900 6200 7000 6600 6600 2d00    l.i.b.p.f.f...	418 - 474
6100 6c00 7000 6800 6100 2d00 3200 3000  a.l.p.h.a...2.0.		
3000 3900 3000 3500 3200 3800 2e00 7400  0.9.0.5.2.8...t.
6100 7200 2e00 6700 7a00		 		 a.r...g.z...
				 2e00 6700 7a00 	         g.z.	474 - 480

6c00 6900 6200 7000 6600 6600 2d00 6100  l.i.b.p.f.f...a.	480 - 536
6c00 7000 6800 6100 2d00 3200 3000 3000  l.p.h.a...2.0.0.
3900 3000 3500 3200 3800 2e00 7400 6100  9.0.5.2.8...t.a.
7200 2e00 6700 7a00 0800 				 r...g.z...
				 0000 0c00 1400	 ......
0a01 1201 8c01 a201 da01 e001 1802 		 ........Ú.à...
---
Number of items: 
8Indexes:
index1: 0 (0)
index2: 12 (c)
index3: 20 (14)
index4: 266 (10a)
index5: 274 (112)
index6: 396 (18c) .
index7: 418 (1a2).
index8: 474 (1da). 
index9: 480 (1e0)
index10: 536 (218)
Table (7c)
Table Index Offset: 536 - 0x218
Table Value Reference: 64 - 0x40
Size Of Item Record: 4 - 0x4
Size Of Item Value: 4 - 0x4
Table Entries Reference: 96 - 0x60
tableEntriesStart: 20
tableEntriesReferenceAsOffset: 542
numberOfEntryDefinitions: 28
valuesArraySize: 122
b5tableIndexReference: 32
valuesArrayIndexRef: 128
Number of entries: 28
Offset after items: 490
 */