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
    
    @Override
    public void seek(long index) throws IOException{
        this.file.seek(index);
    }
    
    @Override
    public long getFilePointer() throws IOException{
        return this.file.getFilePointer();
    }
    
    @Override
    public int read() throws IOException{
        return this.file.read();
    }
    
    @Override
    public int read(byte[] target) throws IOException{
        return this.file.read(target);
    }
    
    @Override
    public byte readByte() throws IOException{
        return this.file.readByte();
    }
    
    @Override
    public void close() throws IOException{
        this.file.close();
    }
    
}
