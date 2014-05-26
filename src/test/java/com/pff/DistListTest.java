package com.pff;

import org.junit.Test;
import java.net.URL;
import java.io.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.pff.*;
import java.util.*;

/**
 * Tests for {@link PSTDistList}.
 *
 * @author Richard Johnson
 */
@RunWith(JUnit4.class)
public class DistListTest {

    /**
     * Test that a click without `wv` (version) has with default value "".
     * @throws ClickEntryParseException on parse error
     */
    @Test
    public final void testGetDistList() {
		try {
			URL dir_url = ClassLoader.getSystemResource("dist-list.pst");
			PSTFile pstFile = new PSTFile(new File(dir_url.toURI()));
			PSTDistList obj = (PSTDistList)PSTObject.detectAndLoadPSTObject(pstFile, 2097188);
			System.out.println(obj);
			String[] members =obj.getDistributionListMembers();
			for (String member : members) {
				System.out.println(member);
			}

			//System.out.println(pstFile.getMessageStore().getDisplayName());
			//processFolder(pstFile.getRootFolder());
		} catch (Exception err) {
			err.printStackTrace();
		}
        //org.junit.Assert.assertTrue(false);
    }

	int depth = -1;
	public void processFolder(PSTFolder folder)
			throws PSTException, java.io.IOException
	{
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
				System.out.println("Email: "+email.getDescriptorNodeId()+" "+email.getSubject());
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
