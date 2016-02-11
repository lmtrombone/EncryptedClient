package edu.ucsb.hopefully_unhackable.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.UploadPartRequest;

import edu.ucsb.hopefully_unhackable.crypto.AESCTR;

public class FileUtils {
    private static final String BUCKET = "ucsb-temp-bucket-name";
    private static final int BUFFER_SIZE = 5 * 1024 * 1024;
    private static final int THREAD_COUNT = 20;

    public static void uploadFile(File file, String id, SecretKey secretKey) {
        AmazonS3 s3 = getClient();

        // Encrypted method
        try {
        	PriorityBlockingQueue<DataBlock> inQueue = new PriorityBlockingQueue<DataBlock>(128);
        	PriorityBlockingQueue<DataBlock> outQueue = new PriorityBlockingQueue<DataBlock>(128);
        	
            InputStream reader = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[BUFFER_SIZE];
            byte[] nonceBuffer = new byte[AESCTR.NONCE_SIZE];
            int bytesRead;
            
            AESCTR.generateRandomNonce(nonceBuffer, 0, AESCTR.NONCE_SIZE);
            System.out.println("====Encryption Begin====");
            long start = System.currentTimeMillis();
            
        	int offset = 0;
            while ((bytesRead = reader.read(buffer)) > -1) {
                byte[] trunBuffer = Arrays.copyOf(buffer, bytesRead);
                //System.out.println("Enqueue: " + Arrays.toString(trunBuffer));
                inQueue.put(new DataBlock(trunBuffer, offset));
                offset += bytesRead;
            }
            
            reader.close();    
            
            ExecutorService service = Executors.newFixedThreadPool(THREAD_COUNT);
            
            //TODO Why is it -1
            for (int i = 1; i < THREAD_COUNT; i++) {
            	EncryptionThread encryption = new EncryptionThread(secretKey, nonceBuffer, inQueue, outQueue);
            	service.submit(encryption);
            }
            
            service.shutdown();
            try {
            	service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            	System.out.println("Time elapsed: " + (System.currentTimeMillis() - start) + " ms");
            	System.out.println("====Encryption Done====");
        	} catch (InterruptedException e) {
        	    e.printStackTrace();
        	}
            
            System.out.println("====Upload Begin====");
            //System.out.println("Nonce: " + Arrays.toString(nonceBuffer));
            
            start = System.currentTimeMillis();
            InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(BUCKET, id);
            InitiateMultipartUploadResult initResponse = s3.initiateMultipartUpload(initRequest);
            List<PartETag> partETags = new ArrayList<PartETag>();
            do {
            	DataBlock block = outQueue.take();
            	//System.out.println("Dequeue: " + block);
            	ByteArrayInputStream bais = new ByteArrayInputStream(block.getData());
            	System.out.println(initResponse.getUploadId());
            	UploadPartRequest uploadRequest = new UploadPartRequest()
            			.withBucketName(BUCKET).withKey(id)
            			.withUploadId(initResponse.getUploadId())
            			.withInputStream(bais)
            			.withPartNumber(block.getOffset() / (BUFFER_SIZE + AESCTR.NONCE_SIZE) + 1)
            			.withPartSize(bais.available());
            	
            	partETags.add(s3.uploadPart(uploadRequest).getPartETag());
            	
            	//System.out.println("Part: " + (block.getOffset() / (BUFFER_SIZE + AESCTR.NONCE_SIZE) + 1));
            	//System.out.println("Remaining " + outQueue.size() + "  " + outQueue.isEmpty());
            } while (!outQueue.isEmpty());
            
            CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(BUCKET, id, initResponse.getUploadId(), partETags);
            s3.completeMultipartUpload(compRequest);
            
            System.out.println("Time elapsed: " + (System.currentTimeMillis() - start) + " ms");
            System.out.println("====Upload Complete====");
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch(InterruptedException e) {
        	Thread.currentThread().interrupt();
        }
    }

    public static void downloadFile(String path, String id, SecretKey secretKey) throws IOException {
        AmazonS3 s3 = getClient();

        GetObjectRequest objReq = new GetObjectRequest(BUCKET, id);
        File file = new File(path);
        //Decrypted method
        InputStream in = s3.getObject(objReq).getObjectContent();
        try {
        	PriorityBlockingQueue<DataBlock> inQueue = new PriorityBlockingQueue<DataBlock>(128);
        	PriorityBlockingQueue<DataBlock> outQueue = new PriorityBlockingQueue<DataBlock>(128);
            int bytesRead;
            byte[] buffer = new byte[BUFFER_SIZE];
            byte[] nonceBuffer = new byte[AESCTR.NONCE_SIZE];
            
            System.out.println("====Decryption Begin====");
            bytesRead = in.read(nonceBuffer);
            if (bytesRead != AESCTR.NONCE_SIZE){
            	in.close();
            	System.out.println("Invalid Nonce Size: " + bytesRead);
            	throw new IOException("Invalid nonce size.");
            }
            //System.out.println("Nonce: " + Arrays.toString(nonceBuffer));
            
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            long start = System.currentTimeMillis();
            int offset = 0;
            while ((bytesRead = in.read(buffer)) > -1) {
                byte[] trunBuffer = Arrays.copyOf(buffer, bytesRead);
                //System.out.println("Enqueue: " + Arrays.toString(trunBuffer));
                inQueue.put(new DataBlock(trunBuffer, offset));
            	offset += bytesRead;
            }

            in.close();
            
            ExecutorService service = Executors.newFixedThreadPool(THREAD_COUNT);
            for(int i = 0; i < (THREAD_COUNT - 1); i++) {
            	DecryptionThread decryption = new DecryptionThread(secretKey, nonceBuffer, inQueue, outQueue);
            	service.submit(decryption);
            }
            
            service.shutdown();
            try {
            	service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            	System.out.println("Time elapsed: " + (System.currentTimeMillis() - start) + " ms");
            	System.out.println("====Decryption Done====");
        	} catch (InterruptedException e) {
        	    e.printStackTrace();
        	}
            
            System.out.println("====Download Begin====");
            start = System.currentTimeMillis();
            while (!outQueue.isEmpty()) {
            	DataBlock block = outQueue.take();
            	byte[] decBuffer = block.getData();
            	//System.out.println("Dequeue: " + block);
            	out.write(decBuffer, 0, decBuffer.length);
            }
            
            out.flush();
            out.close();
            
            System.out.println("Time elapsed: " + (System.currentTimeMillis() - start) + " ms");
            System.out.println("====Download Complete====");
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
