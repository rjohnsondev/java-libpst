/**
 * 
 */
package com.pff;

/**
 * Generic table item.
 * Provides some basic string functions
 * @author Richard Johnson
 */
class PSTTableItem {

	public static final int VALUE_TYPE_PT_UNICODE = 0x1f;
	public static final int VALUE_TYPE_PT_STRING8 = 0x1e;
	public static final int VALUE_TYPE_PT_BIN = 0x102;

	public byte[] data = new byte[0];
	
	public String getStringValue(int stringType) {
		if (this.data.length == 0) {
			return "";
		}
		StringBuffer outputBuffer = new StringBuffer();
		if (stringType == VALUE_TYPE_PT_UNICODE) {
			
			// we are a nice little-endian unicode string.
			// New code - use String class built-in decoding of little-endian unicode.
			try {
				return new String(data, "UTF-16LE");
			} catch (java.io.UnsupportedEncodingException e) {
				System.err.println("Error decoding string: " + data.toString());
				return "";
			}
		} else if (stringType == VALUE_TYPE_PT_STRING8 ||
				stringType == VALUE_TYPE_PT_BIN) {
			// we are a dirty asci character
			char theChar;
			for (int x = 0; x < data.length; x++) {
				theChar = (char)data[x];
				outputBuffer.append(theChar);
			}
//			outputBuffer.append(data);
			
		} else {
			// we are not a normal string, give a hexish sort of output
			StringBuffer hexOut = new StringBuffer();
			for (int y = 0; y < data.length; y++) {
				int valueChar = (int)data[y] & 0xff;
				if (Character.isLetterOrDigit((char)valueChar)) {
					outputBuffer.append((char)valueChar);
					outputBuffer.append(" ");
				}
				else
				{
					outputBuffer.append(". ");
				}
				String hexValue = Long.toHexString(valueChar);
				hexOut.append(hexValue);
				hexOut.append(" ");
				if (hexValue.length() > 1) {
					outputBuffer.append(" ");
				}
			}
			outputBuffer.append("\n");
			outputBuffer.append("	");
			outputBuffer.append(hexOut);
		}
		
		return new String(outputBuffer);
	}

}
