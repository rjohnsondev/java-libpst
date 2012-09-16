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
		PSTTableBCItem item = this.items.get(pstFile.getNameToIdMapItem(0x8055, PSTFile.PSETID_Address));
		item = this.items.get(0x8047);
		String[] out = {};
		if (item != null) {
			PSTObject.printHexFormatted(item.data, true);
			// start 32 bytes in and then we are split by nulls...
			byte[] repeat = new byte[20];
			System.arraycopy(item.data, 16, repeat, 0, repeat.length);
			PSTObject.printHexFormatted(repeat, true);
			int pos = 36;

			boolean entryRemains = true;

			while (entryRemains) {
				int end = findNextNullChar(item.data, pos);
				byte[] d = new byte[end-pos];
				System.arraycopy(item.data, pos, d, 0, d.length);
				String displayName = new String(d, "UTF-16LE");
				System.out.println("Display: "+displayName);
				pos = end + 2;

				end = findNextNullChar(item.data, pos);
				d = new byte[end-pos];
				System.arraycopy(item.data, pos, d, 0, d.length);
				String type = new String(d, "UTF-16LE");
				System.out.println("Type: "+type);
				pos = end + 2;

				end = findNextNullChar(item.data, pos);
				d = new byte[end-pos];
				System.arraycopy(item.data, pos, d, 0, d.length);
				String email = new String(d, "UTF-16LE");
				System.out.println("Email: "+email);
				pos = end + 2;

				entryRemains = compareByteArrays(repeat, item.data, pos, pos+16);

			}

		}

		return out;
	}


}
