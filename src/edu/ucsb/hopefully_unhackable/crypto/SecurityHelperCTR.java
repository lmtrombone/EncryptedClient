package edu.ucsb.hopefully_unhackable.crypto;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class SecurityHelperCTR {
	
    private static final int NONCE_SIZE = 8;
    public static SecretKey secretKey;
    private Cipher cipher;

    public SecurityHelperCTR() {
        try {
            this.cipher = Cipher.getInstance("AES/CTR/NoPadding");
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] hexDecode(final String hex) {
        final byte[] data = new byte[hex.length() / 2];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2),
                    16);
        }
        return data;
    }
    
    private static int generateRandomNonce(final byte[] nonceBuffer,
            final int offset, final int size) {
        final SecureRandom rng = new SecureRandom();
        final byte[] nonce = new byte[size];
        rng.nextBytes(nonce);
        System.arraycopy(nonce, 0, nonceBuffer, offset, size);
        return offset + size;
    }

    private static IvParameterSpec generateIVFromNonce(
            final byte[] nonceBuffer, final int offset, final int size,
            final int blockSize) {
        final byte[] ivData = new byte[blockSize];
        System.arraycopy(nonceBuffer, offset, ivData, 0, size);
        final IvParameterSpec iv = new IvParameterSpec(ivData);
        return iv;
    }

    public String encrypt(final String secret, SecretKey secretKey) {
        final byte[] plaintext = secret.getBytes();
        final byte[] nonceAndCiphertext = new byte[NONCE_SIZE
                + plaintext.length];

        int offset = generateRandomNonce(nonceAndCiphertext, 0, NONCE_SIZE);
        final IvParameterSpec nonceIV = generateIVFromNonce(nonceAndCiphertext,
                0, NONCE_SIZE, this.cipher.getBlockSize());

        try {
            this.cipher.init(Cipher.ENCRYPT_MODE, secretKey,
                    nonceIV);
            offset += this.cipher.doFinal(plaintext, 0, plaintext.length,
                    nonceAndCiphertext, offset);
            if (offset != nonceAndCiphertext.length) {
                throw new IllegalStateException(
                        "Something wrong during encryption");
            }
            // Java 8 contains java.util.Base64
            return DatatypeConverter.printBase64Binary(nonceAndCiphertext);
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Missing basic functionality from Java runtime", e);
        }
    }

    public String decrypt(final String encrypted, SecretKey secretKey) {
        final byte[] nonceAndCiphertext = DatatypeConverter
                .parseBase64Binary(encrypted);
        final IvParameterSpec nonceIV = generateIVFromNonce(nonceAndCiphertext,
                0, NONCE_SIZE, this.cipher.getBlockSize());
        try {
            this.cipher.init(Cipher.DECRYPT_MODE, secretKey,
                    nonceIV);
            final byte[] plaintext = this.cipher.doFinal(nonceAndCiphertext,
                    NONCE_SIZE, nonceAndCiphertext.length - NONCE_SIZE);
            // note: this may return an invalid result if the value is tampered
            // with
            // it may even contain more or less characters
            return new String(plaintext);
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Missing basic functionality from Java runtime", e);
        }
    }
    
    public byte[] encryptbytes(byte[] bytefile, SecretKey secretKey) {
        final byte[] plaintext = bytefile;
        final byte[] nonceAndCiphertext = new byte[NONCE_SIZE
                + plaintext.length];

        int offset = generateRandomNonce(nonceAndCiphertext, 0, NONCE_SIZE);
        final IvParameterSpec nonceIV = generateIVFromNonce(nonceAndCiphertext,
                0, NONCE_SIZE, this.cipher.getBlockSize());

        try {
            this.cipher.init(Cipher.ENCRYPT_MODE, secretKey,
                    nonceIV);
            offset += this.cipher.doFinal(plaintext, 0, plaintext.length,
                    nonceAndCiphertext, offset);
            if (offset != nonceAndCiphertext.length) {
                throw new IllegalStateException(
                        "Something wrong during encryption");
            }

            return nonceAndCiphertext;
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Missing basic functionality from Java runtime", e);
        }
    }
    
    public byte[] decryptbytes(byte[] encryptedbytefile, SecretKey secretKey) {
        final byte[] nonceAndCiphertext = encryptedbytefile;
        final IvParameterSpec nonceIV = generateIVFromNonce(nonceAndCiphertext,
                0, NONCE_SIZE, this.cipher.getBlockSize());
        try {
            this.cipher.init(Cipher.DECRYPT_MODE, secretKey,
                    nonceIV);
            final byte[] plaintext = this.cipher.doFinal(nonceAndCiphertext,
                    NONCE_SIZE, nonceAndCiphertext.length - NONCE_SIZE);
            // note: this may return an invalid result if the value is tampered
            // with
            // it may even contain more or less characters
            return plaintext;
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Missing basic functionality from Java runtime", e);
        }
    }
    
    
  //converts File object to bytes
    public static byte[] serialize(File selectedFile){
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			ObjectOutputStream out = new ObjectOutputStream(bout);
	        out.writeObject(selectedFile);
	        out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

        return bout.toByteArray();
    }
    
  //convert bytes back to file object
    public static File deserialize(byte[] bytes){
    	File deserializedFile = null;
        ByteArrayInputStream bout = new ByteArrayInputStream(bytes);     
		try {
			ObjectInputStream in = new ObjectInputStream(bout);
	        deserializedFile = (File) in.readObject();
	        in.close();
	        bout.close();
		}
		
		catch (IOException e) {
			e.printStackTrace();
		}
		
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return deserializedFile;
    }

    public static void main(final String[] args) {
        final String secret = "owlstead";
        final String secret1 = "owlstead";
        final String secret2 = "jsrhdf";
        final SecurityHelperCTR securityHelper = new SecurityHelperCTR();
        System.out.println(securityHelper.encrypt(secret, new SecretKeySpec(
                hexDecode("66e517bb5fd7df840060aed7e8b58986"), "AES"))); 
        System.out.println(securityHelper.encrypt(secret1, new SecretKeySpec(
                hexDecode("66e517bb5fd7df840060aed7e8b58986"), "AES"))); 
        //final String ct = securityHelper.encrypt(secret);
        //final String ct1 = securityHelper.encrypt(secret1);
        //final String ct2 = securityHelper.encrypt(secret2);
        //String pt = securityHelper.decrypt(ct);
        //System.out.println(pt);
        //pt = securityHelper.decrypt(ct1);
        //System.out.println(pt);
        //pt = securityHelper.decrypt(ct2);
        //System.out.println(pt);
    }
}