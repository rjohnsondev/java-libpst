/**
 * Copyright 2010 Richard Johnson & Orin Eman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ---
 *
 * This file is part of java-libpst.
 *
 * java-libpst is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * java-libpst is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with java-libpst.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.pff.objects;

import java.io.IOException;
import java.util.*;

import com.pff.PSTUtils;
import com.pff.exceptions.PSTException;
import com.pff.objects.sub.PSTTimeZone;
import com.pff.parsing.DescriptorIndexNode;
import com.pff.parsing.PSTDescriptorItem;
import com.pff.parsing.PSTNodeInputStream;
import com.pff.parsing.tables.PSTTableBC;
import com.pff.parsing.tables.PSTTableBCItem;
import com.pff.source.PSTSource;

/**
 * PST Object is the root class of all PST Items.
 * It also provides a number of static utility functions.  The most important of which is the
 * detectAndLoadPSTObject call which allows extraction of a PST Item from the file.
 * @author Richard Johnson
 */
public class PSTObject {

	public static final int NID_TYPE_HID = 0x00; // Heap node
	public static final int NID_TYPE_INTERNAL = 0x01; // Internal node (section 2.4.1)
	public static final int NID_TYPE_NORMAL_FOLDER = 0x02; // Normal Folder object (PC)
	public static final int NID_TYPE_SEARCH_FOLDER = 0x03; // Search Folder object (PC)
	public static final int NID_TYPE_NORMAL_MESSAGE = 0x04; // Normal Message object (PC)
	public static final int NID_TYPE_ATTACHMENT = 0x05; // Attachment object (PC)
	public static final int NID_TYPE_SEARCH_UPDATE_QUEUE = 0x06; // Queue of changed objects for search Folder objects
	public static final int NID_TYPE_SEARCH_CRITERIA_OBJECT = 0x07; // Defines the search criteria for a search Folder object
	public static final int NID_TYPE_ASSOC_MESSAGE = 0x08; // Folder associated information (FAI) Message object (PC)
	public static final int NID_TYPE_CONTENTS_TABLE_INDEX = 0x0A; // Internal, persisted view-related
	public static final int NID_TYPE_RECEIVE_FOLDER_TABLE = 0X0B; // Receive Folder object (Inbox)
	public static final int NID_TYPE_OUTGOING_QUEUE_TABLE = 0x0C; // Outbound queue (Outbox)
	public static final int NID_TYPE_HIERARCHY_TABLE = 0x0D; // Hierarchy table (TC)
	public static final int NID_TYPE_CONTENTS_TABLE = 0x0E; // Contents table (TC)
	public static final int NID_TYPE_ASSOC_CONTENTS_TABLE = 0x0F; // FAI contents table (TC)
	public static final int NID_TYPE_SEARCH_CONTENTS_TABLE = 0x10; // Contents table (TC) of a search Folder object
	public static final int NID_TYPE_ATTACHMENT_TABLE = 0x11; // Attachment table (TC)
	public static final int NID_TYPE_RECIPIENT_TABLE = 0x12; // Recipient table (TC)
	public static final int NID_TYPE_SEARCH_TABLE_INDEX = 0x13; // Internal, persisted view-related
	public static final int NID_TYPE_LTP = 0x1F; // LTP


	
	public String getItemsString() {
		return items.toString();
	}
	
	protected PSTSource pstFile;
	protected byte[] data;
	protected DescriptorIndexNode descriptorIndexNode;
	protected HashMap<Integer, PSTTableBCItem> items;
	protected HashMap<Integer, PSTDescriptorItem> localDescriptorItems = null;
	
	protected LinkedHashMap<String, HashMap<DescriptorIndexNode, PSTObject>> children;
	
	protected PSTObject(PSTSource theFile, DescriptorIndexNode descriptorIndexNode)
		throws PSTException, IOException
	{
		this.pstFile = theFile;
		this.descriptorIndexNode = descriptorIndexNode;

		//descriptorIndexNode.readData(theFile);
		//PSTTableBC table = new PSTTableBC(descriptorIndexNode.dataBlock.data, descriptorIndexNode.dataBlock.blockOffsets);
		PSTTableBC table = new PSTTableBC(new PSTNodeInputStream(pstFile, pstFile.getOffsetIndexNode(descriptorIndexNode.dataOffsetIndexIdentifier)));
		//System.out.println(table);
		this.items = table.getItems();
		
		if (descriptorIndexNode.localDescriptorsOffsetIndexIdentifier != 0) {
			//PSTDescriptor descriptor = new PSTDescriptor(theFile, descriptorIndexNode.localDescriptorsOffsetIndexIdentifier);
			//localDescriptorItems = descriptor.getChildren();
			this.localDescriptorItems = theFile.getPSTDescriptorItems(descriptorIndexNode.localDescriptorsOffsetIndexIdentifier);
		}
	}
	
	/**
	 * for pre-population
	 * @param theFile
	 * @param folderIndexNode
	 * @param table
	 */
	protected PSTObject(PSTSource theFile, DescriptorIndexNode folderIndexNode, PSTTableBC table, HashMap<Integer, PSTDescriptorItem> localDescriptorItems) {
		this.pstFile = theFile;
		this.descriptorIndexNode = folderIndexNode;
		this.items = table.getItems();
		this.table = table;
		this.localDescriptorItems = localDescriptorItems;
	}
	protected PSTTableBC table;
	
	
	
	/**
	 * get the descriptor node for this item
	 * this identifies the location of the node in the BTree and associated info
	 * @return item's descriptor node
	 */
	public DescriptorIndexNode getDescriptorNode() {
		return this.descriptorIndexNode;
	}
	/**
	 * get the descriptor identifier for this item
	 * can be used for loading objects through detectAndLoadPSTObject(PSTFile theFile, long descriptorIndex)
	 * @return item's descriptor node identifier
	 */
	public long getDescriptorNodeId() {
		//return this.descriptorIndexNode.descriptorIdentifier;
		if (this.descriptorIndexNode != null) { // Prevent null pointer exceptions for embedded messages
			return this.descriptorIndexNode.descriptorIdentifier;
		}
		return 0;
	}

	public int getNodeType() {
		return PSTObject.getNodeType(this.descriptorIndexNode.descriptorIdentifier);
	}
	public static int getNodeType(int descriptorIdentifier) {
		return descriptorIdentifier & 0x1F;
	}
	
	
	protected int getIntItem(int identifier) {
		return getIntItem(identifier, 0);
	}
	protected int getIntItem(int identifier, int defaultValue) {
		if (this.items.containsKey(identifier)) {
			PSTTableBCItem item = this.items.get(identifier);
			return item.entryValueReference;
		}
		return defaultValue;
	}
	
	protected boolean getBooleanItem(int identifier) {
		return getBooleanItem(identifier, false);
	}
	protected boolean getBooleanItem(int identifier, boolean defaultValue) {
		if (this.items.containsKey(identifier)) {
			PSTTableBCItem item = this.items.get(identifier);
			return item.entryValueReference != 0;
		}
		return defaultValue;
	}

	protected double getDoubleItem(int identifier) {
		return getDoubleItem(identifier, 0);
	}
	protected double getDoubleItem(int identifier, double defaultValue) {
		if (this.items.containsKey(identifier)) {
			PSTTableBCItem item = this.items.get(identifier);
			long longVersion = PSTUtils.convertLittleEndianBytesToLong(item.data);
			return Double.longBitsToDouble(longVersion);
		}
		return defaultValue;
	}
	
	
	protected long getLongItem(int identifier) {
		return getLongItem(identifier, 0);
	}
	protected long getLongItem(int identifier, long defaultValue) {
		if (this.items.containsKey(identifier)) {
			PSTTableBCItem item = this.items.get(identifier);
			if (item.entryValueType == 0x0003) {
				// we are really just an int
				return item.entryValueReference;
			} else if ( item.entryValueType == 0x0014 ){
				// we are a long
				if ( item.data != null && item.data.length == 8 ) {
					return PSTUtils.convertLittleEndianBytesToLong(item.data, 0, 8);
				} else {
					System.err.printf("Invalid data length for long id 0x%04X\n", identifier);
					// Return the default value for now...
				}
			}
		}
		return defaultValue;
	}
	
	protected String getStringItem(int identifier) {
		return getStringItem(identifier, 0);
	}
	protected String getStringItem(int identifier, int stringType) {
		return getStringItem(identifier, stringType, null);
	}
	protected String getStringItem(int identifier, int stringType, String codepage) {
		PSTTableBCItem item = this.items.get(identifier);
		if ( item != null ) {

			if (codepage == null) {
				codepage = this.getStringCodepage();
			}

			// get the string type from the item if not explicitly set
			if ( stringType == 0 ) {
				stringType = item.entryValueType;
			}

			// see if there is a descriptor entry
			if ( !item.isExternalValueReference ) {
				//System.out.println("here: "+new String(item.data)+this.descriptorIndexNode.descriptorIdentifier);
				return PSTObject.createJavaString(item.data, stringType, codepage);
			}
			if (this.localDescriptorItems != null &&
				this.localDescriptorItems.containsKey(item.entryValueReference))
			{
				// we have a hit!
				PSTDescriptorItem descItem = this.localDescriptorItems.get(item.entryValueReference);
				
				try {
					byte[] data = descItem.getData();
					if ( data == null ) {
						return "";
					}

					return PSTObject.createJavaString(data, stringType, codepage);
				} catch (Exception e) {
					System.err.printf("Exception %s decoding string %s: %s\n",
							e.toString(),
							PSTSource.getPropertyDescription(identifier, stringType), data != null ? data.toString() : "null");
					return "";
				}
				//System.out.printf("PSTObject.getStringItem - item isn't a string: 0x%08X\n", identifier);
				//return "";
			}

			return PSTObject.createJavaString(data, stringType, codepage);
		}
		return "";
	}

	static String createJavaString(byte[] data, int stringType, String codepage)
	{
		try {
			if ( stringType == 0x1F ) {
				return new String(data, "UTF-16LE");
			}

			if (codepage == null) {
				return new String(data);
			} else {
				codepage = codepage.toUpperCase();
				return new String(data, codepage);
			}
			/*
			if (codepage == null || codepage.toUpperCase().equals("UTF-8") || codepage.toUpperCase().equals("UTF-7")) {
				// PST UTF-8 strings are not... really UTF-8
				// it seems that they just don't use multibyte chars at all.
				// indeed, with some crylic chars in there, the difficult chars are just converted to %3F(?)
				// I suspect that outlook actually uses RTF to store these problematic strings.
				StringBuffer sbOut = new StringBuffer();
				for (int x = 0; x < data.length; x++) {
					sbOut.append((char)(data[x] & 0xFF)); // just blindly accept the byte as a UTF char, seems right half the time
				}
				return new String(sbOut);
			} else {
				codepage = codepage.toUpperCase();
				return new String(data, codepage);
			}
			 */
		} catch (Exception err) {
			System.err.println("Unable to decode string");
			err.printStackTrace();
			return "";
		}
	}

	private String getStringCodepage() {
		// try and get the codepage
		PSTTableBCItem cpItem = this.items.get(0x3FFD); // PidTagMessageCodepage
		if (cpItem == null) {
			cpItem = this.items.get(0x3FDE); // PidTagInternetCodepage
		}
		if (cpItem != null) {
			return PSTSource.getInternetCodePageCharset(cpItem.entryValueReference);
		}
		return null;
	}
	
	public Date getDateItem(int identifier) {
		if ( this.items.containsKey(identifier) ) {
			PSTTableBCItem item = this.items.get(identifier);
			if (item.data.length == 0 ) {
				return new Date(0);
			}
			int high = (int)PSTUtils.convertLittleEndianBytesToLong(item.data, 4, 8);
			int low = (int)PSTUtils.convertLittleEndianBytesToLong(item.data, 0, 4);
			 
			return PSTUtils.filetimeToDate(high, low);
		}
		return null;
	}
	
	protected byte[] getBinaryItem(int identifier) {
		if (this.items.containsKey(identifier)) {
			PSTTableBCItem item = this.items.get(identifier);
			if ( item.entryValueType == 0x0102 ) {
				if ( !item.isExternalValueReference ) {
					return item.data;
				}
				if ( this.localDescriptorItems != null &&
					 this.localDescriptorItems.containsKey(item.entryValueReference))
				{
					// we have a hit!
					PSTDescriptorItem descItem = this.localDescriptorItems.get(item.entryValueReference);
					try {
						return descItem.getData();
					} catch (Exception e) {
						System.err.printf("Exception reading binary item: reference 0x%08X\n", item.entryValueReference);
						
						return null;
					}
				}
				
				//System.out.println("External reference!!!\n");
			}
		}
		return null;
	}
	
	protected PSTTimeZone getTimeZoneItem(int identifier) {
		byte[] tzData = getBinaryItem(identifier);
		if ( tzData != null && tzData.length != 0 ) {
			return new PSTTimeZone(tzData);
		}
		return null;
	}

	public String getMessageClass() {
		return this.getStringItem(0x001a);
	}
	
	public String toString() {
		return this.localDescriptorItems + "\n" +
				(this.items);
	}
	
	/**
	 * These are the common properties, some don't really appear to be common across folders and emails, but hey
	 */
	
	/**
	 * get the display name
	 */
	public String getDisplayName() {
		return this.getStringItem(0x3001);
	}
	/**
	 * Address type
	 * Known values are SMTP, EX (Exchange) and UNKNOWN
	 */
	public String getAddrType() {
		return this.getStringItem(0x3002);
	}
	/**
	 * E-mail address
	 */
	public String getEmailAddress() {
		return this.getStringItem(0x3003);
	}
	/**
	 * Comment
	 */
	public String getComment() {
		return this.getStringItem(0x3004);
	}
	/**
	 * Creation time
	 */
	public Date getCreationTime() {
		return this.getDateItem(0x3007);
	}
	/**
	 * Modification time
	 */
	public Date getLastModificationTime() {
		return this.getDateItem(0x3008);
	}

	
	
	/**
	 * Detect and load a PST Object from a file with the specified descriptor index
	 * @param theFile
	 * @param descriptorIndex
	 * @return PSTObject with that index
	 * @throws IOException
	 * @throws PSTException
	 */
	public static PSTObject detectAndLoadPSTObject(PSTSource theFile, long descriptorIndex)
		throws IOException, PSTException
	{
		return detectAndLoadPSTObject(theFile, theFile.getDescriptorIndexNode(descriptorIndex));
	}

	/**
	 * Detect and load a PST Object from a file with the specified descriptor index
	 * @param theFile
	 * @param folderIndexNode
	 * @return PSTObject with that index
	 * @throws IOException
	 * @throws PSTException
	 */
	public static PSTObject detectAndLoadPSTObject(PSTSource theFile, DescriptorIndexNode folderIndexNode)
		throws IOException, PSTException
	{
		int nidType = (folderIndexNode.descriptorIdentifier & 0x1F);
		if ( nidType == 0x02 || nidType == 0x03 || nidType == 0x04 ) {

			PSTTableBC table = new PSTTableBC(new PSTNodeInputStream(theFile, theFile.getOffsetIndexNode(folderIndexNode.dataOffsetIndexIdentifier)));

			HashMap<Integer, PSTDescriptorItem> localDescriptorItems = null;
			if (folderIndexNode.localDescriptorsOffsetIndexIdentifier != 0) {
				localDescriptorItems = theFile.getPSTDescriptorItems(folderIndexNode.localDescriptorsOffsetIndexIdentifier);
			}
			
			if ( nidType == 0x02 || nidType == 0x03 ) {
				return new PSTFolder(theFile, folderIndexNode, table, localDescriptorItems);
			} else  {
				return createAppropriatePSTMessageObject(theFile, folderIndexNode, table, localDescriptorItems);
			}
		}
		else
		{			
			throw new PSTException("Unknown child type with offset id: "+folderIndexNode.localDescriptorsOffsetIndexIdentifier);
		}
	}
	
	
	
	static PSTMessage createAppropriatePSTMessageObject(PSTSource theFile, DescriptorIndexNode folderIndexNode, PSTTableBC table, HashMap<Integer, PSTDescriptorItem> localDescriptorItems)
	{

		PSTTableBCItem item = table.getItems().get(0x001a);
		String messageClass = "";
		if ( item != null )
		{
			messageClass = item.getStringValue();
		}

		if (messageClass.equals("IPM.Note")) {
			return new PSTMessage(theFile, folderIndexNode, table, localDescriptorItems);
		} else if (messageClass.equals("IPM.Appointment") ||
				   messageClass.equals("IPM.OLE.CLASS.{00061055-0000-0000-C000-000000000046}") ||
				   messageClass.startsWith("IPM.Schedule.Meeting")) {
			return new PSTAppointment(theFile, folderIndexNode, table, localDescriptorItems);
		} else if (messageClass.equals("IPM.Contact")) {
			return new PSTContact(theFile, folderIndexNode, table, localDescriptorItems);
		} else if (messageClass.equals("IPM.Task")) {
			return new PSTTask(theFile, folderIndexNode, table, localDescriptorItems);
		} else if (messageClass.equals("IPM.Activity")) {
			return new PSTActivity(theFile, folderIndexNode, table, localDescriptorItems);
		} else if (messageClass.equals("IPM.Post.Rss")) {
			return new PSTRss(theFile, folderIndexNode, table, localDescriptorItems);
		} else {
			System.err.println("Unknown message type: "+messageClass);
		}

		return new PSTMessage(theFile, folderIndexNode, table, localDescriptorItems);
	}

	
	public static String guessPSTObjectType(PSTSource theFile, DescriptorIndexNode folderIndexNode)
		throws IOException, PSTException
	{

		PSTTableBC table = new PSTTableBC(new PSTNodeInputStream(theFile, theFile.getOffsetIndexNode(folderIndexNode.dataOffsetIndexIdentifier)));

		// get the table items and look at the types we are dealing with
		Set<Integer> keySet = table.getItems().keySet();
		Iterator<Integer> iterator = keySet.iterator();

		while (iterator.hasNext()) {
			Integer key = iterator.next();
			if (key.intValue() >= 0x0001 &&
				key.intValue() <= 0x0bff)
			{
				return "Message envelope";
			}
			else if (key.intValue() >= 0x1000 &&
					 key.intValue() <= 0x2fff)
			{
				return "Message content";
			}
			else if (key.intValue() >= 0x3400 &&
					 key.intValue() <= 0x35ff)
			{
				return "Message store";
			}
			else if (key.intValue() >= 0x3600 &&
				key.intValue() <= 0x36ff)
			{
				return "Folder and address book";
			}
			else if (key.intValue() >= 0x3700 &&
					key.intValue() <= 0x38ff)
			{
				return "Attachment";
			}
			else if (key.intValue() >= 0x3900 &&
					key.intValue() <= 0x39ff)
			{
				return "Address book";
			}
			else if (key.intValue() >= 0x3a00 &&
					key.intValue() <= 0x3bff)
			{
				return "Messaging user";
			}
			else if (key.intValue() >= 0x3c00 &&
					key.intValue() <= 0x3cff)
			{
				return "Distribution list";
			}
		}
		return "Unknown";
	}

}
