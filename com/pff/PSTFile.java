/**
 * 
 */
package com.pff;
import java.io.*;
import java.util.*;

/**
 * PSTFile is the containing class that allows you to access items within a .pst file.
 * Start here, get the root of the folders and work your way down through your items.
 * @author Richard Johnson
 */
public class PSTFile {

	public static final int ENCRYPTION_TYPE_NONE = 0;
	public static final int ENCRYPTION_TYPE_COMPRESSIBLE = 1;

	private static final int MESSAGE_STORE_DESCRIPTOR_IDENTIFIER = 33;
	private static final int ROOT_FOLDER_DESCRIPTOR_IDENTIFIER = 290;
	
	// Known GUIDs
	// Local IDs first
	public static final int PS_PUBLIC_STRINGS = 0;
	public static final int PSETID_Common = 1;
	public static final int PSETID_Address = 2;
	public static final int PS_INTERNET_HEADERS = 3;
	public static final int PSETID_Appointment = 4;
	public static final int PSETID_Meeting = 5;
	public static final int PSETID_Log = 6;
	public static final int PSETID_Messaging = 7;
	public static final int PSETID_Note = 8;
	public static final int PSETID_PostRss = 9;
	public static final int PSETID_Task = 10;
	public static final int PSETID_UnifiedMessaging = 11;
	public static final int PS_MAPI = 12;
	public static final int PSETID_AirSync = 13;
	public static final int PSETID_Sharing = 14;

	// Now the string guids
	private static final String guidStrings[] =
		{ "00020329-0000-0000-C000-000000000046",
		  "00062008-0000-0000-C000-000000000046",
		  "00062004-0000-0000-C000-000000000046",
		  "00020386-0000-0000-C000-000000000046",
		  "00062002-0000-0000-C000-000000000046",
		  "6ED8DA90-450B-101B-98DA-00AA003F1305",
		  "0006200A-0000-0000-C000-000000000046",
		  "41F28F13-83F4-4114-A584-EEDB5A6B0BFF",
		  "0006200E-0000-0000-C000-000000000046",
		  "00062041-0000-0000-C000-000000000046",
		  "00062003-0000-0000-C000-000000000046",
		  "4442858E-A9E3-4E80-B900-317A210CC15B",
		  "00020328-0000-0000-C000-000000000046",
		  "71035549-0739-4DCB-9163-00F0580DBBDF",
		  "00062040-0000-0000-C000-000000000046" };
	
	private HashMap<UUID, Integer> guidMap = new HashMap<UUID, Integer>();
	
	// the type of encryption the files uses.
	private int encryptionType = 0;
	
	// our all important tree.
	private LinkedHashMap<Integer, HashMap<Integer, DescriptorIndexNode>> childrenDescriptorTree = new LinkedHashMap<Integer, HashMap<Integer, DescriptorIndexNode>>();
	
	private HashMap<Long, Integer> nameToId = new HashMap<Long, Integer>();
	private static HashMap<Integer, Long> idToName = new HashMap<Integer, Long>();
	private byte[] guids = null;
	
	private int itemCount = 0;
	
	private RandomAccessFile in;
	
	/**
	 * constructor
	 * @param fileName
	 * @throws FileNotFoundException
	 * @throws PSTException
	 * @throws IOException
	 */
	public PSTFile(String fileName)
		throws FileNotFoundException, PSTException, IOException
	{
		// attempt to open the file.
		in = new RandomAccessFile(fileName, "r");

		// get the first 4 bytes, should be !BDN
		try {
			byte[] temp = new byte[4];
			in.read(temp);
			String strValue = new String(temp);
			if (!strValue.equals("!BDN")) {
				throw new PSTException("Invalid file header: "+strValue+", expected: !BDN"); 
			}
			
			// make sure we are using a 64bit version of a PST...
			temp = new byte[2];
			in.seek(10);
			in.read(temp);
			if ((temp[0] != 0x15 && temp[0] != 0x17) ||
				temp[1] != 0x00)
			{
	
				throw new PSTException("Only 64bit PST files are supported at this time"); 
			}
			
			// make sure encryption is turned off at this stage...
			in.seek(513);
			encryptionType = in.readByte();
			if (encryptionType == 0x02) {
				throw new PSTException("Only unencrypted and compressable PST files are supported at this time"); 
			}
			
			// process the descriptor tree and create our children
			buildDescriptorTree(in);
			
			// build out name to id map.
			processNameToIdMap(in);
			
		}  catch (IOException err) {
			throw new PSTException("Unable to read PST Sig", err);
		}

	}
	
	/**
	 * read the name-to-id map from the file and load it in
	 * @param in
	 * @throws IOException
	 * @throws PSTException
	 */
	private void processNameToIdMap(RandomAccessFile in)
		throws IOException, PSTException
	{
		// Create our guid map
		for ( int i = 0; i < guidStrings.length; ++i ) {
			UUID uuid = UUID.fromString(guidStrings[i]);
			guidMap.put(uuid, i);
/*
			System.out.printf("guidMap[{%s}] = %d\n", uuid.toString(), i);
/**/
		}
		
		// process the name to id map
		DescriptorIndexNode nameToIdMapDescriptorNode = (PSTObject.getDescriptorIndexNode(in, 97));
		nameToIdMapDescriptorNode.readData(this);

		// get the descriptors if we have them
		HashMap<Integer, PSTDescriptorItem> localDescriptorItems = null;
		if (nameToIdMapDescriptorNode.localDescriptorsOffsetIndexIdentifier != 0) {
			PSTDescriptor descriptor = new PSTDescriptor(this, nameToIdMapDescriptorNode.localDescriptorsOffsetIndexIdentifier);
			localDescriptorItems = descriptor.getChildren();
		}
		
		// process the map
		PSTTableBC bcTable = new PSTTableBC(nameToIdMapDescriptorNode.dataBlock.data, nameToIdMapDescriptorNode.dataBlock.blockOffsets);
		HashMap<Integer, PSTTableBCItem> tableItems = (bcTable.getItems());
		
		// Get the guids
		PSTTableBCItem guidEntry = tableItems.get(2);	// PidTagNameidStreamGuid
		guids = getData(guidEntry, localDescriptorItems);
		int nGuids = guids.length / 16;
		UUID[] uuidArray = new UUID[nGuids];
		int[] uuidIndexes = new int[nGuids];
		int offset = 0;
		for ( int i = 0; i < nGuids; ++i ) {
			long mostSigBits = (PSTObject.convertLittleEndianBytesToLong(guids, offset, offset+4) << 32) |
								(PSTObject.convertLittleEndianBytesToLong(guids, offset+4, offset+6) << 16) |
								PSTObject.convertLittleEndianBytesToLong(guids, offset+6, offset+8);
			long leastSigBits = PSTObject.convertBigEndianBytesToLong(guids, offset+8, offset+16);
			uuidArray[i] = new UUID(mostSigBits, leastSigBits);
			if ( guidMap.containsKey(uuidArray[i]) ) {
				uuidIndexes[i] = guidMap.get(uuidArray[i]);
			} else {
				uuidIndexes[i] = -1;	// We don't know this guid
			}
/*
			System.out.printf("uuidArray[%d] = {%s},%d\n", i, uuidArray[i].toString(), uuidIndexes[i]);
/**/
			offset += 16;
		}
		
		// if we have a reference to an internal descriptor
		PSTTableBCItem mapEntries = tableItems.get(3);	// 
		byte[] nameToIdByte = getData(mapEntries, localDescriptorItems);
		
		// process the entries
		for (int x = 0; x+8 < nameToIdByte.length; x += 8) {
			int dwPropertyId = (int)PSTObject.convertLittleEndianBytesToLong(nameToIdByte, x, x+4);
			int wGuid = (int)PSTObject.convertLittleEndianBytesToLong(nameToIdByte, x+4, x+6);
			int wPropIdx = ((int)PSTObject.convertLittleEndianBytesToLong(nameToIdByte, x+6, x+8));
			if ( (wGuid & 0x0001) == 0 ) {
				wPropIdx += 0x8000;
				wGuid >>= 1;
				int guidIndex;
				if ( wGuid == 1 ) {
					guidIndex = PS_MAPI;
				} else if ( wGuid == 2 ) {
					guidIndex = PS_PUBLIC_STRINGS;
				} else {
					guidIndex = uuidIndexes[wGuid-3];
				}
				nameToId.put((long)dwPropertyId | ((long)guidIndex << 32), wPropIdx);
				idToName.put(wPropIdx, (long)dwPropertyId);
/*
				System.out.printf("0x%08X:%04X, 0x%08X\n", dwPropertyId, guidIndex, wPropIdx);
/**/
			}
			// else the identifier is a string
		}
	}
	
	private byte [] getData(PSTTableItem item, HashMap<Integer, PSTDescriptorItem> localDescriptorItems)
		throws IOException, PSTException
	{
		if ( item.data.length != 0 ) {
			return item.data;
		}

		if ( localDescriptorItems == null ) {
			throw new PSTException("External reference but no localDescriptorItems in PSTFile.getData()");
		}
		
		if ( item.entryValueType != 0x0102 ) {
			throw new PSTException("Attempting to get non-binary data in PSTFile.getData()");
		}

		PSTDescriptorItem mapDescriptorItem = localDescriptorItems.get(item.entryValueReference);
		return mapDescriptorItem.getData();
	}
	
	int getNameToIdMapItem(int key, int propertySetIndex)
	{
		long lKey = ((long)propertySetIndex << 32) | (long)key;
		Integer i = nameToId.get(lKey);
		if ( i == null )
		{
			return -1;
		}
		return i;
	}


	static long getNameToIdMapKey(int id)
		//throws PSTException
	{
		Long i = idToName.get(id);
		if ( i == null )
		{
			//throw new PSTException("Name to Id mapping not found");
			return -1;
		}
		return i;
	}

	static private Properties propertyNames = null;
	static private boolean bFirstTime = true;
	
	static String getPropertyName(int propertyId, boolean bNamed) {
		if ( bFirstTime ) {
			bFirstTime = false;
			propertyNames = new Properties();
			try {
				InputStream propertyStream = PSTFile.class.getResourceAsStream("/PropertyNames.txt");
				if ( propertyStream != null ) {
					propertyNames.load(propertyStream);
				} else {
					propertyNames = null;
				}
			} catch (FileNotFoundException e) {
				propertyNames = null;
				e.printStackTrace();
			} catch (IOException e) {
				propertyNames = null;
				e.printStackTrace();
			}
		}

		if ( propertyNames != null ) {
			String key = String.format((bNamed ? "%08X" : "%04X"), propertyId);
			return propertyNames.getProperty(key);
		}

		return null;
	}

	static String getPropertyDescription(int entryType, int entryValueType) {
		String ret = "";
		if ( entryType < 0x8000 ) {
			String name = PSTFile.getPropertyName(entryType, false);
			if ( name != null ) {
				ret = String.format("%s:%04X: ", name, entryValueType);
			} else {
				ret = String.format("0x%04X:%04X: ", entryType, entryValueType);
			}
		} else {
			long type = PSTFile.getNameToIdMapKey(entryType);
			if ( type == -1 ) {
				ret = String.format("0xFFFF(%04X):%04X: ", entryType, entryValueType);
			} else {
				String name = PSTFile.getPropertyName((int)type, true);
				if ( name != null ) {
					ret = String.format("%s(%04X):%04X: ", name, entryType, entryValueType);
				} else {
					ret = String.format("0x%04X(%04X):%04X: ", type, entryType, entryValueType);
				}
			}
		}

		return ret;
	}

	/**
	 * destructor just closes the file handle...
	 */
	protected void finalize()
		throws IOException
	{
		in.close();
	}
	
	/**
	 * get the type of encryption the file uses
	 * @return encryption type used in the PST File
	 */
	public int getEncryptionType() {
		return this.encryptionType;
	}
	
	/**
	 * get the handle to the file we are currently accessing
	 */
	public RandomAccessFile getFileHandle() {
		return this.in;
	}
	
	/**
	 * Build the children descriptor tree
	 * This goes through the entire descriptor B-Tree and adds every item to the childrenDescriptorTree.
	 * Was looking for an existing data structure in the PST file for this, but apparently they don't exist! 
	 * @param in
	 * @throws IOException
	 * @throws PSTException
	 */
	private void buildDescriptorTree(RandomAccessFile in)
		throws IOException, PSTException
	{
		long btreeStartOffset = PSTObject.extractLEFileOffset(in, 224);
		processTree(in, btreeStartOffset);
	}
	
	/**
	 * Recursive function for building the descriptor tree, used by buildDescriptorTree
	 * @param in
	 * @param btreeStartOffset
	 * @throws IOException
	 * @throws PSTException
	 */
	private void processTree(RandomAccessFile in, long btreeStartOffset)
		throws IOException, PSTException
	{
		byte[] temp = new byte[2];
		in.seek(btreeStartOffset+496);
		in.read(temp);
		
		if ((temp[0] == 0xffffff81 && temp[1] == 0xffffff81)) {
			
			in.seek(btreeStartOffset+488);

			int numberOfItems = in.read();
			in.read(); // maxNumberOfItems
			in.read(); // itemSize
			int levelsToLeaf = in.read();
			
			if (levelsToLeaf > 0) {
				
				for (int x = 0; x < numberOfItems; x++) {
					long branchNodeItemStartIndex = (btreeStartOffset + (24*x));
					long nextLevelStartsAt =  PSTObject.extractLEFileOffset(in, branchNodeItemStartIndex+16);
					processTree(in, nextLevelStartsAt);
				}
			}
			else
			{
				for (int x = 0; x < numberOfItems; x++) {
					// The 64-bit descriptor index b-tree leaf node item
					// give me the offset index please!
					in.seek(btreeStartOffset + (x * 32));
					temp = new byte[32];
					in.read(temp);
					
					DescriptorIndexNode tempNode = new DescriptorIndexNode(temp);
					
					// we don't want to be children of ourselves...
					if (tempNode.parentDescriptorIndexIdentifier == tempNode.descriptorIdentifier) {
						// skip!
					} else if (childrenDescriptorTree.containsKey(tempNode.parentDescriptorIndexIdentifier)) {
						// add this entry to the existing list of children
						LinkedHashMap<Integer, DescriptorIndexNode> children =
							(LinkedHashMap<Integer, DescriptorIndexNode>)
							childrenDescriptorTree.get(tempNode.parentDescriptorIndexIdentifier);
						children.put(tempNode.descriptorIdentifier, tempNode);
					} else {
						// create a new entry and add this one to that
						LinkedHashMap<Integer, DescriptorIndexNode> children = new LinkedHashMap<Integer, DescriptorIndexNode>();
						children.put(tempNode.descriptorIdentifier, tempNode);
						childrenDescriptorTree.put(tempNode.parentDescriptorIndexIdentifier, children);
					}
					
					this.itemCount++;

				}
			}
		}
		else
		{
			PSTObject.printHexFormatted(temp, true);
			throw new PSTException("Unable to read descriptor node, is not a descriptor");
		}
	}

	/**
	 * get the child item descriptors for a specific descriptor
	 * @return
	 */
	LinkedHashMap<Integer, DescriptorIndexNode> getChildrenDescriptors(int descriptorIdentifier)
	{
		if (!this.childrenDescriptorTree.containsKey(descriptorIdentifier)) {
			return new LinkedHashMap<Integer, DescriptorIndexNode>();
		}
		return (LinkedHashMap<Integer, DescriptorIndexNode>)this.childrenDescriptorTree.get(descriptorIdentifier);
	}
	
	/**
	 * get the message store of the PST file.
	 * Note that this doesn't really have much information, better to look under the root folder
	 * @throws PSTException
	 * @throws IOException
	 */
	public PSTMessageStore getMessageStore()
		throws PSTException, IOException
	{
		DescriptorIndexNode messageStoreDescriptor = PSTObject.getDescriptorIndexNode(in, MESSAGE_STORE_DESCRIPTOR_IDENTIFIER);
		return new PSTMessageStore(this, messageStoreDescriptor);
	}

	/**
	 * get the root folder for the PST file.
	 * You should find all of your data under here...
	 * @throws PSTException
	 * @throws IOException
	 */
	public PSTFolder getRootFolder()
		throws PSTException, IOException
	{
		DescriptorIndexNode rootFolderDescriptor = PSTObject.getDescriptorIndexNode(in, ROOT_FOLDER_DESCRIPTOR_IDENTIFIER);
		PSTFolder output = new PSTFolder(this, rootFolderDescriptor);
		return output;
	}
	
	
	class PSTFileBlock {
		byte[]	data = null;
		int[]	blockOffsets = null;
	}
	
	public PSTFileBlock readLeaf(long bid)
		throws IOException, PSTException
	{
		PSTFileBlock ret = new PSTFileBlock();

		// get the index node for the descriptor index
		OffsetIndexItem offsetItem = PSTObject.getOffsetIndexNode(in, bid);
		boolean bInternal = (offsetItem.indexIdentifier & 0x02) != 0;

		ret.data = new byte[offsetItem.size];
		in.seek(offsetItem.fileOffset);
		in.read(ret.data);
		
		if ( bInternal &&
			 offsetItem.size >= 8 &&
			 ret.data[0] == 1 )
		{
			// (X)XBLOCK
			if ( ret.data[1] == 2 ) {
				throw new PSTException("XXBLOCKS not supported yet!");
			}

			ret.blockOffsets = PSTObject.getBlockOffsets(in, ret.data);
			ret.data = PSTObject.processArray(in, ret.data);
			bInternal = false;
		}

		// (Internal blocks aren't compressed)
		if ( !bInternal &&
			 encryptionType == PSTFile.ENCRYPTION_TYPE_COMPRESSIBLE)
		{
			ret.data = PSTObject.decode(ret.data);
		}
		
		return ret;
	}
	
	
	public int getLeafSize(long bid)
		throws IOException, PSTException
	{
		OffsetIndexItem offsetItem = PSTObject.getOffsetIndexNode(in, bid);

		// Internal block?
		if ( (offsetItem.indexIdentifier & 0x02) == 0 ) {
			// No, return the raw size
			return offsetItem.size;
		}
	
		// we only need the first 8 bytes
		byte[] data = new byte[8];
		in.seek(offsetItem.fileOffset);
		in.read(data);
	
		// we are an array, get the sum of the sizes...
		return (int)PSTObject.convertLittleEndianBytesToLong(data, 4, 8);
	}

}
