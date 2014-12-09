package com.pff;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.Stack;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Extracts the mail(in the IPF.Imap folder) to EML file.
 * 
 * 1. Travel Folder.
 * 2. Create MimeMessage from PSTMessage.
 * 3. Write to EML file.
 * 
 * @author inter6
 *
 */
@RunWith(JUnit4.class)
public class ExtractToEmlTest {

	private final static String INPUT_PST_FILE = "E:/eml/pst/inter6@naver.com.pst";
	private final static String OUTPUT_EML_DIR = "E:/output";

	@Test
	public final void testExtractToEmlTest() throws Exception {
		PSTFile pstFile = new PSTFile(new File(INPUT_PST_FILE));
		PSTMessageStore store = pstFile.getMessageStore();
		System.out.println("execute extract to eml - NAME:" + store.getDisplayName());

		travelFolder(pstFile.getRootFolder(), new Stack<PSTFolder>());

		System.out.println("extract done.");
	}

	private void travelFolder(PSTFolder currentFolder, Stack<PSTFolder> parentFolderStack) throws Exception {
		System.out.println("travel folder - FLD:" + getTravelFolderInfo(currentFolder, parentFolderStack));

		if (currentFolder.hasSubfolders()) {
			// travel sub folders
			parentFolderStack.push(currentFolder);
			for (PSTFolder subFolder : currentFolder.getSubFolders()) {
				travelFolder(subFolder, parentFolderStack);
			}
			parentFolderStack.pop();
		}

		if (!"IPF.Imap".equals(currentFolder.getContainerClass())) {
			System.out.println("not support folder class ! - CLASS:" + currentFolder.getContainerClass());
			return;
		}
		extractToEml(currentFolder, parentFolderStack);
	}

	private void extractToEml(PSTFolder currentFolder, Stack<PSTFolder> parentFolderStack) throws Exception {
		int size = currentFolder.getContentCount();
		if (size == 0) {
			System.out.println("empty folder !");
			return;
		}

		for (int idx = 0; idx < size; idx++) {
			try {
				PSTObject object = currentFolder.getNextChild();
				if (!"IPM.Note".equals(object.getMessageClass())) {
					throw new IllegalArgumentException("not support message class ! - IDX:" + idx + " MSG_CLASS:" + object.getMessageClass());
				}
				if (!(object instanceof PSTMessage)) {
					throw new IllegalArgumentException("is not PSTMessage ! - IDX:" + idx + " JAVA_CLASS:" + object.getClass().getName());
				}

				File outputDir = generateOutputDir(currentFolder, parentFolderStack);
				PSTMessage msg = (PSTMessage) object;
				MimeMessage mime = createMime(msg);
				OutputStream os = null;
				try {
					// TODO improve filename
					os = new FileOutputStream(new File(outputDir, idx + ".eml"));
					mime.writeTo(os);
				} finally {
					closeQuitely(os);
				}
			} catch (Exception e) {
				System.out.println("extract fail ! - IDX:" + idx);
				e.printStackTrace();
			}
		}
	}

	private MimeMessage createMime(PSTMessage msg) throws MessagingException, PSTException, IOException {
		MimeMessage mime = new MimeMessage(Session.getDefaultInstance(new Properties()));
		// TODO build header

		MimeMultipart rootMp = new MimeMultipart("mixed");
		{
			// build message part
			MimeMultipart msgMp = new MimeMultipart("alternative");
			{
				MimeBodyPart plainPart = new MimeBodyPart();
				plainPart.setContent(msg.getBody(), "text/plain; charset=utf-8");
				msgMp.addBodyPart(plainPart);

				MimeBodyPart htmlPart = new MimeBodyPart();
				htmlPart.setContent(msg.getBodyHTML(), "text/html; charset=utf-8");
				msgMp.addBodyPart(htmlPart);
			}
			MimeBodyPart msgPart = new MimeBodyPart();
			msgPart.setContent(msgMp);
			rootMp.addBodyPart(msgPart);

			// build attach part
			for (int attachIdx = 0; attachIdx < msg.getNumberOfAttachments(); attachIdx++) {
				PSTAttachment attach = msg.getAttachment(attachIdx);
				String encodedFilename = encodedFilename(attach.getFilename());

				MimeBodyPart attachPart = new MimeBodyPart();
				attachPart.setContent(getAttachBytes(attach), "application/octet-stream; name=\"" + encodedFilename + "\"");
				attachPart.setDisposition("attachment; filename=\"" + encodedFilename + "\"");
				rootMp.addBodyPart(attachPart);
			}
		}
		mime.setContent(rootMp);
		mime.saveChanges();
		return mime;
	}

	private String encodedFilename(String filename) throws UnsupportedEncodingException {
		String plain = filename;
		if (plain == null) {
			plain = "unknown";
		}
		plain = plain.trim();
		if ("".equals(plain)) {
			plain = "unknown";
		}
		return MimeUtility.encodeWord(plain, "utf-8", "B");
	}

	private byte[] getAttachBytes(PSTAttachment attach) throws IOException, PSTException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); // unnecessary flush() and close()
		InputStream in = null;
		try {
			in = attach.getFileInputStream();
			byte[] buf = new byte[4096];
			int len = 0;
			while ((len = in.read(buf)) != -1) {
				baos.write(buf, 0, len);
			}
			return baos.toByteArray();
		} finally {
			closeQuitely(in);
		}
	}

	private File generateOutputDir(PSTFolder currentFolder, Stack<PSTFolder> parentFolderStack) {
		StringBuilder sb = new StringBuilder();
		sb.append(OUTPUT_EML_DIR);
		for (PSTFolder parentFolder : parentFolderStack) {
			sb.append("/" + parentFolder.getDisplayName());
		}
		sb.append("/" + currentFolder.getDisplayName());

		// TODO safely path name
		File outputDir = new File(sb.toString());
		outputDir.mkdirs();
		return outputDir;
	}

	private void closeQuitely(Closeable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (IOException e) {
		}
	}

	private String getTravelFolderInfo(PSTFolder currentFolder, Stack<PSTFolder> parentFolderStack) {
		StringBuilder sb = new StringBuilder();
		for (PSTFolder parentFolder : parentFolderStack) {
			sb.append(getFolderInfo(parentFolder));
		}
		sb.append(getFolderInfo(currentFolder));
		return sb.toString();
	}

	private String getFolderInfo(PSTFolder folder) {
		return "[" + folder.getDisplayName() + "(" + folder.getContentCount() + ")]";
	}
}
