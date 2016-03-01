package com.pff;
import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PSTRAFileContent extends PSTFileContent{
    
    protected RandomAccessFile file;
    
    public PSTRAFileContent(File file) throws FileNotFoundException{
        this.file = new RandomAccessFile(file, "r");
    }
    
    public RandomAccessFile getFile(){
        return this.file;
    }
    public void setFile(RandomAccessFile file){
        this.file = file;
    }
     
    public void seek(long index) throws IOException{
        this.file.seek(index);
    }
    public long getFilePointer() throws IOException{
        return this.file.getFilePointer();
    }
    public int read() throws IOException{
        return this.file.read();
    }
    public int read(byte[] target) throws IOException{
        return this.file.read(target);
    }
    public byte readByte() throws IOException{
        return this.file.readByte();
    }
    public void close() throws IOException{
        this.file.close();
    }
}
