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
	

	public String[] getDistributionListMembers()
		throws PSTException, IOException
	{
		//PidLidDistributionListMembers
		PSTTableBCItem item = this.items.get(pstFile.getNameToIdMapItem(0x8055, PSTFile.PSETID_Address));
		String[] out = {};
		if (item != null) {
			PSTObject.printHexFormatted(item.data, true);
			// get the details
			int addressCount = (int)PSTObject.convertLittleEndianBytesToLong(item.data, 0, 4);
			int pos = 4;
			System.out.println("Addresses: "+addressCount);
			for (int x = 0;x < addressCount; x++) {
				int propertyCount = (int)PSTObject.convertLittleEndianBytesToLong(item.data, pos, pos+4);
				pos += 4;
				for (int y = 0; y < propertyCount; y++) {
					int propertyType = (int)PSTObject.convertLittleEndianBytesToLong(item.data, pos, pos+2);
					System.out.println("Property Type: "+propertyType);
					pos += 2;
					int propertyId = (int)PSTObject.convertLittleEndianBytesToLong(item.data, pos, pos+2);
					System.out.println("Property Id: "+propertyId);
					pos += 2;
					break;
				}
				//PSTObject.printHexFormatted(data, true);
				break;
			}
					
		}

		return out;
	}


}
