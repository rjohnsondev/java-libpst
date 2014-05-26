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
package com.pff;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Arrays;

/**
 * PST DistList contains methods for extracting Addresses from Distribution lists.
 * @author Richard Johnson
 */
public class PSTDistList extends PSTMessage {

	PSTDistList(PSTFile theFile, DescriptorIndexNode descriptorIndexNode)
			throws PSTException, IOException
	{
		super(theFile, descriptorIndexNode);
	}

	PSTDistList(PSTFile theFile, DescriptorIndexNode folderIndexNode, PSTTableBC table, HashMap<Integer, PSTDescriptorItem> localDescriptorItems)
	{
		super(theFile, folderIndexNode, table, localDescriptorItems);
	}
	
	private int findNextNullChar(byte[] data, int start) {
		for (; start < data.length; start += 2) {
			if (data[start] == 0 && data[start+1] == 0) {
				break;
			}
		}
		return start;
	}

    private byte[] oneOffEntryIdUid = {
        (byte)0x81, (byte)0x2b, (byte)0x1f, (byte)0xa4,
        (byte)0xbe, (byte)0xa3, (byte)0x10, (byte)0x19,
        (byte)0x9d, (byte)0x6e, (byte)0x00, (byte)0xdd,
        (byte)0x01, (byte)0x0f, (byte)0x54, (byte)0x02
    };

	private byte[] wrappedEntryIdUid = {
        (byte)0xc0, (byte)0x91, (byte)0xad, (byte)0xd3,
        (byte)0x51, (byte)0x9d, (byte)0xcf, (byte)0x11,
        (byte)0xa4, (byte)0xa9, (byte)0x00, (byte)0xaa,
        (byte)0x00, (byte)0x47, (byte)0xfa, (byte)0xa4
    };

    public class OneOffEntry {
        public String displayName = "";
        public String addressType = "";
        public String emailAddress = "";
        int pos = 0;
    }

    private OneOffEntry parseOneOffEntry(byte[] data, int pos)
        throws IOException
    {
        /*
                   dd01 0f54 0200 0001 8064 0069 0073 0074  Ý..T.....d.i.s.t
                   0020 006e 0061 006d 0065 0020 0032 0000  ...n.a.m.e...2..
                   0053 004d 0054 0050 0000 0064 0069 0073  .S.M.T.P...d.i.s
                   0074 0032 0040 0072 006a 006f 0068 006e  .t.2...r.j.o.h.n
                   0073 006f 006e 002e 0069 0064 002e 0061  .s.o.n...i.d...a
                   0075 0000 00 .u...
                   */
        int version = (int)PSTObject.convertLittleEndianBytesToLong(data, pos, pos+2);
        //System.out.println("Version: "+version);
        pos += 2;

        // http://msdn.microsoft.com/en-us/library/ee202811(v=exchg.80).aspx
        int additionalFlags = (int)PSTObject.convertLittleEndianBytesToLong(data, pos, pos+2);
        //PSTObject.printFormattedNumber("Additional flags: ",additionalFlags);
        pos += 2;

        //PSTObject.printFormattedNumber("flag: ",0x800);
        int pad = additionalFlags & 0x8000;
        //PSTObject.printFormattedNumber("Pad: ",pad);
        int mae = additionalFlags & 0x0C00;
        int format = additionalFlags & 0x1E00;
        int m = additionalFlags & 0x0100;
        int u = additionalFlags & 0x0080;
        int r = additionalFlags & 0x0060;
        int l = additionalFlags & 0x0010;
        int pad2 = additionalFlags & 0x000F;


        int stringEnd = findNextNullChar(data, pos);
        byte[] displayNameBytes = new byte[stringEnd - pos];
        System.arraycopy(data, pos, displayNameBytes, 0, displayNameBytes.length);
        String displayName = new String(displayNameBytes, "UTF-16LE");
        //System.out.println("displayName: "+displayName);
        pos = stringEnd + 2;

        stringEnd = findNextNullChar(data, pos);
        byte[] addressTypeBytes = new byte[stringEnd - pos];
        System.arraycopy(data, pos, addressTypeBytes, 0, addressTypeBytes.length);
        String addressType = new String(addressTypeBytes, "UTF-16LE");
        //System.out.println("addressType "+addressType);
        pos = stringEnd + 2;

        stringEnd = findNextNullChar(data, pos);
        byte[] emailAddressBytes = new byte[stringEnd - pos];
        System.arraycopy(data, pos, emailAddressBytes, 0, emailAddressBytes.length);
        String emailAddress = new String(emailAddressBytes, "UTF-16LE");
        //System.out.println("emailAddress "+emailAddress);
        pos = stringEnd + 2;

        OneOffEntry out = new OneOffEntry();
        out.displayName = displayName;
        out.addressType = addressType;
        out.emailAddress = emailAddress;
        out.pos = pos;
        return out;
    }

	public String[] getDistributionListMembers()
		throws PSTException, IOException
	{
		//PidLidDistributionListMembers
		// this looks like a Recipeint One-Off EntryID Structure
		PSTTableBCItem item = this.items.get(pstFile.getNameToIdMapItem(0x8055, PSTFile.PSETID_Address));
		//item = this.items.get(0x8047);
		String[] out = {};
        if (item != null) {
            PSTObject.printHexFormatted(item.data, true);

            int pos = 0;

            int count = (int)PSTObject.convertLittleEndianBytesToLong(item.data, pos, pos+4);
            //System.out.println("Count: "+count);
            pos += 4;
            pos = (int)PSTObject.convertLittleEndianBytesToLong(item.data, pos, pos+4);
            //System.out.println("pos: "+pos);

            while (pos < item.data.length) {
                /*
                   00 0000 00
                   81 2b1f a4be a310 199d 6e00 dd01 0f54
                   */
                // http://msdn.microsoft.com/en-us/library/ee218661(v=exchg.80).aspx
                // http://msdn.microsoft.com/en-us/library/ee200559(v=exchg.80).aspx
                int flags = (int)PSTObject.convertLittleEndianBytesToLong(item.data, pos, pos+4);
                pos += 4;

                byte[] guid = new byte[16];
                System.arraycopy(item.data, pos, guid, 0, guid.length);
                //PSTObject.printHexFormatted(guid, true);
                //System.out.println(Arrays.equals(guid, wrappedEntryIdUid));
                pos += 16;

                if (Arrays.equals(guid, wrappedEntryIdUid)) {
                    //System.out.println("Wrapped entry");

                    /* c3 */
                    int entryType = item.data[pos] & 0x0F;
                    int entryAddressType = item.data[pos] & 0x70 >> 4;
                    boolean isOneOffEntryId = (item.data[pos] & 0x80) > 0;
                    pos++;
                    if (entryType == 3) {
                        /*
                           00 0000 00
                           a4 1d63 dbc5 3b8e 4ab8 071e <- some guid from some where.
                           15e5 5750 ce
                           64 00 20 00
                           */
                        int wrappedflags = (int)PSTObject.convertLittleEndianBytesToLong(item.data, pos, pos+4);
                        pos += 4;

                        byte[] guid2 = new byte[16];
                        System.arraycopy(item.data, pos, guid, 0, guid.length);
                        //PSTObject.printHexFormatted(guid, true);
                        pos += 16;

                        int descriptorId = (int)PSTObject.convertLittleEndianBytesToLong(item.data, pos, pos+3);
                        pos += 3;

                        byte empty = item.data[pos];
                        pos++;

                    }

                } else if (Arrays.equals(guid, oneOffEntryIdUid)) {
                    OneOffEntry entry = parseOneOffEntry(item.data, pos);
                    System.out.println(entry.emailAddress);
                    pos = entry.pos;
                }

            }
		}
		return out;
	}


}
