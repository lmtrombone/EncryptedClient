package edu.ucsb.hopefully_unhackable.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.crypto.SecretKey;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import edu.ucsb.hopefully_unhackable.crypto.AESCTR;
import edu.ucsb.hopefully_unhackable.crypto.SHA256;
import edu.ucsb.hopefully_unhackable.utils.FileUtils;
import edu.ucsb.hopefully_unhackable.utils.HttpUtil;

public class SearchHandlers {
	public static ActionListener getSearchHandler(JTextField queryField, JList<String> list, DefaultListModel<String> searchResults) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (AESCTR.secretKey == null) {
					JOptionPane.showMessageDialog(null, "Please generate or choose a key");
					return;
				}
				//String[] keywords = queryField.getText().split(" +");
				String[] keyWord = queryField.getText().trim().split(" ");
				if(keyWord.length != 1) {
					System.out.println("Only single word search is supported."
							+ "Searching for documents with only the first search term.");
				}
				//get request
				//result should be list of ind
				//HashMap<String, ArrayList<String>> encIndex = HttpUtil.HttpGet(keyWord[0]);
				//HashMap<String, String> encIndex = HttpUtil.HttpGet(keyWord[0]);
				
				//gets set of encrypted ids and decrypts
				//SecurityHelperCTR securityHelperCTR = new SecurityHelperCTR();
				//ArrayList <String> values, ids = new ArrayList<String>();
				//ArrayList <String> ids = new ArrayList<String>();
				/*
				for (Entry<String, ArrayList<String>> entry : encIndex.entrySet()) {
					String key = entry.getKey();
					System.out.println("Key: " + key);
					values = entry.getValue();
					for(int i = 0; i < values.size(); i++){
						System.out.println("Values: " + values.get(i));
						SecretKey kE = SHA256.createIndexingKey(AES.secretKey, key);
						ids.add(securityHelperCTR.decrypt(values.get(i), kE));
					}
				}
				*/
				SecretKey kE = SHA256.createIndexingKey(AESCTR.secretKey, keyWord[0]);
				List<String> inds = Collections.emptyList();
				if (!keyWord[0].isEmpty()) {
					String encWord = SHA256.createIndexingString(kE, keyWord[0]).replace("+", "X"); // remove + signs TEMP FIX TODO
					inds = HttpUtil.HttpGet(encWord);
				}
				String[] ids = inds.toArray(new String[inds.size()]);
				
				searchResults.clear();
				if (ids.length == 0) {
					searchResults.addElement("No results...");
					list.setEnabled(false);
				} else {
					String[] x = new String[ids.length];
					for(int i = 0; i < ids.length; i++) {
						x[i] = AESCTR.decrypt(ids[i], kE);
						searchResults.addElement(x[i]);
					}
					list.setSelectedIndex(0);
					list.setEnabled(true);
				}
			}
		};
	}
	
	public static ActionListener getDownloadHandler(JList<String> list) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (list.getSelectedIndex() >= 0) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setApproveButtonText("Save");
			        fileChooser.setDialogTitle("Select a file...");
			        
			        // file chooser to save file
			        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			        	//JOptionPane.showMessageDialog(null, "Downloading file: " + list.getSelectedValue() + "[" + list.getSelectedIndex() + "]");
			        	String path = fileChooser.getSelectedFile().getAbsolutePath();
						FileUtils.downloadFile(path, list.getSelectedValue());
						ClientWindow.writeLog("Downloaded to " + path);
						JOptionPane.showMessageDialog(null, "Downloaded to " + path);
			        }
				} else {
					// maybe produce an error message
					System.out.println("No file selected");
					JOptionPane.showMessageDialog(null, "No file selected");
				}
			}
		};
	}
}
