/**
 * 
 */
package com.pff;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Specific functions for the 7c table type ("Table Context").
 * This is used for attachments.
 * @author Richard Johnson
 */
class PSTTable7C extends PSTTable {

	private List<HashMap<Integer, PSTTable7CItem>> items = new ArrayList<HashMap<Integer, PSTTable7CItem>>();
	private int numberOfDataSets = 0;
	
	protected PSTTable7C(byte[] Data, HashMap<Integer, PSTDescriptorItem> subNodeDescriptorItems)
		throws PSTException
	{
		super(Data, new int[0], subNodeDescriptorItems);
		
		if (tableTypeByte != 0x7c)
		{
			System.out.println(Long.toHexString(this.tableTypeByte));
			throw new PSTException("unable to create PSTTable7C, table does not appear to be a 7c!");
		}
		
		if ( Data.length > (8192-16) ) {
			System.out.printf("TC table - more than one block: %d bytes\n", Data.length);
		}

		// TCINFO header is in the hidUserRoot node
		NodeInfo tcHeaderNode = getNodeInfo(hidUserRoot);
		int offset = tcHeaderNode.startOffset;
		
		// get the TCINFO header information
		int cCols = (int)PSTObject.convertLittleEndianBytesToLong(tcHeaderNode.data, offset+1, offset+2);
		@SuppressWarnings("unused")
		int TCI_4b = (int)PSTObject.convertLittleEndianBytesToLong(tcHeaderNode.data, offset+2, offset+4);
		@SuppressWarnings("unused")
		int TCI_2b = (int)PSTObject.convertLittleEndianBytesToLong(tcHeaderNode.data, offset+4, offset+6);
		int TCI_1b = (int)PSTObject.convertLittleEndianBytesToLong(tcHeaderNode.data, offset+6, offset+8);
		int TCI_bm = (int)PSTObject.convertLittleEndianBytesToLong(tcHeaderNode.data, offset+8, offset+10);
		int hidRowIndex = (int)PSTObject.convertLittleEndianBytesToLong(tcHeaderNode.data, offset+10, offset+14);
		int hnidRows = (int)PSTObject.convertLittleEndianBytesToLong(tcHeaderNode.data, offset+14, offset+18);// was 18
		// 18..22 hidIndex - deprecated

		// 22... column descriptors
		offset += 22;
		if ( cCols != 0 ) {
			columnDescriptors = new ColumnDescriptor[cCols];
			
			for ( int col = 0; col < cCols; ++col ) {
				columnDescriptors[col] = new ColumnDescriptor(tcHeaderNode.data, offset);
				offset += 8;
			}
		}

		// Read the key table
/*		System.out.printf("Key table:\n");	/**/
		keyMap = new HashMap<Integer, Integer>();
		NodeInfo keyTableInfo = getNodeInfo(hidRoot);
		numberOfKeys = (keyTableInfo.endOffset - keyTableInfo.startOffset) / (sizeOfItemKey+sizeOfItemValue);
		offset = keyTableInfo.startOffset;
		for (int x = 0; x < numberOfKeys; x++) {
			int Context = (int)PSTObject.convertLittleEndianBytesToLong(keyTableInfo.data, offset, offset+4);
			int RowIndex = (int)PSTObject.convertLittleEndianBytesToLong(keyTableInfo.data, offset+4, offset+8);
			keyMap.put(Context, RowIndex);
/*			System.out.printf("\t0x%08X, 0x%08X\n", Context, RowIndex);	/**/
			offset += 8;
		}
		
		// Read the Row Matrix
		NodeInfo rowNodeInfo = getNodeInfo(hnidRows);
		numberOfDataSets = (rowNodeInfo.endOffset - rowNodeInfo.startOffset) / TCI_bm;

		description += 
			"Number of keys: "+numberOfKeys+"\n"+
			"Number of columns: "+cCols+"\n"+
			"Row Size: "+TCI_bm+"\n"+
			"hidRowIndex: "+hidRowIndex+"\n"+
			"hnidRows: "+hnidRows+"\n"+
			"rowArrayStart: "+rowNodeInfo.startOffset+"\n"+
			"rowArrayEnd: "+rowNodeInfo.endOffset+"\n"+
			"Number of rows: "+numberOfDataSets+"\n";
		
		// repeat the reading process for every dataset
		for ( int dataSetNumber = 0; dataSetNumber < numberOfDataSets; dataSetNumber++ )
		{
			HashMap<Integer, PSTTable7CItem> currentItem = new HashMap<Integer, PSTTable7CItem>();
			int currentValueArrayStart = rowNodeInfo.startOffset + (dataSetNumber*TCI_bm);
			byte[] bitmap = new byte[(cCols+7)/8];
			System.arraycopy(rowNodeInfo.data, currentValueArrayStart+TCI_1b, bitmap, 0, bitmap.length);

			int id = (int)PSTObject.convertLittleEndianBytesToLong(rowNodeInfo.data, currentValueArrayStart, currentValueArrayStart+4);
/*			System.out.printf("Row %d, id 0x%08X:\n", dataSetNumber, id);	/**/

			// Put into the item map as PidTagLtpRowId (0x67F2)
			PSTTable7CItem item = new PSTTable7CItem();
			item.itemIndex = -1;
			item.entryValueType = 3;
			item.entryType = 0x67F2;
			item.entryValueReference = id;
			item.isExternalValueReference = true;
			currentItem.put(item.entryType, item);
/*
			String bitmapString = "";
			for ( int b = 0; b < bitmap.length; ++b ) {
				bitmapString += String.format(" %02X", bitmap[b]&0xFF);
			}
			System.out.printf("Bitmap: %s\n", bitmapString);
/**/		
			for ( int col = 0; col < cCols; ++col )
			{
				// Does this column exist for this row?
				int bitIndex = columnDescriptors[col].iBit / 8;
				int bit = columnDescriptors[col].iBit % 8;
				if ( (bitmap[bitIndex] & (1<<bit)) == 0 )
				{
					// Column doesn't exist
/*					System.out.printf("Col %d (0x%04X) not present\n", col, columnDescriptors[col].id);	/**/
					continue;
				}

				item = new PSTTable7CItem();
				item.itemIndex = col;
				
				item.entryValueType = columnDescriptors[col].type;
				item.entryType = columnDescriptors[col].id;
				item.entryValueReference = 0;
				
				switch ( columnDescriptors[col].cbData ) {
				case 1:	// Single byte data
					item.entryValueReference = rowNodeInfo.data[currentValueArrayStart+columnDescriptors[col].ibData] & 0xFF;
					item.isExternalValueReference = true;
/*
					System.out.printf("\tboolean: %s %s\n",
							PSTFile.getPropertyDescription(item.entryType, item.entryValueType),
							item.entryValueReference == 0 ? "false" : "true");
/**/
					break;

				case 2:	// Two byte data
					item.entryValueReference = (rowNodeInfo.data[currentValueArrayStart+columnDescriptors[col].ibData] & 0xFF) |
												  ((rowNodeInfo.data[currentValueArrayStart+columnDescriptors[col].ibData+1] & 0xFF) << 8);
					item.isExternalValueReference = true;
/*
					short i16 = (short)item.entryValueReference;
					System.out.printf("\tInteger16: %s %d\n", 
							PSTFile.getPropertyDescription(item.entryType, item.entryValueType),
							i16);
/**/
					break;

				case 8:	// 8 byte data
					item.data = new byte[8];
					System.arraycopy(rowNodeInfo.data, currentValueArrayStart+columnDescriptors[col].ibData, item.data, 0, 8);
/*					System.out.printf("\tInteger64: %s\n",
							PSTFile.getPropertyDescription(item.entryType, item.entryValueType));	/**/
					break;

				default:// Four byte data
					item.entryValueReference = (int)PSTObject.convertLittleEndianBytesToLong(rowNodeInfo.data,
							currentValueArrayStart+columnDescriptors[col].ibData,
							currentValueArrayStart+columnDescriptors[col].ibData+4);
					if ( columnDescriptors[col].type == 0x0003 ||
						 columnDescriptors[col].type == 0x0004 ||
						 columnDescriptors[col].type == 0x000A ) {
						// True 32bit data
						item.isExternalValueReference = true;
/*						System.out.printf("\tInteger32: %s %d\n", 
								PSTFile.getPropertyDescription(item.entryType, item.entryValueType),
								item.entryValueReference);	/**/
						break;
					}

					// Variable length data so it's an hnid
					if ( (item.entryValueReference & 0x1F) != 0 ) {
						// Some kind of external reference...
						item.isExternalValueReference = true;
/*						System.out.printf("\tOther: %s 0x%08X\n",
								PSTFile.getPropertyDescription(item.entryType, item.entryValueType), item.entryValueReference);	/**/
						break;						
					}
					
					if ( item.entryValueReference == 0 ) {
/*						System.out.printf("\tOther: %s 0 bytes\n",
								PSTFile.getPropertyDescription(item.entryType, item.entryValueType));	/**/
						item.data = new byte[0];
						break;
					} else {					
						NodeInfo entryInfo = getNodeInfo(item.entryValueReference);
						item.data = new byte[entryInfo.length()];
						System.arraycopy(entryInfo.data, entryInfo.startOffset, item.data, 0, item.data.length);
					}
/*
					if ( item.entryValueType != 0x001F ) {
						System.out.printf("\tOther: %s %d bytes\n",
								PSTFile.getPropertyDescription(item.entryType, item.entryValueType),
								item.data.length);
					} else {
						try {
							String s = new String(item.data, "UTF-16LE");
							System.out.printf("\tString: %s \"%s\"\n", 
									PSTFile.getPropertyDescription(item.entryType, item.entryValueType),
									s);
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}
/**/
					break;
				}
				
				currentItem.put(item.entryType, item);
				
				description += item.toString()+"\n\n";
			}
			items.add(dataSetNumber, currentItem);
		}
		
//		System.out.println(description);

		ReleaseRawData();
	}
	
	class ColumnDescriptor {
		ColumnDescriptor(byte[] data, int offset) {
			type = (int)(PSTObject.convertLittleEndianBytesToLong(data, offset, offset+2) & 0xFFFF);
			id = (int)(PSTObject.convertLittleEndianBytesToLong(data, offset+2, offset+4) & 0xFFFF);
			ibData = (int)(PSTObject.convertLittleEndianBytesToLong(data, offset+4, offset+6) & 0xFFFF);
			cbData = (int)data[offset+6] & 0xFF;
			iBit = (int)data[offset+7] & 0xFF;
		}

		int		type;
		int		id;
		int		ibData;
		int		cbData;
		int		iBit;
	}
	
	public int getRowCount() {
		return items.size();
	}

	/**
	 * get the items parsed out of this table.
	 * @return
	 */
	public List<HashMap<Integer, PSTTable7CItem>> getItems() {
		return items;
	}
	
	public HashMap<Integer, PSTTable7CItem> getItem(int itemNumber) {
		if ( items == null || itemNumber >= items.size() ) {
			return null;
		}
		
		return items.get(itemNumber);
	}
	
	public String toString() {
		return this.description;
	}
	
	public String getItemsString() {
		if ( items == null ) {
			return "";
		}
		
		return items.toString();
	}
	
	ColumnDescriptor[]		  columnDescriptors = null;
	HashMap<Integer, Integer> keyMap = null;
}
