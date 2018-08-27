package com.pff;

import java.util.Date;

public interface IActivity extends IMessage {
    /**
     * Type
     */
    String getLogType();

    /**
     * Start
     */
    Date getLogStart();

    /**
     * Duration
     */
    int getLogDuration();

    /**
     * End
     */
    Date getLogEnd();

    /**
     * LogFlags
     */
    int getLogFlags();

    /**
     * DocPrinted
     */
    boolean isDocumentPrinted();

    /**
     * DocSaved
     */
    boolean isDocumentSaved();

    /**
     * DocRouted
     */
    boolean isDocumentRouted();

    /**
     * DocPosted
     */
    boolean isDocumentPosted();

    /**
     * Type Description
     */
    String getLogTypeDesc();
}
