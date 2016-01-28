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

import edu.ucsb.hopefully_unhackable.client.SearchHandlers;
import edu.ucsb.hopefully_unhackable.utils.StringPair;

public class SSE {
	public static HashMap<String, StringPair> tSet;
	//public static HashMap<String, ArrayList<String>> tSet;
	
	public static HashMap<String, StringPair> EDBSetup(File selectedFile, SecretKey kS, String key) {
		//TODO: Parse documents with indexing
		String filename = com.google.common.io.Files.getNameWithoutExtension(selectedFile.getName());
		Set<String> fileWords = readFile(selectedFile);
		//tSet = new HashMap<String, ArrayList<String>>();
		tSet = new HashMap<String, StringPair>();
		for (String word : fileWords) {
			SearchHandlers.cache.invalidate(word);
			SecretKey kE = SHA256.createIndexingKey(kS, word);
			
			String encWord = SHA256.createIndexingString(kE, word).replace("+", "X"); // remove + signs TEMP FIX TODO
			String encId = AESCTR.encrypt(key, kE);
			String encName = AESCTR.encrypt(filename, kE);
			//tSet.put(encryptedFileWords[i], encryptedIndex);
			//String keyStr = Base64.getEncoder().encodeToString(kE.getEncoded());
			//if(tSet.get(fileWords[i]) == null){
				//tSet.put(fileWords[i], new ArrayList<String>());
			//}
			//tSet.get(fileWords[i]).add(encryptedIndex);
			tSet.put(encWord, new StringPair(encId, encName));
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
		String filename = com.google.common.io.Files.getNameWithoutExtension(selectedFile.getName());
		Set<String> keywords = new HashSet<>();
		keywords.add(filename);
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