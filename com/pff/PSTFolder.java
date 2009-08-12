/**
 * 
 */
package com.pff;
import java.io.*;
import java.util.*;


/**
 * @author toweruser
 *
 */
public class PSTFolder extends PSTObject {
	
	private int folderCount = 0;
	private int emailCount = 0;
	
	PSTFolder(PSTFile theFile, DescriptorIndexNode descriptorIndexNode)
		throws PSTException, IOException
	{
		super(theFile, descriptorIndexNode);
	}
	
	/**
	 * For pre-populating a folder object with values.
	 * Not recommended for use outside this library
	 * @param theFile
	 * @param folderIndexNode
	 * @param table
	 */
	PSTFolder(PSTFile theFile, DescriptorIndexNode folderIndexNode, PSTTableBC table, HashMap<Integer, PSTDescriptorItem> localDescriptorItems) {
		super(theFile, folderIndexNode, table, localDescriptorItems);
	}
	

	public HashMap<DescriptorIndexNode, PSTObject> getSubFolders()
		throws PSTException, IOException
	{
		this.processChildren();
		return this.children.get("PSTFolder");
	}
	
	public PSTEmail[] getEmails() { return new PSTEmail[0]; }
//	public PSTAppointment[] getAppointments() { return new PSTAppointment[0]; }
//	public PSTContact[] getContacts() { return new PSTContact[0]; }
	
	private void processChildren()
		throws PSTException, IOException
	{
		if (this.children != null) 
		{
			// we have already processed
			return;
		}
		this.children = new LinkedHashMap<String, HashMap<DescriptorIndexNode, PSTObject>>();
		LinkedHashMap<DescriptorIndexNode, PSTObject> folders = new LinkedHashMap<DescriptorIndexNode, PSTObject>();
		LinkedHashMap<Integer, DescriptorIndexNode> childDescriptors = pstFile.getChildrenDescriptors(this.descriptorIndexNode.descriptorIdentifier);
		
		Iterator<DescriptorIndexNode> iterator = childDescriptors.values().iterator();
		while (iterator.hasNext()) {
			DescriptorIndexNode childDescriptor = (DescriptorIndexNode)iterator.next();

			// just kinda assuming that all folders are less than this magic number.
			if (childDescriptor.descriptorIdentifier < 0x100000) {
				PSTObject child = PSTObject.detectAndLoadPSTObject(pstFile, childDescriptor);
				if (child instanceof PSTFolder) {
					folders.put(childDescriptor, child);
				}
				folderCount++;
			} else if (childDescriptor.descriptorIdentifier < 0x200000) {
				// we are something else...
				// like a wunderBar or FolderDesign, or named view or whatever
			} else if (childDescriptor.descriptorIdentifier > 0x200000) {
				emailCount++;
			}
		}

		this.children.put("PSTFolder", folders);
	}
	
	Iterator<DescriptorIndexNode> someChildrenIterator = null;
	int childrenIteratorCursor = 0;
	
	public LinkedHashMap<DescriptorIndexNode, PSTObject> getChildren(int numberToReturn)
		throws PSTException, IOException
	{
		if (someChildrenIterator == null) {
			LinkedHashMap<Integer, DescriptorIndexNode> childDescriptors = pstFile.getChildrenDescriptors(this.descriptorIndexNode.descriptorIdentifier);
			someChildrenIterator = childDescriptors.values().iterator();
			childrenIteratorCursor = 0;
		}

		LinkedHashMap<DescriptorIndexNode, PSTObject> output = new LinkedHashMap<DescriptorIndexNode, PSTObject>();
		
		for (int x = 0; x < numberToReturn; x++) {
			if (!someChildrenIterator.hasNext())
			{
				// no more!
				break;
			}
			DescriptorIndexNode childDescriptor = (DescriptorIndexNode)someChildrenIterator.next();
			
			// only non-folders plz!!
			if (childDescriptor.descriptorIdentifier >= 0x200000) {
				PSTObject child = PSTObject.detectAndLoadPSTObject(pstFile, childDescriptor);
				childrenIteratorCursor++;
				output.put(childDescriptor, child);
			} else {
				x--;
			}
		}

		return output;
	}
	
	public PSTObject getNextChild()
		throws PSTException, IOException
	{
		if (someChildrenIterator == null) {
			LinkedHashMap<Integer, DescriptorIndexNode> childDescriptors = pstFile.getChildrenDescriptors(this.descriptorIndexNode.descriptorIdentifier);
			someChildrenIterator = childDescriptors.values().iterator();
			childrenIteratorCursor = 0;
		}
		
		while (someChildrenIterator.hasNext()) {
			DescriptorIndexNode childDescriptor = (DescriptorIndexNode)someChildrenIterator.next();
			if (childDescriptor.descriptorIdentifier >= 0x200000) {
				PSTObject child = PSTObject.detectAndLoadPSTObject(pstFile, childDescriptor);
				childrenIteratorCursor++;
				return child;
			}
		}
		
		return null;
	}
	
	/**
	 * this really needs to be made more efficient...
	 * @param numberToReturn
	 */
	public void moveChildCursorTo(int newIndex) {
		if (newIndex < 1) {
			childrenIteratorCursor = 0;
			return;
		}
		if (newIndex < childrenIteratorCursor || someChildrenIterator == null) {
			// bah! we need to go backwards :(
			LinkedHashMap<Integer, DescriptorIndexNode> childDescriptors = pstFile.getChildrenDescriptors(this.descriptorIndexNode.descriptorIdentifier);
			someChildrenIterator = childDescriptors.values().iterator();
			childrenIteratorCursor = 0;
		}
		
		// move the iterator along until we get to the record.
		// this really sucks
		for (int x = childrenIteratorCursor; x < newIndex && someChildrenIterator.hasNext(); x++) {
			DescriptorIndexNode childDescriptor = (DescriptorIndexNode)someChildrenIterator.next();
			if (childDescriptor.descriptorIdentifier < 0x200000) {
				x--;
			}
		}
		childrenIteratorCursor = newIndex;
	}
	
	
	public int getFolderCount()
		throws IOException, PSTException
	{
		processChildren();
		return this.folderCount;
	}
	
	public int getEmailCount()
		throws IOException, PSTException
	{
		processChildren();
		return this.emailCount;
	}


	/**
	 * get the message store display name
	 * @return
	 */
	public String getDisplayName() {
		// attempt to find in the table.
		int displayNameEntryType = 0x3001;
		if (this.items.containsKey(displayNameEntryType)) {
			PSTTableBCItem item = (PSTTableBCItem)this.items.get(displayNameEntryType);
			return new String(item.getStringValue());
		}
		return "";
	}
	
	public String getFolderType() {
		return this.getStringItem(0x3601);
	}
	
}
