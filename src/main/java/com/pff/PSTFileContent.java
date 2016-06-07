package com.pff;

import java.io.IOException;

public abstract class PSTFileContent{
    public abstract void seek(long index) throws IOException;
    public abstract long getFilePointer() throws IOException;
    public abstract int read() throws IOException;
    public abstract int read(byte[] target) throws IOException;
    public abstract byte readByte() throws IOException;
    public abstract void close() throws IOException;
}
