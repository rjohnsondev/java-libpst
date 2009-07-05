/**
 * 
 */
package com.pff;

/**
 * @author toweruser
 *
 */
class PSTTableBCItem extends PSTTableItem
{
	
	public int itemIndex = 0;
	public int entryType = 0;
	public int entryValueType = 0;
	public int entryValueReference = 0;
	public int entryValueReferenceAsOffset = 0;
	public boolean isExternalValueReference = false;

	/**
	 * get a string value of the data
	 * @param forceString if true, you won't get the hex representation of the data
	 * @return
	 */
	public String getStringValue() {
		return super.getStringValue(this.entryValueType);
	}
	
	public String toString() {
		long value = 0;
		if (this.data.length > 0) {
			value = (int)PSTObject.convertLittleEndianBytesToLong(data);
		}
		return "Table Item:\n"+
			"Entry Type: "+entryType+" - 0x"+Long.toHexString(entryType)+"\n"+
			"Entry Value Type: "+entryValueType+" - 0x"+Long.toHexString(entryValueType)+"\n"+
			"Entry Value Reference: "+entryValueReference+" - 0x"+Long.toHexString(entryValueReference)+"\n"+
			"Entry Value Reference As Offset: "+entryValueReferenceAsOffset+" - 0x"+Long.toHexString(entryValueReferenceAsOffset)+"\n"+
			"Value: "+ value + " - 0x"+Long.toHexString(value)+"\n"+
			"	"+this.getStringValue();
	}
}
