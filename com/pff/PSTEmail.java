/**
 * 
 */
package com.pff;

import java.io.*;
import java.util.*;


/**
 * @author toweruser
 *
 */
public class PSTEmail extends PSTMessage {
	
	PSTEmail(PSTFile theFile, DescriptorIndexNode descriptorIndexNode)
			throws PSTException, IOException
	{
		super(theFile, descriptorIndexNode);
	}

	PSTEmail(PSTFile theFile, DescriptorIndexNode folderIndexNode, PSTTableBC table, HashMap<Integer, PSTDescriptorItem> localDescriptorItems){
		super(theFile, folderIndexNode, table, localDescriptorItems);
	}
	
	
	public String getPlainText() {
		return this.getStringItem(0x1000);
	}

	public String getRTFBody()
		throws PSTException, IOException
	{
		// do we have an entry for it?
		if (this.items.containsKey(0x1009))
		{
			// is it a reference?
			PSTTableBCItem item = this.items.get(0x1009);
			if (item.data.length > 0) {
				throw new PSTException("Umm, not sure what to do with this data here, was just expecting a local descriptor node ref.");
			}
			int ref = this.getIntItem(0x1009);
			PSTDescriptorItem descItem = this.localDescriptorItems.get(ref);
			RandomAccessFile in = this.pstFile.getFileHandle();
			//get the data at the location
			OffsetIndexItem indexItem = PSTObject.getOffsetIndexNode(in, descItem.offsetIndexIdentifier);
			in.seek(indexItem.fileOffset);
			byte[] temp = new byte[indexItem.size];
			in.read(temp);
			temp = PSTObject.decode(temp);
			return (LZFu.decode(temp));
		}
		
		return "";
	}

	
	public String getBodyHTML() {
		return this.getStringItem(0x1013, PSTTableItem.VALUE_TYPE_PT_STRING8);
	}
	
	
}
