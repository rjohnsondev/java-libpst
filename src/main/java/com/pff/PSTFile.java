/**
 * Copyright 2010 Richard Johnson & Orin Eman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with java-libpst. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.pff;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.UUID;

/**
 * PSTFile is the containing class that allows you to access items within a .pst
 * file.
 * Start here, get the root of the folders and work your way down through your
 * items.
 * 
 * @author Richard Johnson
 */
public class PSTFile {

    public static final int ENCRYPTION_TYPE_NONE = 0;
    public static final int ENCRYPTION_TYPE_COMPRESSIBLE = 1;

    private static final int MESSAGE_STORE_DESCRIPTOR_IDENTIFIER = 33;
    private static final int ROOT_FOLDER_DESCRIPTOR_IDENTIFIER = 290;

    public static final int PST_TYPE_ANSI = 14;
    protected static final int PST_TYPE_ANSI_2 = 15;
    public static final int PST_TYPE_UNICODE = 23;
    public static final int PST_TYPE_2013_UNICODE = 36;

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
    private static final String guidStrings[] = {
        "00020329-0000-0000-C000-000000000046",
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
        "00062040-0000-0000-C000-000000000046"};

    private final HashMap<UUID, Integer> guidMap = new HashMap<>();

    // the type of encryption the files uses.
    private int encryptionType = 0;

    // our all important tree.
    private LinkedHashMap<Integer, LinkedList<DescriptorIndexNode>> childrenDescriptorTree = null;

    private final HashMap<Long, Integer> nameToId = new HashMap<>();
    private final HashMap<String, Integer> stringToId = new HashMap<>();
    private static HashMap<Integer, Long> idToName = new HashMap<>();
    private final HashMap<Integer, String> idToString = new HashMap<>();
    private byte[] guids = null;

    private int itemCount = 0;

    private final PSTFileContent in;

    /**
     * constructor
     * 
     * @param fileName
     * @throws FileNotFoundException
     * @throws PSTException
     * @throws IOException
     */
    public PSTFile(final String fileName) throws FileNotFoundException, PSTException, IOException {
        this(new File(fileName));
    }

    public PSTFile(final File file) throws FileNotFoundException, PSTException, IOException {
        this(new PSTRAFileContent(file));
    }

    public PSTFile(final byte[] bytes) throws FileNotFoundException, PSTException, IOException {
        this(new PSTByteFileContent(bytes));
    }

    public PSTFile(final PSTFileContent content) throws FileNotFoundException, PSTException, IOException {
        // attempt to open the file.
        this.in = content;

        // get the first 4 bytes, should be !BDN
        try {
            final byte[] temp = new byte[4];
            this.in.readCompletely(temp);
            final String strValue = new String(temp);
            if (!strValue.equals("!BDN")) {
                throw new PSTException("Invalid file header: " + strValue + ", expected: !BDN");
            }

            // make sure we are using a supported version of a PST...
            final byte[] fileTypeBytes = new byte[2];
            this.in.seek(10);
            this.in.readCompletely(fileTypeBytes);
            // ANSI file types can be 14 or 15:
            if (fileTypeBytes[0] == PSTFile.PST_TYPE_ANSI_2) {
                fileTypeBytes[0] = PSTFile.PST_TYPE_ANSI;
            }
            if (fileTypeBytes[0] != PSTFile.PST_TYPE_ANSI && fileTypeBytes[0] != PSTFile.PST_TYPE_UNICODE
                && fileTypeBytes[0] != PSTFile.PST_TYPE_2013_UNICODE) {
                throw new PSTException("Unrecognised PST File version: " + fileTypeBytes[0]);
            }
            this.pstFileType = fileTypeBytes[0];

            // make sure encryption is turned off at this stage...
            if (this.getPSTFileType() == PST_TYPE_ANSI) {
                this.in.seek(461);
            } else {
                this.in.seek(513);
            }
            this.encryptionType = this.in.readByte();
            if (this.encryptionType == 0x02) {
                throw new PSTException("Only unencrypted and compressable PST files are supported at this time");
            }

            // build out name to id map.
            this.processNameToIdMap(this.in);

        } catch (final IOException err) {
            throw new PSTException("Unable to read PST Sig", err);
        }

    }

    private int pstFileType = 0;

    public int getPSTFileType() {
        return this.pstFileType;
    }

    /**
     * read the name-to-id map from the file and load it in
     * 
     * @param in
     * @throws IOException
     * @throws PSTException
     */
    private void processNameToIdMap(final PSTFileContent in) throws IOException, PSTException {

        // Create our guid map
        for (int i = 0; i < guidStrings.length; ++i) {
            final UUID uuid = UUID.fromString(guidStrings[i]);
            this.guidMap.put(uuid, i);
            /*
             * System.out.printf("guidMap[{%s}] = %d\n", uuid.toString(), i);
             * /
             **/
        }

        // process the name to id map
        final DescriptorIndexNode nameToIdMapDescriptorNode = (this.getDescriptorIndexNode(97));
        // nameToIdMapDescriptorNode.readData(this);

        // get the descriptors if we have them
        HashMap<Integer, PSTDescriptorItem> localDescriptorItems = null;
        if (nameToIdMapDescriptorNode.localDescriptorsOffsetIndexIdentifier != 0) {
            // PSTDescriptor descriptor = new PSTDescriptor(this,
            // nameToIdMapDescriptorNode.localDescriptorsOffsetIndexIdentifier);
            // localDescriptorItems = descriptor.getChildren();
            localDescriptorItems = this
                .getPSTDescriptorItems(nameToIdMapDescriptorNode.localDescriptorsOffsetIndexIdentifier);
        }

        // process the map
        // PSTTableBC bcTable = new
        // PSTTableBC(nameToIdMapDescriptorNode.dataBlock.data,
        // nameToIdMapDescriptorNode.dataBlock.blockOffsets);
        final OffsetIndexItem off = this.getOffsetIndexNode(nameToIdMapDescriptorNode.dataOffsetIndexIdentifier);
        final PSTNodeInputStream nodein = new PSTNodeInputStream(this, off);
        //final byte[] tmp = new byte[off.size];
        //nodein.readCompletely(tmp);
        final PSTTableBC bcTable = new PSTTableBC(nodein);

        final HashMap<Integer, PSTTableBCItem> tableItems = (bcTable.getItems());
        // Get the guids
        final PSTTableBCItem guidEntry = tableItems.get(2); // PidTagNameidStreamGuid
        this.guids = this.getData(guidEntry, localDescriptorItems);
        final int nGuids = this.guids.length / 16;
        final UUID[] uuidArray = new UUID[nGuids];
        final int[] uuidIndexes = new int[nGuids];
        int offset = 0;
        for (int i = 0; i < nGuids; ++i) {
            final long mostSigBits = (PSTObject.convertLittleEndianBytesToLong(this.guids, offset, offset + 4) << 32)
                | (PSTObject.convertLittleEndianBytesToLong(this.guids, offset + 4, offset + 6) << 16)
                | PSTObject.convertLittleEndianBytesToLong(this.guids, offset + 6, offset + 8);
            final long leastSigBits = PSTObject.convertBigEndianBytesToLong(this.guids, offset + 8, offset + 16);
            uuidArray[i] = new UUID(mostSigBits, leastSigBits);
            if (this.guidMap.containsKey(uuidArray[i])) {
                uuidIndexes[i] = this.guidMap.get(uuidArray[i]);
            } else {
                uuidIndexes[i] = -1; // We don't know this guid
            }
            /*
             * System.out.printf("uuidArray[%d] = {%s},%d\n", i,
             * uuidArray[i].toString(), uuidIndexes[i]);
             * /
             **/
            offset += 16;
        }

        // if we have a reference to an internal descriptor
        final PSTTableBCItem mapEntries = tableItems.get(3); //
        final byte[] nameToIdByte = this.getData(mapEntries, localDescriptorItems);

        final PSTTableBCItem stringMapEntries = tableItems.get(4); //
        final byte[] stringNameToIdByte = this.getData(stringMapEntries, localDescriptorItems);

        // process the entries
        for (int x = 0; x + 8 < nameToIdByte.length; x += 8) {
            final int dwPropertyId = (int) PSTObject.convertLittleEndianBytesToLong(nameToIdByte, x, x + 4);
            int wGuid = (int) PSTObject.convertLittleEndianBytesToLong(nameToIdByte, x + 4, x + 6);
            int wPropIdx = ((int) PSTObject.convertLittleEndianBytesToLong(nameToIdByte, x + 6, x + 8));

            if ((wGuid & 0x0001) == 0) {
                wPropIdx += 0x8000;
                wGuid >>= 1;
                int guidIndex;
                if (wGuid == 1) {
                    guidIndex = PS_MAPI;
                } else if (wGuid == 2) {
                    guidIndex = PS_PUBLIC_STRINGS;
                } else {
                    guidIndex = uuidIndexes[wGuid - 3];
                }
                this.nameToId.put(dwPropertyId | ((long) guidIndex << 32), wPropIdx);
                idToName.put(wPropIdx, (long) dwPropertyId);
                /*
                 * System.out.printf("0x%08X:%04X, 0x%08X\n", dwPropertyId,
                 * guidIndex, wPropIdx);
                 * /
                 **/
            } else {
                // else the identifier is a string
                // dwPropertyId becomes thHke byte offset into the String stream
                // in which the string name of the property is stored.
                final int len = (int) PSTObject.convertLittleEndianBytesToLong(stringNameToIdByte, dwPropertyId,
                    dwPropertyId + 4);
                final byte[] keyByteValue = new byte[len];
                System.arraycopy(stringNameToIdByte, dwPropertyId + 4, keyByteValue, 0, keyByteValue.length);
                wPropIdx += 0x8000;
                final String key = new String(keyByteValue, "UTF-16LE");
                this.stringToId.put(key, wPropIdx);
                this.idToString.put(wPropIdx, key);
            }
        }
    }

    private byte[] getData(final PSTTableItem item, final HashMap<Integer, PSTDescriptorItem> localDescriptorItems)
        throws IOException, PSTException {
        if (item.data.length != 0) {
            return item.data;
        }

        if (localDescriptorItems == null) {
            throw new PSTException("External reference but no localDescriptorItems in PSTFile.getData()");
        }

        if (item.entryValueType != 0x0102) {
            throw new PSTException("Attempting to get non-binary data in PSTFile.getData()");
        }

        final PSTDescriptorItem mapDescriptorItem = localDescriptorItems.get(item.entryValueReference);
        if (mapDescriptorItem == null) {
            throw new PSTException("not here " + item.entryValueReference + "\n" + localDescriptorItems.keySet());
        }
        return mapDescriptorItem.getData();
    }

    int getNameToIdMapItem(final int key, final int propertySetIndex) {
        final long lKey = ((long) propertySetIndex << 32) | key;
        final Integer i = this.nameToId.get(lKey);
        if (i == null) {
            return -1;
        }
        return i;
    }

    int getPublicStringToIdMapItem(final String key) {
        final Integer i = this.stringToId.get(key);
        if (i == null) {
            return -1;
        }
        return i;
    }

    static long getNameToIdMapKey(final int id)
    // throws PSTException
    {
        final Long i = idToName.get(id);
        if (i == null) {
            // throw new PSTException("Name to Id mapping not found");
            return -1;
        }
        return i;
    }

    static private Properties propertyInternetCodePages = null;
    static private boolean bCPFirstTime = true;

    static String getInternetCodePageCharset(final int propertyId) {
        if (bCPFirstTime) {
            bCPFirstTime = false;
            propertyInternetCodePages = new Properties();
            try {
                final InputStream propertyStream = PSTFile.class.getResourceAsStream("/InternetCodepages.txt");
                if (propertyStream != null) {
                    propertyInternetCodePages.load(propertyStream);
                } else {
                    propertyInternetCodePages = null;
                }
            } catch (final FileNotFoundException e) {
                propertyInternetCodePages = null;
                e.printStackTrace();
            } catch (final IOException e) {
                propertyInternetCodePages = null;
                e.printStackTrace();
            }
        }
        if (propertyInternetCodePages != null) {
            return propertyInternetCodePages.getProperty(propertyId + "");
        }
        return null;
    }

    static private Properties propertyNames = null;
    static private boolean bFirstTime = true;

    static String getPropertyName(final int propertyId, final boolean bNamed) {
        if (bFirstTime) {
            bFirstTime = false;
            propertyNames = new Properties();
            try {
                final InputStream propertyStream = PSTFile.class.getResourceAsStream("/PropertyNames.txt");
                if (propertyStream != null) {
                    propertyNames.load(propertyStream);
                } else {
                    propertyNames = null;
                }
            } catch (final FileNotFoundException e) {
                propertyNames = null;
                e.printStackTrace();
            } catch (final IOException e) {
                propertyNames = null;
                e.printStackTrace();
            }
        }

        if (propertyNames != null) {
            final String key = String.format((bNamed ? "%08X" : "%04X"), propertyId);
            return propertyNames.getProperty(key);
        }

        return null;
    }

    static String getPropertyDescription(final int entryType, final int entryValueType) {
        String ret = "";
        if (entryType < 0x8000) {
            final String name = PSTFile.getPropertyName(entryType, false);
            if (name != null) {
                ret = String.format("%s:%04X: ", name, entryValueType);
            } else {
                ret = String.format("0x%04X:%04X: ", entryType, entryValueType);
            }
        } else {
            final long type = PSTFile.getNameToIdMapKey(entryType);
            if (type == -1) {
                ret = String.format("0xFFFF(%04X):%04X: ", entryType, entryValueType);
            } else {
                final String name = PSTFile.getPropertyName((int) type, true);
                if (name != null) {
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
    @Override
    protected void finalize() throws IOException {
        this.in.close();
    }

    /**
     * get the type of encryption the file uses
     * 
     * @return encryption type used in the PST File
     */
    public int getEncryptionType() {
        return this.encryptionType;
    }

    /**
     * get the handle to the RandomAccessFile we are currently accessing (if
     * any)
     */
    public RandomAccessFile getFileHandle() {
        if (this.in instanceof PSTRAFileContent) {
            return ((PSTRAFileContent) this.in).getFile();
        } else {
            return null;
        }
    }

    /**
     * get the handle to the file content we are currently accessing
     */
    public PSTFileContent getContentHandle() {
        return this.in;
    }

    /**
     * get the message store of the PST file.
     * Note that this doesn't really have much information, better to look under
     * the root folder
     * 
     * @throws PSTException
     * @throws IOException
     */
    public PSTMessageStore getMessageStore() throws PSTException, IOException {
        final DescriptorIndexNode messageStoreDescriptor = this
            .getDescriptorIndexNode(MESSAGE_STORE_DESCRIPTOR_IDENTIFIER);
        return new PSTMessageStore(this, messageStoreDescriptor);
    }

    /**
     * get the root folder for the PST file.
     * You should find all of your data under here...
     * 
     * @throws PSTException
     * @throws IOException
     */
    public PSTFolder getRootFolder() throws PSTException, IOException {
        final DescriptorIndexNode rootFolderDescriptor = this.getDescriptorIndexNode(ROOT_FOLDER_DESCRIPTOR_IDENTIFIER);
        final PSTFolder output = new PSTFolder(this, rootFolderDescriptor);
        return output;
    }

    PSTNodeInputStream readLeaf(final long bid) throws IOException, PSTException {
        // PSTFileBlock ret = null;
        final PSTNodeInputStream ret = null;

        // get the index node for the descriptor index
        final OffsetIndexItem offsetItem = this.getOffsetIndexNode(bid);
        return new PSTNodeInputStream(this, offsetItem);

    }

    public int getLeafSize(final long bid) throws IOException, PSTException {
        final OffsetIndexItem offsetItem = this.getOffsetIndexNode(bid);

        // Internal block?
        if ((offsetItem.indexIdentifier & 0x02) == 0) {
            // No, return the raw size
            return offsetItem.size;
        }

        // we only need the first 8 bytes
        final byte[] data = new byte[8];
        this.in.seek(offsetItem.fileOffset);
        this.in.readCompletely(data);

        // we are an array, get the sum of the sizes...
        return (int) PSTObject.convertLittleEndianBytesToLong(data, 4, 8);
    }

    /**
     * Read a file offset from the file
     * PST Files have this tendency to store file offsets (pointers) in 8 little
     * endian bytes.
     * Convert this to a long for seeking to.
     * 
     * @param in
     *            handle for PST file
     * @param startOffset
     *            where to read the 8 bytes from
     * @return long representing the read location
     * @throws IOException
     */
    protected long extractLEFileOffset(final long startOffset) throws IOException {
        long offset = 0;
        if (this.getPSTFileType() == PSTFile.PST_TYPE_ANSI) {
            this.in.seek(startOffset);
            final byte[] temp = new byte[4];
            this.in.readCompletely(temp);
            offset |= temp[3] & 0xff;
            offset <<= 8;
            offset |= temp[2] & 0xff;
            offset <<= 8;
            offset |= temp[1] & 0xff;
            offset <<= 8;
            offset |= temp[0] & 0xff;
        } else {
            this.in.seek(startOffset);
            final byte[] temp = new byte[8];
            this.in.readCompletely(temp);
            offset = temp[7] & 0xff;
            long tmpLongValue;
            for (int x = 6; x >= 0; x--) {
                offset = offset << 8;
                tmpLongValue = (long) temp[x] & 0xff;
                offset |= tmpLongValue;
            }
        }

        return offset;
    }

    /**
     * Generic function used by getOffsetIndexNode and getDescriptorIndexNode
     * for navigating the PST B-Trees
     * 
     * @param in
     * @param index
     * @param descTree
     * @return
     * @throws IOException
     * @throws PSTException
     */
    private byte[] findBtreeItem(final PSTFileContent in, final long index, final boolean descTree)
        throws IOException, PSTException {

        long btreeStartOffset;
        int fileTypeAdjustment;
        // first find the starting point for the offset index
        if (this.getPSTFileType() == PST_TYPE_ANSI) {
            btreeStartOffset = this.extractLEFileOffset(196);
            if (descTree) {
                btreeStartOffset = this.extractLEFileOffset(188);
            }
        } else {
            btreeStartOffset = this.extractLEFileOffset(240);
            if (descTree) {
                btreeStartOffset = this.extractLEFileOffset(224);
            }
        }

        // okay, what we want to do is navigate the tree until you reach the
        // bottom....
        // try and read the index b-tree
        byte[] temp = new byte[2];
        if (this.getPSTFileType() == PST_TYPE_ANSI) {
            fileTypeAdjustment = 500;
        } else if (this.getPSTFileType() == PST_TYPE_2013_UNICODE) {
            fileTypeAdjustment = 0x1000 - 24;
        } else {
            fileTypeAdjustment = 496;
        }
        in.seek(btreeStartOffset + fileTypeAdjustment);
        in.readCompletely(temp);

        while ((temp[0] == 0xffffff80 && temp[1] == 0xffffff80 && !descTree)
            || (temp[0] == 0xffffff81 && temp[1] == 0xffffff81 && descTree)) {
            // get the rest of the data....
            byte[] branchNodeItems;
            if (this.getPSTFileType() == PST_TYPE_ANSI) {
                branchNodeItems = new byte[496];
            } else if (this.getPSTFileType() == PST_TYPE_2013_UNICODE) {
                branchNodeItems = new byte[4056];
            } else {
                branchNodeItems = new byte[488];
            }
            in.seek(btreeStartOffset);
            in.readCompletely(branchNodeItems);

            long numberOfItems = 0;
            if (this.getPSTFileType() == PST_TYPE_2013_UNICODE) {
                final byte[] numberOfItemsBytes = new byte[2];
                in.readCompletely(numberOfItemsBytes);
                numberOfItems = PSTObject.convertLittleEndianBytesToLong(numberOfItemsBytes);
                in.readCompletely(numberOfItemsBytes);
                final long maxNumberOfItems = PSTObject.convertLittleEndianBytesToLong(numberOfItemsBytes);
            } else {
                numberOfItems = in.read();
                in.read(); // maxNumberOfItems
            }
            final int itemSize = in.read(); // itemSize
            final int levelsToLeaf = in.read();

            if (levelsToLeaf > 0) {
                boolean found = false;
                for (long x = 0; x < numberOfItems; x++) {
                    if (this.getPSTFileType() == PST_TYPE_ANSI) {
                        final long indexIdOfFirstChildNode = this.extractLEFileOffset(btreeStartOffset + (x * 12));
                        if (indexIdOfFirstChildNode > index) {
                            // get the address for the child first node in this
                            // group
                            btreeStartOffset = this.extractLEFileOffset(btreeStartOffset + ((x - 1) * 12) + 8);
                            in.seek(btreeStartOffset + 500);
                            in.readCompletely(temp);
                            found = true;
                            break;
                        }
                    } else {
                        final long indexIdOfFirstChildNode = this.extractLEFileOffset(btreeStartOffset + (x * 24));
                        if (indexIdOfFirstChildNode > index) {
                            // get the address for the child first node in this
                            // group
                            btreeStartOffset = this.extractLEFileOffset(btreeStartOffset + ((x - 1) * 24) + 16);
                            in.seek(btreeStartOffset + fileTypeAdjustment);
                            in.readCompletely(temp);
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    // it must be in the very last branch...
                    if (this.getPSTFileType() == PST_TYPE_ANSI) {
                        btreeStartOffset = this.extractLEFileOffset(btreeStartOffset + ((numberOfItems - 1) * 12) + 8);
                        in.seek(btreeStartOffset + 500);
                        in.readCompletely(temp);
                    } else {
                        btreeStartOffset = this.extractLEFileOffset(btreeStartOffset + ((numberOfItems - 1) * 24) + 16);
                        in.seek(btreeStartOffset + fileTypeAdjustment);
                        in.readCompletely(temp);
                    }
                }
            } else {
                // System.out.println(String.format("At bottom, looking through
                // %d items", numberOfItems));
                // we are at the bottom of the tree...
                // we want to get our file offset!
                for (long x = 0; x < numberOfItems; x++) {

                    if (this.getPSTFileType() == PSTFile.PST_TYPE_ANSI) {
                        if (descTree) {
                            // The 32-bit descriptor index b-tree leaf node item
                            in.seek(btreeStartOffset + (x * 16));
                            temp = new byte[4];
                            in.readCompletely(temp);
                            if (PSTObject.convertLittleEndianBytesToLong(temp) == index) {
                                // give me the offset index please!
                                in.seek(btreeStartOffset + (x * 16));
                                temp = new byte[16];
                                in.readCompletely(temp);
                                return temp;
                            }
                        } else {
                            // The 32-bit (file) offset index item
                            final long indexIdOfFirstChildNode = this.extractLEFileOffset(btreeStartOffset + (x * 12));

                            if (indexIdOfFirstChildNode == index) {
                                // we found it!!!! OMG
                                // System.out.println("item found as item #"+x);
                                in.seek(btreeStartOffset + (x * 12));

                                temp = new byte[12];
                                in.readCompletely(temp);
                                return temp;
                            }
                        }
                    } else {
                        if (descTree) {
                            // The 64-bit descriptor index b-tree leaf node item
                            in.seek(btreeStartOffset + (x * 32));

                            temp = new byte[4];
                            in.readCompletely(temp);
                            if (PSTObject.convertLittleEndianBytesToLong(temp) == index) {
                                // give me the offset index please!
                                in.seek(btreeStartOffset + (x * 32));
                                temp = new byte[32];
                                in.readCompletely(temp);
                                // System.out.println("item found!!!");
                                // PSTObject.printHexFormatted(temp, true);
                                return temp;
                            }
                        } else {
                            // The 64-bit (file) offset index item
                            final long indexIdOfFirstChildNode = this.extractLEFileOffset(btreeStartOffset + (x * 24));

                            if (indexIdOfFirstChildNode == index) {
                                // we found it!!!! OMG
                                // System.out.println("item found as item #"+x +
                                // " size (should be 24): "+itemSize);
                                in.seek(btreeStartOffset + (x * 24));

                                temp = new byte[24];
                                in.readCompletely(temp);
                                return temp;
                            }
                        }
                    }
                }
                throw new PSTException("Unable to find " + index + " is desc: " + descTree);
            }
        }

        throw new PSTException("Unable to find node: " + index + " is desc: " + descTree);
    }

    /**
     * navigate the internal descriptor B-Tree and find a specific item
     * 
     * @param in
     * @param identifier
     * @return the descriptor node for the item
     * @throws IOException
     * @throws PSTException
     */
    DescriptorIndexNode getDescriptorIndexNode(final long identifier) throws IOException, PSTException {
        return new DescriptorIndexNode(this.findBtreeItem(this.in, identifier, true), this.getPSTFileType());
    }

    /**
     * navigate the internal index B-Tree and find a specific item
     * 
     * @param in
     * @param identifier
     * @return the offset index item
     * @throws IOException
     * @throws PSTException
     */
    OffsetIndexItem getOffsetIndexNode(final long identifier) throws IOException, PSTException {
        return new OffsetIndexItem(this.findBtreeItem(this.in, identifier, false), this.getPSTFileType());
    }

    /**
     * parse a PSTDescriptor and get all of its items
     */
    HashMap<Integer, PSTDescriptorItem> getPSTDescriptorItems(final long localDescriptorsOffsetIndexIdentifier)
        throws PSTException, IOException {
        return this.getPSTDescriptorItems(this.readLeaf(localDescriptorsOffsetIndexIdentifier));
    }

    HashMap<Integer, PSTDescriptorItem> getPSTDescriptorItems(final PSTNodeInputStream in)
        throws PSTException, IOException {
        // make sure the signature is correct
        in.seek(0);
        final int sig = in.read();
        if (sig != 0x2) {
            throw new PSTException("Unable to process descriptor node, bad signature: " + sig);
        }

        final HashMap<Integer, PSTDescriptorItem> output = new HashMap<>();
        final int numberOfItems = (int) in.seekAndReadLong(2, 2);
        int offset;
        if (this.getPSTFileType() == PSTFile.PST_TYPE_ANSI) {
            offset = 4;
        } else {
            offset = 8;
        }

        final byte[] data = new byte[(int) in.length()];
        in.seek(0);
        in.readCompletely(data);

        for (int x = 0; x < numberOfItems; x++) {
            final PSTDescriptorItem item = new PSTDescriptorItem(data, offset, this);
            output.put(item.descriptorIdentifier, item);
            if (this.getPSTFileType() == PSTFile.PST_TYPE_ANSI) {
                offset += 12;
            } else {
                offset += 24;
            }
        }

        return output;
    }

    /**
     * Build the children descriptor tree
     * This goes through the entire descriptor B-Tree and adds every item to the
     * childrenDescriptorTree.
     * This is used as fallback when the nodes that list file contents are
     * broken.
     * 
     * @param in
     * @throws IOException
     * @throws PSTException
     */
    LinkedHashMap<Integer, LinkedList<DescriptorIndexNode>> getChildDescriptorTree() throws IOException, PSTException {
        if (this.childrenDescriptorTree == null) {
            long btreeStartOffset = 0;
            if (this.getPSTFileType() == PST_TYPE_ANSI) {
                btreeStartOffset = this.extractLEFileOffset(188);
            } else {
                btreeStartOffset = this.extractLEFileOffset(224);
            }
            this.childrenDescriptorTree = new LinkedHashMap<>();
            this.processDescriptorBTree(btreeStartOffset);
        }
        return this.childrenDescriptorTree;
    }

    /**
     * Recursive function for building the descriptor tree, used by
     * buildDescriptorTree
     * 
     * @param in
     * @param btreeStartOffset
     * @throws IOException
     * @throws PSTException
     */
    private void processDescriptorBTree(final long btreeStartOffset) throws IOException, PSTException {
        int fileTypeAdjustment;

        byte[] temp = new byte[2];
        if (this.getPSTFileType() == PST_TYPE_ANSI) {
            fileTypeAdjustment = 500;
        } else if (this.getPSTFileType() == PST_TYPE_2013_UNICODE) {
            fileTypeAdjustment = 0x1000 - 24;
        } else {
            fileTypeAdjustment = 496;
        }
        this.in.seek(btreeStartOffset + fileTypeAdjustment);
        this.in.readCompletely(temp);

        if ((temp[0] == 0xffffff81 && temp[1] == 0xffffff81)) {

            if (this.getPSTFileType() == PST_TYPE_ANSI) {
                this.in.seek(btreeStartOffset + 496);
            } else if (this.getPSTFileType() == PST_TYPE_2013_UNICODE) {
                this.in.seek(btreeStartOffset + 4056);
            } else {
                this.in.seek(btreeStartOffset + 488);
            }

            long numberOfItems = 0;
            if (this.getPSTFileType() == PST_TYPE_2013_UNICODE) {
                final byte[] numberOfItemsBytes = new byte[2];
                this.in.readCompletely(numberOfItemsBytes);
                numberOfItems = PSTObject.convertLittleEndianBytesToLong(numberOfItemsBytes);
                this.in.readCompletely(numberOfItemsBytes);
                final long maxNumberOfItems = PSTObject.convertLittleEndianBytesToLong(numberOfItemsBytes);
            } else {
                numberOfItems = this.in.read();
                this.in.read(); // maxNumberOfItems
            }
            this.in.read(); // itemSize
            final int levelsToLeaf = this.in.read();

            if (levelsToLeaf > 0) {
                for (long x = 0; x < numberOfItems; x++) {
                    if (this.getPSTFileType() == PST_TYPE_ANSI) {
                        final long branchNodeItemStartIndex = (btreeStartOffset + (12 * x));
                        final long nextLevelStartsAt = this.extractLEFileOffset(branchNodeItemStartIndex + 8);
                        this.processDescriptorBTree(nextLevelStartsAt);
                    } else {
                        final long branchNodeItemStartIndex = (btreeStartOffset + (24 * x));
                        final long nextLevelStartsAt = this.extractLEFileOffset(branchNodeItemStartIndex + 16);
                        this.processDescriptorBTree(nextLevelStartsAt);
                    }
                }
            } else {
                for (long x = 0; x < numberOfItems; x++) {
                    // The 64-bit descriptor index b-tree leaf node item
                    // give me the offset index please!
                    if (this.getPSTFileType() == PSTFile.PST_TYPE_ANSI) {
                        this.in.seek(btreeStartOffset + (x * 16));
                        temp = new byte[16];
                        this.in.readCompletely(temp);
                    } else {
                        this.in.seek(btreeStartOffset + (x * 32));
                        temp = new byte[32];
                        this.in.readCompletely(temp);
                    }

                    final DescriptorIndexNode tempNode = new DescriptorIndexNode(temp, this.getPSTFileType());

                    // we don't want to be children of ourselves...
                    if (tempNode.parentDescriptorIndexIdentifier == tempNode.descriptorIdentifier) {
                        // skip!
                    } else if (this.childrenDescriptorTree.containsKey(tempNode.parentDescriptorIndexIdentifier)) {
                        // add this entry to the existing list of children
                        final LinkedList<DescriptorIndexNode> children = this.childrenDescriptorTree
                            .get(tempNode.parentDescriptorIndexIdentifier);
                        children.add(tempNode);
                    } else {
                        // create a new entry and add this one to that
                        final LinkedList<DescriptorIndexNode> children = new LinkedList<>();
                        children.add(tempNode);
                        this.childrenDescriptorTree.put(tempNode.parentDescriptorIndexIdentifier, children);
                    }
                    this.itemCount++;
                }
            }
        } else {
            PSTObject.printHexFormatted(temp, true);
            throw new PSTException("Unable to read descriptor node, is not a descriptor");
        }
    }

    public void close() throws IOException {
        this.in.close();
    }

}
