package com.pff.source;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class PSTRandomAccessFile implements _RandomAccessPSTSource {

	RandomAccessFile raf;
	
	public PSTRandomAccessFile(File f) throws FileNotFoundException {
		this.raf = new RandomAccessFile(f, "r");
	}

	
	public PSTRandomAccessFile(String filename) throws FileNotFoundException {
		this.raf = new RandomAccessFile(filename, "r");
	}


	
	@Override
	public void close() throws IOException {
		this.raf.close();		
	}


	@Override
	public void seek(long pos) throws IOException {
		this.raf.seek(pos);
	}


	@Override
	public int read() throws IOException {
		return this.raf.read();
	}


	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		return this.raf.read(buffer, offset, length);
	}


	@Override
	public int read(byte[] b) throws IOException {
		return this.raf.read(b);
	}


	@Override
	public long position() throws IOException {
		return this.raf.getFilePointer();
	}
}
