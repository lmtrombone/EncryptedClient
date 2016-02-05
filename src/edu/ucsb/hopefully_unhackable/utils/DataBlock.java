package edu.ucsb.hopefully_unhackable.utils;

import java.util.Arrays;

public class DataBlock implements Comparable<DataBlock> {
    
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

	@Override
	public int compareTo(DataBlock dataBlock) {
		return Integer.compare(offset, dataBlock.offset);
	}
    
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Offset: " + offset + ", Data: ");
	    /*for (byte b: data) {
	       sb.append(String.format("%02X ", b & 0xFF));
	    }*/
		sb.append(Arrays.toString(data));
	    return sb.toString();
	}
}
