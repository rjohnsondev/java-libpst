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
		return getStringItem(pstFile.getNameToIdMapItem(0x00008700, PSTFile.PSETID_Log));
	}
	/**
	 * Start
	 */
	public Date getLogStart() {
		return getDateItem(pstFile.getNameToIdMapItem(0x00008706, PSTFile.PSETID_Log));
	}
	/**
	 * Duration
	 */
	public int getLogDuration() {
		return getIntItem(pstFile.getNameToIdMapItem(0x00008707, PSTFile.PSETID_Log));
	}
	/**
	 * End
	 */
	public Date getLogEnd() {
		return getDateItem(pstFile.getNameToIdMapItem(0x00008708, PSTFile.PSETID_Log));
	}
	/**
	 * LogFlags
	 */
	public int getLogFlags() {
		return getIntItem(pstFile.getNameToIdMapItem(0x0000870c, PSTFile.PSETID_Log));
	}
	/**
	 * DocPrinted
	 */
	public boolean isDocumentPrinted() {
		return (getBooleanItem(pstFile.getNameToIdMapItem(0x0000870e, PSTFile.PSETID_Log)));
	}
	/**
	 * DocSaved
	 */
	public boolean isDocumentSaved() {
		return (getBooleanItem(pstFile.getNameToIdMapItem(0x0000870f, PSTFile.PSETID_Log)));
	}
	/**
	 * DocRouted
	 */
	public boolean isDocumentRouted() {
		return (getBooleanItem(pstFile.getNameToIdMapItem(0x00008710, PSTFile.PSETID_Log)));
	}
	/**
	 * DocPosted
	 */
	public boolean isDocumentPosted() {
		return (getBooleanItem(pstFile.getNameToIdMapItem(0x00008711, PSTFile.PSETID_Log)));
	}
	/**
	 * Type Description
	 */
	public String getLogTypeDesc() {
		return getStringItem(pstFile.getNameToIdMapItem(0x00008712, PSTFile.PSETID_Log));
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
