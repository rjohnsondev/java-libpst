/**
 * PSTRss is used for RSS Items...
 */
package com.pff;

import java.io.IOException;
import java.util.HashMap;

/**
 * Object that represents a RSS item
 * @author Richard Johnson
 */
public class PSTRss extends PSTMessage {

	/**
	 * @param theFile
	 * @param descriptorIndexNode
	 * @throws PSTException
	 * @throws IOException
	 */
	public PSTRss(PSTFile theFile, DescriptorIndexNode descriptorIndexNode)
			throws PSTException, IOException {
		super(theFile, descriptorIndexNode);
	}

	/**
	 * @param theFile
	 * @param folderIndexNode
	 * @param table
	 * @param localDescriptorItems
	 */
	public PSTRss(PSTFile theFile, DescriptorIndexNode folderIndexNode,
			PSTTableBC table,
			HashMap<Integer, PSTDescriptorItem> localDescriptorItems) {
		super(theFile, folderIndexNode, table, localDescriptorItems);
	}
	
	/**
	 * Channel
	 */
	public String getPostRssChannelLink() {
		return this.getStringItem(pstFile.getNameToIdMapItem(0x00008900));
	}
	/**
	 * Item link
	 */
	public String getPostRssItemLink() {
		return this.getStringItem(pstFile.getNameToIdMapItem(0x00008901));
	}
	/**
	 * Item hash Integer 32-bit signed
	 */
	public int getPostRssItemHash() {
		return this.getIntItem(pstFile.getNameToIdMapItem(0x00008902));
	}
	/**
	 * Item GUID
	 */
	public String getPostRssItemGuid() {
		return this.getStringItem(pstFile.getNameToIdMapItem(0x00008903));
	}
	/**
	 * Channel GUID
	 */
	public String getPostRssChannel() {
		return this.getStringItem(pstFile.getNameToIdMapItem(0x00008904));
	}
	/**
	 * Item XML
	 */
	public String getPostRssItemXml() {
		return this.getStringItem(pstFile.getNameToIdMapItem(0x00008905));
	}
	/**
	 * Subscription
	 */
	public String getPostRssSubscription() {
		return this.getStringItem(pstFile.getNameToIdMapItem(0x00008906));
	}

	public String toString() {
		return
		 "Channel ASCII or Unicode string values: "+ getPostRssChannelLink() + "\n" +
		 "Item link ASCII or Unicode string values: "+ getPostRssItemLink() + "\n" +
		 "Item hash Integer 32-bit signed: "+ getPostRssItemHash() + "\n" +
		 "Item GUID ASCII or Unicode string values: "+ getPostRssItemGuid() + "\n" +
		 "Channel GUID ASCII or Unicode string values: "+ getPostRssChannel() + "\n" +
		 "Item XML ASCII or Unicode string values: "+ getPostRssItemXml() + "\n" +
		 "Subscription ASCII or Unicode string values: "+ getPostRssSubscription();
	}
}
