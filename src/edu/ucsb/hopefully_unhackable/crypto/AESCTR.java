package edu.ucsb.hopefully_unhackable.crypto;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.xml.bind.DatatypeConverter;

import edu.ucsb.hopefully_unhackable.utils.StringPair;

public class AESCTR {
	
	public static final int AES_KEY_LENGTH = 256;
    public static final int NONCE_SIZE = 8;
    public static SecretKey secretKey;
    public static Cipher cipher;
    
    private static final int BLOCK_SIZE = 16;

    public static SecretKey generateKey() {
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(AES_KEY_LENGTH);
			
			return keyGen.generateKey();
		} catch (NoSuchAlgorithmException ex) {
			// This should never happen?
			return null;
		}
	}
    
    public static int generateRandomNonce(final byte[] nonceBuffer,
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
    
    private static IvParameterSpec calculateIVForOffset(final IvParameterSpec iv,
            final long blockOffset) {
    	System.out.println("Offset " + blockOffset);
        
        final BigInteger ivBI = new BigInteger(1, iv.getIV());
        final BigInteger ivForOffsetBI = ivBI.add(BigInteger.valueOf(blockOffset
                / BLOCK_SIZE));

        final byte[] ivForOffsetBA = ivForOffsetBI.toByteArray();
        final IvParameterSpec ivForOffset;
        if (ivForOffsetBA.length >= BLOCK_SIZE) {
            ivForOffset = new IvParameterSpec(ivForOffsetBA, ivForOffsetBA.length - BLOCK_SIZE,
            		BLOCK_SIZE);
        } else {
            final byte[] ivForOffsetBASized = new byte[BLOCK_SIZE];
            System.arraycopy(ivForOffsetBA, 0, ivForOffsetBASized, BLOCK_SIZE
                    - ivForOffsetBA.length, ivForOffsetBA.length);
            ivForOffset = new IvParameterSpec(ivForOffsetBASized);
        }

        return ivForOffset;
    }

    public static String encrypt(final String secret, SecretKey secretKey) {
        
    	String strCipherText = null;
        try {
        	final byte[] plaintext = secret.getBytes();
            final byte[] nonceAndCiphertext = new byte[NONCE_SIZE
                    + plaintext.length];

            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            int offset = generateRandomNonce(nonceAndCiphertext, 0, NONCE_SIZE);
            final IvParameterSpec nonceIV = generateIVFromNonce(nonceAndCiphertext,
                    0, NONCE_SIZE, cipher.getBlockSize());
        	
            cipher.init(Cipher.ENCRYPT_MODE, secretKey,
                    nonceIV);
            offset += cipher.doFinal(plaintext, 0, plaintext.length,
                    nonceAndCiphertext, offset);
            if (offset != nonceAndCiphertext.length) {
                throw new IllegalStateException(
                        "Something wrong during encryption");
            }
            strCipherText = DatatypeConverter.printBase64Binary(nonceAndCiphertext);
        } 
        
        catch (final GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Missing basic functionality from Java runtime", e);
        }
        
        return strCipherText;
    }

    public static String decrypt(final String encrypted, SecretKey secretKey) {
    	
    	String plainText = null;
        
        try {
        	
        	final byte[] nonceAndCiphertext = DatatypeConverter
                    .parseBase64Binary(encrypted);
        	Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            final IvParameterSpec nonceIV = generateIVFromNonce(nonceAndCiphertext,
                    0, NONCE_SIZE, cipher.getBlockSize());
            cipher.init(Cipher.DECRYPT_MODE, secretKey,
                    nonceIV);
            final byte[] byteplaintext = cipher.doFinal(nonceAndCiphertext,
                    NONCE_SIZE, nonceAndCiphertext.length - NONCE_SIZE);
            // note: this may return an invalid result if the value is tampered
            // with
            // it may even contain more or less characters
            plainText = new String(byteplaintext);
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Missing basic functionality from Java runtime", e);
        }
        
        return plainText;
    }
    
    public static StringPair decrypt(final StringPair encrypted, SecretKey secretKey) {
    	String decName = decrypt(encrypted.getFileName(), secretKey);
    	String decId = decrypt(encrypted.getFileId(), secretKey);
    	
    	return new StringPair(decId, decName);
    }
    
    public static byte[] encryptbytes(byte[] bytefile, SecretKey secretKey, byte[] nonceBuffer, int offset) {
        
    	byte[] byteCipherText = null;
        try {
        	final byte[] plaintext = bytefile;
        	final byte[] nonce = nonceBuffer;
        	final byte[] ciphertext = new byte[plaintext.length];

            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            final int skip = offset % BLOCK_SIZE;
            final IvParameterSpec nonceIV = calculateIVForOffset(generateIVFromNonce(nonce,
            		0, NONCE_SIZE, cipher.getBlockSize()), offset - skip);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, nonceIV);
            final byte[] skipBuffer = new byte[skip];
            cipher.update(skipBuffer, 0, skip, skipBuffer);
            cipher.doFinal(plaintext, 0, plaintext.length, ciphertext);

            byteCipherText =  ciphertext;
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Missing basic functionality from Java runtime", e);
        }
        return byteCipherText;
    }
    
    public static byte[] decryptbytes(byte[] encryptedbytefile, SecretKey secretKey,
    		byte[] nonceBuffer, int offset) {
        
    	byte[] plaintext = null;
        try {
        	final byte[] nonce = nonceBuffer;
        	final byte[] ciphertext = encryptedbytefile;
        	Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        	final int skip = offset % BLOCK_SIZE;
            final IvParameterSpec nonceIV = calculateIVForOffset(generateIVFromNonce(nonce,
            		0, NONCE_SIZE, cipher.getBlockSize()), offset - skip);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, nonceIV);
            final byte[] skipBuffer = new byte[skip];
            cipher.update(skipBuffer, 0, skip, skipBuffer);
            plaintext = cipher.doFinal(ciphertext);
        } catch (final GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Missing basic functionality from Java runtime", e);
        }
        return plaintext;
    }
}