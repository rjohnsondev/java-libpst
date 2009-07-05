/**
 * 
 */
package com.pff;

/**
 * @author toweruser
 *
 */
class PSTTableItem {

	public static final int VALUE_TYPE_PT_UNICODE = 0x1f;
	public static final int VALUE_TYPE_PT_STRING8 = 0x1e;

	public byte[] data = new byte[0];
	
	public String getStringValue(int stringType) {
		if (this.data.length == 0) {
			return "";
		}
		StringBuffer outputBuffer = new StringBuffer();
		if (stringType == VALUE_TYPE_PT_UNICODE) {
			
			// we are a nice little-endian unicode string.
			char theChar;
			for (int x = 0; x < data.length-1; x = x + 2) {
				theChar = 0;
				theChar = (char)data[x+1];
				theChar <<= 8;
				theChar |= (char)data[x];
				outputBuffer.append(theChar);
			}
			// if we are odd, we may have 1 char left, don't think this actually happens,
			// but better safe than sorry
			if (data.length % 2 == 1) {
				outputBuffer.append((char)data[data.length-1]);
			}
		} else if (stringType == VALUE_TYPE_PT_STRING8) {
			// we are a dirty asci character
//			char theChar;
//			for (int x = 0; x < data.length; x++) {
//				theChar = (char)data[x];
//				outputBuffer.append(theChar);
//			}
			outputBuffer.append(data);
			
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
