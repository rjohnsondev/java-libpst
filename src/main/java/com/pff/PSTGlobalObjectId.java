package com.pff;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Date;

/**
 * Class to represent a decoded PidLidGlobalObjectId or
 * PidLidCleanGlobalObjectId (for Meeting Exceptions)
 * See [MS-OXOCAL]
 * https://msdn.microsoft.com/en-us/library/cc425490(v=exchg.80).aspx, sections
 * 2.2.1.27(PidLidGlobalObjectId) & 2.2.1.28(PidLidCleanGlobalObjectId)
 *
 * @author Nick Buller
 *         NOTE: Following MS Doc for variable names so are exactly the same as
 *         in MS-OXOCAL (not following Java conventions)
 */
public class PSTGlobalObjectId {
    final protected static byte[] ReferenceByteArrayID = { 0x04, 0x00, 0x00, 0x00, (byte) 0x82, 0x00, (byte) 0xe0, 0x00,
        0x74, (byte) 0xc5, (byte) 0xb7, 0x10, 0x1a, (byte) 0x82, (byte) 0xe0, 0x08 };
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    byte[] ByteArrayID = new byte[16];
    byte YH;
    byte YL;
    byte M;
    byte D;
    int CreationTimeH;
    int CreationTimeL;
    Date CreationTime;
    long X = 0x0L;
    int Size;
    byte[] Data;

    public PSTGlobalObjectId(final byte[] pidData) {
        if (pidData.length < 32) {
            throw new AssertionError("pidDate is too short");
        }

        System.arraycopy(pidData, 0, this.ByteArrayID, 0, ReferenceByteArrayID.length);

        if (!Arrays.equals(this.ByteArrayID, ReferenceByteArrayID)) {
            throw new AssertionError("ByteArrayID is incorrect");
        }

        final ByteBuffer buffer = ByteBuffer
            .wrap(pidData, ReferenceByteArrayID.length, pidData.length - ReferenceByteArrayID.length)
            .order(ByteOrder.LITTLE_ENDIAN);

        this.YH = buffer.get();
        this.YL = buffer.get();
        this.M = buffer.get();
        this.D = buffer.get();
        this.CreationTimeL = buffer.getInt();
        this.CreationTimeH = buffer.getInt();
        this.CreationTime = PSTObject.filetimeToDate(this.CreationTimeH, this.CreationTimeL);
        this.X = buffer.getLong();
        this.Size = buffer.getInt();

        if (buffer.remaining() != this.Size) {
            throw new AssertionError("Incorrect remaining date in buffer to extract data");
        }

        this.Data = new byte[buffer.remaining()];
        buffer.get(this.Data, 0, buffer.remaining());
    }

    public static String bytesToHex(final byte[] bytes) {
        final char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            final int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    protected int getYearHigh() {
        return this.YH;
    }

    protected int getYearLow() {
        return (this.YL & 0xFF);
    }

    public int getYear() {
        return (this.YH << 8) | (this.YL & 0xFF);
    }

    public int getMonth() {
        return this.M;
    }

    public int getDay() {
        return this.D;
    }

    public Date getCreationTime() {
        return this.CreationTime;
    }

    protected int getCreationTimeLow() {
        return this.CreationTimeL;
    }

    protected int getCreationTimeHigh() {
        return this.CreationTimeH;
    }

    public int getDataSize() {
        return this.Size;
    }

    public byte[] getData() {
        return this.Data;
    }

    @Override
    public String toString() {
        return "Byte Array ID[" + bytesToHex(this.ByteArrayID) + "] " + "Year [" + this.getYear() + "] " + "Month["
            + this.M + "] " + "Day[" + this.D + "] CreationTime[" + this.CreationTime + "] " + "X[" + this.X + "] "
            + "Size[" + this.Size + "] " + "Data[" + bytesToHex(this.Data) + "]";
    }
}
