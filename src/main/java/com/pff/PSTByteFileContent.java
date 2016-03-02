package com.pff;

public class PSTByteFileContent extends PSTFileContent{
    
    protected byte[] content;
    protected int index;
    
    public PSTByteFileContent(byte[] content){
        this.content = content;
        this.index = 0;
    }
    
    public byte[] getBytes(){
        return this.content;
    }
    public void setBytes(byte[] content){
        this.content = content;
    }
    
    @Override
    public void seek(long index){
        this.index = (int) index;
    }
    
    @Override
    public long getFilePointer(){
        return this.index;
    }
    
    @Override
    public int read(){
        return (this.index >= this.content.length) ? -1 : (int) this.content[index++];
    }
    
    @Override
    public int read(byte[] target){
        if(this.index >= this.content.length) return -1;
        int targetindex = 0;
        while(targetindex < target.length & this.index < this.content.length){
            target[targetindex++] = this.content[this.index++];
        }
        return targetindex;
    }
    
    @Override
    public byte readByte(){
        return this.content[index++];
    }
    
    @Override
    public void close(){
        // Do nothing
    }
    
}
