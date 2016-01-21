package edu.ucsb.hopefully_unhackable.crypto;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.crypto.SecretKey;

public class SSE {
	public static HashMap<String, String> tSet;
	//public static HashMap<String, ArrayList<String>> tSet;
	
	public static HashMap<String, String> EDBSetup(File selectedFile, SecretKey kS, String key) {
		//TODO: Parse documents with indexing
		
		Set<String> fileWords = readFile(selectedFile);
		//tSet = new HashMap<String, ArrayList<String>>();
		tSet = new HashMap<String, String>();
		for (String word : fileWords) {
			SecretKey kE = SHA256.createIndexingKey(kS, word);
			
			String encWord = SHA256.createIndexingString(kE, word).replace("+", "X"); // remove + signs TEMP FIX TODO
			String encryptedIndex = AESCTR.encrypt(key, kE);
			//tSet.put(encryptedFileWords[i], encryptedIndex);
			//String keyStr = Base64.getEncoder().encodeToString(kE.getEncoded());
			//if(tSet.get(fileWords[i]) == null){
				//tSet.put(fileWords[i], new ArrayList<String>());
			//}
			//tSet.get(fileWords[i]).add(encryptedIndex);
			tSet.put(encWord, encryptedIndex);
		}
		
		//for (Entry<String, ArrayList<String>> entry : tSet.entrySet()) {
			//String key = entry.getKey();
			//System.out.println("Key: " + key);
			//ArrayList <String> values = entry.getValue();
			//for(int i = 0; i < values.size(); i++){
				//System.out.println("Values: " + values.get(i));
			//}
		//}
		
		//for now not used
		//AES.generateAESEncryptionKey();
		//kT = AES.secretKey;	
		
		return tSet;
	}
	
	//TODO: Look into Kt
	
	//reads file and returns unique words in a set
	public static Set<String> readFile(File selectedFile) {
		Set<String> keywords = new HashSet<>();
		try {
			List<String> lines = Files.readAllLines(Paths.get(selectedFile.toURI()));
			for (String line : lines) {
				// Make lowercase, split on everything that is not alphanumeric
				String[] words = line.toLowerCase().split("[^\\w']+");
				for (String word : words) {
					keywords.add(word);
				}
			}
			keywords.remove(""); // remove empty string
		} catch (IOException ex) {
			System.out.println("Error parsing file...");
		}
		
		return keywords;
	}
}