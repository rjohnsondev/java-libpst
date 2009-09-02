/**
 * 
 */
package com.pff;

import java.io.IOException;
import java.util.HashMap;
import java.util.Date;

/**
 * @author lappyuser
 *
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
	 * Status Integer 32-bit signed 0x0 => Not started [TODO]
	 */
	public int getTaskStatus() {
		return this.getIntItem(pstFile.getNameToIdMapItem(0x00008101));
	}
	/**
	 * Percent Complete Floating point double precision (64-bit)
	 */
	public double getPercentComplete() {
		return this.getDoubleItem(pstFile.getNameToIdMapItem(0x00008102));
	}
	/**
	 * Is team task Boolean
	 */
	public boolean isTeamTask() {
		return (this.getIntItem(pstFile.getNameToIdMapItem(0x00008103)) != 0);
	}
	/**
	 * Start date Filetime
	 */
	public Date getTaskStartDate() {
		return this.getDateItem(pstFile.getNameToIdMapItem(0x00008104));
	}
	/**
	 * Due date Filetime
	 */
	public Date getTaskDueDate() {
		return this.getDateItem(pstFile.getNameToIdMapItem(0x00008105));
	}
	/**
	 * Date completed Filetime
	 */
	public Date getTaskDateCompleted() {
		return this.getDateItem(pstFile.getNameToIdMapItem(0x0000810f));
	}
	/**
	 * Actual effort in minutes Integer 32-bit signed
	 */
	public int getTaskActualEffort() {
		return this.getIntItem(pstFile.getNameToIdMapItem(0x00008110));
	}
	/**
	 * Total effort in minutes Integer 32-bit signed
	 */
	public int getTaskEstimatedEffort() {
		return this.getIntItem(pstFile.getNameToIdMapItem(0x00008111));
	}
	/**
	 * Task version Integer 32-bit signed FTK: Access count
	 */
	public int getTaskVersion() {
		return this.getIntItem(pstFile.getNameToIdMapItem(0x00008112));
	}
	/**
	 * Complete Boolean
	 */
	public boolean isTaskComplete() {
		return (this.getIntItem(pstFile.getNameToIdMapItem(0x0000811c)) != 0);
	}
	/**
	 * Owner ASCII or Unicode string
	 */
	public String getTaskOwner() {
		return this.getStringItem(pstFile.getNameToIdMapItem(0x0000811f));
	}
	/**
	 * Delegator ASCII or Unicode string
	 */
	public String getTaskAssigner() {
		return this.getStringItem(pstFile.getNameToIdMapItem(0x00008121));
	}
	/**
	 * Unknown ASCII or Unicode string
	 */
	public String getTaskLastUser() {
		return this.getStringItem(pstFile.getNameToIdMapItem(0x00008122));
	}
	/**
	 * Ordinal Integer 32-bit signed
	 */
	public int getTaskOrdinal() {
		return this.getIntItem(pstFile.getNameToIdMapItem(0x00008123));
	}
	/**
	 * Is recurring Boolean
	 */
	public boolean isTaskFRecurring() {
		return (this.getIntItem(pstFile.getNameToIdMapItem(0x00008126)) != 0);
	}
	/**
	 * Role ASCII or Unicode string
	 */
	public String getTaskRole() {
		return this.getStringItem(pstFile.getNameToIdMapItem(0x00008127));
	}
	/**
	 * Ownership Integer 32-bit signed
	 */
	public int getTaskOwnership() {
		return this.getIntItem(pstFile.getNameToIdMapItem(0x00008129));
	}
	/**
	 * Delegation State
	 */
	public int getAcceptanceState() {
		return this.getIntItem(pstFile.getNameToIdMapItem(0x0000812a));
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
