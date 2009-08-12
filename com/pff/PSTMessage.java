/**
 * 
 */
package com.pff;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author toweruser
 *
 */
public class PSTMessage extends PSTObject {

	public static final int IMPORTANCE_LOW = 0;
	public static final int IMPORTANCE_NORMAL = 1;
	public static final int IMPORTANCE_HIGH = 2;

	PSTMessage(PSTFile theFile, DescriptorIndexNode descriptorIndexNode)
			throws PSTException, IOException
	{
		super(theFile, descriptorIndexNode);
	}

	PSTMessage(PSTFile theFile, DescriptorIndexNode folderIndexNode, PSTTableBC table, HashMap<Integer, PSTDescriptorItem> localDescriptorItems){
		super(theFile, folderIndexNode, table, localDescriptorItems);
	}
	
	/**
	 * get the importance of the email
	 * @return IMPORTANCE_NORMAL if unknown
	 */
	public int getImportance() {
		return getIntItem(0x0017, IMPORTANCE_NORMAL);
	}
	
	/**
	 * get the message class for the email
	 * @return empty string if unknown
	 */
	public String getMessageClass() {
		return this.getStringItem(0x001a);
	}
	
	/**
	 * get the subject
	 * @return empty string if not found
	 */
	public String getSubject() {
		String subject = this.getStringItem(0x0037);
		byte[] controlCodesA = {0x01, 0x01};
		byte[] controlCodesB = {0x01, 0x05};
		byte[] controlCodesC = {0x01, 0x10};
		if (subject.startsWith(new String(controlCodesA)) ||
			subject.startsWith(new String(controlCodesB)) ||
			subject.startsWith(new String(controlCodesC)))
		{
			subject = subject.substring(2,subject.length());
		}
		return subject;
	}
	
	/**
	 * get the client submit time
	 * @returns null if not found
	 */
	public Date getClientSubmitTime() {
		return this.getDateItem(0x0039);
	}
	
	/**
	 * get received by name
	 * @returns empty string if not found
	 */
	public String getReceivedByName() {
		return this.getStringItem(0x0040);
	}
	
	/**
	 * get sent representing name
	 * @returns empty string if not found
	 */
	public String getSentRepresentingName() {
		return this.getStringItem(0x0042);
	}
	
	/**
	 * Sent representing address type
	 * Known values are SMTP, EX (Exchange) and UNKNOWN
	 * @return empty string if not found
	 */
	public String getSentRepresentingAddressType() {
		return this.getStringItem(0x0064);
	}

	/**
	 * Sent representing email address
	 * @return empty string if not found
	 */
	public String getSentRepresentingEmailAddress() {
		return this.getStringItem(0x0065);
	}
	
	/**
	 * Conversation topic
	 * This is basically the subject from which Fwd:, Re, etc. has been removed
	 * @return empty string if not found
	 */
	public String getConversationTopic() {
		return this.getStringItem(0x0070);
	}

	/**
	 * Received by address type
	 * Known values are SMTP, EX (Exchange) and UNKNOWN
	 * @return empty string if not found
	 */
	public String getReceivedByAddressType() {
		return this.getStringItem(0x0075);
	}
	
	/**
	 * Received by email address
	 * @return empty string if not found
	 */
	public String getReceivedByAddress() {
		return this.getStringItem(0x0076);
	}
	
	public String getTransportMessageHeaders() {
		return this.getStringItem(0x007d, PSTTableItem.VALUE_TYPE_PT_STRING8);
	}
	
	public String getBodyHTML() {
		return this.getStringItem(0x1013, PSTTableItem.VALUE_TYPE_PT_STRING8);
	}
	
	public String getPlainText() {
//		System.out.println(this);
//		System.out.println(this.items);
		return this.getStringItem(0x1000);
	}
	
	public String getRTFBody()
		throws PSTException, IOException
	{
		// do we have an entry for it?
		if (this.items.containsKey(0x1009))
		{
			// is it a reference?
			PSTTableBCItem item = this.items.get(0x1009);
			if (item.data.length > 0) {
				throw new PSTException("Umm, not sure what to do with this data here, was just expecting a local descriptor node ref.");
			}
			int ref = this.getIntItem(0x1009);
			PSTDescriptorItem descItem = this.localDescriptorItems.get(ref);
			RandomAccessFile in = this.pstFile.getFileHandle();
			//get the data at the location
			OffsetIndexItem indexItem = PSTObject.getOffsetIndexNode(in, descItem.offsetIndexIdentifier);
			in.seek(indexItem.fileOffset);
			byte[] temp = new byte[indexItem.size];
			in.read(temp);
			temp = PSTObject.decode(temp);
			return (LZFu.decode(temp));
		}
		
		return "";
	}
	
	private PSTTable7C attachmentTable = null;
	
	private void processAttachments()
		throws PSTException, IOException
	{
		int attachmentTableKey = 0x0671;
		if (this.attachmentTable == null &&
			this.localDescriptorItems != null &&
			this.localDescriptorItems.containsKey(attachmentTableKey))
		{
			PSTDescriptorItem item = this.localDescriptorItems.get(attachmentTableKey);
			if (item.data.length > 0) {
				byte[] valuesArray = null;
				if (item.subNodeOffsetIndexIdentifier != 0) {
					// we have a sub-node!!!!
					// most likely an external values array.  Doesn't really contain any values
					// as far as I can tell, but useful for knowing how many entries we are dealing with
					if (item.subNodeDescriptorItems.size() > 1) {
						throw new PSTException("not sure how to deal with multiple value arrays in subdescriptors");
					}
					// get the first value out
					Iterator<PSTDescriptorItem> valueIterator = item.subNodeDescriptorItems.values().iterator();
					PSTDescriptorItem next = valueIterator.next();
					
					RandomAccessFile in = this.pstFile.getFileHandle();
					OffsetIndexItem offset = PSTObject.getOffsetIndexNode(in, next.offsetIndexIdentifier);
					valuesArray = new byte[offset.size];
					in.seek(offset.fileOffset);
					in.read(valuesArray);
					valuesArray = PSTObject.decode(valuesArray);
				}
				attachmentTable = new PSTTable7C(item.data, valuesArray);
			}
			
		}
	}
	
	public int getNumberOfAttachments()
		throws PSTException, IOException
	{
		this.processAttachments();
		// still nothing? must be no attachments...
		if (this.attachmentTable == null) {
			return 0;
		}
		return this.attachmentTable.getItemCount();
	}
	
	public PSTAttachment getAttachment(int attachmentNumber)
		throws PSTException, IOException
	{
		this.processAttachments();
		
		if (attachmentNumber >= this.getNumberOfAttachments()) {
			throw new PSTException("unable to fetch attachment number "+attachmentNumber+", only "+this.attachmentTable.getItemCount()+" in this email");
		}
		
		// we process the C7 table here, basically we just want the attachment local descriptor...
		HashMap<Integer, PSTTable7CItem> attachmentDetails = this.attachmentTable.getItems().get(attachmentNumber);
		PSTTable7CItem attachmentTableItem = attachmentDetails.get(0x67f2);
		int descriptorItemId = (int)attachmentTableItem.getLongValue();
		// get the local descriptor for the attachmentDetails table.
		PSTDescriptorItem descriptorItem = this.localDescriptorItems.get(descriptorItemId); 
		OffsetIndexItem attachmentOffset = PSTObject.getOffsetIndexNode(this.pstFile.getFileHandle(), descriptorItem.offsetIndexIdentifier);
		// read in the data from the attachmentOffset
		RandomAccessFile in = this.pstFile.getFileHandle();
		in.seek(attachmentOffset.fileOffset);
		byte[] attachmentData = new byte[attachmentOffset.size];
		in.read(attachmentData);
		if (this.pstFile.getEncryptionType() == PSTFile.ENCRYPTION_TYPE_COMPRESSIBLE) {
			attachmentData = PSTObject.decode(attachmentData);
		}
		
		// try and decode it
		PSTTableBC attachmentDetailsTable = new PSTTableBC(attachmentData);
		
		// create our all-precious attachment object.
		// note that all the information that was in the c7 table is repeated in the eb table in attachment data.
		// so no need to pass it...
		return new PSTAttachment(this.pstFile, attachmentDetailsTable, this.localDescriptorItems);
	}
	
	public boolean isRead() {
		return ((this.getIntItem(0x0e07) & 0x01) != 0);
	}
	public boolean isUnmodified() {
		return ((this.getIntItem(0x0e07) & 0x02) != 0);
	}
	public boolean isSubmitted() {
		return ((this.getIntItem(0x0e07) & 0x04) != 0);
	}
	public boolean isUnsent() {
		return ((this.getIntItem(0x0e07) & 0x08) != 0);
	}
	public boolean hasAttachments() {
		return ((this.getIntItem(0x0e07) & 0x10) != 0);
	}
	public boolean isFromMe() {
		return ((this.getIntItem(0x0e07) & 0x20) != 0);
	}
	public boolean isAssociated() {
		return ((this.getIntItem(0x0e07) & 0x40) != 0);
	}
	public boolean isResent() {
		return ((this.getIntItem(0x0e07) & 0x80) != 0);
	}
	
	
	/**
	 * string representation of this email
	 */
	public String toString() {
		return
			"PSTEmail: "+this.getSubject()+"\n"+
			"Importance: "+this.getImportance()+"\n"+
			"Message Class: "+this.getMessageClass();
	}
	
}
