package com.pff;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Arrays;
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
        URL dirUrl = ClassLoader.getSystemResource("albert_meyers/albert_meyers_000_1_1.pst");
        //PSTFile pstFile = new PSTFile(new File(dirUrl.toURI()));
        //pstFile.getRootFolder();
        //pstFile.getMessageStore().getDisplayName();
        //System.out.println("\n\n\n");

        //dirUrl = ClassLoader.getSystemResource("dist-list.pst");
        //PSTFile pstFile1 = new PSTFile(new File(dirUrl.toURI()));
        //pstFile1.getMessageStore().getDisplayName();
        //System.out.println("\n\n\n");

        dirUrl = ClassLoader.getSystemResource("arc.test1@apogeephysicians.com - Arc Test.ost");
        PSTFile pstFile2 = new PSTFile(new File(dirUrl.toURI()));
        pstFile2.getMessageStore().getDisplayName();
        processFolder(pstFile2.getRootFolder());



        //DescriptorIndexNode getDescriptorIndexNode(long identifier)
		//DescriptorIndexNode(findBtreeItem(in, identifier, true), this.getPSTFileType());

	    //PSTNodeInputStream in = new PSTNodeInputStream(PSTFile pstFile, byte[] attachmentData, boolean encrypted) {
    }

	int depth = -1;
	public void processFolder(PSTFolder folder)
			throws PSTException, java.io.IOException {
		depth++;
		// the root folder doesn't have a display name
		if (depth > 0) {
			printDepth();
			System.out.println(folder.getDisplayName());
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
