/**
 * Copyright 2010 Richard Johnson & Orin Eman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ---
 *
 * This file is part of java-libpst.
 *
 * java-libpst is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * java-libpst is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with java-libpst.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.pff.objects;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import com.pff.exceptions.PSTException;
import com.pff.objects.sub.PSTGlobalObjectId;
import com.pff.objects.sub.PSTTimeZone;
import com.pff.parsing.DescriptorIndexNode;
import com.pff.parsing.PSTDescriptorItem;
import com.pff.parsing.tables.PSTTableBC;
import com.pff.source.PSTSource;

/**
 * PSTAppointment is for Calendar items
 * @author Richard Johnson
 */
public class PSTAppointment extends PSTMessage {

	public PSTAppointment(PSTSource theFile, DescriptorIndexNode descriptorIndexNode)
		throws PSTException, IOException
	{
		super(theFile, descriptorIndexNode);
	}

	public PSTAppointment(PSTSource theFile, DescriptorIndexNode folderIndexNode, PSTTableBC table, HashMap<Integer, PSTDescriptorItem> localDescriptorItems)
	{
		super(theFile, folderIndexNode, table, localDescriptorItems);
	}

	public boolean getSendAsICAL() {
		return (getBooleanItem(pstFile.getNameToIdMapItem(0x00008200, PSTSource.PSETID_Appointment)));
	}
	public int getBusyStatus()
	{
		return getIntItem(pstFile.getNameToIdMapItem(0x00008205, PSTSource.PSETID_Appointment));
	}
	public boolean getShowAsBusy() {
		return getBusyStatus() == 2;
	}
	public String getLocation() {
		return getStringItem(pstFile.getNameToIdMapItem(0x00008208, PSTSource.PSETID_Appointment));
	}
	public Date getStartTime() {
		return getDateItem(pstFile.getNameToIdMapItem(0x0000820d, PSTSource.PSETID_Appointment));
	}
	public PSTTimeZone getStartTimeZone() {
		return getTimeZoneItem(pstFile.getNameToIdMapItem(0x0000825e, PSTSource.PSETID_Appointment));
	}
	public Date getEndTime() {
		return getDateItem(pstFile.getNameToIdMapItem(0x0000820e, PSTSource.PSETID_Appointment));
	}
	public PSTTimeZone getEndTimeZone() {
		return getTimeZoneItem(pstFile.getNameToIdMapItem(0x0000825f, PSTSource.PSETID_Appointment));
	}
	
	public PSTTimeZone getRecurrenceTimeZone() {
		String desc = getStringItem(pstFile.getNameToIdMapItem(0x00008234, PSTSource.PSETID_Appointment));
		if ( desc!= null && desc.length() != 0 ) {
			byte[] tzData = getBinaryItem(pstFile.getNameToIdMapItem(0x00008233, PSTSource.PSETID_Appointment));
			if ( tzData != null && tzData.length != 0 ) {
				return new PSTTimeZone(desc, tzData);
			}
		}
		return null;
	}
	public int getDuration() {
		return getIntItem(pstFile.getNameToIdMapItem(0x00008213, PSTSource.PSETID_Appointment));
	}
	public int getColor() {
		return getIntItem(pstFile.getNameToIdMapItem(0x00008214, PSTSource.PSETID_Appointment));
	}
	public boolean getSubType() {
		return (getIntItem(pstFile.getNameToIdMapItem(0x00008215, PSTSource.PSETID_Appointment)) != 0);
	}
	public int getMeetingStatus() {
		return getIntItem(pstFile.getNameToIdMapItem(0x00008217, PSTSource.PSETID_Appointment));
	}
	public int getResponseStatus() {
		return getIntItem(pstFile.getNameToIdMapItem(0x00008218, PSTSource.PSETID_Appointment));
	}
	public boolean isRecurring() {
		return getBooleanItem(pstFile.getNameToIdMapItem(0x00008223, PSTSource.PSETID_Appointment));
	}
	public Date getRecurrenceBase() {
		return getDateItem(pstFile.getNameToIdMapItem(0x00008228, PSTSource.PSETID_Appointment));
	}
	public int getRecurrenceType() {
		return getIntItem(pstFile.getNameToIdMapItem(0x00008231, PSTSource.PSETID_Appointment));
	}
	public String getRecurrencePattern() {
		return getStringItem(pstFile.getNameToIdMapItem(0x00008232, PSTSource.PSETID_Appointment));
	}
	public byte[] getRecurrenceStructure() {
		return getBinaryItem(pstFile.getNameToIdMapItem(0x00008216, PSTSource.PSETID_Appointment));
	}
	public byte[] getTimezone() {
		return getBinaryItem(pstFile.getNameToIdMapItem(0x00008233, PSTSource.PSETID_Appointment));
	}
	public String getAllAttendees() {
		return getStringItem(pstFile.getNameToIdMapItem(0x00008238, PSTSource.PSETID_Appointment));
	}
	public String getToAttendees() {
		return getStringItem(pstFile.getNameToIdMapItem(0x0000823b, PSTSource.PSETID_Appointment));
	}
	public String getCCAttendees() {
		return getStringItem(pstFile.getNameToIdMapItem(0x0000823c, PSTSource.PSETID_Appointment));
	}
	public int getAppointmentSequence() {
		return getIntItem(pstFile.getNameToIdMapItem(0x00008201, PSTSource.PSETID_Appointment));
	}
	
	// online meeting properties
	public boolean isOnlineMeeting() {
		return (getBooleanItem(pstFile.getNameToIdMapItem(0x00008240, PSTSource.PSETID_Appointment)));
	}
	public int getNetMeetingType() {
		return getIntItem(pstFile.getNameToIdMapItem(0x00008241, PSTSource.PSETID_Appointment));
	}
	public String getNetMeetingServer() {
		return getStringItem(pstFile.getNameToIdMapItem(0x00008242, PSTSource.PSETID_Appointment));
	}
	public String getNetMeetingOrganizerAlias() {
		return getStringItem(pstFile.getNameToIdMapItem(0x00008243, PSTSource.PSETID_Appointment));
	}
	public boolean getNetMeetingAutostart() {
		return (getIntItem(pstFile.getNameToIdMapItem(0x00008245, PSTSource.PSETID_Appointment)) != 0);
	}
	public boolean getConferenceServerAllowExternal() {
		return (getBooleanItem(pstFile.getNameToIdMapItem(0x00008246, PSTSource.PSETID_Appointment)));
	}
	public String getNetMeetingDocumentPathName() {
		return getStringItem(pstFile.getNameToIdMapItem(0x00008247, PSTSource.PSETID_Appointment));
	}
	public String getNetShowURL() {
		return getStringItem(pstFile.getNameToIdMapItem(0x00008248, PSTSource.PSETID_Appointment));
	}
	public Date getAttendeeCriticalChange() {
		return getDateItem(pstFile.getNameToIdMapItem(0x00000001, PSTSource.PSETID_Meeting));
	}
	public Date getOwnerCriticalChange() {
		return getDateItem(pstFile.getNameToIdMapItem(0x0000001a, PSTSource.PSETID_Meeting));
	}
	public String getConferenceServerPassword() {
		return getStringItem(pstFile.getNameToIdMapItem(0x00008249, PSTSource.PSETID_Appointment));
	}
	
	public boolean getAppointmentCounterProposal() {
		return (getBooleanItem(pstFile.getNameToIdMapItem(0x00008257, PSTSource.PSETID_Appointment)));
	}

	public boolean isSilent() {
		return (getBooleanItem(pstFile.getNameToIdMapItem(0x00000004, PSTSource.PSETID_Meeting)));
	}

	public String getRequiredAttendees() {
		return getStringItem(this.pstFile.getNameToIdMapItem(0x00000006, PSTSource.PSETID_Meeting));
	}
	
	public int getLocaleId() {
		return getIntItem(0x3ff1);
	}

	/*public byte[] getGlobalObjectId() {
		return getBinaryItem(pstFile.getNameToIdMapItem(0x00000003, PSTSource.PSETID_Meeting));
	}*/
	public PSTGlobalObjectId getGlobalObjectId() {
	return new PSTGlobalObjectId(getBinaryItem(pstFile.getNameToIdMapItem(0x00000003, PSTSource.PSETID_Meeting)));
	}
	
	public PSTGlobalObjectId getCleanGlobalObjectId() {
		return new PSTGlobalObjectId(getBinaryItem(pstFile.getNameToIdMapItem(0x00000023, PSTSource.PSETID_Meeting)));
	}
}
