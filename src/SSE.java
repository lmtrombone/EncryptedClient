import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.crypto.SecretKey;

public class SSE{
	
	public static HashMap<String, ArrayList<String>> Tset;
	
	public static HashMap<String, ArrayList<String>> EDBSetup(File selectedFile, SecretKey kS){
		
		//TODO: Parse documents with indexing
		
		String[] fileWords = readFile(selectedFile);
		SecurityHelperCTR securityHelperCTR = new SecurityHelperCTR();
		Tset = new HashMap<String, ArrayList<String>>();
		for (int i = 0; i < fileWords.length; i++){
			SecretKey kE = SHA256.createIndexingKey(kS, fileWords[i]);
			
			//for now uses same key to encrypt keywords
			String encryptedIndex = securityHelperCTR.encrypt("1", kE);
			//Tset.put(encryptedFileWords[i], encryptedIndex);
			//String keyStr = Base64.getEncoder().encodeToString(kE.getEncoded());
			if(Tset.get(fileWords[i]) == null){
				Tset.put(fileWords[i], new ArrayList<String>());
			}
			Tset.get(fileWords[i]).add(encryptedIndex);
		}
		
		for (Entry<String, ArrayList<String>> entry : Tset.entrySet()) {
			String key = entry.getKey();
			System.out.println("Key: " + key);
			ArrayList <String> values = entry.getValue();
			for(int i = 0; i < values.size(); i++){
				System.out.println("Values: " + values.get(i));
			}
		}
		
		//for now not used
		//AES.generateAESEncryptionKey();
		//kT = AES.secretKey;	
		

		
		return Tset;
	}
	
	//TODO: Look into Kt
	
	//reads file and returns unique words in an array
	public static String[] readFile(File selectedFile){
		Scanner fileScanner = null;
		try {
			fileScanner = new Scanner(selectedFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	    fileScanner.useDelimiter(" |\r\n");
	    ArrayList<String> words = new ArrayList<String>();
	    while (fileScanner.hasNext())
	    { 
	    	String nextWord = fileScanner.next();
	        if (!words.contains(nextWord))
	        {
	            words.add(nextWord);
	        }
	    }
	    fileScanner.close();
	    
	    return words.toArray(new String[words.size()]);
	}
		
	
	//decrypts File
	public static void decryptFile(HashMap<String, String> Tset, SecretKey kS){
		String key, value;
		SecurityHelperCTR securityHelper = new SecurityHelperCTR();
		SecretKey kE;
		for(Entry<String, String> entry: Tset.entrySet()){
			kE = SHA256.createIndexingKey(kS, entry.getKey());
			key = securityHelper.decrypt(entry.getKey(), kS);
			value = securityHelper.decrypt(entry.getValue(), kE);
			System.out.println("key is: " + key + " and value is: " + value);
		}
	}
}