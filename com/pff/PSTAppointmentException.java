package com.pff;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

/**
 * Class containing information on exceptions to a recurring appointment
 * @author Orin Eman
 * 
 * 
 */
public class PSTAppointmentException {
	
	// Access methods - return the value from the exception
	// if OverrideFlags say it's present, otherwise the value from the appointment.
	public String getSubject() {
		if ( (OverrideFlags & 0x0001) != 0 ) {
			try {
				return new String(WideCharSubject, "UTF-16LE");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		return this.appt.getSubject();
	}

	
	public int getMeetingType()
	{
		if ( (OverrideFlags & 0x0002) != 0 ) {
			return MeetingType;
		}
		
		return appt.getMeetingStatus();
	}

	
	public int getReminderDelta() {
		if ( (OverrideFlags & 0x0004) != 0 ) {
			return ReminderDelta;
		}
		
		return appt.getReminderDelta();
	}

	
	public boolean getReminderSet() {
		if ( (OverrideFlags & 0x0008) != 0 ) {
			return ReminderSet;
		}
		
		return appt.getReminderSet();
	}
	
	
	public String getLocation() {
		if ( (OverrideFlags & 0x0010) != 0 ) {
			try {
				return new String(WideCharLocation, "UTF-16LE");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	
		return appt.getLocation();
	}

	
	public int getBusyStatus() {
		if ( (OverrideFlags & 0x0020) != 0 ) {
			return BusyStatus;
		}
		
		return appt.getBusyStatus();
	}
	
	
	public boolean getSubType() {
		if ( (OverrideFlags & 0x0080) != 0 ) {
			return SubType;
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
		return StartDateTime;
	}
	
	public int getEndDateTime() {
		return EndDateTime;
	}
	
	public int getOriginalStartDate() {
		return OriginalStartDate;
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
		if ( (OverrideFlags & 0x0010) != 0 ) {
			return Subject;
		}
		
		return null;
	}
	
	public byte[] getLocationBytes() {
		if ( (OverrideFlags & 0x0010) != 0 ) {
			return Location;
		}

		return null;
	}
	
	public boolean attachmentsPresent() {
		if ( (OverrideFlags & 0x0040) != 0 &&
			  Attachment == 0x00000001 )
		{
			return true;
		}
		
		return false;
	}
	
	PSTAppointmentException(byte[] recurrencePattern,
			int offset,
			int writerVersion2,
			PSTAppointment appt) {
		this.writerVersion2 = writerVersion2;
		int initialOffset = offset;
		this.appt = appt;
		embeddedMessage = null;

		StartDateTime = (int)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
		offset += 4;
		EndDateTime = (int)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
		offset += 4;
		OriginalStartDate = (int)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
		offset += 4;
		OverrideFlags = (short)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+2);
		offset += 2;
		
		if ( (OverrideFlags & 0x0001) != 0 ) {
			@SuppressWarnings("unused")
			short SubjectLength = (short)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+2);
			offset += 2;
			short SubjectLength2 = (short)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+2);
			offset += 2;
			Subject = new byte[SubjectLength2];
			System.arraycopy(recurrencePattern, offset, Subject, 0, SubjectLength2);
			offset += SubjectLength2;
		}
		
		if ( (OverrideFlags & 0x0002) != 0 ) {
			MeetingType = (int)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
			offset += 4;
		}

		if ( (OverrideFlags & 0x0004) != 0 ) {
			ReminderDelta = (int)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
			offset += 4;
		}

		if ( (OverrideFlags & 0x0008) != 0 ) {
			ReminderSet = ((int)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4) != 0);
			offset += 4;
		}

		if ( (OverrideFlags & 0x0010) != 0 ) {
			@SuppressWarnings("unused")
			short LocationLength = (short)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+2);
			offset += 2;
			short LocationLength2 = (short)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+2);
			offset += 2;
			Location = new byte[LocationLength2];
			System.arraycopy(recurrencePattern, offset, Location, 0, LocationLength2);
			offset += LocationLength2;
		}

		if ( (OverrideFlags & 0x0020) != 0 ) {
			BusyStatus = (int)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
			offset += 4;
		}

		if ( (OverrideFlags & 0x0040) != 0 ) {
			Attachment = (int)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
			offset += 4;
		}

		if ( (OverrideFlags & 0x0080) != 0 ) {
			SubType = ((int)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4) != 0);
			offset += 4;
		}

		length = offset - initialOffset;
	}
	
	void ExtendedException(byte[] recurrencePattern, int offset) {
		int initialOffset = offset;
		
		if ( writerVersion2 >= 0x00003009 ) {
			int ChangeHighlightSize = (int)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
			offset += 4;
			ChangeHighlightValue = (int)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
			offset += ChangeHighlightSize;
		}
		
		int ReservedBlockEESize = (int)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
		offset += 4 + ReservedBlockEESize;

		// Same as regular Exception structure?
		StartDateTime = (int)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
		offset += 4;
		EndDateTime = (int)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
		offset += 4;
		OriginalStartDate = (int)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
		offset += 4;

		if ( (OverrideFlags & 0x0001) != 0 ) {
			WideCharSubjectLength = (short)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+2);
			offset += 2;
			WideCharSubject = new byte[WideCharSubjectLength * 2];
			System.arraycopy(recurrencePattern, offset, WideCharSubject, 0, WideCharSubject.length);
			offset += WideCharSubject.length;
			
			try {
				String subject = new String(WideCharSubject, "UTF-16LE");
				System.out.printf("Exception Subject: %s\n", subject);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		if ( (OverrideFlags & 0x0010) != 0 ) {
			WideCharLocationLength = (short)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+2);
			offset += 2;
			WideCharLocation = new byte[WideCharLocationLength*2];
			System.arraycopy(recurrencePattern, offset, WideCharLocation, 0, WideCharLocation.length);
			offset += WideCharLocation.length;
		}
		ReservedBlockEESize = (int)PSTObject.convertLittleEndianBytesToLong(recurrencePattern, offset, offset+4);
		offset += 4 + ReservedBlockEESize;

		extendedLength = offset - initialOffset;
	}
	
	void setEmbeddedMessage(PSTAppointment embeddedMessage) {
		this.embeddedMessage = embeddedMessage;
	}
	
	private int		writerVersion2;
	private int		StartDateTime;
	private int		EndDateTime;
	private int		OriginalStartDate;
	private short	OverrideFlags;
	private byte[]	Subject = null;
	private int		MeetingType;
	private int		ReminderDelta;
	private boolean	ReminderSet;
	private byte[]	Location = null;
	private int		BusyStatus;
	private int		Attachment;
	private boolean	SubType;
//	private int		AppointmentColor;
	@SuppressWarnings("unused")
	private int		ChangeHighlightValue;
	private int		WideCharSubjectLength = 0;
	private byte[]	WideCharSubject = null;
	private int		WideCharLocationLength = 0;
	private byte[]	WideCharLocation = null;
	
	int getLength() {
		return length;
	}
	
	int getExtendedLength() {
		return extendedLength;
	}

	private PSTAppointment	embeddedMessage;
	private PSTAppointment	appt;
	private int length;
	private int extendedLength;
}
