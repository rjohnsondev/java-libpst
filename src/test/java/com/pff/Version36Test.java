package com.pff;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class Version36Test {

    @Test
    public final void testVersion36()
            throws PSTException, IOException, URISyntaxException {
        URL dirUrl = ClassLoader.getSystemResource("example-2013.ost");
        PSTFile pstFile2 = new PSTFile(new File(dirUrl.toURI()));
        PSTFolder inbox = (PSTFolder)PSTObject.detectAndLoadPSTObject(pstFile2, 8578);
        Assert.assertEquals(
                "Number of emails in folder",
                inbox.getContentCount(),
                2);
        PSTMessage msg = (PSTMessage)PSTObject.detectAndLoadPSTObject(pstFile2, 2097284);
        Assert.assertEquals(
                "correct email text.",
                "This is an e-mail message sent automatically by Microsoft "
                + "Outlook while testing the settings for your account.",
                msg.getBodyHTML().trim());
        //processFolder(pstFile2.getRootFolder());
    }

    int depth = -1;
    public void processFolder(PSTFolder folder)
            throws PSTException, java.io.IOException {
        depth++;
        // the root folder doesn't have a display name
        if (depth > 0) {
            printDepth();
            System.out.println("Folder: " + folder.getDescriptorNodeId() + " - " + folder.getDisplayName());
        }

        // go through the folders...
        if (folder.hasSubfolders()) {
            Vector<PSTFolder> childFolders = folder.getSubFolders();
            for (PSTFolder childFolder : childFolders) {
                processFolder(childFolder);
            }
        }

        // and now the emails for this folder
        if (folder.getContentCount() > 0) {
            depth++;
            PSTMessage email = (PSTMessage)folder.getNextChild();
            while (email != null) {
                printDepth();
                System.out.println("Email: "+ email.getDescriptorNodeId() + " - " + email.getSubject());
                email = (PSTMessage)folder.getNextChild();
            }
            depth--;
        }
        depth--;
    }

    public void printDepth() {
        for (int x = 0; x < depth-1; x++) {
            System.out.print(" | ");
        }
        System.out.print(" |- ");
    }
}
