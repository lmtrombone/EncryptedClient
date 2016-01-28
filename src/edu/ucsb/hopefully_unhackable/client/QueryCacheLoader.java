package edu.ucsb.hopefully_unhackable.client;

import java.util.HashSet;
import java.util.Set;

import javax.crypto.SecretKey;

import com.google.common.cache.CacheLoader;

import edu.ucsb.hopefully_unhackable.crypto.AESCTR;
import edu.ucsb.hopefully_unhackable.crypto.SHA256;
import edu.ucsb.hopefully_unhackable.utils.HttpUtil;

public class QueryCacheLoader extends CacheLoader<String, Set<String>> {
	@Override
	public Set<String> load(String key) throws Exception {
		SecretKey kE = SHA256.createIndexingKey(AESCTR.secretKey, key);
		// remove + signs TEMP FIX TODO
		String encWord = SHA256.createIndexingString(kE, key).replace("+", "X");
		Set<String> inds = HttpUtil.HttpGet(encWord);
		// Decrypt all inds and add to listSet
		Set<String> decInds = new HashSet<>();
		for (String ind : inds) {
			decInds.add(AESCTR.decrypt(ind, kE));
		}
		return decInds;
	}
}
