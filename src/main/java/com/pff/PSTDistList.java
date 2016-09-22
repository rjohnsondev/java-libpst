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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * PST DistList for extracting Addresses from Distribution lists.
 * 
 * @author Richard Johnson
 */
public class PSTDistList extends PSTMessage {

    /**
     * constructor.
     * 
     * @param theFile
     *            pst file
     * @param descriptorIndexNode
     *            index of the list
     * @throws PSTException
     *             on parsing error
     * @throws IOException
     *             on data access error
     */
    PSTDistList(final PSTFile theFile, final DescriptorIndexNode descriptorIndexNode) throws PSTException, IOException {
        super(theFile, descriptorIndexNode);
    }

    /**
     * Internal constructor for performance.
     * 
     * @param theFile
     *            pst file
     * @param folderIndexNode
     *            index of the list
     * @param table
     *            the PSTTableBC this object is represented by
     * @param localDescriptorItems
     *            additional external items that represent
     *            this object.
     */
    PSTDistList(final PSTFile theFile, final DescriptorIndexNode folderIndexNode, final PSTTableBC table,
        final HashMap<Integer, PSTDescriptorItem> localDescriptorItems) {
        super(theFile, folderIndexNode, table, localDescriptorItems);
    }

    /**
     * Find the next two null bytes in an array given start.
     * 
     * @param data
     *            the array to search
     * @param start
     *            the starting index
     * @return position of the next null char
     */
    private int findNextNullChar(final byte[] data, int start) {
        for (; start < data.length; start += 2) {
            if (data[start] == 0 && data[start + 1] == 0) {
                break;
            }
        }
        return start;
    }

    /**
     * identifier for one-off entries.
     */
    private final byte[] oneOffEntryIdUid = { (byte) 0x81, (byte) 0x2b, (byte) 0x1f, (byte) 0xa4, (byte) 0xbe,
        (byte) 0xa3, (byte) 0x10, (byte) 0x19, (byte) 0x9d, (byte) 0x6e, (byte) 0x00, (byte) 0xdd, (byte) 0x01,
        (byte) 0x0f, (byte) 0x54, (byte) 0x02 };

    /**
     * identifier for wrapped entries.
     */
    private final byte[] wrappedEntryIdUid = { (byte) 0xc0, (byte) 0x91, (byte) 0xad, (byte) 0xd3, (byte) 0x51,
        (byte) 0x9d, (byte) 0xcf, (byte) 0x11, (byte) 0xa4, (byte) 0xa9, (byte) 0x00, (byte) 0xaa, (byte) 0x00,
        (byte) 0x47, (byte) 0xfa, (byte) 0xa4 };

    /**
     * Inner class to represent distribution list one-off entries.
     */
    public class OneOffEntry {
        /** display name. */
        private String displayName = "";

        /**
         * @return display name
         */
        public String getDisplayName() {
            return this.displayName;
        }

        /** address type (smtp). */
        private String addressType = "";

        /**
         * @return address type
         */
        public String getAddressType() {
            return this.addressType;
        }

        /** email address. */
        private String emailAddress = "";

        /**
         * @return email address.
         */
        public String getEmailAddress() {
            return this.emailAddress;
        }

        /** ending position of this object in the data array. */
        private int pos = 0;

        /**
         * @return formatted record
         */
        @Override
        public String toString() {
            return String.format("Display Name: %s\n" + "Address Type: %s\n" + "Email Address: %s\n", this.displayName,
                this.addressType, this.emailAddress);
        }
    }

    /**
     * Parse a one-off entry from this Distribution List.
     * 
     * @param data
     *            the item data
     * @param pos
     *            the current position in the data.
     * @throws IOException
     *             on string reading fail
     * @return the one-off entry
     */
    private OneOffEntry parseOneOffEntry(final byte[] data, int pos) throws IOException {
        final int version = (int) PSTObject.convertLittleEndianBytesToLong(data, pos, pos + 2);
        pos += 2;

        // http://msdn.microsoft.com/en-us/library/ee202811(v=exchg.80).aspx
        final int additionalFlags = (int) PSTObject.convertLittleEndianBytesToLong(data, pos, pos + 2);
        pos += 2;

        final int pad = additionalFlags & 0x8000;
        final int mae = additionalFlags & 0x0C00;
        final int format = additionalFlags & 0x1E00;
        final int m = additionalFlags & 0x0100;
        final int u = additionalFlags & 0x0080;
        final int r = additionalFlags & 0x0060;
        final int l = additionalFlags & 0x0010;
        final int pad2 = additionalFlags & 0x000F;

        int stringEnd = this.findNextNullChar(data, pos);
        final byte[] displayNameBytes = new byte[stringEnd - pos];
        System.arraycopy(data, pos, displayNameBytes, 0, displayNameBytes.length);
        final String displayName = new String(displayNameBytes, "UTF-16LE");
        pos = stringEnd + 2;

        stringEnd = this.findNextNullChar(data, pos);
        final byte[] addressTypeBytes = new byte[stringEnd - pos];
        System.arraycopy(data, pos, addressTypeBytes, 0, addressTypeBytes.length);
        final String addressType = new String(addressTypeBytes, "UTF-16LE");
        pos = stringEnd + 2;

        stringEnd = this.findNextNullChar(data, pos);
        final byte[] emailAddressBytes = new byte[stringEnd - pos];
        System.arraycopy(data, pos, emailAddressBytes, 0, emailAddressBytes.length);
        final String emailAddress = new String(emailAddressBytes, "UTF-16LE");
        pos = stringEnd + 2;

        final OneOffEntry out = new OneOffEntry();
        out.displayName = displayName;
        out.addressType = addressType;
        out.emailAddress = emailAddress;
        out.pos = pos;
        return out;
    }

    /**
     * Get an array of the members in this distribution list.
     * 
     * @throws PSTException
     *             on corrupted data
     * @throws IOException
     *             on bad string reading
     * @return array of entries that can either be PSTDistList.OneOffEntry
     *         or a PSTObject, generally PSTContact.
     */
    public Object[] getDistributionListMembers() throws PSTException, IOException {
        final PSTTableBCItem item = this.items.get(this.pstFile.getNameToIdMapItem(0x8055, PSTFile.PSETID_Address));
        Object[] out = {};
        if (item != null) {
            int pos = 0;
            final int count = (int) PSTObject.convertLittleEndianBytesToLong(item.data, pos, pos + 4);
            out = new Object[count];
            pos += 4;
            pos = (int) PSTObject.convertLittleEndianBytesToLong(item.data, pos, pos + 4);

            for (int x = 0; x < count; x++) {
                // http://msdn.microsoft.com/en-us/library/ee218661(v=exchg.80).aspx
                // http://msdn.microsoft.com/en-us/library/ee200559(v=exchg.80).aspx
                final int flags = (int) PSTObject.convertLittleEndianBytesToLong(item.data, pos, pos + 4);
                pos += 4;

                final byte[] guid = new byte[16];
                System.arraycopy(item.data, pos, guid, 0, guid.length);
                pos += 16;

                if (Arrays.equals(guid, this.wrappedEntryIdUid)) {
                    /* c3 */
                    final int entryType = item.data[pos] & 0x0F;
                    final int entryAddressType = item.data[pos] & 0x70 >> 4;
                    final boolean isOneOffEntryId = (item.data[pos] & 0x80) > 0;
                    pos++;
                    final int wrappedflags = (int) PSTObject.convertLittleEndianBytesToLong(item.data, pos, pos + 4);
                    pos += 4;

                    final byte[] guid2 = new byte[16];
                    System.arraycopy(item.data, pos, guid, 0, guid.length);
                    pos += 16;

                    final int descriptorIndex = (int) PSTObject.convertLittleEndianBytesToLong(item.data, pos, pos + 3);
                    pos += 3;

                    final byte empty = item.data[pos];
                    pos++;

                    out[x] = PSTObject.detectAndLoadPSTObject(this.pstFile, descriptorIndex);

                } else if (Arrays.equals(guid, this.oneOffEntryIdUid)) {
                    final OneOffEntry entry = this.parseOneOffEntry(item.data, pos);
                    pos = entry.pos;
                    out[x] = entry;
                }
            }
        }
        return out;
    }
}
