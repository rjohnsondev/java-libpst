package com.pff;

import java.io.IOException;
import java.util.Date;

public interface IMessage extends IObject {
    String getRTFBody() throws PSTException, IOException;

    /**
     * get the importance of the email
     *
     * @return IMPORTANCE_NORMAL if unknown
     */
    int getImportance();

    /**
     * get the message class for the email
     *
     * @return empty string if unknown
     */
    String getMessageClass();

    /**
     * get the subject
     *
     * @return empty string if not found
     */
    String getSubject();

    /**
     * get the client submit time
     *
     * @return null if not found
     */
    Date getClientSubmitTime();

    /**
     * get received by name
     *
     * @return empty string if not found
     */
    String getReceivedByName();

    /**
     * get sent representing name
     *
     * @return empty string if not found
     */
    String getSentRepresentingName();

    /**
     * Sent representing address type
     * Known values are SMTP, EX (Exchange) and UNKNOWN
     *
     * @return empty string if not found
     */
    String getSentRepresentingAddressType();

    /**
     * Sent representing email address
     *
     * @return empty string if not found
     */
    String getSentRepresentingEmailAddress();

    /**
     * Conversation topic
     * This is basically the subject from which Fwd:, Re, etc. has been removed
     *
     * @return empty string if not found
     */
    String getConversationTopic();

    /**
     * Received by address type
     * Known values are SMTP, EX (Exchange) and UNKNOWN
     *
     * @return empty string if not found
     */
    String getReceivedByAddressType();

    /**
     * Received by email address
     *
     * @return empty string if not found
     */
    String getReceivedByAddress();

    /**
     * Transport message headers ASCII or Unicode string These contain the SMTP
     * e-mail headers.
     */
    String getTransportMessageHeaders();

    boolean isRead();

    boolean isUnmodified();

    boolean isSubmitted();

    boolean isUnsent();

    boolean hasAttachments();

    boolean isFromMe();

    boolean isAssociated();

    boolean isResent();

    /**
     * Acknowledgment mode Integer 32-bit signed
     */
    int getAcknowledgementMode();

    /**
     * Originator delivery report requested set if the sender wants a delivery
     * report from all recipients 0 = false 0 != true
     */
    boolean getOriginatorDeliveryReportRequested();

    /**
     * Priority Integer 32-bit signed -1 = NonUrgent 0 = Normal 1 = Urgent
     */
    int getPriority();

    /**
     * Read Receipt Requested Boolean 0 = false 0 != true
     */
    boolean getReadReceiptRequested();

    /**
     * Recipient Reassignment Prohibited Boolean 0 = false 0 != true
     */
    boolean getRecipientReassignmentProhibited();

    /**
     * Original sensitivity Integer 32-bit signed the sensitivity of the message
     * before being replied to or forwarded 0 = None 1 = Personal 2 = Private 3
     * = Company Confidential
     */
    int getOriginalSensitivity();

    /**
     * Sensitivity Integer 32-bit signed sender's opinion of the sensitivity of
     * an email 0 = None 1 = Personal 2 = Private 3 = Company Confidential
     */
    int getSensitivity();

    /*
     * Address book search key
     */
    byte[] getPidTagSentRepresentingSearchKey();

    /**
     * Received representing name ASCII or Unicode string
     */
    String getRcvdRepresentingName();

    /**
     * Original subject ASCII or Unicode string
     */
    String getOriginalSubject();

    /**
     * Reply recipients names ASCII or Unicode string
     */
    String getReplyRecipientNames();

    /**
     * My address in To field Boolean
     */
    boolean getMessageToMe();

    /**
     * My address in CC field Boolean
     */
    boolean getMessageCcMe();

    /**
     * Indicates that the receiving mailbox owner is a primary or a carbon copy
     * (Cc) recipient
     */
    boolean getMessageRecipMe();

    /**
     * Response requested Boolean
     */
    boolean getResponseRequested();

    /**
     * Sent representing address type ASCII or Unicode string Known values are
     * SMTP, EX (Exchange) and UNKNOWN
     */
    String getSentRepresentingAddrtype();

    /**
     * Original display BCC ASCII or Unicode string
     */
    String getOriginalDisplayBcc();

    /**
     * Original display CC ASCII or Unicode string
     */
    String getOriginalDisplayCc();

    /**
     * Original display TO ASCII or Unicode string
     */
    String getOriginalDisplayTo();

    /**
     * Received representing address type.
     * Known values are SMTP, EX (Exchange) and UNKNOWN
     */
    String getRcvdRepresentingAddrtype();

    /**
     * Received representing e-mail address
     */
    String getRcvdRepresentingEmailAddress();

    /**
     * Non receipt notification requested
     */
    boolean isNonReceiptNotificationRequested();

    /**
     * Originator non delivery report requested
     */
    boolean isOriginatorNonDeliveryReportRequested();

    /**
     * Recipient type Integer 32-bit signed 0x01 => To 0x02 =>CC
     */
    int getRecipientType();

    /**
     * Reply requested
     */
    boolean isReplyRequested();

    /*
     * Sending mailbox owner's address book entry ID
     */
    byte[] getSenderEntryId();

    /**
     * Sender name
     */
    String getSenderName();

    /**
     * Sender address type.
     * Known values are SMTP, EX (Exchange) and UNKNOWN
     */
    String getSenderAddrtype();

    /**
     * Sender e-mail address
     */
    String getSenderEmailAddress();

    /**
     * Message size
     */
    long getMessageSize();

    /**
     * Internet article number
     */
    int getInternetArticleNumber();

    /*
     * Server that the client should attempt to send the mail with
     */
    String getPrimarySendAccount();

    /*
     * Server that the client is currently using to send mail
     */
    String getNextSendAcct();

    /**
     * URL computer name postfix
     */
    int getURLCompNamePostfix();

    /**
     * Object type
     */
    int getObjectType();

    /**
     * Delete after submit
     */
    boolean getDeleteAfterSubmit();

    /**
     * Responsibility
     */
    boolean getResponsibility();

    /**
     * Compressed RTF in Sync Boolean
     */
    boolean isRTFInSync();

    /**
     * URL computer name set
     */
    boolean isURLCompNameSet();

    /**
     * Display BCC
     */
    String getDisplayBCC();

    /**
     * Display CC
     */
    String getDisplayCC();

    /**
     * Display To
     */
    String getDisplayTo();

    /**
     * Message delivery time
     */
    Date getMessageDeliveryTime();

    /**
     * Message content properties
     */
    int getNativeBodyType();

    /**
     * Plain text e-mail body
     */
    String getBody();

    /*
     * Plain text body prefix
     */
    String getBodyPrefix();

    /**
     * RTF Sync Body CRC
     */
    int getRTFSyncBodyCRC();

    /**
     * RTF Sync Body character count
     */
    int getRTFSyncBodyCount();

    /**
     * RTF Sync body tag
     */
    String getRTFSyncBodyTag();

    /**
     * RTF whitespace prefix count
     */
    int getRTFSyncPrefixCount();

    /**
     * RTF whitespace tailing count
     */
    int getRTFSyncTrailingCount();

    /**
     * HTML e-mail body
     */
    String getBodyHTML();

    /**
     * Message ID for this email as allocated per rfc2822
     */
    String getInternetMessageId();

    /**
     * In-Reply-To
     */
    String getInReplyToId();

    /**
     * Return Path
     */
    String getReturnPath();

    /**
     * Icon index
     */
    int getIconIndex();

    /**
     * Action flag
     * This relates to the replying / forwarding of messages.
     * It is classified as "unknown" atm, so just provided here
     * in case someone works out what all the various flags mean.
     */
    int getActionFlag();

    /**
     * is the action flag for this item "forward"?
     */
    boolean hasForwarded();

    /**
     * is the action flag for this item "replied"?
     */
    boolean hasReplied();

    /**
     * the date that this item had an action performed (eg. replied or
     * forwarded)
     */
    Date getActionDate();

    /**
     * Disable full fidelity
     */
    boolean getDisableFullFidelity();

    /**
     * URL computer name
     * Contains the .eml file name
     */
    String getURLCompName();

    /**
     * Attribute hidden
     */
    boolean getAttrHidden();

    /**
     * Attribute system
     */
    boolean getAttrSystem();

    /**
     * Attribute read only
     */
    boolean getAttrReadonly();

    /**
     * get the number of recipients for this message
     *
     * @throws PSTException
     * @throws IOException
     */
    int getNumberOfRecipients() throws PSTException, IOException;

    /**
     * Start date Filetime
     */
    Date getTaskStartDate();

    /**
     * Due date Filetime
     */
    Date getTaskDueDate();

    /**
     * Is a reminder set on this object?
     *
     * @return
     */
    boolean getReminderSet();

    int getReminderDelta();

    /**
     * "flagged" items are actually emails with a due date.
     * This convience method just checks to see if that is true.
     */
    boolean isFlagged();

    /**
     * get the categories defined for this message
     */
    String[] getColorCategories() throws PSTException;

    /**
     * get the number of attachments for this message
     *
     * @throws PSTException
     * @throws IOException
     */
    int getNumberOfAttachments();

    /**
     * get a specific attachment from this email.
     *
     * @param attachmentNumber
     * @return the attachment at the defined index
     * @throws PSTException
     * @throws IOException
     */
    IAttachment getAttachment(int attachmentNumber) throws PSTException, IOException;

    /**
     * get a specific recipient from this email.
     *
     * @param recipientNumber
     * @return the recipient at the defined index
     * @throws PSTException
     * @throws IOException
     */
    IRecipient getRecipient(int recipientNumber) throws PSTException, IOException;

    String getRecipientsString();

    byte[] getConversationId();

    PSTConversationIndex getConversationIndex();

    boolean isConversationIndexTracking();
}
