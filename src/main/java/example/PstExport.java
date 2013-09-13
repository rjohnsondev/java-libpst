package example;
import com.pff.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

import org.json.simple.*;

@SuppressWarnings("unchecked")
public class PstExport {
	public static void main(String[] args)
	{
		if (args.length < 2)
		{
			System.out.println("Usage: PstExport Src_File Dst_Folder");
		}
		else
			new PstExport(args[0], args[1]);
	}

	public PstExport(String filename, String destfolder) {
		try {
			PSTFile pstFile = new PSTFile(filename);
			System.out.println(pstFile.getMessageStore().getDisplayName());
			processFolder(pstFile.getRootFolder(), new java.io.File(destfolder));
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	int depth = -1;
	int msgs = -1;
	Random rnd = new Random();
	
	public JSONObject processFolder(PSTFolder folder, File destfolder)
			throws PSTException, java.io.IOException
	{
		depth++;
		// the root folder doesn't have a display name
		
		JSONObject json = new JSONObject();
		json.put("Id", folder.getDescriptorNodeId());
		JSONObject jsonfld = new JSONObject();
		JSONObject jsonmsg = new JSONObject();
		
		if (depth > 0) {
			printDepth();
			System.out.println(folder.getDisplayName());
			json.put("Name", folder.getDisplayName());
		}
		else
		{
			json.put("Name","[root]");
		}

		// go through the folders...
		if (folder.hasSubfolders()) {
			Vector<PSTFolder> childFolders = folder.getSubFolders();
			for (PSTFolder childFolder : childFolders) {
				String subf="F"+ Long.toString(childFolder.getDescriptorNodeId());
				JSONObject fld = processFolder(childFolder, new File(destfolder, subf));
				jsonfld.put(subf, fld);
			}
		}

		// and now the emails for this folder
		if (folder.getContentCount() > 0) {
			depth++;
			PSTMessage email = (PSTMessage)folder.getNextChild();
			while (email != null) {
				printDepth();
				System.out.println("Email: "+email.getSubject());
				if (msgs<0 || (rnd.nextInt(10)==1 && email.getSubject().compareTo("")!=0 && msgs!=0))
				{
					File subf = new File(destfolder, "XX");
					String msgn = "M"+email.getDescriptorNodeId();
					Date dtime = email.getMessageDeliveryTime();
					if (dtime != null)
					{
						String y = new SimpleDateFormat("yyyy").format(dtime);
						String m = new SimpleDateFormat("MM").format(dtime);
						subf = new File(destfolder, y);
						subf = new File(subf, m);
						msgn = y+"-"+m+"-"+msgn;
					}
					else
					{
						msgn = "XX-" + msgn;
					}
					subf = new File(subf, msgn);
					JSONObject msg = processEmail(email, subf);
					jsonmsg.put(msgn, msg);
					msgs--;
				}
				email = (PSTMessage)folder.getNextChild();
			}
			depth--;
		}
		
		if (jsonfld.size()>0)
			json.put("Folders", jsonfld);
		if (jsonmsg.size()>0)
			json.put("Messages", jsonmsg);

		destfolder.mkdirs();
		FileWriter fw = new FileWriter(new File(destfolder,"index.json"));
		json.writeJSONString(fw);
		fw.flush();
		fw.close();
		depth--;
		return json;
	}
	
	public JSONObject processEmail(PSTMessage email, File destfolder)
		throws PSTException, IOException, FileNotFoundException
	{
		JSONObject json = new JSONObject();
		json.put("Subject", email.getSubject());
		json.put("Id", email.getDescriptorNodeId());
		Date dtime = email.getMessageDeliveryTime();
		if (dtime != null)
		{
			json.put("DeliveryTime",new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(dtime));
		}
		json.put("SenderAddress", email.getSenderEmailAddress());
		json.put("Sender", email.getSenderName());
		destfolder.mkdirs();

		JSONObject jsonrec = new JSONObject();
		int numberOfRecipients = email.getNumberOfRecipients();
		for (int x = 0; x < numberOfRecipients; x++) 
		{
			JSONObject rec = new JSONObject();
			PSTRecipient recipient;
			try
			{
				recipient = email.getRecipient(x);
			}
			catch (java.lang.IndexOutOfBoundsException e)
			{
				recipient = null;
			}
			if (recipient != null)
			{
				rec.put("Name", recipient.getDisplayName());
				rec.put("Address", recipient.getEmailAddress());
				switch (recipient.getRecipientType())
				{
				case PSTRecipient.MAPI_TO:
					rec.put("Type", "To");
					break;
				case PSTRecipient.MAPI_CC:
					rec.put("Type", "CC");
					break;
				case PSTRecipient.MAPI_BCC:
					rec.put("Type", "BCC");
					break;

				default:
					break;
				}
				jsonrec.put(x, rec);
			}
		}
		
		JSONObject jsonatt = new JSONObject();
		int numberOfAttachments = email.getNumberOfAttachments();
		for (int x = 0; x < numberOfAttachments; x++) 
		{
			JSONObject att = new JSONObject();
			PSTAttachment attach = email.getAttachment(x);
			att.put("Id",x);
			jsonatt.put(x, att);
			
			InputStream attachmentStream = attach.getFileInputStream();
			// both long and short filenames can be used for attachments
			String filename = attach.getLongFilename();
			if (filename.isEmpty()) {
					filename = attach.getFilename();
			}
			att.put("Name", filename);
			att.put("Size", attach.getAttachSize());
			
			FileOutputStream out;
			try
			{
				out = new FileOutputStream(new File(destfolder,filename));
			}
			catch (FileNotFoundException e)
			{
				String[] tok = filename.split("\\.(?=[^\\.]+$)");
				if (tok.length > 1)
				{
					filename = String.valueOf(x) + "." + tok[1];
				}
				else
				{
					filename = String.valueOf(x);
				}
				out = new FileOutputStream(new File(destfolder,filename));
				att.put("SavedAs", filename);
			}
			// 8176 is the block size used internally and should give the best performance
			int bufferSize = 8176;
			byte[] buffer = new byte[bufferSize];
			int count = attachmentStream.read(buffer);
			while (count == bufferSize) {
					out.write(buffer);
					count = attachmentStream.read(buffer);
			}
			byte[] endBuffer = new byte[count];
			System.arraycopy(buffer, 0, endBuffer, 0, count);
			out.write(endBuffer);
			out.close();
			attachmentStream.close();
		}

		FileWriter fw;

		String html = email.getBodyHTML();
		if (html.compareTo("") != 0)
		{
			json.put("HTML", "Message.html");
			fw = new FileWriter(new File(destfolder,"Message.html"));
			fw.write(html);
			fw.close();
		}
		
		String txt = email.getBody();
		if (txt.compareTo("") != 0)
		{
			json.put("txt", "Message.txt");
			fw = new FileWriter(new File(destfolder,"Message.txt"));
			fw.write(txt);
			fw.close();
		}
		
		String rtf = email.getRTFBody();
		if (rtf.compareTo("") != 0)
		{
			json.put("rtf", "Message.rtf");
			fw = new FileWriter(new File(destfolder,"Message.rtf"));
			fw.write(rtf);
			fw.close();
		}
		
		String headers = email.getTransportMessageHeaders();
		if (headers.compareTo("") != 0)
		{
			json.put("Headers", "Headers.txt");
			fw = new FileWriter(new File(destfolder,"Headers.txt"));
			fw.write(headers);
			fw.close();
		}
		
		if (jsonatt.size()>0)
			json.put("Attachments", jsonatt);
		if (jsonrec.size()>0)
			json.put("Recipients", jsonrec);
		fw = new FileWriter(new File(destfolder,"index.json"));
		json.writeJSONString(fw);
		fw.flush();
		fw.close();
		
		return json;
	}

	public void printDepth() {
		for (int x = 0; x < depth-1; x++) {
			System.out.print(" | ");
		}
		System.out.print(" |- ");
	}
}
