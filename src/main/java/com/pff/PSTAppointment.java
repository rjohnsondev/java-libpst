/**
 * Copyright 2010 Richard Johnson & Orin Eman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with java-libpst. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.pff;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

/**
 * PSTAppointment is for Calendar items
 * 
 * @author Richard Johnson
 */
public class PSTAppointment extends PSTMessage implements IAppointment {

    PSTAppointment(final PSTFile theFile, final DescriptorIndexNode descriptorIndexNode)
        throws PSTException, IOException {
        super(theFile, descriptorIndexNode);
    }

    PSTAppointment(final PSTFile theFile, final DescriptorIndexNode folderIndexNode, final PSTTableBC table,
        final HashMap<Integer, PSTDescriptorItem> localDescriptorItems) {
        super(theFile, folderIndexNode, table, localDescriptorItems);
    }

    @Override
    public boolean getSendAsICAL() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x00008200, PSTFile.PSETID_Appointment)));
    }

    @Override
    public int getBusyStatus() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008205, PSTFile.PSETID_Appointment));
    }

    @Override
    public boolean getShowAsBusy() {
        return this.getBusyStatus() == 2;
    }

    @Override
    public String getLocation() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008208, PSTFile.PSETID_Appointment));
    }

    @Override
    public Date getStartTime() {
        return this.getDateItem(this.pstFile.getNameToIdMapItem(0x0000820d, PSTFile.PSETID_Appointment));
    }

    @Override
    public PSTTimeZone getStartTimeZone() {
        return this.getTimeZoneItem(this.pstFile.getNameToIdMapItem(0x0000825e, PSTFile.PSETID_Appointment));
    }

    @Override
    public Date getEndTime() {
        return this.getDateItem(this.pstFile.getNameToIdMapItem(0x0000820e, PSTFile.PSETID_Appointment));
    }

    @Override
    public PSTTimeZone getEndTimeZone() {
        return this.getTimeZoneItem(this.pstFile.getNameToIdMapItem(0x0000825f, PSTFile.PSETID_Appointment));
    }

    @Override
    public PSTTimeZone getRecurrenceTimeZone() {
        final String desc = this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008234, PSTFile.PSETID_Appointment));
        if (desc != null && desc.length() != 0) {
            final byte[] tzData = this
                .getBinaryItem(this.pstFile.getNameToIdMapItem(0x00008233, PSTFile.PSETID_Appointment));
            if (tzData != null && tzData.length != 0) {
                return new PSTTimeZone(desc, tzData);
            }
        }
        return null;
    }

    @Override
    public int getDuration() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008213, PSTFile.PSETID_Appointment));
    }

    @Override
    public int getColor() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008214, PSTFile.PSETID_Appointment));
    }

    @Override
    public boolean getSubType() {
        return (this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008215, PSTFile.PSETID_Appointment)) != 0);
    }

    @Override
    public int getMeetingStatus() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008217, PSTFile.PSETID_Appointment));
    }

    @Override
    public int getResponseStatus() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008218, PSTFile.PSETID_Appointment));
    }

    @Override
    public boolean isRecurring() {
        return this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x00008223, PSTFile.PSETID_Appointment));
    }

    @Override
    public Date getRecurrenceBase() {
        return this.getDateItem(this.pstFile.getNameToIdMapItem(0x00008228, PSTFile.PSETID_Appointment));
    }

    @Override
    public int getRecurrenceType() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008231, PSTFile.PSETID_Appointment));
    }

    @Override
    public String getRecurrencePattern() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008232, PSTFile.PSETID_Appointment));
    }

    @Override
    public byte[] getRecurrenceStructure() {
        return this.getBinaryItem(this.pstFile.getNameToIdMapItem(0x00008216, PSTFile.PSETID_Appointment));
    }

    @Override
    public byte[] getTimezone() {
        return this.getBinaryItem(this.pstFile.getNameToIdMapItem(0x00008233, PSTFile.PSETID_Appointment));
    }

    @Override
    public String getAllAttendees() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008238, PSTFile.PSETID_Appointment));
    }

    @Override
    public String getToAttendees() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x0000823b, PSTFile.PSETID_Appointment));
    }

    @Override
    public String getCCAttendees() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x0000823c, PSTFile.PSETID_Appointment));
    }

    @Override
    public int getAppointmentSequence() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008201, PSTFile.PSETID_Appointment));
    }

    // online meeting properties
    @Override
    public boolean isOnlineMeeting() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x00008240, PSTFile.PSETID_Appointment)));
    }

    @Override
    public int getNetMeetingType() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008241, PSTFile.PSETID_Appointment));
    }

    @Override
    public String getNetMeetingServer() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008242, PSTFile.PSETID_Appointment));
    }

    @Override
    public String getNetMeetingOrganizerAlias() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008243, PSTFile.PSETID_Appointment));
    }

    @Override
    public boolean getNetMeetingAutostart() {
        return (this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008245, PSTFile.PSETID_Appointment)) != 0);
    }

    @Override
    public boolean getConferenceServerAllowExternal() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x00008246, PSTFile.PSETID_Appointment)));
    }

    @Override
    public String getNetMeetingDocumentPathName() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008247, PSTFile.PSETID_Appointment));
    }

    @Override
    public String getNetShowURL() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008248, PSTFile.PSETID_Appointment));
    }

    @Override
    public Date getAttendeeCriticalChange() {
        return this.getDateItem(this.pstFile.getNameToIdMapItem(0x00000001, PSTFile.PSETID_Meeting));
    }

    @Override
    public Date getOwnerCriticalChange() {
        return this.getDateItem(this.pstFile.getNameToIdMapItem(0x0000001a, PSTFile.PSETID_Meeting));
    }

    @Override
    public String getConferenceServerPassword() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008249, PSTFile.PSETID_Appointment));
    }

    @Override
    public boolean getAppointmentCounterProposal() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x00008257, PSTFile.PSETID_Appointment)));
    }

    @Override
    public boolean isSilent() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x00000004, PSTFile.PSETID_Meeting)));
    }

    @Override
    public String getRequiredAttendees() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00000006, PSTFile.PSETID_Meeting));
    }

    @Override
    public int getLocaleId() {
        return this.getIntItem(0x3ff1);
    }

    @Override
    public PSTGlobalObjectId getGlobalObjectId() {
        return new PSTGlobalObjectId(
            this.getBinaryItem(this.pstFile.getNameToIdMapItem(0x00000003, PSTFile.PSETID_Meeting)));
    }

    @Override
    public PSTGlobalObjectId getCleanGlobalObjectId() {
        return new PSTGlobalObjectId(
            this.getBinaryItem(this.pstFile.getNameToIdMapItem(0x00000023, PSTFile.PSETID_Meeting)));
    }
}
