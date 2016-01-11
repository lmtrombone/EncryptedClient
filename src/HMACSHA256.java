import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Encoder;

public class HMACSHA256{
	
	//creates an AES key using another AES key and keyWord
	public static SecretKey createIndexingKey(SecretKey secretKey, String keyWord){
		SecretKey indexingKey = null;
		try{
			//constructs a key for HMACSHA256
			byte[] key = secretKey.getEncoded();
			SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA256");
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			sha256_HMAC.init(keySpec);
			
			//hashes keyWord and stores in result
			byte[] result = sha256_HMAC.doFinal(keyWord.getBytes());

			indexingKey = new SecretKeySpec(result, 0, result.length, "AES");
			System.out.println(result.length);
		}
		
		catch(NoSuchAlgorithmException nosuchAlgo){
			System.out.println("The algorithm " + nosuchAlgo + " does not exist.");
		} 
		
		catch(InvalidKeyException invalidKey){
			System.out.println("Invalid key: " + invalidKey);
		}
		
		return indexingKey;
	}
	
	//test for HMACSHA256
	public static void main(String args[]) {

		//generates AES key
		AES.generateAESEncryptionKey();
		
		//generates another AES with previously generated AES key and a keyWord and prints it
		SecretKey secretKey = createIndexingKey(AES.secretKey, "hello");
		byte[] key = secretKey.getEncoded();
		System.out.println("Secret key: " + Arrays.toString(key));
		
		//encrypts plaintext and prints it
		String cipherText = AES.AESEncryption(secretKey, "Test");
		System.out.println("Ciphertext generated using AES encryption is: " + cipherText);
		
		//decrypts ciphertext and prints it
		String plainText = AES.AESDecryption(secretKey, cipherText);
		System.out.println("Plaintext generated using AES decryption is: " + plainText);
	}
}