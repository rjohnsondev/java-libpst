/**
 * 
 */
package com.pff;
import java.io.*;
import java.util.*;


/**
 * Represents a folder in the PST File
 * Allows you to access child folders or items.  Items are accessed through a sort of cursor arrangement.
 * This allows for incremental reading of a folder which may have _lots_ of emails.
 * @author Richard Johnson
 */
public class PSTFolder extends PSTObject {
	
	/**
	 * a constructor for the rest of us...
	 * @param theFile
	 * @param descriptorIndexNode
	 * @throws PSTException
	 * @throws IOException
	 */
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
	
	/**
	 * get all of the sub folders...
	 * there are not usually thousands, so we just do it in one big operation.
	 * @return all of the subfolders
	 * @throws PSTException
	 * @throws IOException
	 */
	public LinkedHashSet<PSTObject> getSubFolders()
		throws PSTException, IOException
	{
		this.processChildren();
		LinkedHashSet<PSTObject> output = new LinkedHashSet<PSTObject>();
		for (DescriptorIndexNode childDescriptor : this.subFolders) {
			output.add(PSTObject.detectAndLoadPSTObject(this.pstFile, childDescriptor));
		}
		return output;
	}

	/**
	 * internal vars for the tracking of things..
	 */
	private LinkedHashSet<DescriptorIndexNode> subFolders = null;
	private LinkedHashSet<DescriptorIndexNode> emails = null;
	private Iterator<DescriptorIndexNode> emailIterator = null;
	private int emailIteratorPosition = 0;
	private LinkedHashSet<DescriptorIndexNode> otherItems = null;
	
	/**
	 * this method goes through all of the children and sorts them into one of the three hash sets.
	 * @throws PSTException
	 * @throws IOException
	 */
	private void processChildren()
		throws PSTException, IOException
	{
		if (this.emailIterator != null) {
			// we have already processed
			return;
		}
		// create out our sets
		subFolders = new LinkedHashSet<DescriptorIndexNode>();
		emails = new LinkedHashSet<DescriptorIndexNode>();
		otherItems = new LinkedHashSet<DescriptorIndexNode>();
		
		LinkedHashMap<Integer, DescriptorIndexNode> childDescriptors = pstFile.getChildrenDescriptors(this.descriptorIndexNode.descriptorIdentifier);
		Iterator<DescriptorIndexNode> iterator = childDescriptors.values().iterator();
		while (iterator.hasNext()) {
			DescriptorIndexNode childDescriptor = (DescriptorIndexNode)iterator.next();

			// just kinda assuming that all folders are less than this magic number.
			if (childDescriptor.descriptorIdentifier < 0x100000) {
				subFolders.add(childDescriptor);
			} else if (childDescriptor.descriptorIdentifier < 0x200000) {
				// we are something else...
				// like a wunderBar or FolderDesign, or named view or whatever
				otherItems.add(childDescriptor);
//				System.out.println("unknown child type: " + childDescriptor.descriptorIdentifier);
			} else if (childDescriptor.descriptorIdentifier > 0x200000) {
				emails.add(childDescriptor);
			}
		}
		emailIterator = emails.iterator();
	}
	
	/**
	 * get some children from the folder
	 * This is implemented as a cursor of sorts, as there could be thousands
	 * and that is just too many to process at once.
	 * @param numberToReturn
	 * @return bunch of children in this folder
	 * @throws PSTException
	 * @throws IOException
	 */
	public LinkedHashSet<PSTObject> getChildren(int numberToReturn)
		throws PSTException, IOException
	{
		processChildren();
		
		LinkedHashSet<PSTObject> output = new LinkedHashSet<PSTObject>();
		
		for (int x = 0; x < numberToReturn; x++) {
			if (!emailIterator.hasNext())
			{
				// no more!
				break;
			}
//			DescriptorIndexNode childDescriptor = (DescriptorIndexNode)emailIterator.next();
			emailIterator.next();
			emailIteratorPosition++;
		}

		return output;
	}
	
	/**
	 * Get the next child of this folder
	 * As there could be thousands of emails, we have these kind of cursor operations
	 * @return the next email in the folder or null if at the end of the folder
	 * @throws PSTException
	 * @throws IOException
	 */
	public PSTObject getNextChild()
		throws PSTException, IOException
	{
		processChildren();
		
		if (!emailIterator.hasNext()) {
			// we have no more!!
			return null;
		}
		
		DescriptorIndexNode childDescriptor = (DescriptorIndexNode)emailIterator.next();
		PSTObject child = PSTObject.detectAndLoadPSTObject(pstFile, childDescriptor);
		emailIteratorPosition++;
		
		return child;
	}
	
	/**
	 * move the internal folder cursor to the desired position
	 * position 0 is before the first record.
	 * @param newIndex
	 */
	public void moveChildCursorTo(int newIndex) {
		// this really needs to be made more efficient...
		if (newIndex < 1) {
			emailIteratorPosition = 0;
			emailIterator = emails.iterator();
			return;
		}
		if (newIndex < emailIteratorPosition) {
			// bah! we need to go backwards :(
			emailIteratorPosition = 0;
			emailIterator = emails.iterator();
		}
		
		// move the iterator along until we get to the record.
		// this really sucks, a bit of forethought would have helped here
		for (int x = emailIteratorPosition; x < newIndex && emailIterator.hasNext(); x++) {
//			DescriptorIndexNode childDescriptor = (DescriptorIndexNode)emailIterator.next();
			emailIterator.next();
		}
		emailIteratorPosition = newIndex;
	}
	
	/**
	 * the number of child folders in this folder
	 * @return number of subfolders as counted
	 * @throws IOException
	 * @throws PSTException
	 */
	public int getSubFolderCount()
		throws IOException, PSTException
	{
		processChildren();
		return this.subFolders.size();
	}
	
	/**
	 * the number of emails in this folder
	 * this is the count of emails made by the library and will therefore should be more accurate than getContentCount
	 * @return number of emails in this folder (as counted)
	 * @throws IOException
	 * @throws PSTException
	 */
	public int getEmailCount()
		throws IOException, PSTException
	{
		processChildren();
		return this.emails.size();
	}

	
	public int getFolderType() {
		return this.getIntItem(0x3601);
	}
	
	/**
	 * the number of emails in this folder
	 * this is as reported by the PST file, for a number calculated by the library use getEmailCount
	 * @return number of items as reported by PST File
	 */
	public int getContentCount() {
		return this.getIntItem(0x3602);
	}

	/**
	 * Amount of unread content items Integer 32-bit signed
	 */
	public int getUnreadCount() {
		return this.getIntItem(0x3603);
	}
	
	/**
	 * does this folder have subfolders
	 * once again, read from the PST, use getSubFolderCount if you want to know what the library makes of it all
	 * @return has subfolders as reported by the PST File
	 */
	public boolean hasSubfolders() {
		return (this.getIntItem(0x360a) != 0);
	}
	
	public String getContainerClass() {
		return this.getStringItem(0x3613);
	}
	
	public int getAssociateContentCount() {
		return this.getIntItem(0x3617);
	}
	
	/**
	 * Container flags Integer 32-bit signed
	 */
	public int getContainerFlags() {
		return this.getIntItem(0x3600);
	}
	
}
