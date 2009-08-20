/**
 * 
 */
package com.pff;

import java.io.IOException;
import java.util.Date;
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

	public boolean getSendAsICAL() {
		return (this.getIntItem(this.pstFile.getNameToIdMapItem(0x8200)) != 0);
	}
	public boolean getShowAsBusy() {
		return (this.getIntItem(this.pstFile.getNameToIdMapItem(0x8205)) == 2);
	}
	public String getLocation() {
		return this.getStringItem(this.pstFile.getNameToIdMapItem(0x8208));
	}
	public Date getStartTime() {
		return this.getDateItem(this.pstFile.getNameToIdMapItem(0x820d));
	}
	public Date getEndTime() {
		return this.getDateItem(this.pstFile.getNameToIdMapItem(0x820e));
	}
	public int getDuration() {
		return this.getIntItem(this.pstFile.getNameToIdMapItem(0x8213));
	}
	public int getColor() {
		return this.getIntItem(this.pstFile.getNameToIdMapItem(0x8214));
	}
	public boolean getSubType() {
		return (this.getIntItem(this.pstFile.getNameToIdMapItem(0x8215)) != 0);
	}
	public int getMeetingStatus() {
		return this.getIntItem(this.pstFile.getNameToIdMapItem(0x8217));
	}
	public int getResponseStatus() {
		return this.getIntItem(this.pstFile.getNameToIdMapItem(0x8218));
	}
	public int isRecurring() {
		return this.getIntItem(this.pstFile.getNameToIdMapItem(0x8223));
	}
	public Date getRecurrenceBase() {
		return this.getDateItem(this.pstFile.getNameToIdMapItem(0x8228));
	}
	public int getRecurrenceType() {
		return this.getIntItem(this.pstFile.getNameToIdMapItem(0x8231));
	}
	public String getRecurrencePattern() {
		return this.getStringItem(this.pstFile.getNameToIdMapItem(0x8232));
	}
	public String getTimezone() {
		return this.getStringItem(this.pstFile.getNameToIdMapItem(0x8233));
	}
	public String getAllAttendees() {
		System.out.println(this.items);
		return this.getStringItem(this.pstFile.getNameToIdMapItem(0x8238));
	}
	public String getToAttendees() {
		return this.getStringItem(this.pstFile.getNameToIdMapItem(0x823b));
	}
	public String getCCAttendees() {
		return this.getStringItem(this.pstFile.getNameToIdMapItem(0x823c));
	}
	
	// online meeting properties
	public boolean isOnlineMeeting() {
		return (this.getIntItem(this.pstFile.getNameToIdMapItem(0x8240)) != 0);
	}
	public int getNetMeetingType() {
		return this.getIntItem(this.pstFile.getNameToIdMapItem(0x8241));
	}
	public String getNetMeetingServer() {
		return this.getStringItem(this.pstFile.getNameToIdMapItem(0x8242));
	}
	public String getNetMeetingOrganizerAlias() {
		return this.getStringItem(this.pstFile.getNameToIdMapItem(0x8243));
	}
	public boolean getNetMeetingAutostart() {
		return (this.getIntItem(this.pstFile.getNameToIdMapItem(0x8245)) != 0);
	}
	public boolean getConferenceServerAllowExternal() {
		return (this.getIntItem(this.pstFile.getNameToIdMapItem(0x8246)) != 0);
	}
	public String getNetMeetingDocumentPathName() {
		return this.getStringItem(this.pstFile.getNameToIdMapItem(0x8247));
	}
	public String getNetShowURL() {
		return this.getStringItem(this.pstFile.getNameToIdMapItem(0x8248));
	}
	public String getConferenceServerPassword() {
		return this.getStringItem(this.pstFile.getNameToIdMapItem(0x8249));
	}
	
	public boolean getAppointmentCounterProposal() {
		return (this.getIntItem(this.pstFile.getNameToIdMapItem(0x8257)) != 0);
	}
//
//	public boolean isSilent() {
//		return (this.getIntItem(this.pstFile.getNameToIdMapItem(0x4)) > 0);
//	}
//	public String getRequiredAttendees() {
//		return this.getStringItem(this.pstFile.getNameToIdMapItem(0x6));
//	}
	
	
}
