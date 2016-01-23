package edu.ucsb.hopefully_unhackable.client;

import javax.crypto.SecretKey;

public class KeyItem {
	private SecretKey kS;
	private String name;
	
	public KeyItem(SecretKey kS, String name) {
		this.kS = kS;
		this.name = name;
	}
	
	public SecretKey getKey() {
		return kS;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof KeyItem)) {
			return false;
		} else if (o == this) {
			return true;
		}
		
		KeyItem keyItem = (KeyItem) o;
		return name.equals(keyItem.name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public String toString() {
		return name;
	}
}
