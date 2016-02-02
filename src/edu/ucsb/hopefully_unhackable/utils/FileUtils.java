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
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

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
    public static Comparator<DataBlock> comp = new Comparator<DataBlock>() {
		@Override
		public int compare(DataBlock o1, DataBlock o2) {
			return Integer.compare(o1.getOffset(), o2.getOffset());
		}
    	
    };

    public static void uploadFile(File file, String id, SecretKey secretKey) {
        AmazonS3 s3 = getClient();

        // Encrypted method
        PipedInputStream in = new PipedInputStream();
        try {
            OutputStream out = new PipedOutputStream(in);
            InputStream reader = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[BUFFER_SIZE];
            byte[] nonceBuffer = new byte[AESCTR.NONCE_SIZE];
            int bytesRead;
            int offset = 0;
            int count = 0;
            AESCTR.generateRandomNonce(nonceBuffer, 0, AESCTR.NONCE_SIZE);
            PriorityBlockingQueue<DataBlock> queue = new PriorityBlockingQueue<DataBlock>(128,
            		comp);
            while ((bytesRead = reader.read(buffer)) > -1) {
                byte[] trunBuffer = null;
                byte[] encBuffer;
                trunBuffer = Arrays.copyOf(buffer, bytesRead);
                System.out.println("Before queue:" + Arrays.toString(trunBuffer));
                queue.put(new DataBlock(trunBuffer, offset));
                count++;
                offset+=bytesRead;
            }

            reader.close();
            
            /*
            DataBlock block1 = queue.take();
            byte[] b1 = block1.getData();
            int o1 = block1.getOffset();
            System.out.println("Bytes " + o1 + " : " + Arrays.toString(b1));
            
            DataBlock block2 = queue.take();
            byte[] b2 = block2.getData();
            int o2 = block2.getOffset();
            System.out.println("Bytes " + o2 + " : " + Arrays.toString(b2));
            
            DataBlock block3 = queue.take();
            byte[] b3 = block3.getData();
            int o3 = block3.getOffset();
            System.out.println("Bytes " + o3 + " : " + Arrays.toString(b3));
            
            DataBlock block4 = queue.take();
            byte[] b4 = block4.getData();
            int o4 = block4.getOffset();
            System.out.println("Bytes " + o4 + " : " + Arrays.toString(b4));
            */         
            
            ExecutorService service = Executors.newFixedThreadPool(THREAD_COUNT);
            EncryptionThread encryption = new EncryptionThread(count, secretKey,
            		nonceBuffer, out);
            EncryptionThread.blockingQueue = queue;
            
            /*
            DataBlock block1 = EncryptionThread.blockingQueue.take();
            byte[] b1 = block1.getData();
            int o1 = block1.getOffset();
            System.out.println("Bytes " + o1 + " : " + Arrays.toString(b1));
            
            DataBlock block2 = EncryptionThread.blockingQueue.take();
            byte[] b2 = block2.getData();
            int o2 = block2.getOffset();
            System.out.println("Bytes " + o2 + " : " + Arrays.toString(b2));
            
            DataBlock block3 = EncryptionThread.blockingQueue.take();
            byte[] b3 = block3.getData();
            int o3 = block3.getOffset();
            System.out.println("Bytes " + o3 + " : " + Arrays.toString(b3));
            
            DataBlock block4 = EncryptionThread.blockingQueue.take();
            byte[] b4 = block4.getData();
            int o4 = block4.getOffset();
            System.out.println("Bytes " + o4 + " : " + Arrays.toString(b4));
            */         
            
            for(int i = 0; i < (THREAD_COUNT - 1); i++) {
            	service.submit(encryption);
            }
            
            service.shutdownNow();
            
            /*
            DataBlock block1 = EncryptionThread.encQueue.take();
            byte[] b1 = block1.getData();
            int o1 = block1.getOffset();
            System.out.println("Bytes " + o1 + " : " + Arrays.toString(b1));
            
            DataBlock block2 = EncryptionThread.encQueue.take();
            byte[] b2 = block2.getData();
            int o2 = block2.getOffset();
            System.out.println("Bytes " + o2 + " : " + Arrays.toString(b2));
            
            DataBlock block3 = EncryptionThread.encQueue.take();
            byte[] b3 = block3.getData();
            int o3 = block3.getOffset();
            System.out.println("Bytes " + o3 + " : " + Arrays.toString(b3));
            
            DataBlock block4 = EncryptionThread.encQueue.take();
            byte[] b4 = block4.getData();
            int o4 = block4.getOffset();
            System.out.println("Bytes " + o4 + " : " + Arrays.toString(b4));
            */         
            
            
            while(EncryptionThread.encQueue.size() > 0) {
            	DataBlock block = EncryptionThread.encQueue.take();
            	byte[] encBuffer = block.getData();
            	System.out.println("Get " + block.getOffset() + " : " + Arrays.toString(encBuffer));
            	out.write(encBuffer, 0, encBuffer.length);
            }
            
            
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(in.available());
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
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            int encBufferSize = AESCTR.NONCE_SIZE + BUFFER_SIZE;
            int bytesRead;
            int offset = 0;
            int count = 0;
            byte[] buffer = new byte[encBufferSize];
            PriorityBlockingQueue<DataBlock> queue = new PriorityBlockingQueue<DataBlock>(128,
            		comp);
            while ((bytesRead = in.read(buffer)) > -1) {
                byte[] trunBuffer = null;
                byte[] decBuffer;
                trunBuffer = Arrays.copyOf(buffer, bytesRead);
                System.out.println("Enc array: " + Arrays.toString(trunBuffer));
                System.out.println("offset: " + offset);
                queue.put(new DataBlock(trunBuffer, offset));
            	offset+=bytesRead;
            	count++;
            }

            
            in.close();
            
            ExecutorService service = Executors.newFixedThreadPool(THREAD_COUNT);
            DecryptionThread decryption = new DecryptionThread(count, secretKey, out);
            DecryptionThread.blockingQueue = queue;
            
            for(int i = 0; i < (THREAD_COUNT - 1); i++){
            	service.submit(decryption);
            }
            
            service.shutdownNow();
            
            while(DecryptionThread.decQueue.size() > 0){
            	DataBlock block = DecryptionThread.decQueue.take();
            	byte[] decBuffer = block.getData();
            	System.out.println("Get " + block.getOffset() + " : " + Arrays.toString(decBuffer));
            	out.write(decBuffer, 0, decBuffer.length);
            }
            
            out.flush();
            out.close();
            
            
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        catch(InterruptedException e){
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
