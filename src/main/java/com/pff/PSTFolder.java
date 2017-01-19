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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

/**
 * Represents a folder in the PST File
 * Allows you to access child folders or items. Items are accessed through a
 * sort of cursor arrangement.
 * This allows for incremental reading of a folder which may have _lots_ of
 * emails.
 * 
 * @author Richard Johnson
 */
public class PSTFolder extends PSTObject {

    /**
     * a constructor for the rest of us...
     * 
     * @param theFile
     * @param descriptorIndexNode
     * @throws PSTException
     * @throws IOException
     */
    PSTFolder(final PSTFile theFile, final DescriptorIndexNode descriptorIndexNode) throws PSTException, IOException {
        super(theFile, descriptorIndexNode);
    }

    /**
     * For pre-populating a folder object with values.
     * Not recommended for use outside this library
     * 
     * @param theFile
     * @param folderIndexNode
     * @param table
     */
    PSTFolder(final PSTFile theFile, final DescriptorIndexNode folderIndexNode, final PSTTableBC table,
        final HashMap<Integer, PSTDescriptorItem> localDescriptorItems) {
        super(theFile, folderIndexNode, table, localDescriptorItems);
    }

    /**
     * get all of the sub folders...
     * there are not usually thousands, so we just do it in one big operation.
     * 
     * @return all of the subfolders
     * @throws PSTException
     * @throws IOException
     */
    public Vector<PSTFolder> getSubFolders() throws PSTException, IOException {
        final Vector<PSTFolder> output = new Vector<>();
        try {
            this.initSubfoldersTable();
            final List<HashMap<Integer, PSTTable7CItem>> itemMapSet = this.subfoldersTable.getItems();
            for (final HashMap<Integer, PSTTable7CItem> itemMap : itemMapSet) {
                final PSTTable7CItem item = itemMap.get(26610);
                final PSTFolder folder = (PSTFolder) PSTObject.detectAndLoadPSTObject(this.pstFile,
                    item.entryValueReference);
                output.add(folder);
            }
        } catch (final PSTException err) {
            // hierarchy node doesn't exist: This is OK if child count is 0.
        	// Seen with special search folders at the top of the hierarchy:
        	// "8739 - SPAM Search Folder 2", "8739 - Content.Filter".
        	// this.subfoldersTable may remain uninitialized (null) in that case.
        	if (this.getContentCount() != 0) {
        		if (err.getMessage().startsWith("Can't get child folders")) {
        			throw err;
        		} else {
                	//err.printStackTrace();
                    throw new PSTException("Can't get child folders for folder " + this.getDisplayName() + "("
                            + this.getDescriptorNodeId() + ") child count: " + this.getContentCount() + " - " + err.toString(), err);
        		}
        	}
        }
        return output;
    }

    private void initSubfoldersTable() throws IOException, PSTException {
        if (this.subfoldersTable != null) {
            return;
        }

        final long folderDescriptorIndex = this.descriptorIndexNode.descriptorIdentifier + 11;
        try {
            final DescriptorIndexNode folderDescriptor = this.pstFile.getDescriptorIndexNode(folderDescriptorIndex);
            HashMap<Integer, PSTDescriptorItem> tmp = null;
            if (folderDescriptor.localDescriptorsOffsetIndexIdentifier > 0) {
                // tmp = new PSTDescriptor(pstFile,
                // folderDescriptor.localDescriptorsOffsetIndexIdentifier).getChildren();
                tmp = this.pstFile.getPSTDescriptorItems(folderDescriptor.localDescriptorsOffsetIndexIdentifier);
            }
            this.subfoldersTable = new PSTTable7C(new PSTNodeInputStream(this.pstFile,
                this.pstFile.getOffsetIndexNode(folderDescriptor.dataOffsetIndexIdentifier)), tmp);
        } catch (final PSTException err) {
            // hierarchy node doesn't exist
            throw new PSTException("Can't get child folders for folder " + this.getDisplayName() + "("
                + this.getDescriptorNodeId() + ") child count: " + this.getContentCount() + " - " + err.toString(), err);
        }
    }

    /**
     * internal vars for the tracking of things..
     */
    private int currentEmailIndex = 0;
    private final LinkedHashSet<DescriptorIndexNode> otherItems = null;

    private PSTTable7C emailsTable = null;
    private LinkedList<DescriptorIndexNode> fallbackEmailsTable = null;
    private PSTTable7C subfoldersTable = null;

    /**
     * this method goes through all of the children and sorts them into one of
     * the three hash sets.
     * 
     * @throws PSTException
     * @throws IOException
     */
    private void initEmailsTable() throws PSTException, IOException {
        if (this.emailsTable != null || this.fallbackEmailsTable != null) {
            return;
        }

        // some folder types don't have children:
        if (this.getNodeType() == PSTObject.NID_TYPE_SEARCH_FOLDER) {
            return;
        }

        try {
            final long folderDescriptorIndex = this.descriptorIndexNode.descriptorIdentifier + 12; // +12
                                                                                                   // lists
                                                                                                   // emails!
                                                                                                   // :D
            final DescriptorIndexNode folderDescriptor = this.pstFile.getDescriptorIndexNode(folderDescriptorIndex);
            HashMap<Integer, PSTDescriptorItem> tmp = null;
            if (folderDescriptor.localDescriptorsOffsetIndexIdentifier > 0) {
                // tmp = new PSTDescriptor(pstFile,
                // folderDescriptor.localDescriptorsOffsetIndexIdentifier).getChildren();
                tmp = this.pstFile.getPSTDescriptorItems(folderDescriptor.localDescriptorsOffsetIndexIdentifier);
            }
            // PSTTable7CForFolder folderDescriptorTable = new
            // PSTTable7CForFolder(folderDescriptor.dataBlock.data,
            // folderDescriptor.dataBlock.blockOffsets,tmp, 0x67F2);
            this.emailsTable = new PSTTable7C(new PSTNodeInputStream(this.pstFile,
                this.pstFile.getOffsetIndexNode(folderDescriptor.dataOffsetIndexIdentifier)), tmp, 0x67F2);
        } catch (final Exception err) {

            // here we have to attempt to fallback onto the children as listed
            // by the descriptor b-tree
            final LinkedHashMap<Integer, LinkedList<DescriptorIndexNode>> tree = this.pstFile.getChildDescriptorTree();

            this.fallbackEmailsTable = new LinkedList<>();
            final LinkedList<DescriptorIndexNode> allChildren = tree.get(this.getDescriptorNode().descriptorIdentifier);

            if (allChildren != null) {
                // quickly go through and remove those entries that are not
                // messages!
                for (final DescriptorIndexNode node : allChildren) {
                    if (node != null
                        && PSTObject.getNodeType(node.descriptorIdentifier) == PSTObject.NID_TYPE_NORMAL_MESSAGE) {
                        this.fallbackEmailsTable.add(node);
                    }
                }
            }

            System.err.println("Can't get children for folder " + this.getDisplayName() + "("
                + this.getDescriptorNodeId() + ") child count: " + this.getContentCount() + " - " + err.toString()
                + ", using alternate child tree with " + this.fallbackEmailsTable.size() + " items");
        }
    }

    /**
     * get some children from the folder
     * This is implemented as a cursor of sorts, as there could be thousands
     * and that is just too many to process at once.
     * 
     * @param numberToReturn
     * @return bunch of children in this folder
     * @throws PSTException
     * @throws IOException
     */
    public Vector<PSTObject> getChildren(final int numberToReturn) throws PSTException, IOException {
        this.initEmailsTable();

        final Vector<PSTObject> output = new Vector<>();
        if (this.emailsTable != null) {
            final List<HashMap<Integer, PSTTable7CItem>> rows = this.emailsTable.getItems(this.currentEmailIndex,
                numberToReturn);

            for (int x = 0; x < rows.size(); x++) {
                if (this.currentEmailIndex >= this.getContentCount()) {
                    // no more!
                    break;
                }
                // get the emails from the rows
                final PSTTable7CItem emailRow = rows.get(x).get(0x67F2);
                final DescriptorIndexNode childDescriptor = this.pstFile
                    .getDescriptorIndexNode(emailRow.entryValueReference);
                final PSTObject child = PSTObject.detectAndLoadPSTObject(this.pstFile, childDescriptor);
                output.add(child);
                this.currentEmailIndex++;
            }
        } else if (this.fallbackEmailsTable != null) {
            // we use the fallback
            final ListIterator<DescriptorIndexNode> iterator = this.fallbackEmailsTable
                .listIterator(this.currentEmailIndex);
            for (int x = 0; x < numberToReturn; x++) {
                if (this.currentEmailIndex >= this.getContentCount()) {
                    // no more!
                    break;
                }
                final DescriptorIndexNode childDescriptor = iterator.next();
                final PSTObject child = PSTObject.detectAndLoadPSTObject(this.pstFile, childDescriptor);
                output.add(child);
                this.currentEmailIndex++;
            }
        }

        return output;
    }

    public LinkedList<Integer> getChildDescriptorNodes() throws PSTException, IOException {
        this.initEmailsTable();
        if (this.emailsTable == null) {
            return new LinkedList<>();
        }
        final LinkedList<Integer> output = new LinkedList<>();
        final List<HashMap<Integer, PSTTable7CItem>> rows = this.emailsTable.getItems();
        for (final HashMap<Integer, PSTTable7CItem> row : rows) {
            // get the emails from the rows
            if (this.currentEmailIndex == this.getContentCount()) {
                // no more!
                break;
            }
            final PSTTable7CItem emailRow = row.get(0x67F2);
            if (emailRow.entryValueReference == 0) {
                break;
            }
            output.add(emailRow.entryValueReference);
        }
        return output;
    }

    /**
     * Get the next child of this folder
     * As there could be thousands of emails, we have these kind of cursor
     * operations
     * 
     * @return the next email in the folder or null if at the end of the folder
     * @throws PSTException
     * @throws IOException
     */
    public PSTObject getNextChild() throws PSTException, IOException {
        this.initEmailsTable();

        if (this.emailsTable != null) {
            final List<HashMap<Integer, PSTTable7CItem>> rows = this.emailsTable.getItems(this.currentEmailIndex, 1);

            if (this.currentEmailIndex == this.getContentCount()) {
                // no more!
                return null;
            }
            // get the emails from the rows
            final PSTTable7CItem emailRow = rows.get(0).get(0x67F2);
            final DescriptorIndexNode childDescriptor = this.pstFile
                .getDescriptorIndexNode(emailRow.entryValueReference);
            final PSTObject child = PSTObject.detectAndLoadPSTObject(this.pstFile, childDescriptor);
            this.currentEmailIndex++;

            return child;
        } else if (this.fallbackEmailsTable != null) {
            if (this.currentEmailIndex >= this.getContentCount()
                || this.currentEmailIndex >= this.fallbackEmailsTable.size()) {
                // no more!
                return null;
            }
            // get the emails from the rows
            final DescriptorIndexNode childDescriptor = this.fallbackEmailsTable.get(this.currentEmailIndex);
            final PSTObject child = PSTObject.detectAndLoadPSTObject(this.pstFile, childDescriptor);
            this.currentEmailIndex++;
            return child;
        }
        return null;
    }

    /**
     * move the internal folder cursor to the desired position
     * position 0 is before the first record.
     * 
     * @param newIndex
     */
    public void moveChildCursorTo(int newIndex) throws IOException, PSTException {
        this.initEmailsTable();

        if (newIndex < 1) {
            this.currentEmailIndex = 0;
            return;
        }
        if (newIndex > this.getContentCount()) {
            newIndex = this.getContentCount();
        }
        this.currentEmailIndex = newIndex;
    }

    /**
     * the number of child folders in this folder
     * 
     * @return number of subfolders as counted
     * @throws IOException
     * @throws PSTException
     */
    public int getSubFolderCount() throws IOException, PSTException {
        this.initSubfoldersTable();
        if (this.subfoldersTable != null) {
            return this.subfoldersTable.getRowCount();
        } else {
            return 0;
        }
    }

    /**
     * the number of emails in this folder
     * this is the count of emails made by the library and will therefore should
     * be more accurate than getContentCount
     * 
     * @return number of emails in this folder (as counted)
     * @throws IOException
     * @throws PSTException
     */
    public int getEmailCount() throws IOException, PSTException {
        this.initEmailsTable();
        if (this.emailsTable == null) {
            return -1;
        }
        return this.emailsTable.getRowCount();
    }

    public int getFolderType() {
        return this.getIntItem(0x3601);
    }

    /**
     * the number of emails in this folder
     * this is as reported by the PST file, for a number calculated by the
     * library use getEmailCount
     * 
     * @return number of items as reported by PST File
     */
    public int getContentCount() {
        return this.getIntItem(0x3602);
    }

    /**
     * Amount of unread content items Integer 32-bit signed
     */
    public int getUnreadCount() {
        return this.getIntItem(0x3603);
    }

    /**
     * does this folder have subfolders
     * once again, read from the PST, use getSubFolderCount if you want to know
     * what the library makes of it all
     * 
     * @return has subfolders as reported by the PST File
     */
    public boolean hasSubfolders() {
        return (this.getIntItem(0x360a) != 0);
    }

    public String getContainerClass() {
        return this.getStringItem(0x3613);
    }

    public int getAssociateContentCount() {
        return this.getIntItem(0x3617);
    }

    /**
     * Container flags Integer 32-bit signed
     */
    public int getContainerFlags() {
        return this.getIntItem(0x3600);
    }

}
// vim: set noexpandtab:
