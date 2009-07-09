/**
 * 
 */
package com.pff;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author lappyuser
 *
 */
public class PSTAppointment extends PSTMessage {

	PSTAppointment(PSTFile theFile, DescriptorIndexNode descriptorIndexNode)
		throws PSTException, IOException
	{
		super(theFile, descriptorIndexNode);
	}

	PSTAppointment(PSTFile theFile, DescriptorIndexNode folderIndexNode, PSTTableBC table, HashMap<Integer, PSTDescriptorItem> localDescriptorItems)
	{
		super(theFile, folderIndexNode, table, localDescriptorItems);
	}

	public String getLocation() {
		// okay, the item we are looking for is number "2" in the name to id map
		System.out.println(this.items);
		return this.getStringItem(this.pstFile.getNameToIdMapItem(0x2));
	}

	public boolean isSilent() {
		return (this.getIntItem(this.pstFile.getNameToIdMapItem(0x4)) > 0);
	}
	public int isRecurring() {
		System.out.println(this.items);
		System.out.println(this.pstFile.getNameToIdMapItem(0x5));
		return this.getIntItem(this.pstFile.getNameToIdMapItem(0x5));
	}
	public String getRequiredAttendees() {
		return this.getStringItem(this.pstFile.getNameToIdMapItem(0x6));
	}
	
	
}
