package com.pff;

import java.util.Date;

public interface IObject {
    String getItemsString();

    /**
     * get the descriptor node for this item
     * this identifies the location of the node in the BTree and associated info
     *
     * @return item's descriptor node
     */
    DescriptorIndexNode getDescriptorNode();

    /**
     * get the descriptor identifier for this item
     * can be used for loading objects through detectAndLoadPSTObject(PSTFile
     * theFile, long descriptorIndex)
     *
     * @return item's descriptor node identifier
     */
    long getDescriptorNodeId();

    int getNodeType();

    Date getDateItem(int identifier);

    String getMessageClass();

    /**
     * get the display name
     */
    String getDisplayName();

    /**
     * Address type
     * Known values are SMTP, EX (Exchange) and UNKNOWN
     */
    String getAddrType();

    /**
     * E-mail address
     */
    String getEmailAddress();

    /**
     * Comment
     */
    String getComment();

    /**
     * Creation time
     */
    Date getCreationTime();

    /**
     * Modification time
     */
    Date getLastModificationTime();
}
