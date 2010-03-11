/*
 *
 */

package com.pff;

/**
 * An implementation of the LZFu algorithm to decompress RTF content
 * @author Richard Johnson
 */
public class LZFu {

	public static final String LZFU_HEADER = "{\\rtf1\\ansi\\mac\\deff0\\deftab720{\\fonttbl;}{\\f0\\fnil \\froman \\fswiss \\fmodern \\fscript \\fdecor MS Sans SerifSymbolArialTimes New RomanCourier{\\colortbl\\red0\\green0\\blue0\n\r\\par \\pard\\plain\\f0\\fs20\\b\\i\\u\\tab\\tx";

	public static String decode(byte[] data) {

		int compressedSize = (int)PSTObject.convertLittleEndianBytesToLong(data, 0, 4);
		int uncompressedSize = (int)PSTObject.convertLittleEndianBytesToLong(data, 4, 8);
		int compressionSig =  (int)PSTObject.convertLittleEndianBytesToLong(data, 8, 12);
		int compressedCRC =  (int)PSTObject.convertLittleEndianBytesToLong(data, 12, 16);

		if (compressionSig == 0x75465a4c) {
			// we are compressed...
			byte[] output = new byte[uncompressedSize];
			int outputPosition = 0;
			byte[] lzBuffer = new byte[4096];
			// preload our buffer.
			System.arraycopy(LZFU_HEADER.getBytes(), 0, lzBuffer, 0, LZFU_HEADER.length());
			int bufferPosition = LZFU_HEADER.length();
			int currentDataPosition = 16;

			// next byte is the flags,
			while (currentDataPosition < data.length - 7) {
				int flags = data[currentDataPosition++];
				for (int x = 0; x < 8; x++) {
					boolean isRef = (((flags >> x) & 1) == 1);
					if (isRef) {
						// get the starting point for the buffer and the
						// length to read
						int refOffsetOrig = data[currentDataPosition++] & 0xFF;
						int refSizeOrig = data[currentDataPosition++] & 0xFF;

						int refOffset = (refOffsetOrig << 4) | (refSizeOrig >>> 4);
						refOffset &= 0xFFF;
						int refSize = refSizeOrig & 0xF;
						refSize += 2;
						// copy the data from the buffer
						for (int y = 0; y < refSize; y++) {
							int index = refOffset+y;
							index %= 4096;
							output[outputPosition++] = lzBuffer[index];
							lzBuffer[bufferPosition] = lzBuffer[index];
							bufferPosition++;
							bufferPosition %= 4096;
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
