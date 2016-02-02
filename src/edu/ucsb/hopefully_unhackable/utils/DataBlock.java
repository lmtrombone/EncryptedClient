package edu.ucsb.hopefully_unhackable.utils;


public class DataBlock {
    
    private byte[] data;
    private int offset;
    
    public DataBlock(byte[] data, int offset) {
        this.data = data;
        this.offset = offset;
    }
    
    public byte[] getData() {
        return data;
    }
    
    public void setData(byte[] data) {
        this.data = data;
    }
    
    public int getOffset() {
        return offset;
    }
    
    public void setOffset(int offset) {
        this.offset = offset;
    }
    
}
