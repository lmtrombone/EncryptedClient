package edu.ucsb.hopefully_unhackable.utils;

// pair of (file id, file name)
public class StringPair {
	private String fileId;
	private String fileName;
	
	public StringPair() { }
	
	public StringPair(String fileId, String fileName) {
		this.fileId = fileId;
		this.fileName = fileName;

	}
	
	public String getFileId() {
		return this.fileId;
	}
	
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getFileName() {
		return this.fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof StringPair)) {
			return false;
		} else if (o == this) {
			return true;
		}
		
		StringPair stringPair = (StringPair) o;
		return fileId.equals(stringPair.fileId);
	}
	
	@Override
	public int hashCode() {
		return fileId.hashCode();
	}
	
	@Override
	public String toString() {
		return this.fileName;
	}
}
