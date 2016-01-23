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
    private static final int BUFFER_SIZE = 10; // 10Kb

    public static void uploadFile(File file, String id, SecretKey secretKey) {
        AmazonS3 s3 = getClient();

        // Encrypted method
        PipedInputStream in = new PipedInputStream();
        try {
            OutputStream out = new PipedOutputStream(in);
            InputStream reader = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[BUFFER_SIZE];
            //byte[] nonceBuffer = new byte[AESCTR.NONCE_SIZE + BUFFER_SIZE];
            int bytesRead;
            //AESCTR.generateRandomNonce(nonceBuffer, 0, AESCTR.NONCE_SIZE);
            while ((bytesRead = reader.read(buffer)) > -1) {
                byte[] trunBuffer = null;
                byte[] encBuffer;
                if(bytesRead < BUFFER_SIZE){
                	trunBuffer = Arrays.copyOf(buffer, bytesRead);
                }
                else{
                	trunBuffer = buffer;
                }
            	encBuffer = AESCTR.encryptbytes(trunBuffer, secretKey);
                out.write(encBuffer, 0, encBuffer.length);
            }
            out.flush();
            out.close();
            reader.close();

            ObjectMetadata metadata = new ObjectMetadata();
            System.out.println(in.available());
            metadata.setContentLength(in.available());
            s3.putObject(BUCKET, id, in, metadata);
        } catch (IOException ex) {
            ex.printStackTrace();
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
            int encBufferSize = findEncBufferSize(secretKey);
            byte[] buffer = new byte[encBufferSize];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) > -1) {
                byte[] trunBuffer = null;
                byte[] decBuffer;
                if(bytesRead < encBufferSize){
                	trunBuffer = Arrays.copyOf(buffer, bytesRead);
                }
                else{
                	trunBuffer = buffer;
                }
            	decBuffer = AESCTR.decryptbytes(trunBuffer, secretKey);
                out.write(decBuffer, 0, decBuffer.length);
            	
            }
            out.flush();
            out.close();
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
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
    
    public static int findEncBufferSize(SecretKey secretKey){
    	byte[] buffer = new byte[BUFFER_SIZE];
    	return AESCTR.encryptbytes(buffer, secretKey).length;
    }
    
}
