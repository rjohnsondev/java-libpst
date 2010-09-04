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
	protected int numberOfIndexLevels = 0;

	private PSTNodeInputStream in;
	
	private int[][]	rgbiAlloc = null;
	//private byte[]	data = null;
	private HashMap<Integer, PSTDescriptorItem> subNodeDescriptorItems = null;
	
	protected String description = "";
	
	protected PSTTable(PSTNodeInputStream in, HashMap<Integer, PSTDescriptorItem> subNodeDescriptorItems)
		throws PSTException, IOException
	{
		this.subNodeDescriptorItems = subNodeDescriptorItems;
		this.in = in;

		Long[] arrayBlocks = in.getBlockOffsets();

		// the next two bytes should be the table type (bSig)
		// 0xEC is HN (Heap-on-Node)
		in.seek(0);
		byte[] headdata = new byte[4];
		in.read(headdata);
		if ((int)headdata[2] != 0xffffffec) {
			System.out.println(in.isEncrypted());
			PSTObject.decode(headdata);
			PSTObject.printHexFormatted(headdata, true);
			throw new PSTException("Unable to parse table, bad table type...");
		}

		tableTypeByte = headdata[3];
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
				throw new PSTException("Unable to parse table, bad table type.  Unknown identifier: 0x"+Long.toHexString(headdata[3]));
		}
		
		// process the page maps
		rgbiAlloc = new int[arrayBlocks.length][];
		int blockOffset = 0;
		for ( int block = 0; block < arrayBlocks.length; ++block ) {
			// Get offset of HN page map
			//System.out.println("block: "+blockOffset);
			int iHeapNodePageMap = (int)in.seekAndReadLong(blockOffset, 2) + blockOffset;
			int cAlloc = (int)in.seekAndReadLong(iHeapNodePageMap, 2);
			//int cFree = (int)PSTObject.convertLittleEndianBytesToLong(data, iHeapNodePageMap+2, iHeapNodePageMap+4);
			iHeapNodePageMap += 4;
			//description += "Block["+block+"] number of items: "+cAlloc+" allocated, "+cFree+" freed\n";
			rgbiAlloc[block] = new int[cAlloc+1];	// There are actually cAlloc+1 entries in the array
			//description += "Page map:\n";
			//System.out.println("blcok");
			for (int x = 0; x < cAlloc+1; x++) {
				rgbiAlloc[block][x] = (int)in.seekAndReadLong(iHeapNodePageMap, 2)
										+ blockOffset;
				//System.out.println(rgbiAlloc[block][x]);
				iHeapNodePageMap += 2;
				//if (x > 1) {
				//	description += " "+(rgbiAlloc[block][x] - rgbiAlloc[block][x-1])+"\n";
				//}
				//description += "   index"+x+": "+ rgbiAlloc[block][x]+" ("+Long.toHexString(rgbiAlloc[block][x])+")";
			}
			//description += "\n";

			// Get offset of next block in data[]
			blockOffset = arrayBlocks[block].intValue();
		}

		//System.exit(0);

		hidUserRoot = (int)in.seekAndReadLong(4, 4);		// hidUserRoot
/*
		System.out.printf("Table %s: hidUserRoot 0x%08X\n", tableType, hidUserRoot);
/**/

		// all tables should have a BTHHEADER at hnid == 0x20
		NodeInfo headerNodeInfo = getNodeInfo(0x20);
		headerNodeInfo.in.seek(headerNodeInfo.startOffset);
		int headerByte = headerNodeInfo.in.read() &0xFF;
		if ( headerByte != 0xb5 ) {
			headerNodeInfo.in.seek(headerNodeInfo.startOffset);
			headerByte = headerNodeInfo.in.read() &0xFF;
			headerNodeInfo.in.seek(headerNodeInfo.startOffset);
			byte[] tmp = new byte[1024];
			headerNodeInfo.in.read(tmp);
			PSTObject.printHexFormatted(tmp, true);
			System.out.println(PSTObject.compEnc[headerByte]);
			throw new PSTException("Unable to parse table, can't find BTHHEADER header information: "+headerByte);
		}
		
		sizeOfItemKey = (int)headerNodeInfo.in.read() & 0xFF;		// Size of key in key table
		sizeOfItemValue = (int)headerNodeInfo.in.read() & 0xFF;	// Size of value in key table

		numberOfIndexLevels = (int)headerNodeInfo.in.read() & 0xFF;
		if ( numberOfIndexLevels != 0 ) {
			System.out.println(this.tableType);
			System.out.printf("Table with %d index levels\n", numberOfIndexLevels);
		}
		//hidRoot = (int)PSTObject.convertLittleEndianBytesToLong(nodeInfo, 4, 8);	// hidRoot
		hidRoot = (int)headerNodeInfo.seekAndReadLong(4, 4);
		//System.out.println(hidRoot);
		//System.exit(0);
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
		//data = null;
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
		//byte[]	data;
		PSTNodeInputStream in;
		
		NodeInfo(int start, int end, PSTNodeInputStream in) {
			startOffset = start;
			endOffset = end;
			this.in = in;
			//this.data = data;
		}
		
		int length() {
			return endOffset - startOffset;
		}

		long seekAndReadLong(long offset, int length)
				throws IOException, PSTException
		{
			return this.in.seekAndReadLong(startOffset+offset, length);
		}
	}
	
	protected NodeInfo getNodeInfo(int hnid)
		throws PSTException, IOException
	{
		////if ( data == null ) {
			////throw new PSTException("Accessing PSTTable heap after release!");
		//}

		// Zero-length node?
		if ( hnid == 0 ) {
			//return new NodeInfo(0, 0, data);
			//throw new PSTException("accessing a hnid of 0??");
			//byte[] data = new byte[(int)this.in.length()];
			//in.seek(0);
			//return data;
			return new NodeInfo(0, 0, this.in);
		}

		// Is it a subnode ID?
		if ( subNodeDescriptorItems != null &&
			 subNodeDescriptorItems.containsKey(hnid) )
		{
			PSTDescriptorItem item = subNodeDescriptorItems.get(hnid);
			//byte[] data;
			NodeInfo subNodeInfo = null;

			try {
				//data = item.getData();
				PSTNodeInputStream subNodeIn = new PSTNodeInputStream(in.getPSTFile(), item);
				subNodeInfo = new NodeInfo(0, (int)subNodeIn.length(), subNodeIn);
			} catch (IOException e) {
				throw new PSTException(String.format("IOException reading subNode: 0x%08X", hnid));
			}

			//return new NodeInfo(0, data.length, data);
			return subNodeInfo;
		}

		if ( (hnid & 0x1F) != 0 ) {
			// Some kind of external node
			return null;
		}

		int whichBlock = (hnid >>> 16);
		if ( whichBlock > rgbiAlloc.length ) {
			// Block doesn't exist!
			System.out.printf("getNodeInfo: block doesn't exist! hnid = 0x%08X\n", hnid);
			System.out.printf("getNodeInfo: block doesn't exist! whichBlock = 0x%08X\n", whichBlock);
			System.out.println(rgbiAlloc.length);
			throw new PSTException("wigging out");
			//return null;
		}

		// A normal node in a local heap
		int index = (hnid & 0xFFFF) >> 5;
		if ( index >= rgbiAlloc[whichBlock].length ) {
			System.out.printf("getNodeInfo: node index doesn't exist! nid = 0x%08X\n", hnid);
			return null;
		}


		NodeInfo out = new NodeInfo(rgbiAlloc[whichBlock][index-1], rgbiAlloc[whichBlock][index], in);
		//System.out.println(hnid+ " - "+out.startOffset);
		return out;
	}

}
