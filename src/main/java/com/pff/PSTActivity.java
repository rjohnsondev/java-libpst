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
import java.util.Date;
import java.util.HashMap;

/**
 * PSTActivity represents Journal entries
 * 
 * @author Richard Johnson
 */
public class PSTActivity extends PSTMessage implements IActivity {

    /**
     * @param theFile
     * @param descriptorIndexNode
     * @throws PSTException
     * @throws IOException
     */
    public PSTActivity(final PSTFile theFile, final DescriptorIndexNode descriptorIndexNode)
        throws PSTException, IOException {
        super(theFile, descriptorIndexNode);
    }

    /**
     * @param theFile
     * @param folderIndexNode
     * @param table
     * @param localDescriptorItems
     */
    public PSTActivity(final PSTFile theFile, final DescriptorIndexNode folderIndexNode, final PSTTableBC table,
        final HashMap<Integer, PSTDescriptorItem> localDescriptorItems) {
        super(theFile, folderIndexNode, table, localDescriptorItems);
    }

    /**
     * Type
     */
    @Override
    public String getLogType() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008700, PSTFile.PSETID_Log));
    }

    /**
     * Start
     */
    @Override
    public Date getLogStart() {
        return this.getDateItem(this.pstFile.getNameToIdMapItem(0x00008706, PSTFile.PSETID_Log));
    }

    /**
     * Duration
     */
    @Override
    public int getLogDuration() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008707, PSTFile.PSETID_Log));
    }

    /**
     * End
     */
    @Override
    public Date getLogEnd() {
        return this.getDateItem(this.pstFile.getNameToIdMapItem(0x00008708, PSTFile.PSETID_Log));
    }

    /**
     * LogFlags
     */
    @Override
    public int getLogFlags() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x0000870c, PSTFile.PSETID_Log));
    }

    /**
     * DocPrinted
     */
    @Override
    public boolean isDocumentPrinted() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x0000870e, PSTFile.PSETID_Log)));
    }

    /**
     * DocSaved
     */
    @Override
    public boolean isDocumentSaved() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x0000870f, PSTFile.PSETID_Log)));
    }

    /**
     * DocRouted
     */
    @Override
    public boolean isDocumentRouted() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x00008710, PSTFile.PSETID_Log)));
    }

    /**
     * DocPosted
     */
    @Override
    public boolean isDocumentPosted() {
        return (this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x00008711, PSTFile.PSETID_Log)));
    }

    /**
     * Type Description
     */
    @Override
    public String getLogTypeDesc() {
        return this.getStringItem(this.pstFile.getNameToIdMapItem(0x00008712, PSTFile.PSETID_Log));
    }

    @Override
    public String toString() {
        return "Type ASCII or Unicode string: " + this.getLogType() + "\n" + "Start Filetime: " + this.getLogStart()
            + "\n" + "Duration Integer 32-bit signed: " + this.getLogDuration() + "\n" + "End Filetime: "
            + this.getLogEnd() + "\n" + "LogFlags Integer 32-bit signed: " + this.getLogFlags() + "\n"
            + "DocPrinted Boolean: " + this.isDocumentPrinted() + "\n" + "DocSaved Boolean: " + this.isDocumentSaved()
            + "\n" + "DocRouted Boolean: " + this.isDocumentRouted() + "\n" + "DocPosted Boolean: "
            + this.isDocumentPosted() + "\n" + "TypeDescription ASCII or Unicode string: " + this.getLogTypeDesc();

    }

}
