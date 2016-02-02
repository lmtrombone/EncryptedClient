package edu.ucsb.hopefully_unhackable.utils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;

import edu.ucsb.hopefully_unhackable.crypto.AESCTR;

public class FileUtils
{
    private static final String BUCKET = "ucsb-temp-bucket-name";
    private static final int BUFFER_SIZE = 10;
    private static final int THREAD_COUNT = 10;

    public static void uploadFile(File file, String id, SecretKey secretKey) {
        AmazonS3 s3 = getClient();

        // Encrypted method
        PipedInputStream in = new PipedInputStream();
        try {
        	PriorityBlockingQueue<DataBlock> inQueue = new PriorityBlockingQueue<DataBlock>(128);
        	PriorityBlockingQueue<DataBlock> outQueue = new PriorityBlockingQueue<DataBlock>(128);
            OutputStream out = new PipedOutputStream(in);
            InputStream reader = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[BUFFER_SIZE];
            byte[] nonceBuffer = new byte[AESCTR.NONCE_SIZE];
            int bytesRead;
            int offset = 0;
            
            AESCTR.generateRandomNonce(nonceBuffer, 0, AESCTR.NONCE_SIZE);
            System.out.println("====Encryption Begin====");
            while ((bytesRead = reader.read(buffer)) > -1) {
                byte[] trunBuffer = Arrays.copyOf(buffer, bytesRead);
                System.out.println("Enqueue: " + Arrays.toString(trunBuffer));
                inQueue.put(new DataBlock(trunBuffer, offset));
                offset += bytesRead;
            }
            
            reader.close();    
            
            ExecutorService service = Executors.newFixedThreadPool(THREAD_COUNT);
            
            //TODO Why is it -1
            for (int i = 0; i < (THREAD_COUNT - 1); i++) {
            	EncryptionThread encryption = new EncryptionThread(secretKey, nonceBuffer, inQueue, outQueue);
            	service.submit(encryption);
            }
            
            service.shutdown();
            try {
            	service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            	System.out.println("====Encryption Done====");
        	} catch (InterruptedException e) {
        	    e.printStackTrace();
        	}
            
            System.out.println("====Upload Begin====");
            while (!outQueue.isEmpty()) {
            	DataBlock block = outQueue.take();
            	byte[] encBuffer = block.getData();
            	System.out.println("Dequeue: " + block);
            	out.write(encBuffer, 0, encBuffer.length);
            }
            
            out.flush();
            out.close();
            
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(in.available());
            System.out.println("====Upload Complete (" + in.available() + " bytes)====");
            s3.putObject(BUCKET, id, in, metadata);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        catch(InterruptedException e){
        	Thread.currentThread().interrupt();
        }
        
    }

    public static void downloadFile(String path, String id, SecretKey secretKey) {
        AmazonS3 s3 = getClient();

        GetObjectRequest objReq = new GetObjectRequest(BUCKET, id);
        File file = new File(path);

        //Decrypted method
        InputStream in = s3.getObject(objReq).getObjectContent();
        try {
        	PriorityBlockingQueue<DataBlock> inQueue = new PriorityBlockingQueue<DataBlock>(128);
        	PriorityBlockingQueue<DataBlock> outQueue = new PriorityBlockingQueue<DataBlock>(128);
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            int encBufferSize = AESCTR.NONCE_SIZE + BUFFER_SIZE;
            int bytesRead;
            int offset = 0;
            byte[] buffer = new byte[encBufferSize];
            
            System.out.println("====Decryption Begin====");
            while ((bytesRead = in.read(buffer)) > -1) {
                byte[] trunBuffer = Arrays.copyOf(buffer, bytesRead);
                System.out.println("Enqueue: " + Arrays.toString(trunBuffer));
                inQueue.put(new DataBlock(trunBuffer, offset));
            	offset += bytesRead;
            }

            in.close();
            
            ExecutorService service = Executors.newFixedThreadPool(THREAD_COUNT);
            
            for(int i = 0; i < (THREAD_COUNT - 1); i++) {
            	DecryptionThread decryption = new DecryptionThread(secretKey, null, inQueue, outQueue);
            	service.submit(decryption);
            }
            
            service.shutdown();
            try {
            	service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            	System.out.println("====Decryption Done====");
        	} catch (InterruptedException e) {
        	    e.printStackTrace();
        	}
            
            System.out.println("====Download Begin====");
            while (!outQueue.isEmpty()) {
            	DataBlock block = outQueue.take();
            	byte[] decBuffer = block.getData();
            	System.out.println("Dequeue: " + block);
            	out.write(decBuffer, 0, decBuffer.length);
            }
            
            out.flush();
            out.close();
            
            System.out.println("====Download Complete====");
            
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch(InterruptedException e) {
        	Thread.currentThread().interrupt();
        }
    }

    private static AmazonS3 getClient() {
        // Load AWS Credentials (This needs to be setup on each computer)
        AWSCredentials credentials;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception ex) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.", ex);
        }

        // Create S3 client
        AmazonS3 s3 = new AmazonS3Client(credentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        s3.setRegion(usWest2);

        return s3;
    }
}
