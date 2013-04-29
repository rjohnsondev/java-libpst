package example;
import java.util.ArrayList;

import com.pff.exceptions.PSTException;
import com.pff.objects.PSTFolder;
import com.pff.objects.PSTMessage;
import com.pff.source.PSTRandomAccessFile;
import com.pff.source.PSTSource;
import com.pff.source._RandomAccessPSTSource;

public class Test {
	
	public static void main(String[] args) {
		new Test(args[0]);
	}

	public Test(String filename) {
		try {
			_RandomAccessPSTSource raSrc = new PSTRandomAccessFile(filename);
			PSTSource pstFile = new PSTSource(raSrc);
			System.out.println(pstFile.getMessageStore().getDisplayName());
			processFolder(pstFile.getRootFolder());
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	int depth = -1;
	public void processFolder(PSTFolder folder) throws PSTException, java.io.IOException {
		depth++;
		// the root folder doesn't have a display name
		if (depth > 0) {
			printDepth();
			System.out.println(folder.getDisplayName());
		}

		// go through the folders...
		if (folder.hasSubfolders()) {
			ArrayList<PSTFolder> childFolders = folder.getSubFolders();
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
				System.out.println("Email: "+email.getSubject());
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
