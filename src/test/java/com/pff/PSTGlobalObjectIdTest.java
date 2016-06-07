package com.pff;

import java.util.Date;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Author: Nick Buller
 */
public class PSTGlobalObjectIdTest {

	@Test
	public void unpackValid() {
//		Global Object ID:
//		Byte Array ID = cb: 16 lpb: 040000008200E00074C5B7101A82E008
//		Year: 0x0000 = 0
//		Month: 0x00 = 0 = 0x0
//		Day: 0x00 = 0
//		Creation Time = 0x01D04F6F:0xA226A470 = 01:50:07.415 PM 23/02/2015
//		X: 0x00000000:0x00000000
//		Size: 0x10 = 16
//		Data = cb: 16 lpb: 086DFAD3919FD44089199898CDCF4DC2
		byte[] objectId = {
			0x04, 0x00, 0x00, 0x00, (byte) 0x82, 0x00, (byte) 0xE0, 0x00, 0x74, (byte) 0xC5, (byte) 0xB7, 0x10, 0x1A, (byte) 0x82, (byte) 0xE0, 0x08, // Byte Array ID
			0x07, // Year Hi
			(byte) 0xde, // Year Low
			0x0b, // Month
			0x14, // Day
			0x70, (byte) 0xA4, 0x26, (byte) 0xA2, 0x6F, 0x4F, (byte) 0xD0, 0x01, // Creation Time
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // X
			0x10, 0x00, 0x00, 0x00, // Size
			0x08, 0x6D, (byte) 0xFA, (byte) 0xD3, (byte) 0x91, (byte) 0x9F, (byte) 0xD4, 0x40, (byte) 0x89, 0x19, (byte) 0x98, (byte) 0x98, (byte) 0xCD, (byte) 0xCF, 0x4D, (byte) 0xC2 // Data
		};

		PSTGlobalObjectId object = new PSTGlobalObjectId(objectId);

		assertThat("Validate YearHi is correct", object.getYearLow(), equalTo(0x00DE));
		assertThat("Validate YearLow is correct", object.getYearHigh(), equalTo(0x7));
		assertThat("Validate Year is correct", object.getYear(), equalTo(2014));
		assertThat("Validate Day is correct", object.getDay(), equalTo(20));
		assertThat("Validate Month is correct", object.getMonth(), equalTo(11));
		assertThat("Validate CreationTimeLow is correct", object.getCreationTimeLow(), equalTo(0xA226A470));
		assertThat("Validate CreationTimeHigh is correct", object.getCreationTimeHigh(), equalTo(0x01D04F6F));
		assertThat("Validate CreationTime is correct", object.getCreationTime().getTime(), equalTo(new Date(1424699407415L).getTime()));
		assertThat("Validate Size is correct", object.getDataSize(), equalTo(16));
		assertThat("Validate Size of date matches actual data size", object.getData().length, equalTo(object.getDataSize()));
		assertThat("Validate Date is correct", PSTGlobalObjectId.bytesToHex(object.getData()), equalTo("086DFAD3919FD44089199898CDCF4DC2"));
	}

	@Test(expected = AssertionError.class)
	public void unpackWithInvalidIdSignature() {
		byte[] objectId = {
			0x04, 0x00, 0x00, 0x00, (byte) 0x82, 0x00, (byte) 0xE0, 0x00, 0x74, (byte) 0xC5, (byte) 0xB7, 0x10, 0x1A, (byte) 0x82, (byte) 0xE0, 0x00, // Byte Array ID (last byte 00 rather then 08
			0x07, // Year Hi
			(byte) 0xde, // Year Low
			0x0b, // Month
			0x14, // Day
			0x70, (byte) 0xA4, 0x26, (byte) 0xA2, 0x6F, 0x4F, (byte) 0xD0, 0x01, // Creation Time
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // X
			0x10, 0x00, 0x00, 0x00, // Size
			0x08, 0x6D, (byte) 0xFA, (byte) 0xD3, (byte) 0x91, (byte) 0x9F, (byte) 0xD4, 0x40, (byte) 0x89, 0x19, (byte) 0x98, (byte) 0x98, (byte) 0xCD, (byte) 0xCF, 0x4D, (byte) 0xC2 // Data
		};

		PSTGlobalObjectId object = new PSTGlobalObjectId(objectId);
	}

	@Test(expected = AssertionError.class)
	public void unpackWithInvalidIdData() {
		byte[] objectId = {
			0x04, 0x00, 0x00, 0x00, (byte) 0x82, 0x00, (byte) 0xE0, 0x00, 0x74, (byte) 0xC5, (byte) 0xB7, 0x10, 0x1A, (byte) 0x82, (byte) 0xE0, 0x08, // Byte Array ID
			0x07, // Year Hi
			(byte) 0xde, // Year Low
			0x0b, // Month
			0x14, // Day
			0x70, (byte) 0xA4, 0x26, (byte) 0xA2, 0x6F, 0x4F, (byte) 0xD0, 0x01, // Creation Time
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // X
			0x10, 0x00, 0x00, 0x00, // Size
			0x08, 0x6D, (byte) 0xFA, (byte) 0xD3, (byte) 0x91, (byte) 0x9F, (byte) 0xD4, 0x40, (byte) 0x89, 0x19, (byte) 0x98, (byte) 0x98, (byte) 0xCD, (byte) 0xCF, 0x4D // Data (missing last byte)
		};

		PSTGlobalObjectId object = new PSTGlobalObjectId(objectId);
	}

	@Test(expected = AssertionError.class)
	public void unpackWithInvalidIdDataLength() {
		byte[] objectId = {
			0x04, 0x00, 0x00, 0x00, (byte) 0x82, 0x00, (byte) 0xE0, 0x00, 0x74, (byte) 0xC5, (byte) 0xB7, 0x10, 0x1A, (byte) 0x82, (byte) 0xE0, 0x08, // Byte Array ID
			0x07, // Year Hi
			(byte) 0xde, // Year Low
			0x0b, // Month
			0x14, // Day
			0x70, (byte) 0xA4, 0x26, (byte) 0xA2, 0x6F, 0x4F, (byte) 0xD0, 0x01, // Creation Time
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // X
			0x10, 0x10, 0x00, 0x00, // Size
			0x08, 0x6D, (byte) 0xFA, (byte) 0xD3, (byte) 0x91, (byte) 0x9F, (byte) 0xD4, 0x40, (byte) 0x89, 0x19, (byte) 0x98, (byte) 0x98, (byte) 0xCD, (byte) 0xCF, 0x4D, (byte) 0xC2 // Data
		};

		PSTGlobalObjectId object = new PSTGlobalObjectId(objectId);
	}
}
