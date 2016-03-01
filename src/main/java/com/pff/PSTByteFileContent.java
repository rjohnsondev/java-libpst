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
    
    public void seek(long index){
        this.index = (int) index;
    }
    public long getFilePointer(){
        return this.index;
    }
    public int read(){
        return (this.index >= this.content.length) ? -1 : (int) this.content[index++];
    }
    public int read(byte[] target){
        if(this.index >= this.content.length) return -1;
        int targetindex = 0;
        while(targetindex < target.length & this.index < this.content.length){
            target[targetindex++] = this.content[this.index++];
        }
        return targetindex;
    }
    public byte readByte(){
        return this.content[index++];
    }
    public void close(){
        // Do nothing
    }
}
