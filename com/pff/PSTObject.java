package com.pff;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * PST Object is the root class of all PST Items.
 * It also provides a number of static utility functions.  The most important of which is the
 * detectAndLoadPSTObject call which allows extraction of a PST Item from the file.
 * @author Richard Johnson
 */
public class PSTObject {
	
	protected PSTFile pstFile;
	protected byte[] data;
	protected DescriptorIndexNode descriptorIndexNode;
	protected HashMap<Integer, PSTTableBCItem> items;
	protected HashMap<Integer, PSTDescriptorItem> localDescriptorItems = null;
	
	protected LinkedHashMap<String, HashMap<DescriptorIndexNode, PSTObject>> children;
	
	protected PSTObject(PSTFile theFile, DescriptorIndexNode descriptorIndexNode)
		throws PSTException, IOException
	{
		this.pstFile = theFile;
		this.descriptorIndexNode = descriptorIndexNode;
		
		// we need to process ourselves, that means:
		
		// get the index node for the descriptor index
		RandomAccessFile in = theFile.getFileHandle();
		OffsetIndexItem offsetItem = PSTObject.getOffsetIndexNode(in, descriptorIndexNode.dataOffsetIndexIdentifier);

		// process the table, obtaining our information and setting internal vars.
		data = new byte[offsetItem.size];
		in.seek(offsetItem.fileOffset);
		in.read(data);
		
		if (pstFile.getEncryptionType() == PSTFile.ENCRYPTION_TYPE_COMPRESSIBLE) {
			data = PSTObject.decode(data);
		}
		
		//PSTObject.printHexFormatted(data, true);
		
		PSTTableBC table = new PSTTableBC(data);
		//System.out.println(table);
		this.items = table.getItems();
		
		if (descriptorIndexNode.localDescriptorsOffsetIndexIdentifier != 0) {
			PSTDescriptor descriptor = new PSTDescriptor(theFile, descriptorIndexNode.localDescriptorsOffsetIndexIdentifier);
			localDescriptorItems = descriptor.getChildren();
		}
	}
	
	/**
	 * for pre-population
	 * @param theFile
	 * @param folderIndexNode
	 * @param table
	 */
	protected PSTObject(PSTFile theFile, DescriptorIndexNode folderIndexNode, PSTTableBC table, HashMap<Integer, PSTDescriptorItem> localDescriptorItems) {
		this.pstFile = theFile;
		this.descriptorIndexNode = folderIndexNode;
		this.items = table.getItems();
		this.localDescriptorItems = localDescriptorItems;
	}
	
	
	
	public DescriptorIndexNode getDescriptorNode() {
		return this.descriptorIndexNode;
	}
	
	
	protected int getIntItem(int identifier) {
		return getIntItem(identifier, 0);
	}
	protected int getIntItem(int identifier, int defaultValue) {
		if (this.items.containsKey(identifier)) {
			PSTTableBCItem item = (PSTTableBCItem)this.items.get(identifier);
			return item.entryValueReference;
		}
		return defaultValue;
	}
	

	protected double getDoubleItem(int identifier) {
		return getDoubleItem(identifier, 0);
	}
	protected double getDoubleItem(int identifier, double defaultValue) {
		if (this.items.containsKey(identifier)) {
			PSTTableBCItem item = (PSTTableBCItem)this.items.get(identifier);
			long longVersion = PSTObject.convertLittleEndianBytesToLong(item.data);
			return Double.longBitsToDouble(longVersion);
		}
		return defaultValue;
	}
	
	
	protected long getLongItem(int identifier) {
		return getLongItem(0);
	}
	protected long getLongItem(int identifier, long defaultValue) {
		if (this.items.containsKey(identifier)) {
			PSTTableBCItem item = (PSTTableBCItem)this.items.get(identifier);
			if (item.entryType == 0x0003) {
				// we are really just an int
				return item.entryValueReference;
			} else {
				// we are a long
				// don't really know what to do with this yet
				PSTObject.convertLittleEndianBytesToLong(item.data);
			}
		}
		return defaultValue;
	}
	
	protected String getStringItem(int identifier) {
		return getStringItem(identifier, 0);
	}
	protected String getStringItem(int identifier, int stringType) {
		if (this.items.containsKey(identifier)) {
			PSTTableBCItem item = (PSTTableBCItem)this.items.get(identifier);
			
			// see if there is a descriptor entry
			if (item.isExternalValueReference &&
				this.localDescriptorItems != null &&
				this.localDescriptorItems.containsKey(item.entryValueReference))
			{
				// we have a hit!
				PSTDescriptorItem descItem = (PSTDescriptorItem)this.localDescriptorItems.get(item.entryValueReference);
				
//				PSTObject.printHexFormatted(descItem.data, true);
				
				// and we want a string.
				if (stringType == 0) {
					return descItem.getStringValue(item.entryValueType);
				} else {
					return descItem.getStringValue(stringType);
				}
			}
			
			return item.getStringValue();
		}
		return "";
	}
	
	protected Date getDateItem(int identifier) {
		if (this.items.containsKey(identifier)) {
			PSTTableBCItem item = (PSTTableBCItem)this.items.get(identifier);
			if (item.data.length == 0 ) {
				return new Date(0);
			}
			int high = (int)PSTObject.convertLittleEndianBytesToLong(item.data, 4, 8);
			int low = (int)PSTObject.convertLittleEndianBytesToLong(item.data, 0, 4);
			 
			return PSTObject.filetimeToDate(high, low);
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
	 * Static stuff below
	 * ------------------
	 */

	// substitution table for the compressible encryption type.
	private static int[] compEnc = {
	    0x47, 0xf1, 0xb4, 0xe6, 0x0b, 0x6a, 0x72, 0x48, 0x85, 0x4e, 0x9e, 0xeb, 0xe2, 0xf8, 0x94, 0x53,
	    0xe0, 0xbb, 0xa0, 0x02, 0xe8, 0x5a, 0x09, 0xab, 0xdb, 0xe3, 0xba, 0xc6, 0x7c, 0xc3, 0x10, 0xdd,
	    0x39, 0x05, 0x96, 0x30, 0xf5, 0x37, 0x60, 0x82, 0x8c, 0xc9, 0x13, 0x4a, 0x6b, 0x1d, 0xf3, 0xfb,
	    0x8f, 0x26, 0x97, 0xca, 0x91, 0x17, 0x01, 0xc4, 0x32, 0x2d, 0x6e, 0x31, 0x95, 0xff, 0xd9, 0x23,
	    0xd1, 0x00, 0x5e, 0x79, 0xdc, 0x44, 0x3b, 0x1a, 0x28, 0xc5, 0x61, 0x57, 0x20, 0x90, 0x3d, 0x83,
	    0xb9, 0x43, 0xbe, 0x67, 0xd2, 0x46, 0x42, 0x76, 0xc0, 0x6d, 0x5b, 0x7e, 0xb2, 0x0f, 0x16, 0x29,
	    0x3c, 0xa9, 0x03, 0x54, 0x0d, 0xda, 0x5d, 0xdf, 0xf6, 0xb7, 0xc7, 0x62, 0xcd, 0x8d, 0x06, 0xd3,
	    0x69, 0x5c, 0x86, 0xd6, 0x14, 0xf7, 0xa5, 0x66, 0x75, 0xac, 0xb1, 0xe9, 0x45, 0x21, 0x70, 0x0c,
	    0x87, 0x9f, 0x74, 0xa4, 0x22, 0x4c, 0x6f, 0xbf, 0x1f, 0x56, 0xaa, 0x2e, 0xb3, 0x78, 0x33, 0x50,
	    0xb0, 0xa3, 0x92, 0xbc, 0xcf, 0x19, 0x1c, 0xa7, 0x63, 0xcb, 0x1e, 0x4d, 0x3e, 0x4b, 0x1b, 0x9b,
	    0x4f, 0xe7, 0xf0, 0xee, 0xad, 0x3a, 0xb5, 0x59, 0x04, 0xea, 0x40, 0x55, 0x25, 0x51, 0xe5, 0x7a,
	    0x89, 0x38, 0x68, 0x52, 0x7b, 0xfc, 0x27, 0xae, 0xd7, 0xbd, 0xfa, 0x07, 0xf4, 0xcc, 0x8e, 0x5f,
	    0xef, 0x35, 0x9c, 0x84, 0x2b, 0x15, 0xd5, 0x77, 0x34, 0x49, 0xb6, 0x12, 0x0a, 0x7f, 0x71, 0x88,
	    0xfd, 0x9d, 0x18, 0x41, 0x7d, 0x93, 0xd8, 0x58, 0x2c, 0xce, 0xfe, 0x24, 0xaf, 0xde, 0xb8, 0x36,
	    0xc8, 0xa1, 0x80, 0xa6, 0x99, 0x98, 0xa8, 0x2f, 0x0e, 0x81, 0x65, 0x73, 0xe4, 0xc2, 0xa2, 0x8a,
	    0xd4, 0xe1, 0x11, 0xd0, 0x08, 0x8b, 0x2a, 0xf2, 0xed, 0x9a, 0x64, 0x3f, 0xc1, 0x6c, 0xf9, 0xec
	};
	
	/**
	 * Output a dump of data in hex format in the order it was read in
	 * @param data
	 * @param pretty
	 */
	protected static void printHexFormatted(byte[] data, boolean pretty) {
		printHexFormatted(data,pretty, new int[0]);
	}
	protected static void printHexFormatted(byte[] data, boolean pretty, int[] indexes) {
		// groups of two
		if (pretty) { System.out.println("---"); }
		long tmpLongValue;
		String line = "";
		int nextIndex = 0;
		int indexIndex = 0;
		if (indexes.length > 0) {
			nextIndex = indexes[0];
			indexIndex++;
		}
		for (int x = 0; x < data.length; x++) {
			tmpLongValue = (long)data[x] & 0xff;
			
			if (indexes.length > 0 &&
				x == nextIndex &&
				nextIndex < data.length)
			{
				System.out.print("+");
				line += "+";
				while (indexIndex < indexes.length-1 && indexes[indexIndex] <= nextIndex) 
				{
					indexIndex++;
				}
				nextIndex = indexes[indexIndex];
				//indexIndex++;
			}
			
			if (Character.isLetterOrDigit((char)tmpLongValue)) {
				line += (char)tmpLongValue;
			}
			else
			{
				line += ".";
			}
			
			if (Long.toHexString(tmpLongValue).length() < 2) {
				System.out.print("0");
			}
			System.out.print(Long.toHexString(tmpLongValue));
			if (x % 2 == 1 && pretty) {
				System.out.print(" ");
			}
			if (x % 16 == 15 && pretty) {
				System.out.print(" "+line);
				System.out.println("");
				line = "";
			}
		}
		if (pretty) { System.out.println(" "+line); System.out.println("---"); System.out.println(data.length); } else {  }
	}
	

	/**
	 * navigate the internal descriptor B-Tree and find a specific item
	 * @param in
	 * @param identifier
	 * @return the descriptor node for the item
	 * @throws IOException
	 * @throws PSTException
	 */
	protected static DescriptorIndexNode getDescriptorIndexNode(RandomAccessFile in, long identifier)
		throws IOException, PSTException
	{
		return new DescriptorIndexNode(findBtreeItem(in, identifier, true));
	}
	
	/**
	 * navigate the internal index B-Tree and find a specific item
	 * @param in
	 * @param identifier
	 * @return the offset index item
	 * @throws IOException
	 * @throws PSTException
	 */
	protected static OffsetIndexItem getOffsetIndexNode(RandomAccessFile in, long identifier)
		throws IOException, PSTException
	{
		return new OffsetIndexItem(findBtreeItem(in, identifier, false));
	}
	
	/**
	 * Generic function used by getOffsetIndexNode and getDescriptorIndexNode for navigating the PST B-Trees
	 * @param in
	 * @param index
	 * @param descTree
	 * @return
	 * @throws IOException
	 * @throws PSTException
	 */
	private static byte[] findBtreeItem(RandomAccessFile in, long index, boolean descTree)
		throws IOException, PSTException
	{
		
		// first find the starting point for the offset index
		long btreeStartOffset = extractLEFileOffset(in, 240);
		if (descTree) {
			btreeStartOffset = extractLEFileOffset(in, 224);
		}
		
		// okay, what we want to do is navigate the tree until you reach the bottom....
		// try and read the index b-tree
		byte[] temp = new byte[2];
		in.seek(btreeStartOffset+496);
		in.read(temp);
		while	((temp[0] == 0xffffff80 && temp[1] == 0xffffff80 && !descTree) ||
				 (temp[0] == 0xffffff81 && temp[1] == 0xffffff81 && descTree))
		{
			
			// get the rest of the data....
			byte[] branchNodeItems = new byte[488];
			in.seek(btreeStartOffset);
			in.read(branchNodeItems);
			
			int numberOfItems = in.read();
			in.read(); // maxNumberOfItems
			in.read(); // itemSize
			int levelsToLeaf = in.read();
			
			if (levelsToLeaf > 0) {
				boolean found = false;
				for (int x = 0; x < numberOfItems; x++) {
					long indexIdOfFirstChildNode = extractLEFileOffset(in, btreeStartOffset + (x * 24));
					if (indexIdOfFirstChildNode > index) {
						// get the address for the child first node in this group
						btreeStartOffset = extractLEFileOffset(in, btreeStartOffset+((x-1) * 24)+16);
						in.seek(btreeStartOffset+496);
						in.read(temp);
						found = true;
						break;
					}
				}
				if (!found) {
					// it must be in the very last branch...
					btreeStartOffset = extractLEFileOffset(in, btreeStartOffset+((numberOfItems-1) * 24)+16);
					in.seek(btreeStartOffset+496);
					in.read(temp);
				}
			}
			else
			{
				// we are at the bottom of the tree...
				// we want to get our file offset!
				for (int x = 0; x < numberOfItems; x++) {
					if (descTree)
					{
						// The 64-bit descriptor index b-tree leaf node item
						in.seek(btreeStartOffset + (x * 32));
						
						temp = new byte[4];
						in.read(temp);
						if (convertLittleEndianBytesToLong(temp) == index) {
							// give me the offset index please!
							in.seek(btreeStartOffset + (x * 32));
							temp = new byte[32];
							in.read(temp);
							return temp;			
						}
					}
					else
					{
						// The 64-bit (file) offset index item
						long indexIdOfFirstChildNode = extractLEFileOffset(in, btreeStartOffset + (x * 24));
						
						if (indexIdOfFirstChildNode == index) {
							// we found it!!!! OMG
							//System.out.println("item found as item #"+x);
							in.seek(btreeStartOffset + (x * 24));

							temp = new byte[24];
							in.read(temp);
							return temp;
						}
					}
				}
				throw new PSTException("Unable to find "+index);
			}
		}
		
		throw new PSTException("Unable to find node: "+index);
	}
	
	/**
	 * decode a lump of data that has been encrypted with the compressible encryption
	 * @param data
	 * @return decoded data
	 */
	protected static byte[] decode(byte[] data) {
		int temp;
		for (int x = 0; x < data.length; x++) {
			temp = data[x] & 0xff;
			data[x] = (byte)compEnc[temp];
		}
		
		return data;
	}
	

	protected static byte[] encode(byte[] data) {
		// create the encoding array...
		int[] enc = new int[compEnc.length];
		for (int x = 0; x < enc.length; x++) {
			enc[compEnc[x]] = x;
		}
		
		// now it's just the same as decode...
		int temp;
		for (int x = 0; x < data.length; x++) {
			temp = data[x] & 0xff;
			data[x] = (byte)enc[temp];
		}
		
		return data;
	}

	/**
	 * Read a file offset from the file
	 * PST Files have this tendency to store file offsets (pointers) in 8 little endian bytes.
	 * Convert this to a long for seeking to.
	 * @param in handle for PST file
	 * @param startOffset where to read the 8 bytes from
	 * @return long representing the read location
	 * @throws IOException
	 */
	protected static long extractLEFileOffset(RandomAccessFile in, long startOffset)
		throws IOException
	{
		in.seek(startOffset);
		byte[] temp = new byte[8];
		in.read(temp);
		long offset = temp[7] & 0xff;
		long tmpLongValue;
		for (int x = 6; x >= 0; x--) {
			offset = offset << 8;
			tmpLongValue = (long)temp[x] & 0xff;
			offset |= tmpLongValue;
		}
		
		return offset;
	}
	
	/**
	 * Utility function for converting little endian bytes into a usable java long
	 * @param data
	 * @return long version of the data
	 */
	protected static long convertLittleEndianBytesToLong(byte[] data) {
		return convertLittleEndianBytesToLong(data, 0, data.length);
	}
	/**
	 * Utility function for converting little endian bytes into a usable java long
	 * @param data
	 * @param start
	 * @param end
	 * @return long version of the data
	 */
	protected static long convertLittleEndianBytesToLong(byte[] data, int start, int end) {
		
		long offset = data[end-1] & 0xff;
		long tmpLongValue;
		for (int x = end-2; x >= start; x--) {
			offset = offset << 8;
			tmpLongValue = (long)data[x] & 0xff;
			offset |= tmpLongValue;
		}
		
		return offset;
	}
	
	protected static boolean isPSTArray(byte[] data) {
		return (data[0] == 1 && data[1] == 1);
	}
	
	protected static byte[] processArray(RandomAccessFile in, byte[] data)
		throws IOException, PSTException
	{
		// is the data an array?
		if (!PSTObject.isPSTArray(data))
		{
			throw new PSTException("Unable to process array, does not appear to be one!");
		}
		// we are an array!
		// get the array items and merge them together
		int numberOfEntries = (int)PSTObject.convertLittleEndianBytesToLong(data, 2, 4);
		int dataSize = (int)PSTObject.convertLittleEndianBytesToLong(data, 4, 8);
		byte[] tableOutput = new byte[dataSize];
		int tableOutputIndex = 0;
		int tableOffset = 8;
		for (int y = 0; y < numberOfEntries; y++) {
			// get the offset identifier
			long tableOffsetIdentifierIndex = PSTObject.convertLittleEndianBytesToLong(data, tableOffset, tableOffset+8);
			
			// clear the last bit of the identifier.  Why so hard?
			tableOffsetIdentifierIndex = (tableOffsetIdentifierIndex & 0xfffffffe);
			
			OffsetIndexItem tableOffsetIdentifier = PSTObject.getOffsetIndexNode(in, tableOffsetIdentifierIndex);
//			System.out.println(tableOffsetIdentifier);
			byte[] tempTableOutput = new byte[tableOffsetIdentifier.size];
			in.seek(tableOffsetIdentifier.fileOffset);
			in.read(tempTableOutput);
			System.arraycopy(tempTableOutput, 0, tableOutput, tableOutputIndex, tableOffsetIdentifier.size);
			tableOutputIndex += tableOffsetIdentifier.size;
			tableOffset += 8;
		}
		// replace the item data with the stuff from the array...
		return tableOutput;
	}
	
	protected static int[] getBlockOffsets(RandomAccessFile in, byte[] data)
		throws IOException, PSTException
	{
		// is the data an array?
		if (!PSTObject.isPSTArray(data))
		{
			throw new PSTException("Unable to process array, does not appear to be one!");
		}
		// we are an array!
		// get the array items and merge them together
		int numberOfEntries = (int)PSTObject.convertLittleEndianBytesToLong(data, 2, 4);
		int[] output = new int[numberOfEntries];
		int tableOffset = 8;
		for (int y = 0; y < numberOfEntries; y++) {
			// get the offset identifier
			long tableOffsetIdentifierIndex = PSTObject.convertLittleEndianBytesToLong(data, tableOffset, tableOffset+8);
			// clear the last bit of the identifier.  Why so hard?
			tableOffsetIdentifierIndex = (tableOffsetIdentifierIndex & 0xfffffffe);
			OffsetIndexItem tableOffsetIdentifier = PSTObject.getOffsetIndexNode(in, tableOffsetIdentifierIndex);
			output[y] = tableOffsetIdentifier.size;
			tableOffset += 8;
		}
		// replace the item data with the stuff from the array...
		return output;
	}
	
	public static PSTObject detectAndLoadPSTObject(PSTFile theFile, DescriptorIndexNode folderIndexNode)
		throws IOException, PSTException
	{
		
		// get the index node for the descriptor index
		RandomAccessFile in = theFile.getFileHandle();
		OffsetIndexItem offsetItem = PSTObject.getOffsetIndexNode(in, folderIndexNode.dataOffsetIndexIdentifier);

		// process the table, obtaining our information and setting internal vars.
		byte[] data = new byte[offsetItem.size];
		in.seek(offsetItem.fileOffset);
		in.read(data);
		
		if (theFile.getEncryptionType() == PSTFile.ENCRYPTION_TYPE_COMPRESSIBLE) {
			data = PSTObject.decode(data);
		}
		
		PSTTableBC table = new PSTTableBC(data);
		// get the table items and look at the types we are dealing with
		Set<Integer> keySet = table.getItems().keySet();
		Iterator<Integer> iterator = keySet.iterator();
		
		String type = "";
		
		while (iterator.hasNext()) {
			Integer key = (Integer)iterator.next();
			if (key.intValue() >= 0x0001 &&
				key.intValue() <= 0x0bff)
			{
				type = "Message envelope";
				break;
			}
			/*else if (key.intValue() >= 0x1000 &&
					 key.intValue() <= 0x2fff)
			{
				type = "Message content";
				break;
			}*/
			else if (key.intValue() >= 0x3400 &&
					 key.intValue() <= 0x35ff)
			{
				type = "Message store";
				break;
			}
			else if (key.intValue() >= 0x3600 &&
				key.intValue() <= 0x36ff)
			{
				type = "Folder and address book";
				break;
			}
			/*else if (key.intValue() >= 0x3700 &&
					key.intValue() <= 0x38ff)
			{
				type = "Attachment";
				break;
			}*/
			else if (key.intValue() >= 0x3900 &&
					key.intValue() <= 0x39ff)
			{
				type = "Address book";
				break;
			}
			/*else if (key.intValue() >= 0x3a00 &&
					key.intValue() <= 0x3bff)
			{
				type = "Messaging user";
				break;
			}*/
			else if (key.intValue() >= 0x3c00 &&
					key.intValue() <= 0x3cff)
			{
				type = "Distribution list";
				break;
			}
		}
		
		HashMap<Integer, PSTDescriptorItem> localDescriptorItems = null;
		
		if (folderIndexNode.localDescriptorsOffsetIndexIdentifier != 0) {
			PSTDescriptor descriptor = new PSTDescriptor(theFile, folderIndexNode.localDescriptorsOffsetIndexIdentifier);
			localDescriptorItems = descriptor.getChildren();
		}
		
//		System.out.println(type);
//		System.out.println(table);
		
		if (type.equals("Folder and address book")) {
			return new PSTFolder(theFile, folderIndexNode, table, localDescriptorItems);
		} else if (type.equals("Message envelope")) {
			PSTMessage message = new PSTMessage(theFile, folderIndexNode, table, localDescriptorItems);
			if (message.getMessageClass().equals("IPM.Note")) {
				return message;
			} else if (message.getMessageClass().equals("IPM.Appointment")) {
				return new PSTAppointment(theFile, folderIndexNode, table, localDescriptorItems);
			} else if (message.getMessageClass().equals("IPM.Contact")) {
				return new PSTContact(theFile, folderIndexNode, table, localDescriptorItems);
			} else if (message.getMessageClass().equals("IPM.Task")) {
				return new PSTTask(theFile, folderIndexNode, table, localDescriptorItems);
			} else if (message.getMessageClass().equals("IPM.Activity")) {
				return new PSTActivity(theFile, folderIndexNode, table, localDescriptorItems);
			} else if (message.getMessageClass().equals("IPM.Post.Rss")) {
				return new PSTRss(theFile, folderIndexNode, table, localDescriptorItems);
			} else {
				System.out.println("some kind of message: "+message.getMessageClass());
			}
			
			return message;
		}
		else
		{
//			System.out.println(table);
//			return message;
			
			throw new PSTException("Unknown child type: "+type+" - "+folderIndexNode.localDescriptorsOffsetIndexIdentifier);
			//System.out.println("Unknown child type: "+type);
//			return null;
		}
		
	}
	
	/**
	 * the code below was taken from a random apache project
	 * http://www.koders.com/java/fidA9D4930E7443F69F32571905DD4CA01E4D46908C.aspx
	 * my bit-shifting isn't that 1337
	 */
	
	/**
     * <p>The difference between the Windows epoch (1601-01-01
     * 00:00:00) and the Unix epoch (1970-01-01 00:00:00) in
     * milliseconds: 11644473600000L. (Use your favorite spreadsheet
     * program to verify the correctness of this value. By the way,
     * did you notice that you can tell from the epochs which
     * operating system is the modern one? :-))</p>
     */
    private static final long EPOCH_DIFF = 11644473600000L;
	
	/**
     * <p>Converts a Windows FILETIME into a {@link Date}. The Windows
     * FILETIME structure holds a date and time associated with a
     * file. The structure identifies a 64-bit integer specifying the
     * number of 100-nanosecond intervals which have passed since
     * January 1, 1601. This 64-bit value is split into the two double
     * words stored in the structure.</p>
     *
     * @param high The higher double word of the FILETIME structure.
     * @param low The lower double word of the FILETIME structure.
     * @return The Windows FILETIME as a {@link Date}.
     */
    protected static Date filetimeToDate(final int high, final int low)
    {
        final long filetime = ((long) high) << 32 | (low & 0xffffffffL);
        final long ms_since_16010101 = filetime / (1000 * 10);
        final long ms_since_19700101 = ms_since_16010101 - EPOCH_DIFF;
        return new Date(ms_since_19700101);
    }

}
