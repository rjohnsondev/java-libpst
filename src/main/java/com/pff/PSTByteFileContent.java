package com.pff;

import java.io.IOException;

public class PSTByteFileContent extends PSTFileContent {

    protected byte[] content;
    protected int index;

    public PSTByteFileContent(final byte[] content) {
        this.content = content;
        this.index = 0;
    }

    public byte[] getBytes() {
        return this.content;
    }

    public void setBytes(final byte[] content) {
        this.content = content;
    }

    @Override
    public void seek(final long index) {
        this.index = (int) index;
    }

    @Override
    public long getFilePointer() {
        return this.index;
    }

    @Override
    public int read() {
        return (this.index >= this.content.length) ? -1 : (int) this.content[this.index++];
    }

    @Override
    public int read(final byte[] target) {
        if (this.index >= this.content.length) {
            return -1;
        }
        int targetindex = 0;
        while (targetindex < target.length & this.index < this.content.length) {
            target[targetindex++] = this.content[this.index++];
        }
        return targetindex;
    }

    @Override
    public void readCompletely(final byte[] target) throws IOException {
        if (this.index >= this.content.length) {
            throw new IOException(
                    "unexpected EOF when attempting to read from ByteFileContent");
        }
        int targetindex = 0;
        while (targetindex < target.length & this.index < this.content.length) {
            target[targetindex++] = this.content[this.index++];
        }
    }

    @Override
    public byte readByte() {
        return this.content[this.index++];
    }

    @Override
    public void close() {
        // Do nothing
    }

}
