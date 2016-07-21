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

import java.util.Calendar;
import java.util.SimpleTimeZone;

/**
 * Class containing time zone information
 * 
 * @author Orin Eman
 *
 *
 */

public class PSTTimeZone {
    PSTTimeZone(final byte[] timeZoneData) {
        this.rule = null;
        this.name = "";

        try {
            final int headerLen = (int) PSTObject.convertLittleEndianBytesToLong(timeZoneData, 2, 4);
            final int nameLen = 2 * (int) PSTObject.convertLittleEndianBytesToLong(timeZoneData, 6, 8);
            this.name = new String(timeZoneData, 8, nameLen, "UTF-16LE");
            int ruleOffset = 8 + nameLen;
            final int nRules = (int) PSTObject.convertLittleEndianBytesToLong(timeZoneData, ruleOffset, ruleOffset + 2);

            ruleOffset = 4 + headerLen;
            for (int rule = 0; rule < nRules; ++rule) {
                // Is this rule the effective rule?
                final int flags = (int) PSTObject.convertLittleEndianBytesToLong(timeZoneData, ruleOffset + 4,
                    ruleOffset + 6);
                if ((flags & 0x0002) != 0) {
                    this.rule = new TZRule(timeZoneData, ruleOffset + 6);
                    break;
                }
                ruleOffset += 66;
            }
        } catch (final Exception e) {
            System.err.printf("Exception reading timezone: %s\n", e.toString());
            e.printStackTrace();
            this.rule = null;
            this.name = "";
        }
    }

    PSTTimeZone(String name, final byte[] timeZoneData) {
        this.name = name;
        this.rule = null;

        try {
            this.rule = new TZRule(new SYSTEMTIME(), timeZoneData, 0);
        } catch (final Exception e) {
            System.err.printf("Exception reading timezone: %s\n", e.toString());
            e.printStackTrace();
            this.rule = null;
            name = "";
        }
    }

    public String getName() {
        return this.name;
    }

    public SimpleTimeZone getSimpleTimeZone() {
        if (this.simpleTimeZone != null) {
            return this.simpleTimeZone;
        }

        if (this.rule.startStandard.wMonth == 0) {
            // A time zone with no daylight savings time
            this.simpleTimeZone = new SimpleTimeZone((this.rule.lBias + this.rule.lStandardBias) * 60 * 1000,
                this.name);

            return this.simpleTimeZone;
        }

        final int startMonth = (this.rule.startDaylight.wMonth - 1) + Calendar.JANUARY;
        final int startDayOfMonth = (this.rule.startDaylight.wDay == 5) ? -1
            : ((this.rule.startDaylight.wDay - 1) * 7) + 1;
        final int startDayOfWeek = this.rule.startDaylight.wDayOfWeek + Calendar.SUNDAY;
        final int endMonth = (this.rule.startStandard.wMonth - 1) + Calendar.JANUARY;
        final int endDayOfMonth = (this.rule.startStandard.wDay == 5) ? -1
            : ((this.rule.startStandard.wDay - 1) * 7) + 1;
        final int endDayOfWeek = this.rule.startStandard.wDayOfWeek + Calendar.SUNDAY;
        final int savings = (this.rule.lStandardBias - this.rule.lDaylightBias) * 60 * 1000;

        this.simpleTimeZone = new SimpleTimeZone(-((this.rule.lBias + this.rule.lStandardBias) * 60 * 1000), this.name,
            startMonth, startDayOfMonth, -startDayOfWeek,
            (((((this.rule.startDaylight.wHour * 60) + this.rule.startDaylight.wMinute) * 60)
                + this.rule.startDaylight.wSecond) * 1000) + this.rule.startDaylight.wMilliseconds,
            endMonth, endDayOfMonth, -endDayOfWeek,
            (((((this.rule.startStandard.wHour * 60) + this.rule.startStandard.wMinute) * 60)
                + this.rule.startStandard.wSecond) * 1000) + this.rule.startStandard.wMilliseconds,
            savings);

        return this.simpleTimeZone;
    }

    public boolean isEqual(final PSTTimeZone rhs) {
        if (this.name.equalsIgnoreCase(rhs.name)) {
            if (this.rule.isEqual(rhs.rule)) {
                return true;
            }

            System.err.printf("Warning: different timezones with the same name: %s\n", this.name);
        }
        return false;
    }

    public SYSTEMTIME getStart() {
        return this.rule.dtStart;
    }

    public int getBias() {
        return this.rule.lBias;
    }

    public int getStandardBias() {
        return this.rule.lStandardBias;
    }

    public int getDaylightBias() {
        return this.rule.lDaylightBias;
    }

    public SYSTEMTIME getDaylightStart() {
        return this.rule.startDaylight;
    }

    public SYSTEMTIME getStandardStart() {
        return this.rule.startStandard;
    }

    public class SYSTEMTIME {

        SYSTEMTIME() {
            this.wYear = 0;
            this.wMonth = 0;
            this.wDayOfWeek = 0;
            this.wDay = 0;
            this.wHour = 0;
            this.wMinute = 0;
            this.wSecond = 0;
            this.wMilliseconds = 0;
        }

        SYSTEMTIME(final byte[] timeZoneData, final int offset) {
            this.wYear = (short) (PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset, offset + 2) & 0x7FFF);
            this.wMonth = (short) (PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 2, offset + 4)
                & 0x7FFF);
            this.wDayOfWeek = (short) (PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 4, offset + 6)
                & 0x7FFF);
            this.wDay = (short) (PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 6, offset + 8)
                & 0x7FFF);
            this.wHour = (short) (PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 8, offset + 10)
                & 0x7FFF);
            this.wMinute = (short) (PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 10, offset + 12)
                & 0x7FFF);
            this.wSecond = (short) (PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 12, offset + 14)
                & 0x7FFF);
            this.wMilliseconds = (short) (PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 14,
                offset + 16) & 0x7FFF);
        }

        boolean isEqual(final SYSTEMTIME rhs) {
            return this.wYear == rhs.wYear && this.wMonth == rhs.wMonth && this.wDayOfWeek == rhs.wDayOfWeek
                && this.wDay == rhs.wDay && this.wHour == rhs.wHour && this.wMinute == rhs.wMinute
                && this.wSecond == rhs.wSecond && this.wMilliseconds == rhs.wMilliseconds;
        }

        public short wYear;
        public short wMonth;
        public short wDayOfWeek;
        public short wDay;
        public short wHour;
        public short wMinute;
        public short wSecond;
        public short wMilliseconds;
    }

    /**
     * A static copy of the UTC time zone, available for others to use
     */
    public static SimpleTimeZone utcTimeZone = new SimpleTimeZone(0, "UTC");

    private class TZRule {

        TZRule(final SYSTEMTIME dtStart, final byte[] timeZoneData, final int offset) {
            this.dtStart = dtStart;
            this.InitBiases(timeZoneData, offset);
            @SuppressWarnings("unused")
            final short wStandardYear = (short) PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 12,
                offset + 14);
            this.startStandard = new SYSTEMTIME(timeZoneData, offset + 14);
            @SuppressWarnings("unused")
            final short wDaylightYear = (short) PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 30,
                offset + 32);
            this.startDaylight = new SYSTEMTIME(timeZoneData, offset + 32);
        }

        TZRule(final byte[] timeZoneData, final int offset) {
            this.dtStart = new SYSTEMTIME(timeZoneData, offset);
            this.InitBiases(timeZoneData, offset + 16);
            this.startStandard = new SYSTEMTIME(timeZoneData, offset + 28);
            this.startDaylight = new SYSTEMTIME(timeZoneData, offset + 44);
        }

        private void InitBiases(final byte[] timeZoneData, final int offset) {
            this.lBias = (int) PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset, offset + 4);
            this.lStandardBias = (int) PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 4, offset + 8);
            this.lDaylightBias = (int) PSTObject.convertLittleEndianBytesToLong(timeZoneData, offset + 8, offset + 12);
        }

        boolean isEqual(final TZRule rhs) {
            return this.dtStart.isEqual(rhs.dtStart) && this.lBias == rhs.lBias
                && this.lStandardBias == rhs.lStandardBias && this.lDaylightBias == rhs.lDaylightBias
                && this.startStandard.isEqual(rhs.startStandard) && this.startDaylight.isEqual(rhs.startDaylight);
        }

        SYSTEMTIME dtStart;
        int lBias;
        int lStandardBias;
        int lDaylightBias;
        SYSTEMTIME startStandard;
        SYSTEMTIME startDaylight;
    }

    private String name;
    private TZRule rule;
    private SimpleTimeZone simpleTimeZone = null;
}
