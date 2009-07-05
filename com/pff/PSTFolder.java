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
			
			PSTObject child = PSTObject.detectAndLoadPSTObject(pstFile, childDescriptor);
			//if (child instanceof PSTFolder) {
				folders.put(childDescriptor, child);
			//}
			//else if (child instanceof PSTEmail) {
			//	System.out.println("email: "+childDescriptor.descriptorIdentifier);
			//}
			
		}

		this.children.put("PSTFolder", folders);
	}
	
	Iterator<DescriptorIndexNode> someChildrenIterator = null;
	
	public LinkedHashMap<DescriptorIndexNode, PSTObject> getSomeChildren(int numberToReturn)
		throws PSTException, IOException
	{
		if (someChildrenIterator == null) {
			LinkedHashMap<Integer, DescriptorIndexNode> childDescriptors = pstFile.getChildrenDescriptors(this.descriptorIndexNode.descriptorIdentifier);
			someChildrenIterator = childDescriptors.values().iterator();
		}

		LinkedHashMap<DescriptorIndexNode, PSTObject> output = new LinkedHashMap<DescriptorIndexNode, PSTObject>();
		
		for (int x = 0; x < numberToReturn; x++) {
			if (!someChildrenIterator.hasNext())
			{
				// no more!
				break;
			}
			DescriptorIndexNode childDescriptor = (DescriptorIndexNode)someChildrenIterator.next();
			PSTObject child = PSTObject.detectAndLoadPSTObject(pstFile, childDescriptor);
			output.put(childDescriptor, child);
		}

		return output;
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
