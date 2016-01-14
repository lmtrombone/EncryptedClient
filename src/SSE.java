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
	
	public static HashMap<String, String> Tset;
	
	public static HashMap<String, String> EDBSetup(File selectedFile, SecretKey kS){
		
		//TODO: Parse documents with indexing
		
		String[] encryptedFileWords = readandEncryptFile(selectedFile, kS);
		SecurityHelperCTR securityHelperCTR = new SecurityHelperCTR();
		Tset = new HashMap<String, String>();
		for (int i = 0; i < encryptedFileWords.length; i++){
			SecretKey kE = SHA256.createIndexingKey(kS, encryptedFileWords[i]);
			
			//for now uses same key to encrypt keywords
			String encryptedIndex = securityHelperCTR.encrypt("1", kE);
			Tset.put(encryptedFileWords[i], encryptedIndex);
			
		}
		
		for (Entry<String, String> entry : Tset.entrySet()) {
			System.out.print("key is: "+ entry.getKey() + " & Value is: ");
	    	System.out.println(entry.getValue());
		}
		
		//for now not used
		//AES.generateAESEncryptionKey();
		//kT = AES.secretKey;	
		

		
		return Tset;
	}
	
	//TODO: Look into Kt
	
	//reads file and returns words in an array
	public static String[] readandEncryptFile(File selectedFile, SecretKey secretKey){
		String[] encryptedFileWords = null;
		SecurityHelperCTR securityHelper = new SecurityHelperCTR();
		try{
			FileReader file = new FileReader(selectedFile);
			BufferedReader reader = new BufferedReader(file);
			String line = reader.readLine();
			String[] fileWords = line.split(" ");
			encryptedFileWords = new String[fileWords.length];
			for (int i = 0; i < fileWords.length; i++){
				encryptedFileWords[i] = securityHelper.encrypt(fileWords[i], secretKey);
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
	
	//decrypts File
	public static void decryptFile(HashMap<String, String> Tset, SecretKey kS){
		String key, value;
		SecurityHelperCTR securityHelper = new SecurityHelperCTR();
		SecretKey kE;
		for(Entry<String, String> entry: Tset.entrySet()){
			//System.out.println("key is: "+ entry.getKey() + " & Value is: " + entry.getValue());
			kE = SHA256.createIndexingKey(kS, entry.getKey());
			
			key = securityHelper.decrypt(entry.getKey(), kS);
			value = securityHelper.decrypt(entry.getValue(), kE);
			System.out.println("key is: " + key + " and value is: " + value);
		}
	}
}