package com.pff;

public interface IRss extends IMessage {
    /**
     * Channel
     */
    String getPostRssChannelLink();

    /**
     * Item link
     */
    String getPostRssItemLink();

    /**
     * Item hash Integer 32-bit signed
     */
    int getPostRssItemHash();

    /**
     * Item GUID
     */
    String getPostRssItemGuid();

    /**
     * Channel GUID
     */
    String getPostRssChannel();

    /**
     * Item XML
     */
    String getPostRssItemXml();

    /**
     * Subscription
     */
    String getPostRssSubscription();
}
