/**
 * 
 */
package com.pff;

import java.io.IOException;
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
	
	private int[][]	rgbiAlloc = null;
	private byte[]	data = null;
	private HashMap<Integer, PSTDescriptorItem> subNodeDescriptorItems = null;
	
	protected String description = "";
	
	protected PSTTable(byte[] data, int[] arrayBlocks, HashMap<Integer, PSTDescriptorItem> subNodeDescriptorItems)
		throws PSTException
	{
		this.data = data;
		this.subNodeDescriptorItems = subNodeDescriptorItems;

		if ( arrayBlocks == null ) {
			arrayBlocks = new int[1];
			arrayBlocks[0] = data.length;
		}

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
		
		// process the page maps
		rgbiAlloc = new int[arrayBlocks.length][];
		int blockOffset = 0;
		for ( int block = 0; block < arrayBlocks.length; ++block ) {
			// Get offset of HN page map
			int iHeapNodePageMap = (int)PSTObject.convertLittleEndianBytesToLong(data, blockOffset, blockOffset+2) + blockOffset;
			int cAlloc = (int)PSTObject.convertLittleEndianBytesToLong(data, iHeapNodePageMap, iHeapNodePageMap+2);
			//int cFree = (int)PSTObject.convertLittleEndianBytesToLong(data, iHeapNodePageMap+2, iHeapNodePageMap+4);
			iHeapNodePageMap += 4;
			//description += "Block["+block+"] number of items: "+cAlloc+" allocated, "+cFree+" freed\n";
			rgbiAlloc[block] = new int[cAlloc+1];	// There are actually cAlloc+1 entries in the array
			//description += "Page map:\n";
			for (int x = 0; x < cAlloc+1; x++) {
				rgbiAlloc[block][x] = (int)PSTObject.convertLittleEndianBytesToLong(data, iHeapNodePageMap, iHeapNodePageMap+2)
										+ blockOffset;
				iHeapNodePageMap += 2;
				//if (x > 1) {
				//	description += " "+(rgbiAlloc[block][x] - rgbiAlloc[block][x-1])+"\n";
				//}
				//description += "   index"+x+": "+ rgbiAlloc[block][x]+" ("+Long.toHexString(rgbiAlloc[block][x])+")";
			}
			//description += "\n";

			// Get offset of next block in data[]
			blockOffset = arrayBlocks[block];
		}

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
			"hidUserRoot: "+hidUserRoot+" - 0x"+Long.toHexString(hidUserRoot)+"\n"+
			"Size Of Keys: "+sizeOfItemKey+" - 0x"+Long.toHexString(sizeOfItemKey)+"\n"+
			"Size Of Values: "+sizeOfItemValue+" - 0x"+Long.toHexString(sizeOfItemValue)+"\n"+
			"hidRoot: "+hidRoot+" - 0x"+Long.toHexString(hidRoot)+"\n";
	}


	protected void ReleaseRawData() {
		rgbiAlloc = null;
		data = null;
//		arrayBlocks = null;
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
			byte[] data;
			try {
				data = item.getData();
			} catch (IOException e) {
				throw new PSTException(String.format("IOException reading subNode: 0x%08X", nid));
			}

			if ( data == null ) {
				return null;
			}
			return new NodeInfo(0, data.length, data);
		}

		if ( (nid & 0x1F) != 0 ) {
			// Some kind of external node
			return null;
		}

		int whichBlock = (nid >>> 16);
		if ( whichBlock >= rgbiAlloc.length ) {
			// Block doesn't exist!
			System.out.printf("getNodeInfo: block doesn't exist! nid = 0x%08X\n", nid);
			return null;
		}

		// A normal node in a local heap
		int index = (nid & 0xFFFF) >> 5;
		if ( index >= rgbiAlloc[whichBlock].length ) {
			System.out.printf("getNodeInfo: node index doesn't exist! nid = 0x%08X\n", nid);
			return null;
		}

		return new NodeInfo(rgbiAlloc[whichBlock][index-1], rgbiAlloc[whichBlock][index], data);
	}

}
