/**
 * 
 */
package com.pff;

import java.io.*;
import java.util.*;


/**
 * Object that represents the message store.
 * Not much use other than to get the "name" of the PST file.
 * @author Richard Johnson
 */
public class PSTMessageStore extends PSTObject {
	
	PSTMessageStore(PSTFile theFile, DescriptorIndexNode descriptorIndexNode)
		throws PSTException, IOException
	{
		super(theFile, descriptorIndexNode);
	}
	
	/**
	 * Get the tag record key, unique to this pst
	 */
	public UUID getTagRecordKeyAsUUID() {
		// attempt to find in the table.
		int guidEntryType = 0x0ff9;
		if (this.items.containsKey(guidEntryType)) {
			PSTTableBCItem item = (PSTTableBCItem)this.items.get(guidEntryType);
			int offset = 0;
			byte[] bytes = item.data;
			long mostSigBits = (PSTObject.convertLittleEndianBytesToLong(bytes, offset, offset+4) << 32) |
								(PSTObject.convertLittleEndianBytesToLong(bytes, offset+4, offset+6) << 16) |
								PSTObject.convertLittleEndianBytesToLong(bytes, offset+6, offset+8);
			long leastSigBits = PSTObject.convertBigEndianBytesToLong(bytes, offset+8, offset+16);
			return new UUID(mostSigBits, leastSigBits);
		}
		return null;
	}
	
	/**
	 * get the message store display name
	 */
	public String getDisplayName() {
		// attempt to find in the table.
		int displayNameEntryType = 0x3001;
		if (this.items.containsKey(displayNameEntryType)) {
			return this.getStringItem(displayNameEntryType);
			//PSTTableBCItem item = (PSTTableBCItem)this.items.get(displayNameEntryType);
			//return new String(item.getStringValue());
		}
		return "";
	}


	public void getDetails() {
		System.out.println(this.items);
	}

}
