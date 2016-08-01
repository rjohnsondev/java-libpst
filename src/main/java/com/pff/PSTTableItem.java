/**
 * Copyright 2010 Richard Johnson & Orin Eman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ---
 *
 * This file is part of java-libpst.
 *
 * java-libpst is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * java-libpst is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with java-libpst. If not, see <http://www.gnu.org/licenses/>.
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
 * 
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
        if (this.data.length > 0) {
            return PSTObject.convertLittleEndianBytesToLong(this.data);
        }
        return -1;
    }

    public String getStringValue() {
        return this.getStringValue(this.entryValueType);
    }

    /**
     * get a string value of the data
     * 
     * @param forceString
     *            if true, you won't get the hex representation of the data
     * @return
     */
    public String getStringValue(final int stringType) {

        if (stringType == VALUE_TYPE_PT_UNICODE) {
            // we are a nice little-endian unicode string.
            try {
                if (this.isExternalValueReference) {
                    return "External string reference!";
                }
                return new String(this.data, "UTF-16LE").trim();
            } catch (final UnsupportedEncodingException e) {

                System.err.println("Error decoding string: " + this.data.toString());
                return "";
            }
        }

        if (stringType == VALUE_TYPE_PT_STRING8) {
            // System.out.println("Warning! decoding string8 without charset:
            // "+this.entryType + " - "+ Integer.toHexString(this.entryType));
            return new String(this.data, Charset.forName("UTF-8")).trim();
        }

        final StringBuffer outputBuffer = new StringBuffer();
        /*
         * if ( stringType == VALUE_TYPE_PT_BIN) {
         * int theChar;
         * for (int x = 0; x < data.length; x++) {
         * theChar = data[x] & 0xFF;
         * outputBuffer.append((char)theChar);
         * }
         * }
         * else
         * /
         **/
        {
            // we are not a normal string, give a hexish sort of output
            final StringBuffer hexOut = new StringBuffer();
            for (final byte element : this.data) {
                final int valueChar = element & 0xff;
                if (Character.isLetterOrDigit((char) valueChar)) {
                    outputBuffer.append((char) valueChar);
                    outputBuffer.append(" ");
                } else {
                    outputBuffer.append(". ");
                }
                final String hexValue = Long.toHexString(valueChar);
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

    @Override
    public String toString() {
        final String ret = PSTFile.getPropertyDescription(this.entryType, this.entryValueType);

        if (this.entryValueType == 0x000B) {
            return ret + (this.entryValueReference == 0 ? "false" : "true");
        }

        if (this.isExternalValueReference) {
            // Either a true external reference, or entryValueReference contains
            // the actual data
            return ret + String.format("0x%08X (%d)", this.entryValueReference, this.entryValueReference);
        }

        if (this.entryValueType == 0x0005 || this.entryValueType == 0x0014) {
            // 64bit data
            if (this.data == null) {
                return ret + "no data";
            }
            if (this.data.length == 8) {
                final long l = PSTObject.convertLittleEndianBytesToLong(this.data, 0, 8);
                return String.format("%s0x%016X (%d)", ret, l, l);
            } else {
                return String.format("%s invalid data length: %d", ret, this.data.length);
            }
        }

        if (this.entryValueType == 0x0040) {
            // It's a date...
            final int high = (int) PSTObject.convertLittleEndianBytesToLong(this.data, 4, 8);
            final int low = (int) PSTObject.convertLittleEndianBytesToLong(this.data, 0, 4);

            final Date d = PSTObject.filetimeToDate(high, low);
            this.dateFormatter.setTimeZone(utcTimeZone);
            return ret + this.dateFormatter.format(d);
        }

        if (this.entryValueType == 0x001F) {
            // Unicode string
            String s;
            try {
                s = new String(this.data, "UTF-16LE");
            } catch (final UnsupportedEncodingException e) {
                System.err.println("Error decoding string: " + this.data.toString());
                s = "";
            }

            if (s.length() >= 2 && s.charAt(0) == 0x0001) {
                return String.format("%s [%04X][%04X]%s", ret, (short) s.charAt(0), (short) s.charAt(1),
                    s.substring(2));
            }

            return ret + s;
        }

        return ret + this.getStringValue();
    }

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
    private static SimpleTimeZone utcTimeZone = new SimpleTimeZone(0, "UTC");
}
