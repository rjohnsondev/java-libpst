package com.pff;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link PSTDistList}.
 *
 * @author Richard Johnson
 */
@RunWith(JUnit4.class)
public class DistListTest {

    /**
     * Test we can retrieve distribution lists from the PST.
     */
    @Test
    public final void testGetDistList()
            throws PSTException, IOException, URISyntaxException {
        URL dirUrl = ClassLoader.getSystemResource("dist-list.pst");
        PSTFile pstFile = new PSTFile(new File(dirUrl.toURI()));
        PSTDistList obj = (PSTDistList)PSTObject.detectAndLoadPSTObject(pstFile, 2097188);
        Object[] members = obj.getDistributionListMembers();
        Assert.assertEquals("Correct number of members", members.length, 3);
        int numberOfContacts = 0;
        int numberOfOneOffRecords = 0;
        HashSet<String> emailAddresses = new HashSet<String>();
        HashSet<String> displayNames = new HashSet<String>();
        for (Object member : members) {
            if (member instanceof PSTContact) {
                PSTContact contact = (PSTContact)member;
                Assert.assertEquals("Contact email address",
                                    contact.getEmail1EmailAddress(),
                                    "contact1@rjohnson.id.au");
                numberOfContacts++;
            } else {
                PSTDistList.OneOffEntry entry = (PSTDistList.OneOffEntry)member;
                emailAddresses.add(entry.getEmailAddress());
                displayNames.add(entry.getDisplayName());
                numberOfOneOffRecords++;
            }
        }
        Assert.assertEquals("Correct number of members", members.length, 3);
        Assert.assertEquals("Contains all display names",
                            displayNames,
                            new HashSet<String>(Arrays.asList(
                                    new String[] {"dist name 2",
                                                  "dist name 1"})));
        Assert.assertEquals("Contains all email addresses",
                            emailAddresses,
                            new HashSet<String>(Arrays.asList(
                                    new String[] {"dist1@rjohnson.id.au",
                                                  "dist2@rjohnson.id.au"})));
    }
}
