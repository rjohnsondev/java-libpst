/**
 * 
 */
package com.pff;

import java.util.HashMap;


/**
 * The PST Table is the workhorse of the whole system.
 * It allows for an item to be read and broken down into the individual properties that it consists of.
 * For most PST Objects, it appears that only 7c and bc table types are used.
 * @author Richard Johnson
 */
class PSTTable {

	protected String tableType;
	protected byte tableTypeByte;
	protected int hidUserRoot;
	
	// info from the b5 header
	protected int sizeOfItemKey;
	protected int sizeOfItemValue;
	protected int hidRoot;
	protected int numberOfKeys = 0;
	
	private int		iHeapNodePageMap;
	private int[]	rgbiAlloc = null;
	private byte[]	data = null;
	private int[]	arrayBlocks = null;
	private HashMap<Integer, PSTDescriptorItem> subNodeDescriptorItems = null;
	
	protected String description = "";
	
	protected PSTTable(byte[] data)
		throws PSTException
	{
		this(data, new int[0], new HashMap<Integer, PSTDescriptorItem>());
	}
	
	protected PSTTable(byte[] data, int[] arrayBlocks, HashMap<Integer, PSTDescriptorItem> subNodeDescriptorItems)
		throws PSTException
	{
		this.data = data;
		this.arrayBlocks = arrayBlocks;
		this.subNodeDescriptorItems = subNodeDescriptorItems;

		// the next two bytes should be the table type (bSig)
		// 0xEC is HN (Heap-on-Node)
		if ((int)data[2] != 0xffffffec) {
			throw new PSTException("Unable to parse table, bad table type...");
		}

		tableTypeByte = data[3];
		switch ((int)tableTypeByte) {	// bClientSig
			case 0x7c:					// Table Context (TC/HN)
				tableType = "7c";
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
//			case 0xFFFFFFb5:		// BTree-on-Heap (BTH)
//				tableType = "b5";
//				valid = true;
//				break;
			case 0xffffffbc:
				tableType = "bc";	// Property Context (PC/BTH)
				break;
			default:
				throw new PSTException("Unable to parse table, bad table type.  Unknown identifier: 0x"+Long.toHexString(data[3]));
		}
		
		// process the page map
		iHeapNodePageMap = (int)PSTObject.convertLittleEndianBytesToLong(data, 0, 2);	// Offset of HN page map
		int cAlloc = (int)PSTObject.convertLittleEndianBytesToLong(data, iHeapNodePageMap, iHeapNodePageMap+2);
		int cFree = (int)PSTObject.convertLittleEndianBytesToLong(data, iHeapNodePageMap+2, iHeapNodePageMap+4);
		description += "Number of items: "+cAlloc+" allocated, "+cFree+" freed\n";
		rgbiAlloc = new int[cAlloc+1];	// There are actually cAlloc+1 entries in the array
		description += "Page map:\n";
		int entryOffset = iHeapNodePageMap + 4;
		for (int x = 0; x < cAlloc+1; x++) {
			rgbiAlloc[x] = (int)PSTObject.convertLittleEndianBytesToLong(data, entryOffset, entryOffset+2);
			if (x > 1) {
				description += " "+(rgbiAlloc[x] - rgbiAlloc[x-1])+"\n";
			}
			description += "   index"+x+": "+ rgbiAlloc[x]+" ("+Long.toHexString(rgbiAlloc[x])+")";
			entryOffset += 2;
		}
		description += "\n";

		hidUserRoot = (int)PSTObject.convertLittleEndianBytesToLong(data, 4, 8);		// hidUserRoot
/*
		System.out.printf("Table %s: hidUserRoot 0x%08X\n", tableType, hidUserRoot);
/**/

		// all tables should have a BTHHEADER at hnid == 0x20
		NodeInfo nodeInfo = getNodeInfo(0x20);
		if ( (int)(nodeInfo.data[nodeInfo.startOffset] & 0xff) != 0xb5 ) {
			throw new PSTException("Unable to parse table, can't find BTHHEADER header information: "+Long.toHexString(nodeInfo.data[nodeInfo.startOffset]));
		}
		
		sizeOfItemKey = (int)nodeInfo.data[nodeInfo.startOffset+1] & 0xFF;		// Size of key in key table
		sizeOfItemValue = (int)nodeInfo.data[nodeInfo.startOffset+2] & 0xFF;	// Size of value in key table

		if ( (int)nodeInfo.data[nodeInfo.startOffset+3] != 0 ) {
			System.out.printf("Table with %d index levels\n", (int)nodeInfo.data[nodeInfo.startOffset+3]);
		}
		hidRoot = (int)PSTObject.convertLittleEndianBytesToLong(nodeInfo.data, nodeInfo.startOffset+4, nodeInfo.startOffset+8);	// hidRoot
/*
		System.out.printf("Table %s: hidRoot 0x%08X\n", tableType, hidRoot);
/**/		
		description += "Table ("+tableType+")\n"+
			"Table Page Map Offset: "+iHeapNodePageMap+" - 0x"+Long.toHexString(iHeapNodePageMap)+"\n"+
			"hidUserRoot: "+hidUserRoot+" - 0x"+Long.toHexString(hidUserRoot)+"\n"+
			"Size Of Keys: "+sizeOfItemKey+" - 0x"+Long.toHexString(sizeOfItemKey)+"\n"+
			"Size Of Values: "+sizeOfItemValue+" - 0x"+Long.toHexString(sizeOfItemValue)+"\n"+
			"hidRoot: "+hidRoot+" - 0x"+Long.toHexString(hidRoot)+"\n";
	}


	protected void ReleaseRawData() {
		rgbiAlloc = null;
		data = null;
		arrayBlocks = null;
		subNodeDescriptorItems = null;
	}

	
	/**
	 * get the number of items stored in this table.
	 * @return
	 */
	public int getRowCount() {
		return this.numberOfKeys;
	}
	
	class NodeInfo
	{
		int		startOffset;
		int		endOffset;
		byte[]	data;
		
		NodeInfo(int start, int end, byte[] data) {
			startOffset = start;
			endOffset = end;
			this.data = data;
		}
		
		int length() {
			return endOffset - startOffset;
		}
	}
	
	protected NodeInfo getNodeInfo(int nid)
		throws PSTException
	{
		if ( data == null ) {
			throw new PSTException("Accessing PSTTable heap after release!");
		}

		// Zero-length node?
		if ( nid == 0 ) {
			return new NodeInfo(0, 0, data);
		}

		// Is it a subnode ID?
		if ( subNodeDescriptorItems != null &&
			 subNodeDescriptorItems.containsKey(nid) )
		{
			PSTDescriptorItem item = subNodeDescriptorItems.get(nid);
			return new NodeInfo(0, item.data.length, item.data);
		}

		if ( (nid & 0x1F) != 0 ) {
			// Some kind of external node
			return null;
		}

		int whichBlock = (nid >>> 16);
		if ( whichBlock == 0 )
		{
			// A normal node in the current heap?
			nid >>= 5;
			if ( nid >= rgbiAlloc.length ) {
				return null;	// Invalid
			}
			return new NodeInfo(rgbiAlloc[nid - 1], rgbiAlloc[nid], data);
		}

		// A node in a different block
		if ( whichBlock >= arrayBlocks.length ) {
			// Block doesn't exist!
			return null;
		}

		// just kinda feeling my way around here....
		int blockStart = arrayBlocks[whichBlock-1];
		int tableEntriesReferenceAsOffset2 = ((nid & 0xFFFF)>>>4)-2+4;	// -2 as 1-based, +4 to skip index table header
		
		// get the index offset of the applicable block
		int tableIndexOffset2 = (int)PSTObject.convertLittleEndianBytesToLong(data, blockStart+0, blockStart+2)+blockStart;
		
		// get the location of the block.
		int start =
			(int)
			PSTObject.convertLittleEndianBytesToLong(
				data,
				tableIndexOffset2+tableEntriesReferenceAsOffset2,
				tableIndexOffset2+tableEntriesReferenceAsOffset2+2
			)+blockStart;
		int end =
			(int)
			PSTObject.convertLittleEndianBytesToLong(
					data,
					tableIndexOffset2+tableEntriesReferenceAsOffset2+2,
					tableIndexOffset2+tableEntriesReferenceAsOffset2+4
			)+blockStart;
		
		return new NodeInfo(start, end, data);
	}
	
}
