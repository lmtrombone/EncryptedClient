package edu.ucsb.hopefully_unhackable.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
				
				// Split query into keywords
				String[] keywords = queryField.getText().trim().toLowerCase().split("[^\\w']+");
				List<Set<String>> listSet = new ArrayList<>();
				for (String keyword : keywords) {
					if (keyword.isEmpty()) continue;
					SecretKey kE = SHA256.createIndexingKey(AESCTR.secretKey, keyword);
					String encWord = SHA256.createIndexingString(kE, keyword).replace("+", "X"); // remove + signs TEMP FIX TODO
					Set<String> inds = HttpUtil.HttpGet(encWord);
					// Decrypt all inds and add to listSet
					Set<String> decInds = new HashSet<>();
					for (String ind : inds) {
						decInds.add(AESCTR.decrypt(ind, kE));
					}
					listSet.add(decInds);
				}
				
				// Perform set intersections on results
				Set<String> results = intersect(listSet);
				
				// Add results to gui, and set selected
				searchResults.clear();
				if (results.isEmpty()) {
					searchResults.addElement("No results...");
					list.setEnabled(false);
				} else {
					for (String result : results) {
						searchResults.addElement(result);
					}
					list.setSelectedIndex(0);
					list.setEnabled(true);
				}
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	public static MouseAdapter getListClickHandler() {
		return new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JList<String> list = (JList<String>) e.getSource();
		        if (e.getClickCount() == 2) {
		        	if (list.getSelectedIndex() != -1) {
		        		downloadFromList(list);		        		
		        	}
		        }
		    }
		};
	}
	
	public static ActionListener getDownloadHandler(JList<String> list) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				downloadFromList(list);
			}
		};
	}
	
	private static void downloadFromList(JList<String> list) {
		if (list.getSelectedIndex() >= 0) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setApproveButtonText("Save");
	        fileChooser.setDialogTitle("Select a file...");
	        
	        // file chooser to save file
	        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
	        	//JOptionPane.showMessageDialog(null, "Downloading file: " + list.getSelectedValue() + "[" + list.getSelectedIndex() + "]");
	        	String path = fileChooser.getSelectedFile().getAbsolutePath();
				FileUtils.downloadFile(path, list.getSelectedValue(), AESCTR.secretKey);
				ClientWindow.writeLog("Downloaded to " + path);
				JOptionPane.showMessageDialog(null, "Downloaded to " + path);
	        }
		} else {
			// maybe produce an error message
			System.out.println("No file selected");
			JOptionPane.showMessageDialog(null, "No file selected");
		}
	}
	
	private static Set<String> intersect(List<Set<String>> sets) {
		if (sets.size() < 1) {
			return null;
		}
		// Sort sets by size (ascending)
		Collections.sort(sets, new Comparator<Set<String>>() {
			@Override
			public int compare(Set<String> o1, Set<String> o2) {
				return Integer.compare(o1.size(), o2.size());
			}
		});
		
		Set<String> newSet = sets.get(0);
		for (Set<String> set : sets) {
			if (newSet.size() < 1) break;
			if (set == newSet) continue;
			
			Iterator<String> it = newSet.iterator();
			while (it.hasNext()) {
				if (!set.contains(it.next())) {
					it.remove();
				}
			}
		}
		
		return newSet;
	}
}
