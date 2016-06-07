package com.pff;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Date;

/**
 * Class to represent a decoded PidLidGlobalObjectId or PidLidCleanGlobalObjectId (for Meeting Exceptions)
 * See [MS-OXOCAL] https://msdn.microsoft.com/en-us/library/cc425490(v=exchg.80).aspx, sections 2.2.1.27(PidLidGlobalObjectId) & 2.2.1.28(PidLidCleanGlobalObjectId)
 *
 * @author Nick Buller
 *         NOTE: Following MS Doc for variable names so are exactly the same as in MS-OXOCAL (not following Java conventions)
 */
public class PSTGlobalObjectId {
	final protected static byte[] ReferenceByteArrayID = {0x04, 0x00, 0x00, 0x00, (byte) 0x82, 0x00, (byte) 0xe0, 0x00, 0x74, (byte) 0xc5, (byte) 0xb7, 0x10, 0x1a, (byte) 0x82, (byte) 0xe0, 0x08};
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

	public PSTGlobalObjectId(byte[] pidData) {
		if (pidData.length < 32) {
			throw new AssertionError("pidDate is too short");
		}

		System.arraycopy(pidData, 0, ByteArrayID, 0, ReferenceByteArrayID.length);

		if (!Arrays.equals(ByteArrayID, ReferenceByteArrayID)) {
			throw new AssertionError("ByteArrayID is incorrect");
		}

		ByteBuffer buffer = ByteBuffer.wrap(pidData, ReferenceByteArrayID.length, pidData.length - ReferenceByteArrayID.length).order(ByteOrder.LITTLE_ENDIAN);

		YH = buffer.get();
		YL = buffer.get();
		M = buffer.get();
		D = buffer.get();
		CreationTimeL = buffer.getInt();
		CreationTimeH = buffer.getInt();
		CreationTime = PSTObject.filetimeToDate(CreationTimeH, CreationTimeL);
		X = buffer.getLong();
		Size = buffer.getInt();

		if (buffer.remaining() != Size) {
			throw new AssertionError("Incorrect remaining date in buffer to extract data");
		}

		Data = new byte[buffer.remaining()];
		buffer.get(Data, 0, buffer.remaining());
	}

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	protected int getYearHigh() {
		return YH;
	}

	protected int getYearLow() {
		return (YL & 0xFF);
	}

	public int getYear() {
		return (YH << 8) | (YL & 0xFF);
	}

	public int getMonth() {
		return M;
	}

	public int getDay() {
		return D;
	}

	public Date getCreationTime() {
		return CreationTime;
	}

	protected int getCreationTimeLow() {
		return CreationTimeL;
	}

	protected int getCreationTimeHigh() {
		return CreationTimeH;
	}

	public int getDataSize() {
		return Size;
	}

	public byte[] getData() {
		return Data;
	}

	public String toString() {
		return "Byte Array ID[" + bytesToHex(ByteArrayID) + "] " +
			"Year [" + getYear() + "] " +
			"Month[" + M + "] " +
			"Day[" + D + "] CreationTime[" + CreationTime + "] " +
			"X[" + X + "] " +
			"Size[" + Size + "] " +
			"Data[" + bytesToHex(Data) + "]";
	}
}
