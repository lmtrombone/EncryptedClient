package edu.ucsb.hopefully_unhackable.processor;

import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;

public class AES
{
	//Need to install JCE Unlimited Strength Jurisdiction if AES key is 192 or 256
	//May need to generate new IV for each keyword
	public static final int AES_KEY_LENGTH = 128;
	private static SecretKey secretKey = null;
	private static byte[] IV; 
	
	//Generates a 128 bit AES key
	public static void generateAESEncryptionKey(){
		try{
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(AES_KEY_LENGTH); 
			secretKey = keyGen.generateKey(); 
		}
		
		catch(NoSuchAlgorithmException nosuchAlgo){
			System.out.println("The algorithm " + nosuchAlgo + " does not exist.");
		}		
	}
	
	//Encrypts keywords using a secretKey
	public static String AESEncryption(SecretKey secretKey, String keyWord){
		String strCipherText = new String();
		try{
			//generates Initialization Vector (IV)
			//SecureRandom initializes IV to some random bits using SHA1PRNG algorithm
			IV = new byte[AES_KEY_LENGTH/8];
			SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
			prng.nextBytes(IV);
			System.out.println("Initialization Vector in byte format: " + IV);
			
			//creates an instance of Cipher for encryption
			//AES and CBC encryption with padding
			//TODO: Look into padding & MAC
			Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			
			//initialize cipher for encryption
			aesCipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(IV));
			
			//encrypts the data
			byte[] byteDataToEncrypt = keyWord.getBytes();
			System.out.println("Data to encrypt in byte format: " + byteDataToEncrypt);
			
			byte[] byteCipherText = aesCipher.doFinal(byteDataToEncrypt);
			System.out.println("Ciphertext in byte format: " + byteCipherText);
			
			strCipherText = new BASE64Encoder().encode(byteCipherText);	
			
			/*
			Cipher aesCipher1 = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			aesCipher1.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV));
			System.out.println("Data to decrypt in byte format: " + byteCipherText);
			
			byte[] bytePlainText = aesCipher1.doFinal(byteCipherText);
			System.out.println("Plaintext in byte format: " + bytePlainText);
			
			String test = new String(bytePlainText);
			System.out.println("Decrypted: "+ test);*/
		}
		
		catch(NoSuchAlgorithmException nosuchAlgo){
			System.out.println("No such algorithm exists: " + nosuchAlgo);
		}
		
		catch(NoSuchPaddingException nosuchPad){
			System.out.println("No such padding exists: " + nosuchPad);
		}
		
		catch(InvalidKeyException invalidKey){
			System.out.println("Invalid key: " + invalidKey);
		}
		
		catch(InvalidAlgorithmParameterException invalidParam){
			System.out.println("Invalid parameter: " + invalidParam);
		}
		
		catch(IllegalBlockSizeException illegalBlockSize){
			System.out.println("Illegal block size: " + illegalBlockSize);
		}
		
		catch(BadPaddingException badPadding){
			System.out.println("Bad padding: " + badPadding);
		}
		
		return strCipherText;
	}
	
	public static String AESDecryption(SecretKey secretKey, String cipherText){	
		String strPlainText = new String();	
		try{
			//creates an instance of Cipher for decryption
			//AES and CBC decryption with padding
			//TODO: Look into padding
			Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			aesCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(IV));
			
			//decrypts the data
			byte[] byteDataToDecrypt = new BASE64Decoder().decodeBuffer(cipherText);
			System.out.println("Data to decrypt in byte format: " + byteDataToDecrypt);
			
			byte[] bytePlainText = aesCipher.doFinal(byteDataToDecrypt);
			System.out.println("Plaintext in byte format: " + bytePlainText);
			
			strPlainText = new String(bytePlainText);
		}
		
		catch(NoSuchAlgorithmException nosuchAlgo){
			System.out.println("No such algorithm exists: " + nosuchAlgo);
		}
		
		catch(NoSuchPaddingException nosuchPad){
			System.out.println("No such padding exists: " + nosuchPad);
		}
		
		catch(InvalidKeyException invalidKey){
			System.out.println("Invalid key: " + invalidKey);
		}
		
		catch(InvalidAlgorithmParameterException invalidParam){
			System.out.println("Invalid parameter: " + invalidParam);
		}
		
		catch(IllegalBlockSizeException illegalBlockSize){
			System.out.println("Illegal block size: " + illegalBlockSize);
		}
		
		catch(BadPaddingException badPadding){
			System.out.println("Bad padding: " + badPadding);
		}
		
		catch(IOException ioException){
			System.out.println("IOException: " + ioException);
		}
		
		return strPlainText;
	}

	public SecretKey getSecretKey(){
		return secretKey;
	}
	
	public byte[] getIV(){
		return IV;
	}
	
	//Test for AES scheme
	public static void main(String[] args) {
		try{
			int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
			System.out.println("Max key length: " + maxKeyLen);
		}
		catch(NoSuchAlgorithmException noSuchAlgorithm){
			System.out.println("No such algorithm exists: " + noSuchAlgorithm);
		}
		
		AES aes = new AES();
		
		//generates AES key and prints it
		generateAESEncryptionKey();
		SecretKey secretKey = aes.getSecretKey();
		System.out.println("Secret key: " + secretKey);
		
		//encrypts ciphertext and prints it
		String cipherText = AESEncryption(secretKey, "TestData");
		System.out.println("Ciphertext generated using AES encryption is: " + cipherText);
		
		//decrypts ciphertext and prints it
		String plainText = AESDecryption(secretKey, cipherText);
		System.out.println("Plaintext generated using AES decryption is: " + plainText);
	}
}
