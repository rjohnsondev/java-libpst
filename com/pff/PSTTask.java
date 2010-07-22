/**
 * 
 */
package com.pff;

import java.io.IOException;
import java.util.HashMap;
import java.util.Date;

/**
 * Object that represents Task items
 * @author Richard Johnson
 */
public class PSTTask extends PSTMessage {

	/**
	 * @param theFile
	 * @param descriptorIndexNode
	 * @throws PSTException
	 * @throws IOException
	 */
	public PSTTask(PSTFile theFile, DescriptorIndexNode descriptorIndexNode)
			throws PSTException, IOException {
		super(theFile, descriptorIndexNode);
	}

	/**
	 * @param theFile
	 * @param folderIndexNode
	 * @param table
	 * @param localDescriptorItems
	 */
	public PSTTask(PSTFile theFile, DescriptorIndexNode folderIndexNode,
			PSTTableBC table,
			HashMap<Integer, PSTDescriptorItem> localDescriptorItems) {
		super(theFile, folderIndexNode, table, localDescriptorItems);
	}

	/**
	 * Status Integer 32-bit signed 0x0 => Not started
	 */
	public int getTaskStatus() {
		return getIntItem(pstFile.getNameToIdMapItem(0x00008101, PSTFile.PSETID_Task));
	}
	/**
	 * Percent Complete Floating point double precision (64-bit)
	 */
	public double getPercentComplete() {
		return getDoubleItem(pstFile.getNameToIdMapItem(0x00008102, PSTFile.PSETID_Task));
	}
	/**
	 * Is team task Boolean
	 */
	public boolean isTeamTask() {
		return getBooleanItem(pstFile.getNameToIdMapItem(0x00008103, PSTFile.PSETID_Task));
	}
	
	/**
	 * Date completed Filetime
	 */
	public Date getTaskDateCompleted() {
		return getDateItem(pstFile.getNameToIdMapItem(0x0000810f, PSTFile.PSETID_Task));
	}
	/**
	 * Actual effort in minutes Integer 32-bit signed
	 */
	public int getTaskActualEffort() {
		return getIntItem(pstFile.getNameToIdMapItem(0x00008110, PSTFile.PSETID_Task));
	}
	/**
	 * Total effort in minutes Integer 32-bit signed
	 */
	public int getTaskEstimatedEffort() {
		return getIntItem(pstFile.getNameToIdMapItem(0x00008111, PSTFile.PSETID_Task));
	}
	/**
	 * Task version Integer 32-bit signed FTK: Access count
	 */
	public int getTaskVersion() {
		return getIntItem(pstFile.getNameToIdMapItem(0x00008112, PSTFile.PSETID_Task));
	}
	/**
	 * Complete Boolean
	 */
	public boolean isTaskComplete() {
		return getBooleanItem(pstFile.getNameToIdMapItem(0x0000811c, PSTFile.PSETID_Task));
	}
	/**
	 * Owner ASCII or Unicode string
	 */
	public String getTaskOwner() {
		return getStringItem(pstFile.getNameToIdMapItem(0x0000811f, PSTFile.PSETID_Task));
	}
	/**
	 * Delegator ASCII or Unicode string
	 */
	public String getTaskAssigner() {
		return getStringItem(pstFile.getNameToIdMapItem(0x00008121, PSTFile.PSETID_Task));
	}
	/**
	 * Unknown ASCII or Unicode string
	 */
	public String getTaskLastUser() {
		return getStringItem(pstFile.getNameToIdMapItem(0x00008122, PSTFile.PSETID_Task));
	}
	/**
	 * Ordinal Integer 32-bit signed
	 */
	public int getTaskOrdinal() {
		return this.getIntItem(pstFile.getNameToIdMapItem(0x00008123, PSTFile.PSETID_Task));
	}
	/**
	 * Is recurring Boolean
	 */
	public boolean isTaskFRecurring() {
		return getBooleanItem(pstFile.getNameToIdMapItem(0x00008126, PSTFile.PSETID_Task));
	}
	/**
	 * Role ASCII or Unicode string
	 */
	public String getTaskRole() {
		return getStringItem(pstFile.getNameToIdMapItem(0x00008127, PSTFile.PSETID_Task));
	}
	/**
	 * Ownership Integer 32-bit signed
	 */
	public int getTaskOwnership() {
		return getIntItem(pstFile.getNameToIdMapItem(0x00008129, PSTFile.PSETID_Task));
	}
	/**
	 * Delegation State
	 */
	public int getAcceptanceState() {
		return getIntItem(pstFile.getNameToIdMapItem(0x0000812a, PSTFile.PSETID_Task));
	}

	public String toString() {
		return 
		 "Status Integer 32-bit signed 0x0 => Not started [TODO]: "+getTaskStatus()+"\n"+
		 "Percent Complete Floating point double precision (64-bit): "+getPercentComplete()+"\n"+
		 "Is team task Boolean: "+isTeamTask()+"\n"+
		 "Start date Filetime: "+getTaskStartDate()+"\n"+
		 "Due date Filetime: "+getTaskDueDate()+"\n"+
		 "Date completed Filetime: "+getTaskDateCompleted()+"\n"+
		 "Actual effort in minutes Integer 32-bit signed: "+getTaskActualEffort()+"\n"+
		 "Total effort in minutes Integer 32-bit signed: "+getTaskEstimatedEffort()+"\n"+
		 "Task version Integer 32-bit signed FTK: Access count: "+getTaskVersion()+"\n"+
		 "Complete Boolean: "+isTaskComplete()+"\n"+
		 "Owner ASCII or Unicode string: "+getTaskOwner()+"\n"+
		 "Delegator ASCII or Unicode string: "+getTaskAssigner()+"\n"+
		 "Unknown ASCII or Unicode string: "+getTaskLastUser()+"\n"+
		 "Ordinal Integer 32-bit signed: "+getTaskOrdinal()+"\n"+
		 "Is recurring Boolean: "+isTaskFRecurring()+"\n"+
		 "Role ASCII or Unicode string: "+getTaskRole()+"\n"+
		 "Ownership Integer 32-bit signed: "+getTaskOwnership()+"\n"+
		 "Delegation State: "+getAcceptanceState();



	}
}
