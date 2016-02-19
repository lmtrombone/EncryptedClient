package edu.ucsb.hopefully_unhackable.crypto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.crypto.SecretKey;

import edu.ucsb.hopefully_unhackable.client.SearchHandlers;
import edu.ucsb.hopefully_unhackable.utils.Stemmer;
import edu.ucsb.hopefully_unhackable.utils.Stopper;
import edu.ucsb.hopefully_unhackable.utils.StringPair;

public class SSE {
	public static HashMap<String, ArrayList<StringPair>> tSet;
	
	public static HashMap<String, ArrayList<StringPair>> EDBSetup(File[] selectedFiles, SecretKey kS, String[] key, boolean stem) {
		//TODO: Parse documents with indexing
		//HashMap<Integer, Set<String>> fileStemWords = new HashMap<Integer, Set<String>>();
		List<Set<String>> fileStemWords = new ArrayList<Set<String>>();
		for(int i = 0; i < selectedFiles.length; i++) {
			String filename = selectedFiles[i].getName();
			Set<String> fileWords = readFile(selectedFiles[i]);
			fileWords.add(com.google.common.io.Files.getNameWithoutExtension(filename).toLowerCase());
			Set<String> stemWords = new HashSet<>();
			for(String word : fileWords) {
				if(Stopper.isStop(word)) continue;
				if(stem) {
					stemWords.add(Stemmer.getStem(word));
				} else {
					stemWords.add(word);
				}
			}
			//change later
			fileStemWords.add(stemWords);
		}
		
		tSet = new HashMap<String, ArrayList<StringPair>>();
		for(int i = 0; i < fileStemWords.size(); i++) {
			Set<String> values = fileStemWords.get(i);
			for(String word: values) {
				SearchHandlers.cache.invalidate(word);
				SecretKey kE = SHA3.createIndexingKey(kS, word);
				
				String encWord = SHA2.createIndexingString(kE, word).replace("+", "X");
				String encId = AESCTR.encrypt(key[i], kE);
				
				String filename = selectedFiles[i].getName();
				String encName = AESCTR.encrypt(filename, kE);
				
				if(!tSet.containsKey(encWord)) {
					tSet.put(encWord, new ArrayList<StringPair>());
				}
				tSet.get(encWord).add(new StringPair(encId, encName));
			}
		}
		
		return tSet;
	}
	
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