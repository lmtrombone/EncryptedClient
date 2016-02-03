package edu.ucsb.hopefully_unhackable.utils;

import java.util.Arrays;
import java.util.HashSet;

public class Stopper {
	private static final String[] WORDS = new String[] {
			"i", "a", "about", "an", "are", "as", "at", "be",
			"by", "for", "from", "how", "in", "is", "it", "of",
			"on", "or", "that", "the", "this", "to", "was",
			"what", "when", "where", "who", "will", "with", "the"
	};
	private static final HashSet<String> STOP_WORDS = new HashSet<>(Arrays.asList(WORDS));
	
	public static boolean isStop(String word) {
		return STOP_WORDS.contains(word);
	}
}
