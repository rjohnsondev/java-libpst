package com.pff;

import java.io.IOException;

public abstract class PSTFileContent {
    public abstract void seek(long index) throws IOException;

    public abstract long getFilePointer() throws IOException;

    public abstract int read() throws IOException;

    public abstract int read(byte[] target) throws IOException;

    public final void readCompletely(final byte[] target) throws IOException {
        int read =  this.read(target);
        // bail in common case
        if (read <= 0 || read == target.length) {
            return;
        }

        byte[] buffer = new byte[8192];
        int offset = read;
        while (offset < target.length) {
            read = this.read(buffer);
            if (read <= 0) {
                break;
            }
            System.arraycopy(buffer, 0, target, offset, read);
            offset += read;
        }
    }

    public abstract byte readByte() throws IOException;

    public abstract void close() throws IOException;
}
