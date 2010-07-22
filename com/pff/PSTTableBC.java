/**
 * 
 */
package com.pff;

import java.util.*;


/**
 * The BC Table type. (Property Context)
 * Used by pretty much everything.
 * @author Richard Johnson
 */
class PSTTableBC extends PSTTable {
	
	private HashMap<Integer, PSTTableBCItem> items = new HashMap<Integer, PSTTableBCItem>();
	
	PSTTableBC(byte[] data)
		throws PSTException
	{
		this(data, new int[0]);
	}
	
	PSTTableBC(byte[] data, int[] offsets)
		throws PSTException
	{
		super(data, offsets, new HashMap<Integer, PSTDescriptorItem>());
		data = null;	// No direct access to data!
		

		if (tableTypeByte != 0xffffffbc)
		{
			System.out.println(Long.toHexString(this.tableTypeByte));
			throw new PSTException("unable to create PSTTableBC, table does not appear to be a bc!");
		}

		// go through each of the entries.
		NodeInfo keyTableInfo = getNodeInfo(hidRoot);
		numberOfKeys = (keyTableInfo.endOffset - keyTableInfo.startOffset) / (sizeOfItemKey+sizeOfItemValue);

		description += ("Number of entries: "+numberOfKeys+"\n");

		// Read the key table
		int offset = keyTableInfo.startOffset;
		for (int x = 0; x < numberOfKeys; x++) {
			
			PSTTableBCItem item = new PSTTableBCItem();
			item.itemIndex = x;
			item.entryType =(int)PSTObject.convertLittleEndianBytesToLong(keyTableInfo.data, offset+0, offset+2);
			item.entryValueType = (int)PSTObject.convertLittleEndianBytesToLong(keyTableInfo.data, offset+2, offset+4);
			item.entryValueReference = (int)PSTObject.convertLittleEndianBytesToLong(keyTableInfo.data, offset+4, offset+8);

			// Data is in entryValueReference for all types <= 4 bytes long
			switch ( item.entryValueType ) {

			case 0x0002:	// 16bit integer
				item.entryValueReference &= 0xFFFF;
			case 0x0003:	// 32bit integer
			case 0x000A:	// 32bit error code
/*
				System.out.printf("Integer%s: 0x%04X:%04X, %d\n",
					(item.entryValueType == 0x0002) ? "16" : "32",
					item.entryType, item.entryValueType,
					item.entryValueReference);
/**/
			case 0x0001:	// Place-holder
			case 0x0004:	// 32bit floating
				item.isExternalValueReference = true;
				break;

			case 0x000b:	// Boolean - a single byte
				item.entryValueReference &= 0xFF;
/*
				System.out.printf("boolean: 0x%04X:%04X, %s\n",
						item.entryType, item.entryValueType, 
						(item.entryValueReference == 0) ? "false" : "true");
/**/
				item.isExternalValueReference = true;
				break;

			case 0x000D:
			default:
				// Is it in the local heap?
				item.isExternalValueReference = true; // Assume not
				NodeInfo nodeInfo = getNodeInfo(item.entryValueReference);
				if ( nodeInfo == null ) {
					// It's an external reference that we don't deal with here.
/*
					System.out.printf("%s: %shid 0x%08X\n",
							(item.entryValueType == 0x1f || item.entryValueType == 0x1e) ? "String" : "Other",
									PSTFile.getPropertyDescription(item.entryType, item.entryValueType),
									item.entryValueReference);
/**/
				} else {
					// Make a copy of the data
					item.data = new byte[nodeInfo.endOffset-nodeInfo.startOffset];
					System.arraycopy(nodeInfo.data, nodeInfo.startOffset, item.data, 0, item.data.length);
					item.isExternalValueReference = false;
/*				
					if ( item.entryValueType == 0x1f ||
						 item.entryValueType == 0x1e )
					{
						try {
//							if ( item.entryType == 0x0037 )
							{
								String temp = new String(item.data, item.entryValueType == 0x1E ? "UTF8" : "UTF-16LE");
								System.out.printf("String: 0x%04X:%04X, \"%s\"\n",
										item.entryType, item.entryValueType, temp);
							}
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}
					else
					{

						System.out.printf("Other: 0x%04X:%04X, %d bytes\n",
								item.entryType, item.entryValueType, item.data.length);

					}
/**/
				}
				break;
			}
			
			offset = offset + 8;
						
			items.put(item.entryType, item);
			description += item.toString()+"\n\n";
		}

		ReleaseRawData();
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

