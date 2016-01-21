package edu.ucsb.hopefully_unhackable.utils;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.*;

public class FileUtils
{
    private static final String BUCKET = "ucsb-temp-bucket-name";
    private static final int BUFFER_SIZE = 1024 * 10; // 10Kb

    public static void uploadFile(File file, String id) {
        AmazonS3 s3 = getClient();

        // Upload the files (no encryption)
        //s3.putObject(BUCKET, id, file);

        // Encrypted method
        PipedInputStream in = new PipedInputStream();
        try {
            OutputStream out = new PipedOutputStream(in);
            InputStream reader = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[BUFFER_SIZE];

            int bytesRead;
            while ((bytesRead = reader.read(buffer)) > -1) {
                // TODO: encrypt buffer
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
            out.close();
            reader.close();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(in.available());
            s3.putObject(BUCKET, id, in, metadata);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void downloadFile(String path, String id) {
        AmazonS3 s3 = getClient();

        GetObjectRequest objReq = new GetObjectRequest(BUCKET, id);
        File file = new File(path);

        // No encryption method
        //s3.getObject(objReq, file);

        // Encrypted method
        InputStream in = s3.getObject(objReq).getObjectContent();
        try {
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) > -1) {
                // TODO: decrypt buffer
                out.write(buffer, 0, bytesRead);
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
}
