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
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;

import java.io.ByteArrayInputStream;

/**
 * Class containing attachment information.
 *
 * @author Richard Johnson
 */
public class PSTAttachment extends PSTObject {

    /**
     * Instantiates a new Pst attachment.
     *
     * @param theFile              the the file
     * @param table                the table
     * @param localDescriptorItems the local descriptor items
     */
    PSTAttachment(final PSTFile theFile, final PSTTableBC table,
        final HashMap<Integer, PSTDescriptorItem> localDescriptorItems) {
        super(theFile, null, table, localDescriptorItems);
    }

    /**
     * Gets size.
     *
     * @return the size
     */
    public int getSize() {
        return this.getIntItem(0x0e20);
    }

    @Override
    public Date getCreationTime() {
        return this.getDateItem(0x3007);
    }

    /**
     * Gets modification time.
     *
     * @return the modification time
     */
    public Date getModificationTime() {
        return this.getDateItem(0x3008);
    }

    /**
     * Gets embedded pst message.
     *
     * @return the embedded pst message
     * @throws IOException  the io exception
     * @throws PSTException the pst exception
     */
    public PSTMessage getEmbeddedPSTMessage() throws IOException, PSTException {
        PSTNodeInputStream in = null;
        if (this.getIntItem(0x3705) == PSTAttachment.ATTACHMENT_METHOD_EMBEDDED) {
            final PSTTableBCItem item = this.items.get(0x3701);
            if (item.entryValueType == 0x0102) {
                if (!item.isExternalValueReference) {
                    in = new PSTNodeInputStream(this.pstFile, item.data);
                } else {
                    // We are in trouble!
                    throw new PSTException("External reference in getEmbeddedPSTMessage()!\n");
                }
            } else if (item.entryValueType == 0x000D) {
                final int descriptorItem = (int) PSTObject.convertLittleEndianBytesToLong(item.data, 0, 4);
                // PSTObject.printHexFormatted(item.data, true);
                final PSTDescriptorItem descriptorItemNested = this.localDescriptorItems.get(descriptorItem);
                in = new PSTNodeInputStream(this.pstFile, descriptorItemNested);
                if (descriptorItemNested.subNodeOffsetIndexIdentifier > 0) {
                    this.localDescriptorItems
                        .putAll(this.pstFile
                                    .getPSTDescriptorItems(descriptorItemNested.subNodeOffsetIndexIdentifier));
                }
                /*
                 * if ( descriptorItemNested != null ) {
                 * try {
                 * data = descriptorItemNested.getData();
                 * blockOffsets = descriptorItemNested.getBlockOffsets();
                 * } catch (Exception e) {
                 * e.printStackTrace();
                 * 
                 * data = null;
                 * blockOffsets = null;
                 * }
                 * }
                 *
                 */
            }

            if (in == null) {
                return null;
            }

            try {
                final PSTTableBC attachmentTable = new PSTTableBC(in);
                return PSTObject.createAppropriatePSTMessageObject(this.pstFile, this.descriptorIndexNode,
                    attachmentTable, this.localDescriptorItems);
            } catch (final PSTException e) {
                e.printStackTrace();
            }
            return null;
        }
        return null;
    }

    /**
     * Gets file input stream.
     *
     * @return the file input stream
     * @throws IOException  the io exception
     * @throws PSTException the pst exception
     */
    public InputStream getFileInputStream() throws IOException, PSTException {

        final PSTTableBCItem attachmentDataObject = this.items.get(0x3701);

        if (null == attachmentDataObject) {
            return new ByteArrayInputStream(new byte[0]);
        } else if (attachmentDataObject.isExternalValueReference) {
            final PSTDescriptorItem descriptorItemNested = this.localDescriptorItems
                .get(attachmentDataObject.entryValueReference);
            return new PSTNodeInputStream(this.pstFile, descriptorItemNested);
        } else {
            // internal value references are never encrypted
            return new PSTNodeInputStream(this.pstFile, attachmentDataObject.data, false);
        }

    }

    /**
     * Gets filesize.
     *
     * @return the filesize
     * @throws PSTException the pst exception
     * @throws IOException  the io exception
     */
    public int getFilesize() throws PSTException, IOException {
        final PSTTableBCItem attachmentDataObject = this.items.get(0x3701);
        if (attachmentDataObject.isExternalValueReference) {
            final PSTDescriptorItem descriptorItemNested = this.localDescriptorItems
                .get(attachmentDataObject.entryValueReference);
            if (descriptorItemNested == null) {
                throw new PSTException(
                    "missing attachment descriptor item for: " + attachmentDataObject.entryValueReference);
            }
            return descriptorItemNested.getDataSize();
        } else {
            // raw attachment data, right there!
            return attachmentDataObject.data.length;
        }

    }

    // attachment properties

    /**
     * Attachment (short) filename ASCII or Unicode string
     *
     * @return the filename
     */
    public String getFilename() {
        return this.getStringItem(0x3704);
    }

    /**
     * The constant ATTACHMENT_METHOD_NONE.
     */
    public static final int ATTACHMENT_METHOD_NONE = 0;
    /**
     * The constant ATTACHMENT_METHOD_BY_VALUE.
     */
    public static final int ATTACHMENT_METHOD_BY_VALUE = 1;
    /**
     * The constant ATTACHMENT_METHOD_BY_REFERENCE.
     */
    public static final int ATTACHMENT_METHOD_BY_REFERENCE = 2;
    /**
     * The constant ATTACHMENT_METHOD_BY_REFERENCE_RESOLVE.
     */
    public static final int ATTACHMENT_METHOD_BY_REFERENCE_RESOLVE = 3;
    /**
     * The constant ATTACHMENT_METHOD_BY_REFERENCE_ONLY.
     */
    public static final int ATTACHMENT_METHOD_BY_REFERENCE_ONLY = 4;
    /**
     * The constant ATTACHMENT_METHOD_EMBEDDED.
     */
    public static final int ATTACHMENT_METHOD_EMBEDDED = 5;
    /**
     * The constant ATTACHMENT_METHOD_OLE.
     */
    public static final int ATTACHMENT_METHOD_OLE = 6;

    /**
     * Attachment method Integer 32-bit signed 0 =&gt; None (No attachment) 1 =&gt; By
     * value 2 =&gt; By reference 3 =&gt; By reference resolve 4 =&gt; By reference only
     * 5 =&gt; Embedded message 6 =&gt; OLE
     *
     * @return the attach method
     */
    public int getAttachMethod() {
        return this.getIntItem(0x3705);
    }

    /**
     * Attachment size
     *
     * @return the attach size
     */
    public int getAttachSize() {
        return this.getIntItem(0x0e20);
    }

    /**
     * Attachment number
     *
     * @return the attach num
     */
    public int getAttachNum() {
        return this.getIntItem(0x0e21);
    }

    /**
     * Attachment long filename ASCII or Unicode string
     *
     * @return the long filename
     */
    public String getLongFilename() {
        return this.getStringItem(0x3707);
    }

    /**
     * Attachment (short) pathname ASCII or Unicode string
     *
     * @return the pathname
     */
    public String getPathname() {
        return this.getStringItem(0x3708);
    }

    /**
     * Attachment Position Integer 32-bit signed
     *
     * @return the rendering position
     */
    public int getRenderingPosition() {
        return this.getIntItem(0x370b);
    }

    /**
     * Attachment long pathname ASCII or Unicode string
     *
     * @return the long pathname
     */
    public String getLongPathname() {
        return this.getStringItem(0x370d);
    }

    /**
     * Attachment mime type ASCII or Unicode string
     *
     * @return the mime tag
     */
    public String getMimeTag() {
        return this.getStringItem(0x370e);
    }

    /**
     * Attachment mime sequence
     *
     * @return the mime sequence
     */
    public int getMimeSequence() {
        return this.getIntItem(0x3710);
    }

    /**
     * Attachment Content ID
     *
     * @return the content id
     */
    public String getContentId() {
        return this.getStringItem(0x3712);
    }

    /**
     * Attachment not available in HTML
     *
     * @return the boolean
     */
    public boolean isAttachmentInvisibleInHtml() {
        final int actionFlag = this.getIntItem(0x3714);
        return ((actionFlag & 0x1) > 0);
    }

    /**
     * Attachment not available in RTF
     *
     * @return the boolean
     */
    public boolean isAttachmentInvisibleInRTF() {
        final int actionFlag = this.getIntItem(0x3714);
        return ((actionFlag & 0x2) > 0);
    }

    /**
     * Attachment is MHTML REF
     *
     * @return the boolean
     */
    public boolean isAttachmentMhtmlRef() {
        final int actionFlag = this.getIntItem(0x3714);
        return ((actionFlag & 0x4) > 0);
    }

    /**
     * Attachment content disposition
     *
     * @return the attachment content disposition
     */
    public String getAttachmentContentDisposition() {
        return this.getStringItem(0x3716);
    }

}
