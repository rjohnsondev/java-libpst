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

/*
 * import java.text.SimpleDateFormat;
 * /
 **/

import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * Class containing recurrence information for a recurring appointment
 * 
 * @author Orin Eman
 *
 *
 */

public class PSTAppointmentRecurrence {

    // Access methods

    public short getExceptionCount() {
        return this.ExceptionCount;
    }

    public PSTAppointmentException getException(final int i) {
        if (i < 0 || i >= this.ExceptionCount) {
            return null;
        }
        return this.Exceptions[i];
    }

    public Calendar[] getDeletedInstanceDates() {
        return this.DeletedInstanceDates;
    }

    public Calendar[] getModifiedInstanceDates() {
        return this.ModifiedInstanceDates;
    }

    public short getCalendarType() {
        return this.CalendarType;
    }

    public short getPatternType() {
        return this.PatternType;
    }

    public int getPeriod() {
        return this.Period;
    }

    public int getPatternSpecific() {
        return this.PatternSpecific;
    }

    public int getFirstDOW() {
        return this.FirstDOW;
    }

    public int getPatternSpecificNth() {
        return this.PatternSpecificNth;
    }

    public int getFirstDateTime() {
        return this.FirstDateTime;
    }

    public int getEndType() {
        return this.EndType;
    }

    public int getOccurrenceCount() {
        return this.OccurrenceCount;
    }

    public int getEndDate() {
        return this.EndDate;
    }

    public int getStartTimeOffset() {
        return this.StartTimeOffset;
    }

    public PSTTimeZone getTimeZone() {
        return this.RecurrenceTimeZone;
    }

    public int getRecurFrequency() {
        return this.RecurFrequency;
    }

    public int getSlidingFlag() {
        return this.SlidingFlag;
    }

    public int getStartDate() {
        return this.StartDate;
    }

    public int getEndTimeOffset() {
        return this.EndTimeOffset;
    }

    public PSTAppointmentRecurrence(final byte[] recurrencePattern, final PSTAppointment appt, final PSTTimeZone tz) {
        this.RecurrenceTimeZone = tz;
        final SimpleTimeZone stz = this.RecurrenceTimeZone.getSimpleTimeZone();

        // Read the structure
        this.RecurFrequency = (short) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, 4, 6);
        this.PatternType = (short) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, 6, 8);
        this.CalendarType = (short) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, 8, 10);
        this.FirstDateTime = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, 10, 14);
        this.Period = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, 14, 18);
        this.SlidingFlag = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, 18, 22);
        int offset = 22;
        if (this.PatternType != 0) {
            this.PatternSpecific = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                offset + 4);
            offset += 4;
            if (this.PatternType == 0x0003 || this.PatternType == 0x000B) {
                this.PatternSpecificNth = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                    offset + 4);
                offset += 4;
            }
        }
        this.EndType = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
        offset += 4;
        this.OccurrenceCount = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
        offset += 4;
        this.FirstDOW = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
        offset += 4;

        this.DeletedInstanceCount = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
            offset + 4);
        offset += 4;
        this.DeletedInstanceDates = new Calendar[this.DeletedInstanceCount];
        for (int i = 0; i < this.DeletedInstanceCount; ++i) {
            this.DeletedInstanceDates[i] = PSTObject.apptTimeToUTC(
                (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4),
                this.RecurrenceTimeZone);
            offset += 4;
            /*
             * SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
             * f.setTimeZone(RecurrenceTimeZone.getSimpleTimeZone());
             * System.out.printf("DeletedInstanceDates[%d]: %s\n", i,
             * f.format(DeletedInstanceDates[i].getTime()));
             * /
             **/
        }

        this.ModifiedInstanceCount = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
            offset + 4);
        offset += 4;
        this.ModifiedInstanceDates = new Calendar[this.ModifiedInstanceCount];
        for (int i = 0; i < this.ModifiedInstanceCount; ++i) {
            this.ModifiedInstanceDates[i] = PSTObject.apptTimeToUTC(
                (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4),
                this.RecurrenceTimeZone);
            offset += 4;
            /*
             * SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
             * f.setTimeZone(RecurrenceTimeZone.getSimpleTimeZone());
             * System.out.printf("ModifiedInstanceDates[%d]: %s\n", i,
             * f.format(ModifiedInstanceDates[i].getTime()));
             * /
             **/
        }

        this.StartDate = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
        offset += 4;
        this.EndDate = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
        offset += 4 + 4; // Skip ReaderVersion2

        this.writerVersion2 = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
        offset += 4;

        this.StartTimeOffset = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
        offset += 4;
        this.EndTimeOffset = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 4);
        offset += 4;
        this.ExceptionCount = (short) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset + 2);
        offset += 2;

        // Read exceptions
        this.Exceptions = new PSTAppointmentException[this.ExceptionCount];
        for (int i = 0; i < this.ExceptionCount; ++i) {
            this.Exceptions[i] = new PSTAppointmentException(recurrencePattern, offset, this.writerVersion2, appt);
            offset += this.Exceptions[i].getLength();
        }

        if ((offset + 4) <= recurrencePattern.length) {
            final int ReservedBlock1Size = (int) PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset,
                offset + 4);
            offset += 4 + (ReservedBlock1Size * 4);
        }

        // Read extended exception info
        for (int i = 0; i < this.ExceptionCount; ++i) {
            this.Exceptions[i].ExtendedException(recurrencePattern, offset);
            offset += this.Exceptions[i].getExtendedLength();
            /*
             * Calendar c =
             * PSTObject.apptTimeToUTC(Exceptions[i].getStartDateTime(),
             * RecurrenceTimeZone);
             * System.out.printf("Exception[%d] start: %s\n", i,
             * FormatUTC(c.getTime()));
             * c = PSTObject.apptTimeToUTC(Exceptions[i].getEndDateTime(),
             * RecurrenceTimeZone);
             * System.out.printf("Exception[%d] end: %s\n", i,
             * FormatUTC(c.getTime()));
             * c = PSTObject.apptTimeToUTC(Exceptions[i].getOriginalStartDate(),
             * RecurrenceTimeZone);
             * System.out.printf("Exception[%d] original start: %s\n", i,
             * FormatUTC(c.getTime()));
             * /
             **/
        }
        // Ignore any extra data - see
        // http://msdn.microsoft.com/en-us/library/cc979209(office.12).aspx

        // Get attachments, if any
        PSTAttachment[] attachments = new PSTAttachment[appt.getNumberOfAttachments()];
        for (int i = 0; i < attachments.length; ++i) {
            try {
                attachments[i] = appt.getAttachment(i);
            } catch (final Exception e) {
                e.printStackTrace();
                attachments[i] = null;
            }
        }

        PSTAppointment embeddedMessage = null;
        for (int i = 0; i < this.ExceptionCount; ++i) {
            try {
                // Match up an attachment to this exception...
                for (final PSTAttachment attachment : attachments) {
                    if (attachment != null) {
                        final PSTMessage message = attachment.getEmbeddedPSTMessage();
                        if (!(message instanceof PSTAppointment)) {
                            continue;
                        }
                        embeddedMessage = (PSTAppointment) message;
                        final Date replaceTime = embeddedMessage.getRecurrenceBase();
                        /*
                         * SimpleDateFormat f = new
                         * SimpleDateFormat("yyyyMMdd'T'HHmmss");
                         * f.setTimeZone(stz);
                         * System.out.printf("Attachment[%d] time: %s\n",
                         * iAttachment, f.format(replaceTime));
                         * /
                         **/
                        final Calendar c = Calendar.getInstance(stz);
                        c.setTime(replaceTime);
                        if (c.get(Calendar.YEAR) == this.ModifiedInstanceDates[i].get(Calendar.YEAR)
                            && c.get(Calendar.MONTH) == this.ModifiedInstanceDates[i].get(Calendar.MONTH)
                            && c.get(Calendar.DAY_OF_MONTH) == this.ModifiedInstanceDates[i].get(Calendar.DAY_OF_MONTH)) {
                            /*
                             * System.out.println("\tEmbedded Message matched");
                             * /
                             **/

                            this.Exceptions[i].setEmbeddedMessage(embeddedMessage);
                            break;
                        }
                    }
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        attachments = null;
    }

    /*
     * private String FormatUTC(Date date) {
     * SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
     * f.setTimeZone(PSTTimeZone.utcTimeZone);
     * return f.format(date);
     * }
     * /
     **/

    private final short RecurFrequency;
    private final short PatternType;
    private final short CalendarType;
    private final int FirstDateTime;
    private final int Period;
    private final int SlidingFlag;
    private int PatternSpecific;
    private int PatternSpecificNth;
    private final int EndType;
    private final int OccurrenceCount;
    private final int FirstDOW;
    private final int DeletedInstanceCount;
    private Calendar[] DeletedInstanceDates = null;
    private final int ModifiedInstanceCount;
    private Calendar[] ModifiedInstanceDates = null;
    private final int StartDate;
    private final int EndDate;
    // private int readerVersion2;
    private final int writerVersion2;
    private final int StartTimeOffset;
    private final int EndTimeOffset;
    private final short ExceptionCount;
    private PSTAppointmentException[] Exceptions = null;
    private PSTTimeZone RecurrenceTimeZone = null;
}
