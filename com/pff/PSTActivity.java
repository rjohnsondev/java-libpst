/**
 * 
 */
package com.pff;

import java.io.IOException;
import java.util.HashMap;
import java.util.Date;

/**
 * PSTActivity represents Journal entries 
 * @author Richard Johnson
 */
public class PSTActivity extends PSTMessage {

	/**
	 * @param theFile
	 * @param descriptorIndexNode
	 * @throws PSTException
	 * @throws IOException
	 */
	public PSTActivity(PSTFile theFile, DescriptorIndexNode descriptorIndexNode)
			throws PSTException, IOException {
		super(theFile, descriptorIndexNode);
	}

	/**
	 * @param theFile
	 * @param folderIndexNode
	 * @param table
	 * @param localDescriptorItems
	 */
	public PSTActivity(PSTFile theFile, DescriptorIndexNode folderIndexNode,
			PSTTableBC table,
			HashMap<Integer, PSTDescriptorItem> localDescriptorItems) {
		super(theFile, folderIndexNode, table, localDescriptorItems);
	}
	
	/**
	 * Type
	 */
	public String getLogType() {
		return this.getStringItem(pstFile.getNameToIdMapItem(0x00008700));
	}
	/**
	 * Start
	 */
	public Date getLogStart() {
		return this.getDateItem(pstFile.getNameToIdMapItem(0x00008706));
	}
	/**
	 * Duration
	 */
	public int getLogDuration() {
		return this.getIntItem(pstFile.getNameToIdMapItem(0x00008707));
	}
	/**
	 * End
	 */
	public Date getLogEnd() {
		return this.getDateItem(pstFile.getNameToIdMapItem(0x00008708));
	}
	/**
	 * LogFlags
	 */
	public int getLogFlags() {
		return this.getIntItem(pstFile.getNameToIdMapItem(0x0000870c));
	}
	/**
	 * DocPrinted
	 */
	public boolean isDocumentPrinted() {
		return (this.getIntItem(pstFile.getNameToIdMapItem(0x0000870e)) != 0);
	}
	/**
	 * DocSaved
	 */
	public boolean isDocumentSaved() {
		return (this.getIntItem(pstFile.getNameToIdMapItem(0x0000870f)) != 0);
	}
	/**
	 * DocRouted
	 */
	public boolean isDocumentRouted() {
		return (this.getIntItem(pstFile.getNameToIdMapItem(0x00008710)) != 0);
	}
	/**
	 * DocPosted
	 */
	public boolean isDocumentPosted() {
		return (this.getIntItem(pstFile.getNameToIdMapItem(0x00008711)) != 0);
	}
	/**
	 * Type Description
	 */
	public String getLogTypeDesc() {
		return this.getStringItem(pstFile.getNameToIdMapItem(0x00008712));
	}

	public String toString() {
		return
			 "Type ASCII or Unicode string: "+ getLogType() + "\n" +
			 "Start Filetime: "+ getLogStart() + "\n" +
			 "Duration Integer 32-bit signed: "+ getLogDuration() + "\n" +
			 "End Filetime: "+ getLogEnd() + "\n" +
			 "LogFlags Integer 32-bit signed: "+ getLogFlags() + "\n" +
			 "DocPrinted Boolean: "+ isDocumentPrinted() + "\n" +
			 "DocSaved Boolean: "+ isDocumentSaved() + "\n" +
			 "DocRouted Boolean: "+ isDocumentRouted() + "\n" +
			 "DocPosted Boolean: "+ isDocumentPosted() + "\n" +
			 "TypeDescription ASCII or Unicode string: "+ getLogTypeDesc();

	}

}
