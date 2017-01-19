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
        if (this.index >= this.content.length) {
            return -1;
        }
        return ((int) this.content[this.index++]) & 0xFF;
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
    public byte readByte() {
        return this.content[this.index++];
    }

    @Override
    public void close() {
        // Do nothing
    }

}
