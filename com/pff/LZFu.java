/**

 */
package com.pff;

/**
 * Taken from http://www.freeutils.net/source/jtnef/rtfcompressed.jsp
 * Licensed under GPL, if you want to use this commercially, re-implement
 */
public class LZFu {

	public static final String LZFU_HEADER =  "{\\rtf1\\ansi\\mac\\deff0\\deftab720{\\fonttbl;}{\\f0\\fnil \\froman\\fswiss \\fmodern \\fscript \\fdecor MS Sans SerifSymbolArialTimes NewRomanCourier{\\colortbl\\red0\\green0\\blue0\n\r\\par \\pard\\plain\\f0\\fs20\\b\\i\\u\\tab\\tx";
	
	public static String decode(byte[] data)
	{
		byte[] temp = decompressRTF(data);
		return new String(temp).trim();
	}
	
    /**
     * Returns an unsigned 32-bit value from little-endian ordered bytes.
     *
     * @param buf a byte array from which byte values are taken
     * @param offset the offset within buf from which byte values are taken
     * @return an unsigned 32-bit value as a long
     */
    public static long getU32(byte[] buf, int offset) {
        return ((buf[offset] & 0xFF) | ((buf[offset + 1] & 0xFF) << 8) | 
               ((buf[offset + 2] & 0xFF) << 16) | ((buf[offset + 3] & 0xFF) << 24)) & 0x00000000FFFFFFFFL;
    }

    /** The lookup table used in the CRC32 calculation */
    static int[] CRC32_TABLE;
    static {
        CRC32_TABLE = new int[256];
        for (int i = 0; i < 256; i++) {
            int c = i;
            for (int j = 0; j < 8; j++)
                c = ((c & 1) == 1) ? 0xEDB88320 ^ (c >>> 1) : c >>> 1;
            CRC32_TABLE[i] = c;
        }
    }

	
    /**
     * Calculates the CRC32 of the given bytes.
     * The CRC32 calculation is similar to the standard one as demonstrated
     * in RFC 1952, but with the inversion (before and after the calculation)
     * omitted.
     * 
     * @param buf the byte array to calculate CRC32 on
     * @param off the offset within buf at which the CRC32 calculation will start
     * @param len the number of bytes on which to calculate the CRC32
     * @return the CRC32 value
     */
    public static int calculateCRC32(byte[] buf, int off, int len) {
        int c = 0;
        int end = off + len;
        for (int i = off; i < end; i++)
            c = CRC32_TABLE[(c ^ buf[i]) & 0xFF] ^ (c >>> 8);
        return c;
    }
    
    /**
     * Returns an unsigned 8-bit value from a byte array.
     *
     * @param buf a byte array from which byte value is taken
     * @param offset the offset within buf from which byte value is taken
     * @return an unsigned 8-bit value as an int
     */
    public static int getU8(byte[] buf, int offset) {
        return buf[offset] & 0xFF;
    }



    static byte[] COMPRESSED_RTF_PREBUF;
    static {
        try {
            String prebuf =
                "{\\rtf1\\ansi\\mac\\deff0\\deftab720{\\fonttbl;}" +
                "{\\f0\\fnil \\froman \\fswiss \\fmodern \\fscript " +
                "\\fdecor MS Sans SerifSymbolArialTimes New RomanCourier" +
                "{\\colortbl\\red0\\green0\\blue0\n\r\\par " +
                "\\pard\\plain\\f0\\fs20\\b\\i\\u\\tab\\tx";
            COMPRESSED_RTF_PREBUF = prebuf.getBytes("US-ASCII");
        } catch (Exception uee) {
            // never happens
        }
    }

    
    public static byte[] decompressRTF(byte[] src) {
        byte[] dst; // destination for uncompressed bytes
        int in = 0; // current position in src array
        int out = 0; // current position in dst array

        // get header fields (as defined in RTFLIB.H)
        if (src == null || src.length < 16)
            throw new IllegalArgumentException("Invalid compressed-RTF header");

        int compressedSize = (int)getU32(src, in);
        in += 4;
        int uncompressedSize = (int)getU32(src, in);
        in += 4;
        int magic = (int)getU32(src, in);
        in += 4;
        int crc32 = (int)getU32(src, in);
        in += 4;

        if (compressedSize != src.length - 4) // check size excluding the size field itself
            throw new IllegalArgumentException("compressed-RTF data size mismatch");

        if (crc32 != calculateCRC32(src, 16, src.length - 16))
            throw new IllegalArgumentException("compressed-RTF CRC32 failed");

        // process the data
        if (magic == 0x414c454d) { // magic number that identifies the stream as a uncompressed stream
            dst = new byte[uncompressedSize];
            System.arraycopy(src, in, dst, out, uncompressedSize); // just copy it as it is
        } else if (magic == 0x75465a4c) { // magic number that identifies the stream as a compressed stream
            dst = new byte[COMPRESSED_RTF_PREBUF.length + uncompressedSize];
            System.arraycopy(COMPRESSED_RTF_PREBUF, 0, dst, 0, COMPRESSED_RTF_PREBUF.length);
            out = COMPRESSED_RTF_PREBUF.length;
            int flagCount = 0;
            int flags = 0;
            while (out < dst.length) {
                // each flag byte flags 8 literals/references, 1 per bit
                flags = (flagCount++ % 8 == 0) ? getU8(src, in++) : flags >> 1;
                if ((flags & 1) == 1) { // each flag bit is 1 for reference, 0 for literal
                    int offset = getU8(src, in++);
                    int length = getU8(src, in++);
                    offset = (offset << 4) | (length >>> 4); // the offset relative to block start
                    length = (length & 0xF) + 2; // the number of bytes to copy
                    // the decompression buffer is supposed to wrap around back
                    // to the beginning when the end is reached. we save the
                    // need for such a buffer by pointing straight into the data
                    // buffer, and simulating this behaviour by modifying the
                    // pointers appropriately.
                    offset = (out / 4096) * 4096 + offset;
                    if (offset >= out) // take from previous block
                        offset -= 4096;
                    // note: can't use System.arraycopy, because the referenced
                    // bytes can cross through the current out position.
                    int end = offset + length;
                    while (offset < end)
                        dst[out++] = dst[offset++];
                } else { // literal
                    dst[out++] = src[in++];
                }
            }
            // copy it back without the prebuffered data
            src = dst;
            dst = new byte[uncompressedSize];
            System.arraycopy(src, COMPRESSED_RTF_PREBUF.length, dst, 0, uncompressedSize);
        } else { // unknown magic number
            throw new IllegalArgumentException("Unknown compression type (magic number " + magic + ")");
        }

        return dst;
    }
}

/**
 This will probably be used someday for LGPL

		System.out.println("----------------");
		
		int compressedSize = (int)PSTObject.convertLittleEndianBytesToLong(data, 0, 4);
		int uncompressedSize = (int)PSTObject.convertLittleEndianBytesToLong(data, 4, 8);
		int compressionSig =  (int)PSTObject.convertLittleEndianBytesToLong(data, 8, 12);
		int compressedCRC =  (int)PSTObject.convertLittleEndianBytesToLong(data, 12, 16);
		
		
		if (compressionSig == 0x75465a4c) {
			// we are compressed...
			byte[] output = new byte[uncompressedSize+2];
			int outputPosition = 0;
			byte[] lzBuffer = new byte[4096];
			// preload our buffer.
			System.arraycopy(LZFU_HEADER.getBytes(), 0, lzBuffer, 0, LZFU_HEADER.length());
			int bufferPosition = LZFU_HEADER.length();
			
			int currentDataPosition = 16;
			
//			currentDataPosition++;
			// for each bit in the flag byte...
			// which represents a block
			while (currentDataPosition+1 < data.length) {
				byte flag = data[currentDataPosition];
				int flagMask = 1;
				
				while (flagMask != 0 && (currentDataPosition+1) < data.length) {
					
					if ((flag & flagMask) != 0 ) {

//	                    int offset = getU8(src, in++);
//	                    int length = getU8(src, in++);
//	                    
//	                    offset = (offset << 4) | (length >>> 4); // the offset relative to block start
//	                    length = (length & 0xF) + 2; // the number of bytes to copy
	                    
	                    
//						int ref = ((data[currentDataPosition+1] << 8) | data[currentDataPosition]) & 0xffff;
						
						
						
						
						int refOffset = (data[currentDataPosition+1] & 0xff) << 4 | ((data[currentDataPosition] & 0xff) >>> 4);
						int refSize = (data[currentDataPosition] & 0xf)+2;
						currentDataPosition += 2;
						
	                    System.out.println("Offset: " + refOffset + " length: " + refSize);
						
//						System.out.println(Long.toBinaryString(refOffset));
//						refSize += 2;
//						System.arraycopy(lzBuffer, refOffset, output, outputPosition, refSize);
//						currentDataPosition += 2;
						for (int x = 0; x < refSize; x++) {
							output[outputPosition+x] = lzBuffer[(refOffset+x)%4096];
						}
						outputPosition += refSize;
					} else {
						// we are a direct byte copy.
						byte theByte = data[currentDataPosition];
						lzBuffer[bufferPosition] = theByte;
						output[outputPosition] = theByte;
						outputPosition++;
						currentDataPosition++;
					}
					bufferPosition++;
					bufferPosition %= 4096;
					
					flagMask <<= 1 & 0xff; // we are only dealing with 4 bits, so the & 0xff
				}
			}
			System.exit(0);
//			StringBuffer outputBuffer = new StringBuffer();
//			char theChar;
//			for (int x = 0; x < outputPosition; x++) {
//				theChar = (char)data[x];
//				outputBuffer.append(theChar);
//			}
//			System.out.println(output);
			return new String(output);
			
		} else if (compressionSig == 0x414c454d) {
			// we are not compressed!
			// just return the rest of the contents as a string
			byte[] output = new byte[data.length-16];
			System.arraycopy(data, 16, output, 0, data.length-16);
			return new String(output);
		}
		
		return "";
		*/
