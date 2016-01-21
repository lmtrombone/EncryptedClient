package edu.ucsb.hopefully_unhackable.crypto;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SHA256 {
	
	//creates an AES key using SHA256
	public static SecretKey createIndexingKey(SecretKey secretKey, String keyWord) {
		SecretKey indexingKey = null;
		try {
			//constructs a key for SHA256
			byte[] key = secretKey.getEncoded();
			SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA256");
			Mac sha256 = Mac.getInstance("HmacSHA256");
			sha256.init(keySpec);
			
			//hashes keyWord and stores in result
			byte[] result = sha256.doFinal(keyWord.getBytes());

			//creates AES key using result of SHA256
			indexingKey = new SecretKeySpec(result, 0, result.length, "AES");
		} catch(NoSuchAlgorithmException nosuchAlgo) {
			System.out.println("The algorithm " + nosuchAlgo + " does not exist.");
		} catch(InvalidKeyException invalidKey) {
			System.out.println("Invalid key: " + invalidKey);
		}
		
		return indexingKey;
	}
	
	//creates encrypted string for indexing
	public static String createIndexingString(SecretKey secretKey, String keyWord) {
		String id = null;
		try {
			//constructs a key for SHA256
			byte[] key = secretKey.getEncoded();
			SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA256");
			Mac sha256 = Mac.getInstance("HmacSHA256");
			sha256.init(keySpec);
			
			//hashes keyWord and stores in result
			byte[] result = sha256.doFinal(keyWord.getBytes());

			//creates AES key using result of SHA256
			id = Base64.getEncoder().encodeToString(result);
		} catch(NoSuchAlgorithmException nosuchAlgo) {
			System.out.println("The algorithm " + nosuchAlgo + " does not exist.");
		} catch(InvalidKeyException invalidKey) {
			System.out.println("Invalid key: " + invalidKey);
		}
		
		return id;
	}
	
}