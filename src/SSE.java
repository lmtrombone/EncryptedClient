import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

//for now it's assumed only one file is being encrypted and stored
public class SSE{
	
	public static SecretKey kS, kT, kE;
	public static HashMap<String, String> Tset;
	
	public static void EDBSetup(File selectedFile){
		
		//generate AES key
		AES.generateAESEncryptionKey();
		kS = AES.secretKey;
		
		//TODO: Parse documents with indexing
		
		String[] encryptedFileWords = readandEncryptFile(selectedFile, kS);
		Tset = new HashMap<String, String>();
		for (int i = 0; i < encryptedFileWords.length; i++){
			kE = SHA256.createIndexingKey(kS, encryptedFileWords[i]);
			String encryptedIndex = AES.AESEncryption(kE, "1");
			Tset.put(encryptedFileWords[i], encryptedIndex);
		}
		
		for (Entry<String, String> entry : Tset.entrySet()) {
			System.out.print("key is: "+ entry.getKey() + " & Value is: ");
	    	System.out.println(entry.getValue());
		}
		
		AES.generateAESEncryptionKey();
		kT = AES.secretKey;	
		
		byte[] keyS = SSE.kS.getEncoded();
		System.out.println("Secret key kS: " + Arrays.toString(keyS));
		
		byte[] keyE = SSE.kE.getEncoded();
		System.out.println("Secret key kE: " + Arrays.toString(keyE));
		
	}
	
	//TODO: Look into Kt
	
	//reads file and returns words in an array
	public static String[] readandEncryptFile(File selectedFile, SecretKey secretKey){
		String[] encryptedFileWords = null;
		try{
			FileReader file = new FileReader(selectedFile);
			BufferedReader reader = new BufferedReader(file);
			String line = reader.readLine();
			String[] fileWords = line.split(" ");
			encryptedFileWords = new String[fileWords.length];
			for (int i = 0; i < fileWords.length; i++){
				encryptedFileWords[i] = AES.AESEncryption(secretKey, fileWords[i]);
			}
		}
		
		catch(FileNotFoundException e){
			System.out.println("File not found: " + e.getMessage());
		}
		
		catch(IOException e){
			System.out.println("I/O Exception: " + e.getMessage());
		}
		
		return encryptedFileWords;
	}
	
	//temp decrypt function
	public static void decryptFile(HashMap<String, String> Tset){
		String key, value;
		for(Entry<String, String> entry: Tset.entrySet()){
			System.out.println("key is: "+ entry.getKey() + " & Value is: " + entry.getValue());
			
			byte[] keyS = SSE.kS.getEncoded();
			System.out.println("Secret key kS: " + Arrays.toString(keyS));
			
			byte[] keyE = SSE.kE.getEncoded();
			System.out.println("Secret key kE: " + Arrays.toString(keyE));
			
			key = AES.AESDecryption(SSE.kS, entry.getKey());
			value = AES.AESDecryption(SSE.kE, entry.getValue());
			System.out.println("key is: " + key + " and value is: " + value);
		}
	}
}