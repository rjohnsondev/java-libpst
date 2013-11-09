package com.pff;

import java.io.EOFException;
import java.io.IOException;

import org.apache.commons.vfs2.RandomAccessContent;

/**
 * Implementation based on VFS random access content
 * 
 * @author Paul van Assen
 * 
 */
class PSTRandomAccessContent implements PSTRandomFile {
	private final RandomAccessContent rac;

	PSTRandomAccessContent(RandomAccessContent rac) {
		this.rac = rac;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.pff.PSTRandomFile#seek(long)
	 */
	public void seek(long pos) throws IOException {
		rac.seek(pos);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see com.pff.PSTRandomFile#read(byte[])
	 */
	public void read(byte[] b) throws IOException {
	    try {
		rac.readFully(b);
	    }
	    catch (EOFException e) {
		e.printStackTrace();
		// Ignore
	    }
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see com.pff.PSTRandomFile#readByte()
	 */
	public byte readByte() throws IOException {
		return rac.readByte();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see com.pff.PSTRandomFile#read()
	 */
	public int read() throws IOException {
		return rac.readByte();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see com.pff.PSTRandomFile#close()
	 */
	public void close() throws IOException {
		rac.close();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see com.pff.PSTRandomFile#getFilePointer()
	 */
	public long getFilePointer() throws IOException {
		return rac.getFilePointer();
	}

}
