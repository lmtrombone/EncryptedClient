package edu.ucsb.hopefully_unhackable.utils;

import java.util.concurrent.PriorityBlockingQueue;

import javax.crypto.SecretKey;

import edu.ucsb.hopefully_unhackable.crypto.AESCTR;

public class DecryptionThread implements Runnable {
	
	public PriorityBlockingQueue<DataBlock> blockingQueue;
	public PriorityBlockingQueue<DataBlock> decQueue;
    private SecretKey secretKey;
    private byte[] nonce;
    
    public DecryptionThread(SecretKey secretKey, byte[] nonce, 
    		PriorityBlockingQueue<DataBlock> inQueue, PriorityBlockingQueue<DataBlock> outQueue) {
        this.secretKey = secretKey;
        this.nonce = nonce;
        this.blockingQueue = inQueue;
        this.decQueue = outQueue;
    }
    
    @Override
    public void run() {
        try {
            while(!blockingQueue.isEmpty()) {
                DataBlock block = blockingQueue.take();
                byte[] buffer = block.getData();
                int offset = block.getOffset();
                
                //System.out.println("Enc: " + block);
                
                byte[] decBuffer = AESCTR.decryptbytes(buffer, secretKey, nonce, offset);
                //System.out.println("Dec: Offset: " + offset + ", Data: " + Arrays.toString(decBuffer));
                
                // TODO Consider using same data block so you dont need to create new
                decQueue.put(new DataBlock(decBuffer, offset));
            }
            
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}