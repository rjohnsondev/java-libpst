package com.pff.source;

import java.io.Closeable;
import java.io.IOException;

public interface _RandomAccessPSTSource extends Closeable {

	
	public void seek(long pos) throws IOException;
	
	public int read() throws IOException;
	
	public int read(byte[] buffer, int offset, int length) throws IOException;
	
	public int read( byte[] b ) throws IOException;
	
	public long position() throws IOException;

	
	
	
}
