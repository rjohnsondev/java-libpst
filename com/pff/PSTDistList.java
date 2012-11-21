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

	private boolean compareByteArrays(byte[] orig, byte[] other, int start, int end) {
		if (end - start != orig.length) {
			return false;
		}
		if (end > other.length) {
			return false;
		}
		for (int x = 0; x < orig.length; x++) {
			if (orig[x] != other[x + start]) {
				return false;
			}
		}
		return true;
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
			System.out.println("Count: "+count);
			pos += 4;
			pos = (int)PSTObject.convertLittleEndianBytesToLong(item.data, pos, pos+4);
			System.out.println("pos: "+pos);

			while (pos < item.data.length) {

				int flags = (int)PSTObject.convertLittleEndianBytesToLong(item.data, pos, pos+4);
				pos += 4;

				byte[] guid = new byte[16];
				System.arraycopy(item.data, pos, guid, 0, guid.length);
				PSTObject.printHexFormatted(guid, true);
				pos += 16;

				int version = (int)PSTObject.convertLittleEndianBytesToLong(item.data, pos, pos+2);
				System.out.println("Version: "+version);
				pos += 2;

				int additionalFlags = (int)PSTObject.convertLittleEndianBytesToLong(item.data, pos, pos+2);
				PSTObject.printFormattedNumber("Additional flags: ",additionalFlags);
				pos += 2;

				PSTObject.printFormattedNumber("flag: ",0x800);
				int pad = additionalFlags & 0x8000;
				PSTObject.printFormattedNumber("Pad: ",pad);
				int mae = additionalFlags & 0x0C00;
				int format = additionalFlags & 0x1E00;
				int m = additionalFlags & 0x0100;
				int u = additionalFlags & 0x0080;
				int r = additionalFlags & 0x0060;
				int l = additionalFlags & 0x0010;
				int pad2 = additionalFlags & 0x000F;


				int stringEnd = findNextNullChar(item.data, pos);
				byte[] displayNameBytes = new byte[stringEnd - pos];
				System.arraycopy(item.data, pos, displayNameBytes, 0, displayNameBytes.length);
				String displayName = new String(displayNameBytes, "UTF-16LE");
				//System.out.println("displayName: "+displayName);
				pos = stringEnd + 2;

				stringEnd = findNextNullChar(item.data, pos);
				byte[] addressTypeBytes = new byte[stringEnd - pos];
				System.arraycopy(item.data, pos, addressTypeBytes, 0, addressTypeBytes.length);
				String addressType = new String(addressTypeBytes, "UTF-16LE");
				//System.out.println("addressType "+addressType);
				pos = stringEnd + 2;

				stringEnd = findNextNullChar(item.data, pos);
				byte[] emailAddressBytes = new byte[stringEnd - pos];
				System.arraycopy(item.data, pos, emailAddressBytes, 0, emailAddressBytes.length);
				String emailAddress = new String(emailAddressBytes, "UTF-16LE");
				//System.out.println("emailAddress "+emailAddress);
				pos = stringEnd + 2;

			}
		}

		return out;
	}


}
