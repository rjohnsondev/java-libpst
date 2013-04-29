package com.pff;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

import com.pff.exceptions.PSTException;
import com.pff.objects.sub.PSTTimeZone;

public abstract class PSTUtils {

	public static final String LZFU_HEADER = "{\\rtf1\\ansi\\mac\\deff0\\deftab720{\\fonttbl;}{\\f0\\fnil \\froman \\fswiss \\fmodern \\fscript \\fdecor MS Sans SerifSymbolArialTimes New RomanCourier{\\colortbl\\red0\\green0\\blue0\n\r\\par \\pard\\plain\\f0\\fs20\\b\\i\\u\\tab\\tx";


	// substitution table for the compressible encryption type.
	static int[] compEnc = {
	    0x47, 0xf1, 0xb4, 0xe6, 0x0b, 0x6a, 0x72, 0x48, 0x85, 0x4e, 0x9e, 0xeb, 0xe2, 0xf8, 0x94, 0x53,
	    0xe0, 0xbb, 0xa0, 0x02, 0xe8, 0x5a, 0x09, 0xab, 0xdb, 0xe3, 0xba, 0xc6, 0x7c, 0xc3, 0x10, 0xdd,
	    0x39, 0x05, 0x96, 0x30, 0xf5, 0x37, 0x60, 0x82, 0x8c, 0xc9, 0x13, 0x4a, 0x6b, 0x1d, 0xf3, 0xfb,
	    0x8f, 0x26, 0x97, 0xca, 0x91, 0x17, 0x01, 0xc4, 0x32, 0x2d, 0x6e, 0x31, 0x95, 0xff, 0xd9, 0x23,
	    0xd1, 0x00, 0x5e, 0x79, 0xdc, 0x44, 0x3b, 0x1a, 0x28, 0xc5, 0x61, 0x57, 0x20, 0x90, 0x3d, 0x83,
	    0xb9, 0x43, 0xbe, 0x67, 0xd2, 0x46, 0x42, 0x76, 0xc0, 0x6d, 0x5b, 0x7e, 0xb2, 0x0f, 0x16, 0x29,
	    0x3c, 0xa9, 0x03, 0x54, 0x0d, 0xda, 0x5d, 0xdf, 0xf6, 0xb7, 0xc7, 0x62, 0xcd, 0x8d, 0x06, 0xd3,
	    0x69, 0x5c, 0x86, 0xd6, 0x14, 0xf7, 0xa5, 0x66, 0x75, 0xac, 0xb1, 0xe9, 0x45, 0x21, 0x70, 0x0c,
	    0x87, 0x9f, 0x74, 0xa4, 0x22, 0x4c, 0x6f, 0xbf, 0x1f, 0x56, 0xaa, 0x2e, 0xb3, 0x78, 0x33, 0x50,
	    0xb0, 0xa3, 0x92, 0xbc, 0xcf, 0x19, 0x1c, 0xa7, 0x63, 0xcb, 0x1e, 0x4d, 0x3e, 0x4b, 0x1b, 0x9b,
	    0x4f, 0xe7, 0xf0, 0xee, 0xad, 0x3a, 0xb5, 0x59, 0x04, 0xea, 0x40, 0x55, 0x25, 0x51, 0xe5, 0x7a,
	    0x89, 0x38, 0x68, 0x52, 0x7b, 0xfc, 0x27, 0xae, 0xd7, 0xbd, 0xfa, 0x07, 0xf4, 0xcc, 0x8e, 0x5f,
	    0xef, 0x35, 0x9c, 0x84, 0x2b, 0x15, 0xd5, 0x77, 0x34, 0x49, 0xb6, 0x12, 0x0a, 0x7f, 0x71, 0x88,
	    0xfd, 0x9d, 0x18, 0x41, 0x7d, 0x93, 0xd8, 0x58, 0x2c, 0xce, 0xfe, 0x24, 0xaf, 0xde, 0xb8, 0x36,
	    0xc8, 0xa1, 0x80, 0xa6, 0x99, 0x98, 0xa8, 0x2f, 0x0e, 0x81, 0x65, 0x73, 0xe4, 0xc2, 0xa2, 0x8a,
	    0xd4, 0xe1, 0x11, 0xd0, 0x08, 0x8b, 0x2a, 0xf2, 0xed, 0x9a, 0x64, 0x3f, 0xc1, 0x6c, 0xf9, 0xec
	};
	
	/**
	 * Output a dump of data in hex format in the order it was read in
	 * @param data
	 * @param pretty
	 */
	public static void printHexFormatted(byte[] data, boolean pretty) {
		printHexFormatted(data,pretty, new int[0]);
	}
	protected static void printHexFormatted(byte[] data, boolean pretty, int[] indexes) {
		// groups of two
		if (pretty) { System.out.println("---"); }
		long tmpLongValue;
		String line = "";
		int nextIndex = 0;
		int indexIndex = 0;
		if (indexes.length > 0) {
			nextIndex = indexes[0];
			indexIndex++;
		}
		for (int x = 0; x < data.length; x++) {
			tmpLongValue = (long)data[x] & 0xff;
			
			if (indexes.length > 0 &&
				x == nextIndex &&
				nextIndex < data.length)
			{
				System.out.print("+");
				line += "+";
				while (indexIndex < indexes.length-1 && indexes[indexIndex] <= nextIndex) 
				{
					indexIndex++;
				}
				nextIndex = indexes[indexIndex];
				//indexIndex++;
			}
			
			if (Character.isLetterOrDigit((char)tmpLongValue)) {
				line += (char)tmpLongValue;
			}
			else
			{
				line += ".";
			}
			
			if (Long.toHexString(tmpLongValue).length() < 2) {
				System.out.print("0");
			}
			System.out.print(Long.toHexString(tmpLongValue));
			if (x % 2 == 1 && pretty) {
				System.out.print(" ");
			}
			if (x % 16 == 15 && pretty) {
				System.out.print(" "+line);
				System.out.println("");
				line = "";
			}
		}
		if (pretty) { System.out.println(" "+line); System.out.println("---"); System.out.println(data.length); } else {  }
	}
	

	
	/**
	 * decode a lump of data that has been encrypted with the compressible encryption
	 * @param data
	 * @return decoded data
	 */
	public static byte[] decode(byte[] data) {
		int temp;
		for (int x = 0; x < data.length; x++) {
			temp = data[x] & 0xff;
			data[x] = (byte)compEnc[temp];
		}
		return data;
	}
	

	public static byte[] encode(byte[] data) {
		// create the encoding array...
		int[] enc = new int[compEnc.length];
		for (int x = 0; x < enc.length; x++) {
			enc[compEnc[x]] = x;
		}
		
		// now it's just the same as decode...
		int temp;
		for (int x = 0; x < data.length; x++) {
			temp = data[x] & 0xff;
			data[x] = (byte)enc[temp];
		}
		return data;
	}

	
	/**
	 * Utility function for converting little endian bytes into a usable java long
	 * @param data
	 * @return long version of the data
	 */
	public static long convertLittleEndianBytesToLong(byte[] data) {
		return convertLittleEndianBytesToLong(data, 0, data.length);
	}
	/**
	 * Utility function for converting little endian bytes into a usable java long
	 * @param data
	 * @param start
	 * @param end
	 * @return long version of the data
	 */
	public static long convertLittleEndianBytesToLong(byte[] data, int start, int end) {
		
		long offset = data[end-1] & 0xff;
		long tmpLongValue;
		for (int x = end-2; x >= start; x--) {
			offset = offset << 8;
			tmpLongValue = (long)data[x] & 0xff;
			offset |= tmpLongValue;
		}
		
		return offset;
	}
	
	/**
	 * Utility function for converting big endian bytes into a usable java long
	 * @param data
	 * @param start
	 * @param end
	 * @return long version of the data
	 */
	public static long convertBigEndianBytesToLong(byte[] data, int start, int end) {
		
		long offset = 0;
		for ( int x = start; x < end; ++x ) {
			offset = offset << 8;
			offset |= ((long)data[x] & 0xFFL);
		}
		
		return offset;
	}
/*
	protected static boolean isPSTArray(byte[] data) {
		return (data[0] == 1 && data[1] == 1);
	}
/**/	
/*	
	protected static int[] getBlockOffsets(RandomAccessFile in, byte[] data)
		throws IOException, PSTException
	{
		// is the data an array?
		if (!(data[0] == 1 && data[1] == 1))
		{
			throw new PSTException("Unable to process array, does not appear to be one!");
		}

		// we are an array!
		// get the array items and merge them together
		int numberOfEntries = (int)PSTObject.convertLittleEndianBytesToLong(data, 2, 4);
		int[] output = new int[numberOfEntries];
		int tableOffset = 8;
		int blockOffset = 0;
		for (int y = 0; y < numberOfEntries; y++) {
			// get the offset identifier
			long tableOffsetIdentifierIndex = PSTObject.convertLittleEndianBytesToLong(data, tableOffset, tableOffset+8);
			// clear the last bit of the identifier.  Why so hard?
			tableOffsetIdentifierIndex = (tableOffsetIdentifierIndex & 0xfffffffe);
			OffsetIndexItem tableOffsetIdentifier = PSTObject.getOffsetIndexNode(in, tableOffsetIdentifierIndex);
			blockOffset += tableOffsetIdentifier.size;
			output[y] = blockOffset;
			tableOffset += 8;
		}

		// replace the item data with the stuff from the array...
		return output;
	}
/**/

	

	
	
	/**
	 * the code below was taken from a random apache project
	 * http://www.koders.com/java/fidA9D4930E7443F69F32571905DD4CA01E4D46908C.aspx
	 * my bit-shifting isn't that 1337
	 */
	
	/**
     * <p>The difference between the Windows epoch (1601-01-01
     * 00:00:00) and the Unix epoch (1970-01-01 00:00:00) in
     * milliseconds: 11644473600000L. (Use your favorite spreadsheet
     * program to verify the correctness of this value. By the way,
     * did you notice that you can tell from the epochs which
     * operating system is the modern one? :-))</p>
     */
    private static final long EPOCH_DIFF = 11644473600000L;
	
	/**
     * <p>Converts a Windows FILETIME into a {@link Date}. The Windows
     * FILETIME structure holds a date and time associated with a
     * file. The structure identifies a 64-bit integer specifying the
     * number of 100-nanosecond intervals which have passed since
     * January 1, 1601. This 64-bit value is split into the two double
     * words stored in the structure.</p>
     *
     * @param high The higher double word of the FILETIME structure.
     * @param low The lower double word of the FILETIME structure.
     * @return The Windows FILETIME as a {@link Date}.
     */
    public static Date filetimeToDate(final int high, final int low) {
        final long filetime = ((long) high) << 32 | (low & 0xffffffffL);
		//System.out.printf("0x%X\n", filetime);
        final long ms_since_16010101 = filetime / (1000 * 10);
        final long ms_since_19700101 = ms_since_16010101 - EPOCH_DIFF;
        return new Date(ms_since_19700101);
    }

    public static Calendar apptTimeToCalendar(int minutes) {
    	final long ms_since_16010101 = minutes * (60*1000L);
        final long ms_since_19700101 = ms_since_16010101 - EPOCH_DIFF;
        Calendar c = Calendar.getInstance(PSTTimeZone.utcTimeZone);
        c.setTimeInMillis(ms_since_19700101);
        return c;
    }
    
    public static Calendar apptTimeToUTC(int minutes, PSTTimeZone tz) {
		// Must convert minutes since 1/1/1601 in local time to UTC
		// There's got to be a better way of doing this...
		// First get a UTC calendar object that contains _local time_
		Calendar cUTC = apptTimeToCalendar(minutes);
		if ( tz != null ) {
			// Create an empty Calendar object with the required time zone
			Calendar cLocal = Calendar.getInstance(tz.getSimpleTimeZone());
			cLocal.clear();
			
			// Now transfer the local date/time from the UTC calendar object
			// to the object that knows about the time zone...
			cLocal.set(cUTC.get(Calendar.YEAR),
					   cUTC.get(Calendar.MONTH),
					   cUTC.get(Calendar.DATE),
					   cUTC.get(Calendar.HOUR_OF_DAY),
					   cUTC.get(Calendar.MINUTE),
					   cUTC.get(Calendar.SECOND));
			
			// Get the true UTC from the local time calendar object.
			// Drop any milliseconds, they won't be printed anyway!
			long utcs = cLocal.getTimeInMillis() / 1000;
			
			// Finally, set the true UTC in the UTC calendar object
			cUTC.setTimeInMillis(utcs * 1000);
		} // else hope for the best!

		return cUTC;
	}

    
    
    
    public static int getCompEnc(int value) {
    	return compEnc[value];
    }
    
    
    public static String decodeLZFU(byte[] data) throws PSTException {

		@SuppressWarnings("unused")
		int compressedSize = (int)PSTUtils.convertLittleEndianBytesToLong(data, 0, 4);
		int uncompressedSize = (int)PSTUtils.convertLittleEndianBytesToLong(data, 4, 8);
		int compressionSig =  (int)PSTUtils.convertLittleEndianBytesToLong(data, 8, 12);
		@SuppressWarnings("unused")
		int compressedCRC =  (int)PSTUtils.convertLittleEndianBytesToLong(data, 12, 16);

		if (compressionSig == 0x75465a4c) {
			// we are compressed...
			byte[] output = new byte[uncompressedSize];
			int outputPosition = 0;
			byte[] lzBuffer = new byte[4096];
			// preload our buffer.
			try {
				byte[] bytes = LZFU_HEADER.getBytes("US-ASCII");
				System.arraycopy(bytes, 0, lzBuffer, 0, LZFU_HEADER.length());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			int bufferPosition = LZFU_HEADER.length();
			int currentDataPosition = 16;

			// next byte is the flags,
			while (currentDataPosition < data.length - 2 && outputPosition < output.length) {
				int flags = data[currentDataPosition++] & 0xFF;
				for (int x = 0; x < 8 && outputPosition < output.length; x++) {
					boolean isRef = ((flags & 1) == 1);
					flags >>= 1;
					if (isRef) {
						// get the starting point for the buffer and the
						// length to read
						int refOffsetOrig = data[currentDataPosition++] & 0xFF;
						int refSizeOrig = data[currentDataPosition++] & 0xFF;
						int refOffset = (refOffsetOrig << 4) | (refSizeOrig >>> 4);
						int refSize = (refSizeOrig & 0xF) + 2;
						//refOffset &= 0xFFF;
						try {
							// copy the data from the buffer
							int index = refOffset;
							for (int y = 0; y < refSize && outputPosition < output.length; y++) {
								output[outputPosition++] = lzBuffer[index];
								lzBuffer[bufferPosition] = lzBuffer[index];
								bufferPosition++;
								bufferPosition %= 4096;
								++index;
								index %= 4096;
							}
						} catch ( Exception e ) {
							e.printStackTrace();
						}

					} else {
						// copy the byte over
						lzBuffer[bufferPosition] = 	data[currentDataPosition];
						bufferPosition++;
						bufferPosition %= 4096;
						output[outputPosition++] = data[currentDataPosition++];
					}
				}
			}

			if ( outputPosition != uncompressedSize ) {
				throw new PSTException(String.format("Error decompressing RTF! Expected %d bytes, got %d bytes\n", uncompressedSize, outputPosition));
			}
			return new String(output).trim();

		} else if (compressionSig == 0x414c454d) {
			// we are not compressed!
			// just return the rest of the contents as a string
			byte[] output = new byte[data.length-16];
			System.arraycopy(data, 16, output, 0, data.length-16);
			return new String(output).trim();
		}

		return "";
	}
    
}
