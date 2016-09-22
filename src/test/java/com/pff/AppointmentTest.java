package com.pff;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link PSTAppointment}.
 *
 * @author Richard Johnson
 */
@RunWith(JUnit4.class)
public class AppointmentTest {

    /**
     * Test we can access appointments from the PST.
     */
    @Test
    public final void testGetDistList()
            throws PSTException, IOException, URISyntaxException {
        URL dirUrl = ClassLoader.getSystemResource("dist-list.pst");
        PSTFile pstFile = new PSTFile(new File(dirUrl.toURI()));
        PSTAppointment appt = (PSTAppointment) PSTObject.detectAndLoadPSTObject(pstFile, 2097348);
        PSTAppointmentRecurrence r = new PSTAppointmentRecurrence(
                appt.getRecurrenceStructure(), appt, appt.getRecurrenceTimeZone());


        Assert.assertEquals(
                "Has 3 deleted items (1 removed, 2 changed)",
                3,
                r.getDeletedInstanceDates().length);

        Assert.assertEquals(
                "Number of Exceptions",
                2,
                r.getExceptionCount());

        String d = r.getException(0).getDescription().trim();
        Assert.assertEquals("correct app desc", "This is the appointment at 9", d);

        Calendar c = PSTObject.apptTimeToCalendar(
                r.getException(0).getStartDateTime());
        Assert.assertEquals(
                "First exception correct hour",
                9,
                c.get(Calendar.HOUR));

        d = r.getException(1).getDescription().trim();
        Assert.assertEquals("correct app desc", "This is the one at 10", d);

        c = PSTObject.apptTimeToCalendar(
                r.getException(1).getStartDateTime());
        Assert.assertEquals(
                "Second exception correct hour",
                10,
                c.get(Calendar.HOUR));

        //System.out.println(r.getExceptionCount());
        //System.out.println(r.getException(0).getDTStamp());

        //for (int x = 0; x < r.getDeletedInstanceDates().length; x++) {
        //    System.out.println(r.getDeletedInstanceDates()[x]);
        //}
    }
}

