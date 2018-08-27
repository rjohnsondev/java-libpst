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
 * PST Message contains functions that are common across most MAPI objects.
 * Note that many of these functions may not be applicable for the item in
 * question,
 * however there seems to be no hard and fast outline for what properties apply
 * to which
 * objects. For properties where no value is set, a blank value is returned
 * (rather than
 * an exception being raised).
 * 
 * @author Richard Johnson
 */
public class PSTMessage extends PSTObject implements IMessage {

    public static final int IMPORTANCE_LOW = 0;
    public static final int IMPORTANCE_NORMAL = 1;
    public static final int IMPORTANCE_HIGH = 2;

    PSTMessage(final PSTFile theFile, final DescriptorIndexNode descriptorIndexNode) throws PSTException, IOException {
        super(theFile, descriptorIndexNode);
    }

    PSTMessage(final PSTFile theFile, final DescriptorIndexNode folderIndexNode, final PSTTableBC table,
        final HashMap<Integer, PSTDescriptorItem> localDescriptorItems) {
        super(theFile, folderIndexNode, table, localDescriptorItems);
    }

    @Override
    public String getRTFBody() throws PSTException, IOException {
        // do we have an entry for it?
        if (this.items.containsKey(0x1009)) {
            // is it a reference?
            final PSTTableBCItem item = this.items.get(0x1009);
            if (item.data.length > 0) {
                return (LZFu.decode(item.data));
            }
            final int ref = item.entryValueReference;
            final PSTDescriptorItem descItem = this.localDescriptorItems.get(ref);
            if (descItem != null) {
                return LZFu.decode(descItem.getData());
            }
        }

        return "";
    }

    /**
     * get the importance of the email
     * 
     * @return IMPORTANCE_NORMAL if unknown
     */
    @Override
    public int getImportance() {
        return this.getIntItem(0x0017, IMPORTANCE_NORMAL);
    }

    /**
     * get the message class for the email
     * 
     * @return empty string if unknown
     */
    @Override
    public String getMessageClass() {
        return this.getStringItem(0x001a);
    }

    /**
     * get the subject
     * 
     * @return empty string if not found
     */
    @Override
    public String getSubject() {
        String subject = this.getStringItem(0x0037);

        // byte[] controlCodesA = {0x01, 0x01};
        // byte[] controlCodesB = {0x01, 0x05};
        // byte[] controlCodesC = {0x01, 0x10};
        if (subject != null && (subject.length() >= 2) &&

        // (subject.startsWith(new String(controlCodesA)) ||
        // subject.startsWith(new String(controlCodesB)) ||
        // subject.startsWith(new String(controlCodesC)))
            subject.charAt(0) == 0x01) {
            if (subject.length() == 2) {
                subject = "";
            } else {
                subject = subject.substring(2, subject.length());
            }
        }
        return subject;
    }

    /**
     * get the client submit time
     * 
     * @return null if not found
     */
    @Override
    public Date getClientSubmitTime() {
        return this.getDateItem(0x0039);
    }

    /**
     * get received by name
     * 
     * @return empty string if not found
     */
    @Override
    public String getReceivedByName() {
        return this.getStringItem(0x0040);
    }

    /**
     * get sent representing name
     * 
     * @return empty string if not found
     */
    @Override
    public String getSentRepresentingName() {
        return this.getStringItem(0x0042);
    }

    /**
     * Sent representing address type
     * Known values are SMTP, EX (Exchange) and UNKNOWN
     * 
     * @return empty string if not found
     */
    @Override
    public String getSentRepresentingAddressType() {
        return this.getStringItem(0x0064);
    }

    /**
     * Sent representing email address
     * 
     * @return empty string if not found
     */
    @Override
    public String getSentRepresentingEmailAddress() {
        return this.getStringItem(0x0065);
    }

    /**
     * Conversation topic
     * This is basically the subject from which Fwd:, Re, etc. has been removed
     * 
     * @return empty string if not found
     */
    @Override
    public String getConversationTopic() {
        return this.getStringItem(0x0070);
    }

    /**
     * Received by address type
     * Known values are SMTP, EX (Exchange) and UNKNOWN
     * 
     * @return empty string if not found
     */
    @Override
    public String getReceivedByAddressType() {
        return this.getStringItem(0x0075);
    }

    /**
     * Received by email address
     * 
     * @return empty string if not found
     */
    @Override
    public String getReceivedByAddress() {
        return this.getStringItem(0x0076);
    }

    /**
     * Transport message headers ASCII or Unicode string These contain the SMTP
     * e-mail headers.
     */
    @Override
    public String getTransportMessageHeaders() {
        return this.getStringItem(0x007d);
    }

    @Override
    public boolean isRead() {
        return ((this.getIntItem(0x0e07) & 0x01) != 0);
    }

    @Override
    public boolean isUnmodified() {
        return ((this.getIntItem(0x0e07) & 0x02) != 0);
    }

    @Override
    public boolean isSubmitted() {
        return ((this.getIntItem(0x0e07) & 0x04) != 0);
    }

    @Override
    public boolean isUnsent() {
        return ((this.getIntItem(0x0e07) & 0x08) != 0);
    }

    @Override
    public boolean hasAttachments() {
        return ((this.getIntItem(0x0e07) & 0x10) != 0);
    }

    @Override
    public boolean isFromMe() {
        return ((this.getIntItem(0x0e07) & 0x20) != 0);
    }

    @Override
    public boolean isAssociated() {
        return ((this.getIntItem(0x0e07) & 0x40) != 0);
    }

    @Override
    public boolean isResent() {
        return ((this.getIntItem(0x0e07) & 0x80) != 0);
    }

    /**
     * Acknowledgment mode Integer 32-bit signed
     */
    @Override
    public int getAcknowledgementMode() {
        return this.getIntItem(0x0001);
    }

    /**
     * Originator delivery report requested set if the sender wants a delivery
     * report from all recipients 0 = false 0 != true
     */
    @Override
    public boolean getOriginatorDeliveryReportRequested() {
        return (this.getIntItem(0x0023) != 0);
    }

    // 0x0025 0x0102 PR_PARENT_KEY Parent key Binary data Contains a GUID
    /**
     * Priority Integer 32-bit signed -1 = NonUrgent 0 = Normal 1 = Urgent
     */
    @Override
    public int getPriority() {
        return this.getIntItem(0x0026);
    }

    /**
     * Read Receipt Requested Boolean 0 = false 0 != true
     */
    @Override
    public boolean getReadReceiptRequested() {
        return (this.getIntItem(0x0029) != 0);
    }

    /**
     * Recipient Reassignment Prohibited Boolean 0 = false 0 != true
     */
    @Override
    public boolean getRecipientReassignmentProhibited() {
        return (this.getIntItem(0x002b) != 0);
    }

    /**
     * Original sensitivity Integer 32-bit signed the sensitivity of the message
     * before being replied to or forwarded 0 = None 1 = Personal 2 = Private 3
     * = Company Confidential
     */
    @Override
    public int getOriginalSensitivity() {
        return this.getIntItem(0x002e);
    }

    /**
     * Sensitivity Integer 32-bit signed sender's opinion of the sensitivity of
     * an email 0 = None 1 = Personal 2 = Private 3 = Company Confidential
     */
    @Override
    public int getSensitivity() {
        return this.getIntItem(0x0036);
    }
    // 0x003f 0x0102 PR_RECEIVED_BY_ENTRYID (PidTagReceivedByEntr yId) Received
    // by entry identifier Binary data Contains recipient/sender structure
    // 0x0041 0x0102 PR_SENT_REPRESENTING_ENTRYID Sent representing entry
    // identifier Binary data Contains recipient/sender structure
    // 0x0043 0x0102 PR_RCVD_REPRESENTING_ENTRYID Received representing entry
    // identifier Binary data Contains recipient/sender structure

    /*
     * Address book search key
     */
    @Override
    public byte[] getPidTagSentRepresentingSearchKey() {
        return this.getBinaryItem(0x003b);
    }

    /**
     * Received representing name ASCII or Unicode string
     */
    @Override
    public String getRcvdRepresentingName() {
        return this.getStringItem(0x0044);
    }

    /**
     * Original subject ASCII or Unicode string
     */
    @Override
    public String getOriginalSubject() {
        return this.getStringItem(0x0049);
    }

    // 0x004e 0x0040 PR_ORIGINAL_SUBMIT_TIME Original submit time Filetime
    /**
     * Reply recipients names ASCII or Unicode string
     */
    @Override
    public String getReplyRecipientNames() {
        return this.getStringItem(0x0050);
    }

    /**
     * My address in To field Boolean
     */
    @Override
    public boolean getMessageToMe() {
        return (this.getIntItem(0x0057) != 0);
    }

    /**
     * My address in CC field Boolean
     */
    @Override
    public boolean getMessageCcMe() {
        return (this.getIntItem(0x0058) != 0);
    }

    /**
     * Indicates that the receiving mailbox owner is a primary or a carbon copy
     * (Cc) recipient
     */
    @Override
    public boolean getMessageRecipMe() {
        return this.getIntItem(0x0059) != 0;
    }

    /**
     * Response requested Boolean
     */
    @Override
    public boolean getResponseRequested() {
        return this.getBooleanItem(0x0063);
    }

    /**
     * Sent representing address type ASCII or Unicode string Known values are
     * SMTP, EX (Exchange) and UNKNOWN
     */
    @Override
    public String getSentRepresentingAddrtype() {
        return this.getStringItem(0x0064);
    }

    // 0x0071 0x0102 PR_CONVERSATION_INDEX (PidTagConversationInd ex)
    // Conversation index Binary data
    /**
     * Original display BCC ASCII or Unicode string
     */
    @Override
    public String getOriginalDisplayBcc() {
        return this.getStringItem(0x0072);
    }

    /**
     * Original display CC ASCII or Unicode string
     */
    @Override
    public String getOriginalDisplayCc() {
        return this.getStringItem(0x0073);
    }

    /**
     * Original display TO ASCII or Unicode string
     */
    @Override
    public String getOriginalDisplayTo() {
        return this.getStringItem(0x0074);
    }

    /**
     * Received representing address type.
     * Known values are SMTP, EX (Exchange) and UNKNOWN
     */
    @Override
    public String getRcvdRepresentingAddrtype() {
        return this.getStringItem(0x0077);
    }

    /**
     * Received representing e-mail address
     */
    @Override
    public String getRcvdRepresentingEmailAddress() {
        return this.getStringItem(0x0078);
    }

    /**
     * Recipient details
     */

    /**
     * Non receipt notification requested
     */
    @Override
    public boolean isNonReceiptNotificationRequested() {
        return (this.getIntItem(0x0c06) != 0);
    }

    /**
     * Originator non delivery report requested
     */
    @Override
    public boolean isOriginatorNonDeliveryReportRequested() {
        return (this.getIntItem(0x0c08) != 0);
    }

    public static final int RECIPIENT_TYPE_TO = 1;
    public static final int RECIPIENT_TYPE_CC = 2;

    /**
     * Recipient type Integer 32-bit signed 0x01 => To 0x02 =>CC
     */
    @Override
    public int getRecipientType() {
        return this.getIntItem(0x0c15);
    }

    /**
     * Reply requested
     */
    @Override
    public boolean isReplyRequested() {
        return (this.getIntItem(0x0c17) != 0);
    }

    /*
     * Sending mailbox owner's address book entry ID
     */
    @Override
    public byte[] getSenderEntryId() {
        return this.getBinaryItem(0x0c19);
    }

    /**
     * Sender name
     */
    @Override
    public String getSenderName() {
        return this.getStringItem(0x0c1a);
    }

    /**
     * Sender address type.
     * Known values are SMTP, EX (Exchange) and UNKNOWN
     */
    @Override
    public String getSenderAddrtype() {
        return this.getStringItem(0x0c1e);
    }

    /**
     * Sender e-mail address
     */
    @Override
    public String getSenderEmailAddress() {
        return this.getStringItem(0x0c1f);
    }

    /**
     * Non-transmittable message properties
     */

    /**
     * Message size
     */
    @Override
    public long getMessageSize() {
        return this.getLongItem(0x0e08);
    }

    /**
     * Internet article number
     */
    @Override
    public int getInternetArticleNumber() {
        return this.getIntItem(0x0e23);
    }

    /*
     * Server that the client should attempt to send the mail with
     */
    @Override
    public String getPrimarySendAccount() {
        return this.getStringItem(0x0e28);
    }

    /*
     * Server that the client is currently using to send mail
     */
    @Override
    public String getNextSendAcct() {
        return this.getStringItem(0x0e29);
    }

    /**
     * URL computer name postfix
     */
    @Override
    public int getURLCompNamePostfix() {
        return this.getIntItem(0x0e61);
    }

    /**
     * Object type
     */
    @Override
    public int getObjectType() {
        return this.getIntItem(0x0ffe);
    }

    /**
     * Delete after submit
     */
    @Override
    public boolean getDeleteAfterSubmit() {
        return ((this.getIntItem(0x0e01)) != 0);
    }

    /**
     * Responsibility
     */
    @Override
    public boolean getResponsibility() {
        return ((this.getIntItem(0x0e0f)) != 0);
    }

    /**
     * Compressed RTF in Sync Boolean
     */
    @Override
    public boolean isRTFInSync() {
        return ((this.getIntItem(0x0e1f)) != 0);
    }

    /**
     * URL computer name set
     */
    @Override
    public boolean isURLCompNameSet() {
        return ((this.getIntItem(0x0e62)) != 0);
    }

    /**
     * Display BCC
     */
    @Override
    public String getDisplayBCC() {
        return this.getStringItem(0x0e02);
    }

    /**
     * Display CC
     */
    @Override
    public String getDisplayCC() {
        return this.getStringItem(0x0e03);
    }

    /**
     * Display To
     */
    @Override
    public String getDisplayTo() {
        return this.getStringItem(0x0e04);
    }

    /**
     * Message delivery time
     */
    @Override
    public Date getMessageDeliveryTime() {
        return this.getDateItem(0x0e06);
    }

    //
    // public int getFlags() {
    // if (this.items.containsKey(0x0e17)) {
    // System.out.println(this.items.get(0x0e17));
    // }
    // return this.getIntItem(0x0e17);
    // }
    //
    // /**
    // * The message is to be highlighted in recipients' folder displays.
    // */
    // public boolean isHighlighted() {
    // return (this.getIntItem(0x0e17) & 0x1) != 0;
    // }
    //
    // /**
    // * The message has been tagged for a client-defined purpose.
    // */
    // public boolean isTagged() {
    // return (this.getIntItem(0x0e17) & 0x2) != 0;
    // }
    //
    // /**
    // * The message is to be suppressed from recipients' folder displays.
    // */
    // public boolean isHidden() {
    // return (this.getIntItem(0x0e17) & 0x4) != 0;
    // }
    //
    // /**
    // * The message has been marked for subsequent deletion
    // */
    // public boolean isDelMarked() {
    // return (this.getIntItem(0x0e17) & 0x8) != 0;
    // }
    //
    // /**
    // * The message is in draft revision status.
    // */
    // public boolean isDraft() {
    // return (this.getIntItem(0x0e17) & 0x100) != 0;
    // }
    //
    // /**
    // * The message has been replied to.
    // */
    // public boolean isAnswered() {
    // return (this.getIntItem(0x0e17) & 0x200) != 0;
    // }
    //
    // /**
    // * The message has been marked for downloading from the remote message
    // store to the local client
    // */
    // public boolean isMarkedForDownload() {
    // return (this.getIntItem(0x0e17) & 0x1000) != 0;
    // }
    //
    // /**
    // * The message has been marked for deletion at the remote message store
    // without downloading to the local client.
    // */
    // public boolean isRemoteDelMarked() {
    // return (this.getIntItem(0x0e17) & 0x2000) != 0;
    // }

    /**
     * Message content properties
     */
    @Override
    public int getNativeBodyType() {
        return this.getIntItem(0x1016);
    }

    /**
     * Plain text e-mail body
     */
    @Override
    public String getBody() {
        String cp = null;
        PSTTableBCItem cpItem = this.items.get(0x3FFD); // PidTagMessageCodepage
        if (cpItem == null) {
            cpItem = this.items.get(0x3FDE); // PidTagInternetCodepage
        }
        if (cpItem != null) {
            cp = PSTFile.getInternetCodePageCharset(cpItem.entryValueReference);
        }
        return this.getStringItem(0x1000, 0, cp);
    }

    /*
     * Plain text body prefix
     */
    @Override
    public String getBodyPrefix() {
        return this.getStringItem(0x6619);
    }

    /**
     * RTF Sync Body CRC
     */
    @Override
    public int getRTFSyncBodyCRC() {
        return this.getIntItem(0x1006);
    }

    /**
     * RTF Sync Body character count
     */
    @Override
    public int getRTFSyncBodyCount() {
        return this.getIntItem(0x1007);
    }

    /**
     * RTF Sync body tag
     */
    @Override
    public String getRTFSyncBodyTag() {
        return this.getStringItem(0x1008);
    }

    /**
     * RTF whitespace prefix count
     */
    @Override
    public int getRTFSyncPrefixCount() {
        return this.getIntItem(0x1010);
    }

    /**
     * RTF whitespace tailing count
     */
    @Override
    public int getRTFSyncTrailingCount() {
        return this.getIntItem(0x1011);
    }

    /**
     * HTML e-mail body
     */
    @Override
    public String getBodyHTML() {
        String cp = null;
        PSTTableBCItem cpItem = this.items.get(0x3FDE); // PidTagInternetCodepage
        if (cpItem == null) {
            cpItem = this.items.get(0x3FFD); // PidTagMessageCodepage
        }
        if (cpItem != null) {
            cp = PSTFile.getInternetCodePageCharset(cpItem.entryValueReference);
        }
        return this.getStringItem(0x1013, 0, cp);
    }

    /**
     * Message ID for this email as allocated per rfc2822
     */
    @Override
    public String getInternetMessageId() {
        return this.getStringItem(0x1035);
    }

    /**
     * In-Reply-To
     */
    @Override
    public String getInReplyToId() {
        return this.getStringItem(0x1042);
    }

    /**
     * Return Path
     */
    @Override
    public String getReturnPath() {
        return this.getStringItem(0x1046);
    }

    /**
     * Icon index
     */
    @Override
    public int getIconIndex() {
        return this.getIntItem(0x1080);
    }

    /**
     * Action flag
     * This relates to the replying / forwarding of messages.
     * It is classified as "unknown" atm, so just provided here
     * in case someone works out what all the various flags mean.
     */
    @Override
    public int getActionFlag() {
        return this.getIntItem(0x1081);
    }

    /**
     * is the action flag for this item "forward"?
     */
    @Override
    public boolean hasForwarded() {
        final int actionFlag = this.getIntItem(0x1081);
        return ((actionFlag & 0x8) > 0);
    }

    /**
     * is the action flag for this item "replied"?
     */
    @Override
    public boolean hasReplied() {
        final int actionFlag = this.getIntItem(0x1081);
        return ((actionFlag & 0x4) > 0);
    }

    /**
     * the date that this item had an action performed (eg. replied or
     * forwarded)
     */
    @Override
    public Date getActionDate() {
        return this.getDateItem(0x1082);
    }

    /**
     * Disable full fidelity
     */
    @Override
    public boolean getDisableFullFidelity() {
        return (this.getIntItem(0x10f2) != 0);
    }

    /**
     * URL computer name
     * Contains the .eml file name
     */
    @Override
    public String getURLCompName() {
        return this.getStringItem(0x10f3);
    }

    /**
     * Attribute hidden
     */
    @Override
    public boolean getAttrHidden() {
        return (this.getIntItem(0x10f4) != 0);
    }

    /**
     * Attribute system
     */
    @Override
    public boolean getAttrSystem() {
        return (this.getIntItem(0x10f5) != 0);
    }

    /**
     * Attribute read only
     */
    @Override
    public boolean getAttrReadonly() {
        return (this.getIntItem(0x10f6) != 0);
    }

    private PSTTable7C recipientTable = null;

    /**
     * find, extract and load up all of the attachments in this email
     * necessary for the other operations.
     * 
     * @throws PSTException
     * @throws IOException
     */
    private void processRecipients() {
        try {
            final int recipientTableKey = 0x0692;
            if (this.recipientTable == null && this.localDescriptorItems != null
                && this.localDescriptorItems.containsKey(recipientTableKey)) {
                final PSTDescriptorItem item = this.localDescriptorItems.get(recipientTableKey);
                HashMap<Integer, PSTDescriptorItem> descriptorItems = null;
                if (item.subNodeOffsetIndexIdentifier > 0) {
                    descriptorItems = this.pstFile.getPSTDescriptorItems(item.subNodeOffsetIndexIdentifier);
                }
                this.recipientTable = new PSTTable7C(new PSTNodeInputStream(this.pstFile, item), descriptorItems);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            this.recipientTable = null;
        }
    }

    /**
     * get the number of recipients for this message
     * 
     * @throws PSTException
     * @throws IOException
     */
    @Override
    public int getNumberOfRecipients() throws PSTException, IOException {
        this.processRecipients();

        // still nothing? must be no recipients...
        if (this.recipientTable == null) {
            return 0;
        }
        return this.recipientTable.getRowCount();
    }

    /**
     * attachment stuff here, not sure if these can just exist in emails or not,
     * but a table key of 0x0671 would suggest that this is a property of the
     * envelope
     * rather than a specific email property
     */

    private PSTTable7C attachmentTable = null;

    /**
     * find, extract and load up all of the attachments in this email
     * necessary for the other operations.
     * 
     * @throws PSTException
     * @throws IOException
     */
    private void processAttachments() throws PSTException, IOException {
        final int attachmentTableKey = 0x0671;
        if (this.attachmentTable == null && this.localDescriptorItems != null
            && this.localDescriptorItems.containsKey(attachmentTableKey)) {
            final PSTDescriptorItem item = this.localDescriptorItems.get(attachmentTableKey);
            HashMap<Integer, PSTDescriptorItem> descriptorItems = null;
            if (item.subNodeOffsetIndexIdentifier > 0) {
                descriptorItems = this.pstFile.getPSTDescriptorItems(item.subNodeOffsetIndexIdentifier);
            }
            this.attachmentTable = new PSTTable7C(new PSTNodeInputStream(this.pstFile, item), descriptorItems);
        }
    }

    /**
     * Start date Filetime
     */
    @Override
    public Date getTaskStartDate() {
        return this.getDateItem(this.pstFile.getNameToIdMapItem(0x00008104, PSTFile.PSETID_Task));
    }

    /**
     * Due date Filetime
     */
    @Override
    public Date getTaskDueDate() {
        return this.getDateItem(this.pstFile.getNameToIdMapItem(0x00008105, PSTFile.PSETID_Task));
    }

    /**
     * Is a reminder set on this object?
     * 
     * @return
     */
    @Override
    public boolean getReminderSet() {
        return this.getBooleanItem(this.pstFile.getNameToIdMapItem(0x00008503, PSTFile.PSETID_Common));
    }

    @Override
    public int getReminderDelta() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008501, PSTFile.PSETID_Common));
    }

    /**
     * "flagged" items are actually emails with a due date.
     * This convience method just checks to see if that is true.
     */
    @Override
    public boolean isFlagged() {
        return this.getTaskDueDate() != null;
    }

    /**
     * get the categories defined for this message
     */
    @Override
    public String[] getColorCategories() throws PSTException {
        final int keywordCategory = this.pstFile.getPublicStringToIdMapItem("Keywords");

        String[] categories = new String[0];
        if (this.items.containsKey(keywordCategory)) {
            try {
                final PSTTableBCItem item = this.items.get(keywordCategory);
                if (item.data.length == 0) {
                    return categories;
                }
                final int categoryCount = item.data[0];
                if (categoryCount > 0) {
                    categories = new String[categoryCount];
                    final int[] offsets = new int[categoryCount];
                    for (int x = 0; x < categoryCount; x++) {
                        offsets[x] = (int) PSTObject.convertBigEndianBytesToLong(item.data, (x * 4) + 1,
                            (x + 1) * 4 + 1);
                    }
                    for (int x = 0; x < offsets.length - 1; x++) {
                        final int start = offsets[x];
                        final int end = offsets[x + 1];
                        final int length = (end - start);
                        final byte[] string = new byte[length];
                        System.arraycopy(item.data, start, string, 0, length);
                        final String name = new String(string, "UTF-16LE");
                        categories[x] = name;
                    }
                    final int start = offsets[offsets.length - 1];
                    final int end = item.data.length;
                    final int length = (end - start);
                    final byte[] string = new byte[length];
                    System.arraycopy(item.data, start, string, 0, length);
                    final String name = new String(string, "UTF-16LE");
                    categories[categories.length - 1] = name;
                }
            } catch (final Exception err) {
                throw new PSTException("Unable to decode category data", err);
            }
        }
        return categories;
    }

    /**
     * get the number of attachments for this message
     * 
     * @throws PSTException
     * @throws IOException
     */
    @Override
    public int getNumberOfAttachments() {
        try {
            this.processAttachments();
        } catch (final Exception e) {
            e.printStackTrace();
            return 0;
        }

        // still nothing? must be no attachments...
        if (this.attachmentTable == null) {
            return 0;
        }
        return this.attachmentTable.getRowCount();
    }

    /**
     * get a specific attachment from this email.
     * 
     * @param attachmentNumber
     * @return the attachment at the defined index
     * @throws PSTException
     * @throws IOException
     */
    @Override
    public IAttachment getAttachment(final int attachmentNumber) throws PSTException, IOException {
        this.processAttachments();

        int attachmentCount = 0;
        if (this.attachmentTable != null) {
            attachmentCount = this.attachmentTable.getRowCount();
        }

        if (attachmentNumber >= attachmentCount) {
            throw new PSTException("unable to fetch attachment number " + attachmentNumber + ", only " + attachmentCount
                + " in this email");
        }

        // we process the C7 table here, basically we just want the attachment
        // local descriptor...
        final HashMap<Integer, PSTTable7CItem> attachmentDetails = this.attachmentTable.getItems()
            .get(attachmentNumber);
        final PSTTable7CItem attachmentTableItem = attachmentDetails.get(0x67f2);
        final int descriptorItemId = attachmentTableItem.entryValueReference;

        // get the local descriptor for the attachmentDetails table.
        final PSTDescriptorItem descriptorItem = this.localDescriptorItems.get(descriptorItemId);

        // try and decode it
        final byte[] attachmentData = descriptorItem.getData();
        if (attachmentData != null && attachmentData.length > 0) {
            // PSTTableBC attachmentDetailsTable = new
            // PSTTableBC(descriptorItem.getData(),
            // descriptorItem.getBlockOffsets());
            final PSTTableBC attachmentDetailsTable = new PSTTableBC(
                new PSTNodeInputStream(this.pstFile, descriptorItem));

            // create our all-precious attachment object.
            // note that all the information that was in the c7 table is
            // repeated in the eb table in attachment data.
            // so no need to pass it...
            HashMap<Integer, PSTDescriptorItem> attachmentDescriptorItems = new HashMap<>();
            if (descriptorItem.subNodeOffsetIndexIdentifier > 0) {
                attachmentDescriptorItems = this.pstFile
                    .getPSTDescriptorItems(descriptorItem.subNodeOffsetIndexIdentifier);
            }
            return new PSTAttachment(this.pstFile, attachmentDetailsTable, attachmentDescriptorItems);
        }

        throw new PSTException(
            "unable to fetch attachment number " + attachmentNumber + ", unable to read attachment details table");
    }

    /**
     * get a specific recipient from this email.
     * 
     * @param recipientNumber
     * @return the recipient at the defined index
     * @throws PSTException
     * @throws IOException
     */
    @Override
    public IRecipient getRecipient(final int recipientNumber) throws PSTException, IOException {
        if (recipientNumber >= this.getNumberOfRecipients()
            || recipientNumber >= this.recipientTable.getItems().size()) {
            throw new PSTException("unable to fetch recipient number " + recipientNumber);
        }

        final HashMap<Integer, PSTTable7CItem> recipientDetails = this.recipientTable.getItems().get(recipientNumber);

        if (recipientDetails != null) {
            return new PSTRecipient(recipientDetails);
        }

        return null;
    }

    @Override
    public String getRecipientsString() {
        if (this.recipientTable != null) {
            return this.recipientTable.getItemsString();
        }

        return "No recipients table!";
    }

    @Override
    public byte[] getConversationId() {
        return this.getBinaryItem(0x3013);
    }

    @Override
    public PSTConversationIndex getConversationIndex() {
        return new PSTConversationIndex(this.getBinaryItem(0x0071));
    }

    @Override
    public boolean isConversationIndexTracking() {
        return this.getBooleanItem(0x3016, false);
    }

    /**
     * string representation of this email
     */
    @Override
    public String toString() {
        return "PSTEmail: " + this.getSubject() + "\n" + "Importance: " + this.getImportance() + "\n"
            + "Message Class: " + this.getMessageClass() + "\n\n" + this.getTransportMessageHeaders() + "\n\n\n"
            + this.items + this.localDescriptorItems;
    }

}
