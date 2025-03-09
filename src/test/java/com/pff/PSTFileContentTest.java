package com.pff;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

@RunWith(JUnit4.class)
public class PSTFileContentTest {
    static class TestPSTFileContent extends PSTFileContent {
        int position = 0;

        @Override
        public void seek(long index) throws IOException {
            position = (int) index;
        }

        @Override
        public long getFilePointer() throws IOException {
            return position;
        }

        @Override
        public int read() throws IOException {
            return 0;
        }

        @Override
        public int read(byte[] target) throws IOException {
            int read = 3;
            position += read;
            return read;
        }

        @Override
        public byte readByte() throws IOException {
            return 0;
        }

        @Override
        public void close() throws IOException {

        }
    }

    @Test
    public void testReadCompletely() throws IOException {
        PSTFileContent pstFileContent = new TestPSTFileContent();

        pstFileContent.readCompletely(new byte[10]);
    }
}