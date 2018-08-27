package com.pff;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Vector;

public interface IFolder extends IObject {
    /**
     * get all of the sub folders...
     * there are not usually thousands, so we just do it in one big operation.
     *
     * @return all of the subfolders
     * @throws PSTException
     * @throws IOException
     */
    Vector<IFolder> getSubFolders() throws PSTException, IOException;

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
    Vector<IObject> getChildren(int numberToReturn) throws PSTException, IOException;

    LinkedList<Integer> getChildDescriptorNodes() throws PSTException, IOException;

    /**
     * Get the next child of this folder
     * As there could be thousands of emails, we have these kind of cursor
     * operations
     *
     * @return the next email in the folder or null if at the end of the folder
     * @throws PSTException
     * @throws IOException
     */
    IObject getNextChild() throws PSTException, IOException;

    /**
     * move the internal folder cursor to the desired position
     * position 0 is before the first record.
     *
     * @param newIndex
     */
    void moveChildCursorTo(int newIndex) throws IOException, PSTException;

    /**
     * the number of child folders in this folder
     *
     * @return number of subfolders as counted
     * @throws IOException
     * @throws PSTException
     */
    int getSubFolderCount() throws IOException, PSTException;

    /**
     * the number of emails in this folder
     * this is the count of emails made by the library and will therefore should
     * be more accurate than getContentCount
     *
     * @return number of emails in this folder (as counted)
     * @throws IOException
     * @throws PSTException
     */
    int getEmailCount() throws IOException, PSTException;

    int getFolderType();

    /**
     * the number of emails in this folder
     * this is as reported by the PST file, for a number calculated by the
     * library use getEmailCount
     *
     * @return number of items as reported by PST File
     */
    int getContentCount();

    /**
     * Amount of unread content items Integer 32-bit signed
     */
    int getUnreadCount();

    /**
     * does this folder have subfolders
     * once again, read from the PST, use getSubFolderCount if you want to know
     * what the library makes of it all
     *
     * @return has subfolders as reported by the PST File
     */
    boolean hasSubfolders();

    String getContainerClass();

    int getAssociateContentCount();

    /**
     * Container flags Integer 32-bit signed
     */
    int getContainerFlags();
}
