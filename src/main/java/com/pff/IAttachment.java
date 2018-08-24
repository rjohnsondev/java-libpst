package com.pff;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public interface IAttachment extends IObject {
    int getSize();

    Date getCreationTime();

    Date getModificationTime();

    IMessage getEmbeddedPSTMessage() throws IOException, PSTException;

    InputStream getFileInputStream() throws IOException, PSTException;

    int getFilesize() throws PSTException, IOException;

    /**
     * Attachment (short) filename ASCII or Unicode string
     */
    String getFilename();

    /**
     * Attachment method Integer 32-bit signed 0 => None (No attachment) 1 => By
     * value 2 => By reference 3 => By reference resolve 4 => By reference only
     * 5 => Embedded message 6 => OLE
     */
    int getAttachMethod();

    /**
     * Attachment size
     */
    int getAttachSize();

    /**
     * Attachment number
     */
    int getAttachNum();

    /**
     * Attachment long filename ASCII or Unicode string
     */
    String getLongFilename();

    /**
     * Attachment (short) pathname ASCII or Unicode string
     */
    String getPathname();

    /**
     * Attachment Position Integer 32-bit signed
     */
    int getRenderingPosition();

    /**
     * Attachment long pathname ASCII or Unicode string
     */
    String getLongPathname();

    /**
     * Attachment mime type ASCII or Unicode string
     */
    String getMimeTag();

    /**
     * Attachment mime sequence
     */
    int getMimeSequence();

    /**
     * Attachment Content ID
     */
    String getContentId();

    /**
     * Attachment not available in HTML
     */
    boolean isAttachmentInvisibleInHtml();

    /**
     * Attachment not available in RTF
     */
    boolean isAttachmentInvisibleInRTF();

    /**
     * Attachment is MHTML REF
     */
    boolean isAttachmentMhtmlRef();

    /**
     * Attachment content disposition
     */
    String getAttachmentContentDisposition();
}
