package com.pff;

import java.util.Calendar;

public interface IAppointmentRecurrence {
    short getExceptionCount();

    PSTAppointmentException getException(int i);

    Calendar[] getDeletedInstanceDates();

    Calendar[] getModifiedInstanceDates();

    short getCalendarType();

    short getPatternType();

    int getPeriod();

    int getPatternSpecific();

    int getFirstDOW();

    int getPatternSpecificNth();

    int getFirstDateTime();

    int getEndType();

    int getOccurrenceCount();

    int getEndDate();

    int getStartTimeOffset();

    PSTTimeZone getTimeZone();

    int getRecurFrequency();

    int getSlidingFlag();

    int getStartDate();

    int getEndTimeOffset();
}
