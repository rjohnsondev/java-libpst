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
public class PasswordTest {

    /**
     * Test for password protectedness.
     */
    @Test
    public final void testPasswordProtected()
            throws PSTException, IOException, URISyntaxException {
        URL dirUrl = ClassLoader.getSystemResource("passworded.pst");
        PSTFile pstFile = new PSTFile(new File(dirUrl.toURI()));
        Assert.assertEquals("Is password protected",
                pstFile.getMessageStore().isPasswordProtected(),
                true);
    }

    /**
     * Test for non-password protectedness.
     */
    @Test
    public final void testNotPasswordProtected()
            throws PSTException, IOException, URISyntaxException {
        URL dirUrl = ClassLoader.getSystemResource("dist-list.pst");
        PSTFile pstFile = new PSTFile(new File(dirUrl.toURI()));
        Assert.assertEquals("Is password protected",
                pstFile.getMessageStore().isPasswordProtected(),
                false);
    }
}
