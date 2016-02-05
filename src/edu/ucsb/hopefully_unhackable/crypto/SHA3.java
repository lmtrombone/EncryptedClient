package edu.ucsb.hopefully_unhackable.crypto;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;

public class SHA3 {
	
	//creates an AES key using SHA3-256
	public static SecretKey createIndexingKey(SecretKey secretKey, String keyWord) {
		//constructs a key for SHA3-256
		byte[] key = secretKey.getEncoded();
		Mac mac = new HMac(new SHA3Digest(256));
		mac.init(new KeyParameter(key));
		
		//hashes keyWord and stores in result
		byte[] keyWordBytes = keyWord.getBytes();
		mac.update(keyWordBytes, 0, keyWordBytes.length);
		byte[] result = new byte[mac.getMacSize()];
		mac.doFinal(result, 0);
		
		//creates an AES key using result of SHA3-256
		SecretKey indexingKey = new SecretKeySpec(result, 0, result.length, "AES");
		
		return indexingKey;		
	}
}