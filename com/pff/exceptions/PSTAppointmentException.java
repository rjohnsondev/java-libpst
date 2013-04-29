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

package com.pff.exceptions;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

import com.pff.PSTUtils;
import com.pff.objects.PSTAppointment;
import com.pff.objects.sub.PSTTimeZone;

/**
 * Class containing information on exceptions to a recurring appointment
 * @author Orin Eman
 * 
 * 
 */
public class PSTAppointmentException {
	
	// Access methods - return the value from the exception if
	// OverrideFlags say it's present, otherwise the value from the appointment.
	public String getSubject() {
		if ( (overrideFlags & 0x0001) != 0 ) {
			try {
				return new String(wideCharSubject, "UTF-16LE");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		return this.appt.getSubject();
	}

	
	public int getMeetingType()
	{
		if ( (overrideFlags & 0x0002) != 0 ) {
			return meetingType;
		}
		
		return appt.getMeetingStatus();
	}

	
	public int getReminderDelta() {
		if ( (overrideFlags & 0x0004) != 0 ) {
			return ReminderDelta;
		}
		
		return appt.getReminderDelta();
	}

	
	public boolean getReminderSet() {
		if ( (overrideFlags & 0x0008) != 0 ) {
			return ReminderSet;
		}
		
		return appt.getReminderSet();
	}
	
	
	public String getLocation() {
		if ( (overrideFlags & 0x0010) != 0 ) {
			try {
				return new String(wideCharLocation, "UTF-16LE");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	
		return appt.getLocation();
	}

	
	public int getBusyStatus() {
		if ( (overrideFlags & 0x0020) != 0 ) {
			return busyStatus;
		}
		
		return appt.getBusyStatus();
	}
	
	
	public boolean getSubType() {
		if ( (overrideFlags & 0x0080) != 0 ) {
			return subType;
		}
		
		return appt.getSubType();
	}
	
	public String getDescription()
	{
		if ( embeddedMessage != null ) {
			return embeddedMessage.getBodyPrefix();
		}
		
		return null;
	}
	
	public Date getDTStamp() {
		Date ret = null;
		if ( embeddedMessage != null ) {
			ret = embeddedMessage.getOwnerCriticalChange();
		}
		
		if ( ret == null ) {
			// Use current date/time
			Calendar c = Calendar.getInstance(PSTTimeZone.utcTimeZone);
			ret = c.getTime();
		}
		
		return ret;
	}
	
	public int getStartDateTime() {
		return startDateTime;
	}
	
	public int getEndDateTime() {
		return endDateTime;
	}
	
	public int getOriginalStartDate() {
		return originalStartDate;
	}
	
	public int getAppointmentSequence(int def) {
		if ( embeddedMessage == null ) {
			return def;
		}
		return embeddedMessage.getAppointmentSequence();
	}
	
	public int getImportance(int def) {
		if ( embeddedMessage == null ) {
			return def;
		}
		return embeddedMessage.getImportance();
	}
	
	public byte[] getSubjectBytes() {
		if ( (overrideFlags & 0x0010) != 0 ) {
			return Subject;
		}
		return null;
	}
	
	public byte[] getLocationBytes() {
		if ( (overrideFlags & 0x0010) != 0 ) {
			return location;
		}
		return null;
	}
	
	public boolean attachmentsPresent() {
		if ( (overrideFlags & 0x0040) != 0 &&  attachment == 0x00000001 ) {
			return true;
		}
		return false;
	}
	
	public boolean embeddedMessagePresent() {
		return embeddedMessage != null;
	}

	//
	// Allow access to an embedded message for
	// properties that don't have access methods here.
	//
	public PSTAppointment getEmbeddedMessage() {
		return embeddedMessage;
	}

	public PSTAppointmentException(byte[] recurrencePattern, int offset, int writerVersion2, PSTAppointment appt) {
		this.writerVersion2 = writerVersion2;
		int initialOffset = offset;
		this.appt = appt;
		embeddedMessage = null;

		startDateTime = (int)PSTUtils.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
		offset += 4;
		endDateTime = (int)PSTUtils.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
		offset += 4;
		originalStartDate = (int)PSTUtils.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
		offset += 4;
		overrideFlags = (short)PSTUtils.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+2);
		offset += 2;
		
		if ( (overrideFlags & ARO_SUBJECT) != 0 ) {
			//@SuppressWarnings("unused")
			//short SubjectLength = (short)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+2);
			offset += 2;
			short SubjectLength2 = (short)PSTUtils.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+2);
			offset += 2;
			Subject = new byte[SubjectLength2];
			System.arraycopy(recurrencePattern, offset, Subject, 0, SubjectLength2);
			offset += SubjectLength2;
		}
		
		if ( (overrideFlags & ARO_MEETINGTYPE) != 0 ) {
			meetingType = (int)PSTUtils.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
			offset += 4;
		}

		if ( (overrideFlags & ARO_REMINDERDELTA) != 0 ) {
			ReminderDelta = (int)PSTUtils.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
			offset += 4;
		}

		if ( (overrideFlags & ARO_REMINDER) != 0 ) {
			ReminderSet = ((int)PSTUtils.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4) != 0);
			offset += 4;
		}

		if ( (overrideFlags & ARO_LOCATION) != 0 ) {
			//@SuppressWarnings("unused")
			//short LocationLength = (short)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+2);
			offset += 2;
			short LocationLength2 = (short)PSTUtils.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+2);
			offset += 2;
			location = new byte[LocationLength2];
			System.arraycopy(recurrencePattern, offset, location, 0, LocationLength2);
			offset += LocationLength2;
		}

		if ( (overrideFlags & ARO_BUSYSTATUS) != 0 ) {
			busyStatus = (int)PSTUtils.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
			offset += 4;
		}

		if ( (overrideFlags & ARO_ATTACHMENT) != 0 ) {
			attachment = (int)PSTUtils.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
			offset += 4;
		}

		if ( (overrideFlags & ARO_SUBTYPE) != 0 ) {
			subType = ((int)PSTUtils.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4) != 0);
			offset += 4;
		}

		length = offset - initialOffset;
	}
	
	public void buildExtendedException(byte[] recurrencePattern, int offset) {
		int initialOffset = offset;
		
		if ( writerVersion2 >= 0x00003009 ) {
			int ChangeHighlightSize = (int)PSTUtils.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
			offset += 4;
			changeHighlightValue = (int)PSTUtils.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
			offset += ChangeHighlightSize;
		}
		
		int ReservedBlockEESize = (int)PSTUtils.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
		offset += 4 + ReservedBlockEESize;

		// See http://msdn.microsoft.com/en-us/library/cc979209(office.12).aspx
		if ( (overrideFlags & (ARO_SUBJECT|ARO_LOCATION)) != 0 ) {
			// Same as regular Exception structure?
			startDateTime = (int)PSTUtils.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
			offset += 4;
			endDateTime = (int)PSTUtils.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
			offset += 4;
			originalStartDate = (int)PSTUtils.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
			offset += 4;
		}

		if ( (overrideFlags & ARO_SUBJECT) != 0 ) {
			wideCharSubjectLength = (short)PSTUtils.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+2);
			offset += 2;
			wideCharSubject = new byte[wideCharSubjectLength * 2];
			System.arraycopy(recurrencePattern, offset, wideCharSubject, 0, wideCharSubject.length);
			offset += wideCharSubject.length;
/*			
			try {
				String subject = new String(WideCharSubject, "UTF-16LE");
				System.out.printf("Exception Subject: %s\n", subject);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
/**/
		}
	
		if ( (overrideFlags & ARO_LOCATION) != 0 ) {
			wideCharLocationLength = (short)PSTUtils.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+2);
			offset += 2;
			wideCharLocation = new byte[wideCharLocationLength*2];
			System.arraycopy(recurrencePattern, offset, wideCharLocation, 0, wideCharLocation.length);
			offset += wideCharLocation.length;
		}
		
		// See http://msdn.microsoft.com/en-us/library/cc979209(office.12).aspx
		if ( (overrideFlags & (ARO_SUBJECT|ARO_LOCATION)) != 0 ) {
			ReservedBlockEESize = (int)PSTUtils.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
			offset += 4 + ReservedBlockEESize;
		}

		extendedLength = offset - initialOffset;
	}
	
	public void setEmbeddedMessage(PSTAppointment embeddedMessage) {
		this.embeddedMessage = embeddedMessage;
	}
	
	private int		writerVersion2;
	private int		startDateTime;
	private int		endDateTime;
	private int		originalStartDate;
	private short	overrideFlags;
	private byte[]	Subject = null;
	private int		meetingType;
	private int		ReminderDelta;
	private boolean	ReminderSet;
	private byte[]	location = null;
	private int		busyStatus;
	private int		attachment;
	private boolean	subType;
//	private int		AppointmentColor;	// Reserved - don't read from the PST file
	@SuppressWarnings("unused")
	private int		changeHighlightValue;
	private int		wideCharSubjectLength = 0;
	private byte[]	wideCharSubject = null;
	private int		wideCharLocationLength = 0;
	private byte[]	wideCharLocation = null;
	private PSTAppointment	embeddedMessage = null;
	private PSTAppointment	appt;
	private int 	length;
	private int 	extendedLength;
	
	
	// Length of this ExceptionInfo structure in the PST file
	public int getLength() {
		return length;
	}

	// Length of this ExtendedException structure in the PST file
	public int getExtendedLength() {
		return extendedLength;
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
