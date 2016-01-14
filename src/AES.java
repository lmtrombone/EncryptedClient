import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

//TODO: look into == in ciphertext (Caused by base64 encoding)
//http://serverfault.com/questions/358389/what-is-the-meaning-of-an-equal-sign-or-at-the-end-of-a-ssh-public-key/358391
public class AES
{
	//May need to generate new IV for each keyword
	public static final int AES_KEY_LENGTH = 256;
	public static SecretKey secretKey;
	private static byte[] IV;
	
	// Generates and returns AES key
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
	
	//Generates a 128 bit AES key
	public static void generateAESEncryptionKey(){
		try{
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(AES_KEY_LENGTH); 
			//secretKey = keyGen.generateKey();
			secretKey =  new SecretKeySpec(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, 0, 16, "AES");
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
			IV = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
			//IV = new byte[AES_KEY_LENGTH/16];
			//SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
			//prng.nextBytes(IV);
			//System.out.println("Initialization Vector: " + Arrays.toString(IV));
			
			//creates an instance of Cipher for encryption
			//AES and CBC encryption with padding
			//TODO: Look into padding & MAC
			Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			
			//initialize cipher for encryption
			aesCipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(IV));
			
			//encrypts the data
			byte[] byteDataToEncrypt = keyWord.getBytes();
			//System.out.println("Data to encrypt: " + Arrays.toString(byteDataToEncrypt));
			
			byte[] byteCipherText = aesCipher.doFinal(byteDataToEncrypt);
			//System.out.println("Ciphertext: " + Arrays.toString(byteCipherText));
			
			//Base64Encoder converts to 24 byte string
			//should be acceptable, but converting to 16 byte string preferable
			strCipherText = new BASE64Encoder().encode(byteCipherText);	
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
		
		//catch (NoSuchProviderException noSuchProvider) {
			//System.out.println("No such provider: "+ noSuchProvider);
		//}
		
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
			//System.out.println("Data to decrypt: " + Arrays.toString(byteDataToDecrypt));
			
			byte[] bytePlainText = aesCipher.doFinal(byteDataToDecrypt);
			//System.out.println("Plaintext: " + Arrays.toString(bytePlainText));
			
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
	
	public byte[] getIV(){
		return IV;
	}
	
	//Test for AES scheme
	public static void main(String[] args) {
		try{
			int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
			System.out.println("Max supported key length: " + maxKeyLen);
		}
		catch(NoSuchAlgorithmException noSuchAlgorithm){
			System.out.println("No such algorithm exists: " + noSuchAlgorithm);
		}
		
		//generates AES key and prints it
		generateAESEncryptionKey();
		SecretKey secretKey = AES.secretKey;
		byte[] key = secretKey.getEncoded();
		System.out.println("Secret key: " + Arrays.toString(key));
		
		//encrypts plaintext and prints it
		String cipherText = AESEncryption(secretKey, "TestData");
		System.out.println("Ciphertext generated using AES encryption is: " + cipherText);
		
		//decrypts ciphertext and prints it
		String plainText = AESDecryption(secretKey, cipherText);
		System.out.println("Plaintext generated using AES decryption is: " + plainText);
	}
}
