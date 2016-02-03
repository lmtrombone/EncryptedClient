package edu.ucsb.hopefully_unhackable.crypto;

import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;

public class SHA3{
	
	public static SecretKey createIndexingKey(SecretKey secretKey, String keyWord) {
		byte[] key = secretKey.getEncoded();
		Mac mac = new HMac(new SHA3Digest(256));
		mac.init(new KeyParameter(key));
		byte[] keyWordBytes = keyWord.getBytes();
		mac.update(keyWordBytes, 0, keyWordBytes.length);
		byte[] result = new byte[mac.getMacSize()];
		mac.doFinal(result, 0);
		SecretKey indexingKey = new SecretKeySpec(result, 0, result.length, "AES");
		return indexingKey;
		
	}
	
	public static void main(String[] args) {
		 
		SecretKey secretKey = AESCTR.generateKey();
		SecretKey indexingKey = createIndexingKey(secretKey, "hello");
		byte[] key = secretKey.getEncoded();
		System.out.println(Arrays.toString(key));
	}
}