package edu.ucsb.hopefully_unhackable.utils;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.PriorityBlockingQueue;

import javax.crypto.SecretKey;

import edu.ucsb.hopefully_unhackable.crypto.AESCTR;

public class EncryptionThread implements Runnable{
    
    public static PriorityBlockingQueue<DataBlock> blockingQueue;
    public static PriorityBlockingQueue<DataBlock> encQueue = new PriorityBlockingQueue<>(128,
    		FileUtils.comp);
    private int count;
    private SecretKey secretKey;
    private byte[] nonce;
    private OutputStream out;
    
    public EncryptionThread(int count, SecretKey secretKey, byte[] nonce, OutputStream out) {
        this.count = count;
        this.secretKey = secretKey;
        this.nonce = nonce;
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
                byte[] encBuffer = AESCTR.encryptbytes(buffer, secretKey, nonce);
                //
                System.out.println("Enc to " + offset + " : " + Arrays.toString(encBuffer));
                encQueue.put(new DataBlock(encBuffer, offset));
                count--;
            }

        }
        catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
}