package edu.ucsb.hopefully_unhackable.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.PriorityBlockingQueue;

import javax.crypto.SecretKey;

import edu.ucsb.hopefully_unhackable.crypto.AESCTR;

public class DecryptionThread implements Runnable {
	
	public static PriorityBlockingQueue<DataBlock> blockingQueue;
	public static PriorityBlockingQueue<DataBlock> decQueue = new PriorityBlockingQueue<>(128,
    		FileUtils.comp);
    private int count;
    private SecretKey secretKey;
    private byte[] nonce;
    private OutputStream out;
    
    public DecryptionThread(int count, SecretKey secretKey, OutputStream out) {
        this.count = count;
        this.secretKey = secretKey;
        this.out = out;
    }
    
    @Override
    public void run() {
        
    	
        try {
        	
            while(count > 0) {
                DataBlock block = blockingQueue.take();
                byte[] buffer = block.getData();
                int offset = block.getOffset();
                
                System.out.println("Bytes after queue: " + offset + " : " + Arrays.toString(buffer));
                //
                byte[] decBuffer = AESCTR.decryptbytes(buffer, secretKey);
                System.out.println("Dec to " + offset + " : " + Arrays.toString(decBuffer));
                decQueue.put(new DataBlock(decBuffer, offset));
                count--;
            }
            
        }
        catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}