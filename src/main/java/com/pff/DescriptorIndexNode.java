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

/**
 * DescriptorIndexNode is a leaf item from the Descriptor index b-tree
 * It is like a pointer to an element in the PST file, everything has one...
 * 
 * @author Richard Johnson
 */
public class DescriptorIndexNode {
    public int descriptorIdentifier;
    public long dataOffsetIndexIdentifier;
    public long localDescriptorsOffsetIndexIdentifier;
    public int parentDescriptorIndexIdentifier;
    public int itemType;

    // PSTFile.PSTFileBlock dataBlock = null;

    /**
     * parse the data out into something meaningful
     * 
     * @param data
     */
    DescriptorIndexNode(final byte[] data, final int pstFileType) {
        // parse it out
        // first 4 bytes
        if (pstFileType == PSTFile.PST_TYPE_ANSI) {
            this.descriptorIdentifier = (int) PSTObject.convertLittleEndianBytesToLong(data, 0, 4);
            this.dataOffsetIndexIdentifier = (int) PSTObject.convertLittleEndianBytesToLong(data, 4, 8);
            this.localDescriptorsOffsetIndexIdentifier = (int) PSTObject.convertLittleEndianBytesToLong(data, 8, 12);
            this.parentDescriptorIndexIdentifier = (int) PSTObject.convertLittleEndianBytesToLong(data, 12, 16);
            // itemType = (int)PSTObject.convertLittleEndianBytesToLong(data,
            // 28, 32);
        } else {
            this.descriptorIdentifier = (int) PSTObject.convertLittleEndianBytesToLong(data, 0, 4);
            this.dataOffsetIndexIdentifier = (int) PSTObject.convertLittleEndianBytesToLong(data, 8, 16);
            this.localDescriptorsOffsetIndexIdentifier = (int) PSTObject.convertLittleEndianBytesToLong(data, 16, 24);
            this.parentDescriptorIndexIdentifier = (int) PSTObject.convertLittleEndianBytesToLong(data, 24, 28);
            this.itemType = (int) PSTObject.convertLittleEndianBytesToLong(data, 28, 32);
        }
    }

    /*
     * void readData(PSTFile file)
     * throws IOException, PSTException
     * {
     * if ( dataBlock == null ) {
     * dataBlock = file.readLeaf(dataOffsetIndexIdentifier);
     * }
     * }
     *
     */

    PSTNodeInputStream getNodeInputStream(final PSTFile pstFile) throws IOException, PSTException {
        return new PSTNodeInputStream(pstFile, pstFile.getOffsetIndexNode(this.dataOffsetIndexIdentifier));
    }

    @Override
    public String toString() {

        return "DescriptorIndexNode\n" + "Descriptor Identifier: " + this.descriptorIdentifier + " (0x"
            + Long.toHexString(this.descriptorIdentifier) + ")\n" + "Data offset identifier: "
            + this.dataOffsetIndexIdentifier + " (0x" + Long.toHexString(this.dataOffsetIndexIdentifier) + ")\n"
            + "Local descriptors offset index identifier: " + this.localDescriptorsOffsetIndexIdentifier + " (0x"
            + Long.toHexString(this.localDescriptorsOffsetIndexIdentifier) + ")\n"
            + "Parent Descriptor Index Identifier: " + this.parentDescriptorIndexIdentifier + " (0x"
            + Long.toHexString(this.parentDescriptorIndexIdentifier) + ")\n" + "Item Type: " + this.itemType + " (0x"
            + Long.toHexString(this.itemType) + ")";
    }
}
