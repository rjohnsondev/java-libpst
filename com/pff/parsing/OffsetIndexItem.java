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
package com.pff.parsing;

import com.pff.PSTUtils;
import com.pff.source.PSTSource;

/**
 * OffsetIndexItem is a leaf item from the Offset index b-tree
 * Only really used internally to get the file offset for items
 * @author Richard Johnson
 */
public class OffsetIndexItem {
	long indexIdentifier;
	long fileOffset;
	int size;
	long cRef;
	
	public OffsetIndexItem(byte[] data, int pstFileType) {
		if (pstFileType == PSTSource.PST_TYPE_ANSI) {
			indexIdentifier = PSTUtils.convertLittleEndianBytesToLong(data, 0, 4);
			fileOffset = PSTUtils.convertLittleEndianBytesToLong(data, 4, 8);
			size = (int)PSTUtils.convertLittleEndianBytesToLong(data, 8, 10);
			cRef = (int)PSTUtils.convertLittleEndianBytesToLong(data, 10, 12);
		} else {
			indexIdentifier = PSTUtils.convertLittleEndianBytesToLong(data, 0, 8);
			fileOffset = PSTUtils.convertLittleEndianBytesToLong(data, 8, 16);
			size = (int)PSTUtils.convertLittleEndianBytesToLong(data, 16, 18);
			cRef = (int)PSTUtils.convertLittleEndianBytesToLong(data, 16, 18);
		}
		//System.out.println("Data size: "+data.length);
		
	}
	

	@Override
	public String toString() {
		return "OffsetIndexItem\n"+
			"Index Identifier: "+indexIdentifier+" (0x"+Long.toHexString(indexIdentifier)+")\n"+
			"File Offset: "+fileOffset+" (0x"+Long.toHexString(fileOffset)+")\n"+
			"cRef: "+cRef+" (0x"+Long.toHexString(cRef)+" bin:"+Long.toBinaryString(cRef)+")\n"+
			"Size: "+size+" (0x"+Long.toHexString(size)+")";
	}


	public long getIndexIdentifier() {
		return indexIdentifier;
	}


	public long getFileOffset() {
		return fileOffset;
	}


	public int getSize() {
		return size;
	}
	
	
	
	
}
