/**
 * 
 */
package com.pff;


/**
 * @author toweruser
 *
 */
class PSTTable7CItem extends PSTTableItem
{
	
	public int itemIndex = 0;
	public int entryType = 0;
	public int entryValueType = 0;
	public int valuesArrayEntryOffset = 0;
	public int valuesArrayEntrySize = 0;
	public int valuesArrayEntryNumber = 0;
	public boolean isExternalValueReference = false;

	/**
	 * get a string value of the data
	 * @param forceString if true, you won't get the hex representation of the data
	 * @return
	 */
	public String getStringValue() {
		return super.getStringValue(this.entryValueType);
	}
	
	public long getLongValue() {
		if (this.data.length > 0) {
			return PSTObject.convertLittleEndianBytesToLong(data);
		}
		return -1;
	}
	
	public String toString() {
		long value = 0;
		if (this.data.length > 0) {
			value = (int)PSTObject.convertLittleEndianBytesToLong(data);
		}
		return "7c Table Item:\n"+
			"Entry Type: "+entryType+" - 0x"+Long.toHexString(entryType)+"\n"+
			"Entry Value Type: "+entryValueType+" - 0x"+Long.toHexString(entryValueType)+"\n"+
			"Values Array Entry Offset: "+valuesArrayEntryOffset+" - 0x"+Long.toHexString(valuesArrayEntryOffset)+"\n"+
			"Values Array Entry Size: "+valuesArrayEntrySize+" - 0x"+Long.toHexString(valuesArrayEntrySize)+"\n"+
			"Values Array Entry Number: "+valuesArrayEntryNumber+" - 0x"+Long.toHexString(valuesArrayEntryNumber)+"\n"+
			"Value: "+ value + " - 0x"+Long.toHexString(value)+"\n"+
			"	"+(this.isExternalValueReference ? this.getStringValue(0) : this.getStringValue());
	}
}