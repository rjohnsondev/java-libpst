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
import java.util.HashMap;

/**
 * Object that represents a RSS item
 * 
 * @author Richard Johnson
 */
public class PSTRss extends PSTMessage implements IRss {

    /**
     * @param theFile
     * @param descriptorIndexNode
     * @throws PSTException
     * @throws IOException
     */
    public PSTRss(final PSTFile theFile, final DescriptorIndexNode descriptorIndexNode)
        throws PSTException, IOException {
        super(theFile, descriptorIndexNode);
    }

    /**
     * @param theFile
     * @param folderIndexNode
     * @param table
     * @param localDescriptorItems
     */
    public PSTRss(final PSTFile theFile, final DescriptorIndexNode folderIndexNode, final PSTTableBC table,
        final HashMap<Integer, PSTDescriptorItem> localDescriptorItems) {
        super(theFile, folderIndexNode, table, localDescriptorItems);
    }

    /**
     * Channel
     */
    @Override
    public String getPostRssChannelLink() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008900, PSTFile.PSETID_PostRss));
    }

    /**
     * Item link
     */
    @Override
    public String getPostRssItemLink() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008901, PSTFile.PSETID_PostRss));
    }

    /**
     * Item hash Integer 32-bit signed
     */
    @Override
    public int getPostRssItemHash() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008902, PSTFile.PSETID_PostRss));
    }

    /**
     * Item GUID
     */
    @Override
    public String getPostRssItemGuid() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008903, PSTFile.PSETID_PostRss));
    }

    /**
     * Channel GUID
     */
    @Override
    public String getPostRssChannel() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008904, PSTFile.PSETID_PostRss));
    }

    /**
     * Item XML
     */
    @Override
    public String getPostRssItemXml() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008905, PSTFile.PSETID_PostRss));
    }

    /**
     * Subscription
     */
    @Override
    public String getPostRssSubscription() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008906, PSTFile.PSETID_PostRss));
    }

    @Override
    public String toString() {
        return "Channel ASCII or Unicode string values: " + this.getPostRssChannelLink() + "\n"
            + "Item link ASCII or Unicode string values: " + this.getPostRssItemLink() + "\n"
            + "Item hash Integer 32-bit signed: " + this.getPostRssItemHash() + "\n"
            + "Item GUID ASCII or Unicode string values: " + this.getPostRssItemGuid() + "\n"
            + "Channel GUID ASCII or Unicode string values: " + this.getPostRssChannel() + "\n"
            + "Item XML ASCII or Unicode string values: " + this.getPostRssItemXml() + "\n"
            + "Subscription ASCII or Unicode string values: " + this.getPostRssSubscription();
    }
}
