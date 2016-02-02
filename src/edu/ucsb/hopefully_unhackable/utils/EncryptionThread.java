package edu.ucsb.hopefully_unhackable.utils;
import java.util.Arrays;
import java.util.concurrent.PriorityBlockingQueue;

import javax.crypto.SecretKey;

import edu.ucsb.hopefully_unhackable.crypto.AESCTR;

public class EncryptionThread implements Runnable{
    
    public PriorityBlockingQueue<DataBlock> blockingQueue;
    public PriorityBlockingQueue<DataBlock> encQueue;
    private SecretKey secretKey;
    private byte[] nonce;
    
    public EncryptionThread(SecretKey secretKey, byte[] nonce, 
    		PriorityBlockingQueue<DataBlock> inQueue, PriorityBlockingQueue<DataBlock> outQueue) {
        this.secretKey = secretKey;
        this.nonce = nonce;
        this.blockingQueue = inQueue;
        this.encQueue = outQueue;
    }
    
    @Override
    public void run() {
        try {
            while(!blockingQueue.isEmpty()) {
                DataBlock block = blockingQueue.take();
                byte[] buffer = block.getData();
                int offset = block.getOffset();
                
                System.out.println("Unenc: " + block);
                
                byte[] encBuffer = AESCTR.encryptbytes(buffer, secretKey, nonce);
                //
                System.out.println("Isenc: Offset: " + offset + ", Data: " + Arrays.toString(encBuffer));
                encQueue.put(new DataBlock(encBuffer, offset));
            }
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
}