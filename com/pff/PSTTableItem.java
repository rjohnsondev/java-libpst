/**
 * 
 */
package com.pff;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * Generic table item.
 * Provides some basic string functions
 * @author Richard Johnson
 */
class PSTTableItem {

	public static final int VALUE_TYPE_PT_UNICODE = 0x1f;
	public static final int VALUE_TYPE_PT_STRING8 = 0x1e;
	public static final int VALUE_TYPE_PT_BIN = 0x102;
	
	public int itemIndex = 0;
	public int entryType = 0;
	public int entryValueType = 0;
	public int entryValueReference = 0;
	public byte[] data = new byte[0];
	public boolean isExternalValueReference = false;
	
	public long getLongValue() {
		if ( this.data.length > 0 ) {
			return PSTObject.convertLittleEndianBytesToLong(data);
		}
		return -1;
	}

	public String getStringValue() {
		return getStringValue(entryValueType);
	}
	
	/**
	 * get a string value of the data
	 * @param forceString if true, you won't get the hex representation of the data
	 * @return
	 */
	public String getStringValue(int stringType) {
		
		if (stringType == VALUE_TYPE_PT_UNICODE) {
			// we are a nice little-endian unicode string.
			try {
				if (isExternalValueReference ) {
					return "External string reference!";
				}
				return new String(data, "UTF-16LE");
			} catch (UnsupportedEncodingException e) {
				System.out.println("Error decoding string: " + data.toString());
				return "";
			}
		}
		
		if (stringType == VALUE_TYPE_PT_STRING8 ) {
			return new String(data, Charset.defaultCharset());
		}
		
		StringBuffer outputBuffer = new StringBuffer();
/*
		if ( stringType == VALUE_TYPE_PT_BIN) {
			int theChar;
			for (int x = 0; x < data.length; x++) {
				theChar = data[x] & 0xFF;
				outputBuffer.append((char)theChar);
			}
		}
		else
/**/
		{
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

	public String toString() {
		String ret = PSTFile.getPropertyDescription(entryType, entryValueType);

		if ( entryValueType == 0x000B )
		{
			return ret + (entryValueReference == 0 ? "false" : "true");
		}

		if ( isExternalValueReference ) {
			// Either a true external reference, or entryValueReference contains the actual data
			return ret + String.format("0x%08X (%d)", entryValueReference, entryValueReference);
		}
		
		if ( entryValueType == 0x0005 ||
			 entryValueType == 0x0014 ) {
			// 64bit data
			if ( data == null ) {
				return ret + "no data";
			}
			if ( data.length == 8 ) {
				long l = PSTObject.convertLittleEndianBytesToLong(data, 0, 8);
				return String.format("%s0x%016X (%d)", ret, l, l);
			} else {
				return String.format("%s invalid data length: %d", ret, data.length);
			}
		}
		
		if ( entryValueType == 0x0040 ) {
			// It's a date...
			int high = (int)PSTObject.convertLittleEndianBytesToLong(data, 4, 8);
			int low = (int)PSTObject.convertLittleEndianBytesToLong(data, 0, 4);
			 
			Date d = PSTObject.filetimeToDate(high, low);
			dateFormatter.setTimeZone(utcTimeZone);
			return ret + dateFormatter.format(d);
		}
		
		if ( entryValueType == 0x001F ) {
			// Unicode string
			String s;
			try {
				s = new String(data, "UTF-16LE");
			} catch (UnsupportedEncodingException e) {
				System.out.println("Error decoding string: " + data.toString());
				s = "";
			}
			
			if ( s.length() >= 2 && s.charAt(0) == 0x0001 ) {
				return String.format("%s [%04X][%04X]%s", ret, (short)s.charAt(0), (short)s.charAt(1), s.substring(2));
			}
			
			return ret + s;
		}

		return ret + getStringValue();
	}

	private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
	private static SimpleTimeZone utcTimeZone = new SimpleTimeZone(0, "UTC");
}
