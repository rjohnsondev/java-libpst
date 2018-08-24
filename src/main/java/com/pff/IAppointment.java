package com.pff;

import java.util.Date;

public interface IAppointment extends IMessage {
    boolean getSendAsICAL();

    int getBusyStatus();

    boolean getShowAsBusy();

    String getLocation();

    Date getStartTime();

    PSTTimeZone getStartTimeZone();

    Date getEndTime();

    PSTTimeZone getEndTimeZone();

    PSTTimeZone getRecurrenceTimeZone();

    int getDuration();

    int getColor();

    boolean getSubType();

    int getMeetingStatus();

    int getResponseStatus();

    boolean isRecurring();

    Date getRecurrenceBase();

    int getRecurrenceType();

    String getRecurrencePattern();

    byte[] getRecurrenceStructure();

    byte[] getTimezone();

    String getAllAttendees();

    String getToAttendees();

    String getCCAttendees();

    int getAppointmentSequence();

    // online meeting properties
    boolean isOnlineMeeting();

    int getNetMeetingType();

    String getNetMeetingServer();

    String getNetMeetingOrganizerAlias();

    boolean getNetMeetingAutostart();

    boolean getConferenceServerAllowExternal();

    String getNetMeetingDocumentPathName();

    String getNetShowURL();

    Date getAttendeeCriticalChange();

    Date getOwnerCriticalChange();

    String getConferenceServerPassword();

    boolean getAppointmentCounterProposal();

    boolean isSilent();

    String getRequiredAttendees();

    int getLocaleId();

    PSTGlobalObjectId getGlobalObjectId();

    PSTGlobalObjectId getCleanGlobalObjectId();
}
