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

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

/**
 * Class containing information on exceptions to a recurring appointment
 * 
 * @author Orin Eman
 *
 *
 */
public class PSTAppointmentException {

    // Access methods - return the value from the exception if
    // OverrideFlags say it's present, otherwise the value from the appointment.
    public String getSubject() {
        if ((this.OverrideFlags & 0x0001) != 0) {
            try {
                return new String(this.WideCharSubject, "UTF-16LE");
            } catch (final UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return this.appt.getSubject();
    }

    public int getMeetingType() {
        if ((this.OverrideFlags & 0x0002) != 0) {
            return this.MeetingType;
        }

        return this.appt.getMeetingStatus();
    }

    public int getReminderDelta() {
        if ((this.OverrideFlags & 0x0004) != 0) {
            return this.ReminderDelta;
        }

        return this.appt.getReminderDelta();
    }

    public boolean getReminderSet() {
        if ((this.OverrideFlags & 0x0008) != 0) {
            return this.ReminderSet;
        }

        return this.appt.getReminderSet();
    }

    public String getLocation() {
        if ((this.OverrideFlags & 0x0010) != 0) {
            try {
                return new String(this.WideCharLocation, "UTF-16LE");
            } catch (final UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return this.appt.getLocation();
    }

    public int getBusyStatus() {
        if ((this.OverrideFlags & 0x0020) != 0) {
            return this.BusyStatus;
        }

        return this.appt.getBusyStatus();
    }

    public boolean getSubType() {
        if ((this.OverrideFlags & 0x0080) != 0) {
            return this.SubType;
        }

        return this.appt.getSubType();
    }

    public String getDescription() {
        if (this.embeddedMessage != null) {
            return this.embeddedMessage.getBodyPrefix();
        }

        return null;
    }

    public Date getDTStamp() {
        Date ret = null;
        if (this.embeddedMessage != null) {
            ret = this.embeddedMessage.getOwnerCriticalChange();
        }

        if (ret == null) {
            // Use current date/time
            final Calendar c = Calendar.getInstance(PSTTimeZone.utcTimeZone);
            ret = c.getTime();
        }

        return ret;
    }

    public int getStartDateTime() {
        return this.StartDateTime;
    }

    public int getEndDateTime() {
        return this.EndDateTime;
    }

    public int getOriginalStartDate() {
        return this.OriginalStartDate;
    }

    public int getAppointmentSequence(final int def) {
        if (this.embeddedMessage == null) {
            return def;
        }
        return this.embeddedMessage.getAppointmentSequence();
    }

    public int getImportance(final int def) {
        if (this.embeddedMessage == null) {
            return def;
        }
        return this.embeddedMessage.getImportance();
    }

    public byte[] getSubjectBytes() {
        if ((this.OverrideFlags & 0x0010) != 0) {
            return this.Subject;
        }

        return null;
    }

    public byte[] getLocationBytes() {
        if ((this.OverrideFlags & 0x0010) != 0) {
            return this.Location;
        }

        return null;
    }

    public boolean attachmentsPresent() {
        if ((this.OverrideFlags & 0x0040) != 0 && this.Attachment == 0x00000001) {
            return true;
        }

        return false;
    }

    public boolean embeddedMessagePresent() {
        return this.embeddedMessage != null;
    }

    //
    // Allow access to an embedded message for
    // properties that don't have access methods here.
    //
    public PSTAppointment getEmbeddedMessage() {
        return this.embeddedMessage;
    }

    PSTAppointmentException(final byte[] recurrencePattern, int offset, final int writerVersion2,
        final PSTAppointment appt) {
        this.writerVersion2 = writerVersion2;
        final int initialOffset = offset;
        this.appt = appt;
        this.embeddedMessage = null;

        this.StartDateTime = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
        offset += 4;
        this.EndDateTime = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
        offset += 4;
        this.OriginalStartDate = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
        offset += 4;
        this.OverrideFlags = (short) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 2);
        offset += 2;

        if ((this.OverrideFlags & ARO_SUBJECT) != 0) {
            // @SuppressWarnings("unused")
            // short SubjectLength =
            // (short)PSTObject.convertLittleEndianBytesToLong(recurrencePattern,
            // offset, offset+2);
            offset += 2;
            final short SubjectLength2 = (short) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                offset + 2);
            offset += 2;
            this.Subject = new byte[SubjectLength2];
            System.arraycopy(recurrencePattern, offset, this.Subject, 0, SubjectLength2);
            offset += SubjectLength2;
        }

        if ((this.OverrideFlags & ARO_MEETINGTYPE) != 0) {
            this.MeetingType = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
            offset += 4;
        }

        if ((this.OverrideFlags & ARO_REMINDERDELTA) != 0) {
            this.ReminderDelta = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
            offset += 4;
        }

        if ((this.OverrideFlags & ARO_REMINDER) != 0) {
            this.ReminderSet = ((int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                offset + 4) != 0);
            offset += 4;
        }

        if ((this.OverrideFlags & ARO_LOCATION) != 0) {
            // @SuppressWarnings("unused")
            // short LocationLength =
            // (short)PSTObject.convertLittleEndianBytesToLong(recurrencePattern,
            // offset, offset+2);
            offset += 2;
            final short LocationLength2 = (short) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                offset + 2);
            offset += 2;
            this.Location = new byte[LocationLength2];
            System.arraycopy(recurrencePattern, offset, this.Location, 0, LocationLength2);
            offset += LocationLength2;
        }

        if ((this.OverrideFlags & ARO_BUSYSTATUS) != 0) {
            this.BusyStatus = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
            offset += 4;
        }

        if ((this.OverrideFlags & ARO_ATTACHMENT) != 0) {
            this.Attachment = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
            offset += 4;
        }

        if ((this.OverrideFlags & ARO_SUBTYPE) != 0) {
            this.SubType = ((int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4) != 0);
            offset += 4;
        }

        this.length = offset - initialOffset;
    }

    void ExtendedException(final byte[] recurrencePattern, int offset) {
        final int initialOffset = offset;

        if (this.writerVersion2 >= 0x00003009) {
            final int ChangeHighlightSize = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                offset + 4);
            offset += 4;
            this.ChangeHighlightValue = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                offset + 4);
            offset += ChangeHighlightSize;
        }

        int ReservedBlockEESize = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
        offset += 4 + ReservedBlockEESize;

        // See http://msdn.microsoft.com/en-us/library/cc979209(office.12).aspx
        if ((this.OverrideFlags & (ARO_SUBJECT | ARO_LOCATION)) != 0) {
            // Same as regular Exception structure?
            this.StartDateTime = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
            offset += 4;
            this.EndDateTime = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
            offset += 4;
            this.OriginalStartDate = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                offset + 4);
            offset += 4;
        }

        if ((this.OverrideFlags & ARO_SUBJECT) != 0) {
            this.WideCharSubjectLength = (short) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                offset + 2);
            offset += 2;
            this.WideCharSubject = new byte[this.WideCharSubjectLength * 2];
            System.arraycopy(recurrencePattern, offset, this.WideCharSubject, 0, this.WideCharSubject.length);
            offset += this.WideCharSubject.length;
            /*
             * try {
             * String subject = new String(WideCharSubject, "UTF-16LE");
             * System.out.printf("Exception Subject: %s\n", subject);
             * } catch (UnsupportedEncodingException e) {
             * e.printStackTrace();
             * }
             * /
             **/
        }

        if ((this.OverrideFlags & ARO_LOCATION) != 0) {
            this.WideCharLocationLength = (short) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                offset + 2);
            offset += 2;
            this.WideCharLocation = new byte[this.WideCharLocationLength * 2];
            System.arraycopy(recurrencePattern, offset, this.WideCharLocation, 0, this.WideCharLocation.length);
            offset += this.WideCharLocation.length;
        }

        // See http://msdn.microsoft.com/en-us/library/cc979209(office.12).aspx
        if ((this.OverrideFlags & (ARO_SUBJECT | ARO_LOCATION)) != 0) {
            ReservedBlockEESize = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
            offset += 4 + ReservedBlockEESize;
        }

        this.extendedLength = offset - initialOffset;
    }

    void setEmbeddedMessage(final PSTAppointment embeddedMessage) {
        this.embeddedMessage = embeddedMessage;
    }

    private final int writerVersion2;
    private int StartDateTime;
    private int EndDateTime;
    private int OriginalStartDate;
    private final short OverrideFlags;
    private byte[] Subject = null;
    private int MeetingType;
    private int ReminderDelta;
    private boolean ReminderSet;
    private byte[] Location = null;
    private int BusyStatus;
    private int Attachment;
    private boolean SubType;
    // private int AppointmentColor; // Reserved - don't read from the PST file
    @SuppressWarnings("unused")
    private int ChangeHighlightValue;
    private int WideCharSubjectLength = 0;
    private byte[] WideCharSubject = null;
    private int WideCharLocationLength = 0;
    private byte[] WideCharLocation = null;
    private PSTAppointment embeddedMessage = null;
    private final PSTAppointment appt;
    private final int length;
    private int extendedLength;

    // Length of this ExceptionInfo structure in the PST file
    int getLength() {
        return this.length;
    }

    // Length of this ExtendedException structure in the PST file
    int getExtendedLength() {
        return this.extendedLength;
    }

    static final short ARO_SUBJECT = 0x0001;
    static final short ARO_MEETINGTYPE = 0x0002;
    static final short ARO_REMINDERDELTA = 0x0004;
    static final short ARO_REMINDER = 0x0008;
    static final short ARO_LOCATION = 0x0010;
    static final short ARO_BUSYSTATUS = 0x0020;
    static final short ARO_ATTACHMENT = 0x0040;
    static final short ARO_SUBTYPE = 0x0080;
}
