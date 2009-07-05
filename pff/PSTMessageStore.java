/**
 * 
 */
package com.pff;

import java.io.*;


/**
 * @author toweruser
 *
 */
class PSTMessageStore extends PSTObject {
	
	PSTMessageStore(PSTFile theFile, DescriptorIndexNode descriptorIndexNode)
		throws PSTException, IOException
	{
		super(theFile, descriptorIndexNode);
	}
	
	/**
	 * Get the GUID
	 * Note: I don't know if the endianess is correct!!! 
	 * @return
	 */
	public String getGUID() {
		// attempt to find in the table.
		int guidEntryType = 0x0e34;
		if (this.items.containsKey(guidEntryType)) {
			PSTTableBCItem item = (PSTTableBCItem)this.items.get(guidEntryType);
			byte[] guidBytes = item.data;
			StringBuffer output = new StringBuffer();
			String hexValue = "";
			output.append("{");
			for (int x = 0; x < 16; x++) {
				hexValue = Integer.toHexString(guidBytes[x]);
				if (hexValue.length() == 1) {
					output.append(hexValue);
				}
				if (hexValue.length() > 2) {
					hexValue = hexValue.substring(hexValue.length()-2);
				}
				output.append(hexValue);
				if (x == 4 ||
					x == 6 ||
					x == 8 ||
					x == 10)
				{
					output.append("-");
				}
			}
			output.append("}");
			return new String(output);
		}
		return "";
	}
	
	/**
	 * get the message store display name
	 * @return
	 */
	public String getDisplayName() {
		// attempt to find in the table.
		int displayNameEntryType = 0x3001;
		if (this.items.containsKey(displayNameEntryType)) {
			PSTTableBCItem item = (PSTTableBCItem)this.items.get(displayNameEntryType);
			return new String(item.getStringValue());
		}
		return "";
	}
	
	
}
