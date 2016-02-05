package edu.ucsb.hopefully_unhackable.utils;

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
                
                //System.out.println("Unenc: " + block + " " + buffer.length);
                
                byte[] encBuffer = AESCTR.encryptbytes(buffer, secretKey, nonce, offset);
                //System.out.println("Isenc: Offset: " + offset + ", Data: " + Arrays.toString(encBuffer) + " " + encBuffer.length);
                
                if (offset == 0) { //Append Nonce
                	byte[] firstBuffer = new byte[AESCTR.NONCE_SIZE + encBuffer.length];
                	System.arraycopy(nonce, 0, firstBuffer, 0, AESCTR.NONCE_SIZE);
                	System.arraycopy(encBuffer, 0, firstBuffer, AESCTR.NONCE_SIZE, encBuffer.length);
                	encQueue.put(new DataBlock(firstBuffer, offset));
                } else { // No Nonce (All other offsets +8 due to nonce)
                	encQueue.put(new DataBlock(encBuffer, offset + AESCTR.NONCE_SIZE));
                }
            }
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
}