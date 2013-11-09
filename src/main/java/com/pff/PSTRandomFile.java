package com.pff;

import java.io.IOException;

/**
 * Internal random access content file. This adds support to VFS2 random access
 * content, giving a lot of flexibility to where the PST file actually is. 
 * 
 * This in turn is part of the effort to create a VFS2 provider for PST's
 * 
 * @author Paul van Assen
 * 
 * @see java.io.RandomAccessFile
 * @see org.apache.commons.vfs2.RandomAccessContent
 */
interface PSTRandomFile {

	/**
	 * @see java.io.RandomAccessFile.seek(long)
	 */
	void seek(long pos) throws IOException;

	/**
	 * @see java.io.RandomAccessFile.read(byte[])
	 */
	void read(byte[] b) throws IOException;

	/**
	 * @see java.io.RandomAccessFile.readByte()
	 */
	byte readByte() throws IOException;

	/**
	 * @see java.io.RandomAccessFile.read()
	 */
	int read() throws IOException;

	/**
	 * @see java.io.RandomAccessFile.close()
	 */
	void close() throws IOException;

	/**
	 * @see java.io.RandomAccessFile.getFilePointer()
	 */
	long getFilePointer() throws IOException;
}
