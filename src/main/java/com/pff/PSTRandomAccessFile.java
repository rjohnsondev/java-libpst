package com.pff;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Implementation based on java io random access file
 * 
 * @author Paul van Assen
 * 
 */
class PSTRandomAccessFile implements PSTRandomFile {
	private final RandomAccessFile raf;

	PSTRandomAccessFile(RandomAccessFile raf) {
		this.raf = raf;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.pff.PSTRandomFile#seek(long)
	 */
	public void seek(long pos) throws IOException {
		raf.seek(pos);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.pff.PSTRandomFile#read(byte[])
	 */
	public void read(byte[] b) throws IOException {
		raf.read(b);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see com.pff.PSTRandomFile#readByte()
	 */
	public byte readByte() throws IOException {
		return raf.readByte();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see com.pff.PSTRandomFile#read()
	 */
	public int read() throws IOException {
		return raf.read();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see com.pff.PSTRandomFile#close()
	 */
	public void close() throws IOException {
		raf.close();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see com.pff.PSTRandomFile#getFilePointer()
	 */
	public long getFilePointer() throws IOException {
		return raf.getFilePointer();
	}
}
