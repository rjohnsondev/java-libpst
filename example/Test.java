package example;
import com.pff.*;
import java.util.*;
import java.io.*;

public class Test {
	public static void main(String[] args)
	{
		new Test(args[0]);
	}

	public Test(String filename1) {
		try {
			//PSTFile pstFile = new PSTFile(filename);
			PSTFile pstFile = new PSTFile("C:\\Users\\richard\\Documents\\Outlook Files\\richard@test.com - test.pst");
			//PSTMessage msg = (PSTMessage)PSTObject.detectAndLoadPSTObject(pstFile, 2097636);
			//System.out.println(msg.getTransportMessageHeaders());
			//System.exit(0);

			/*
			PSTFolder folder = (PSTFolder)PSTObject.detectAndLoadPSTObject(pstFile, 33826);
			System.out.println(folder.getContentCount());
			for (int x = 0; x < folder.getContentCount(); x++) {
				System.out.println(folder.getNextChild());
				Vector<PSTObject> msgs = folder.getChildren(1);
				if (msgs.size() == 0) {
					System.out.println("GAH: "+x);
				} else {
					PSTMessage msg = (PSTMessage)msgs.get(0);
					System.out.println(msg.getSubject());
				}
			}
			System.exit(0);
			 *
			 */
			PSTMessage msg = (PSTMessage)PSTObject.detectAndLoadPSTObject(pstFile, 2097252);
			System.out.println(msg);
			//System.exit(0);
			for (int x = 0; x < msg.getNumberOfAttachments(); x++) {
				PSTAttachment attach = msg.getAttachment(x);
				if (attach.getAttachMethod() == PSTAttachment.ATTACHMENT_METHOD_EMBEDDED) {
					PSTMessage msg2 = attach.getEmbeddedPSTMessage();
					for (int y = 0; y < msg2.getNumberOfAttachments(); y++) {
						System.out.println(msg2.getAttachment(y).getFilename());
					}
				} else {
					System.out.println(attach.getFilename());
				}
			}
			//System.out.println(msg);
			System.exit(0);

			//System.out.println(msg);
			//this.saveAttachments(msg);

			processFolder(pstFile.getRootFolder());
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	int depth = -1;
	public void processFolder(PSTFolder folder)
			throws PSTException, java.io.IOException
	{
		depth++;
		// the root folder doesn't have a display name
		if (depth > 0) {
			printDepth();
			System.out.println(folder.getDescriptorNodeId() + " - " +folder.getDisplayName());
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
				System.out.println("Email: "+email.getDescriptorNodeId()+" - "+email.getSubject());
				for (int x = 0; x <email.getNumberOfRecipients(); x++) {
					PSTRecipient recipient = email.getRecipient(x);
					System.out.println(recipient.getDisplayName());
				}

				// do the attachements
				//this.saveAttachments(email);

				email = (PSTMessage)folder.getNextChild();
			}
			depth--;
		}
		depth--;
	}

	public void saveAttachments(PSTMessage message)
			throws PSTException,IOException
 	{
		for (int x = 0; x < message.getNumberOfAttachments(); x++) {
			PSTAttachment attach = message.getAttachment(x);
			if (attach.getLongFilename().indexOf("image001") > -1) {
				//System.out.println("here");
				//System.out.println(attach.getAttachMethod());
						
				//System.exit(0);
			}
			//System.out.println(attach.getLongFilename() + " - " + attach.getContentId() + " - " +attach.isAttachmentMhtmlRef() + " - " +attach.getAttachmentContentDisposition());
			InputStream attachmentStream = attach.getFileInputStream();
			// both long and short filenames can be used for attachments
			String filename = attach.getLongFilename();
			if (filename.isEmpty()) {
				filename = attach.getFilename();
			}
			filename = "attach/"+message.getDescriptorNodeId()+"-" + filename;
			FileOutputStream out = new FileOutputStream(filename);
			// 8176 is the block size used internally and should give the best performance
			int bufferSize = 8176;
			byte[] buffer = new byte[bufferSize];
			int count = attachmentStream.read(buffer);
			while (count == bufferSize) {
				out.write(buffer);
				count = attachmentStream.read(buffer);
			}
			if (count > 0) {
				byte[] endBuffer = new byte[count];
				System.arraycopy(buffer, 0, endBuffer, 0, count);
				out.write(endBuffer);
			} 
			out.close();
			attachmentStream.close();
		}
	}

	public void printDepth() {
		for (int x = 0; x < depth-1; x++) {
			System.out.print(" | ");
		}
		System.out.print(" |- ");
	}
}
