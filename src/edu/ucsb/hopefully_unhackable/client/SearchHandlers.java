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
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

import edu.ucsb.hopefully_unhackable.crypto.AESCTR;
import edu.ucsb.hopefully_unhackable.utils.FileUtils;

public class SearchHandlers {
	private static List<Set<String>> listSet = new ArrayList<>();
	private static LoadingCache<String, Set<String>> cache = CacheBuilder.newBuilder().build(new QueryCacheLoader());
	
	public static ActionListener getSearchHandler(JTextField queryField, JList<String> list, 
			DefaultListModel<String> searchResults, JSlider matchSlider) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (AESCTR.secretKey == null) {
					JOptionPane.showMessageDialog(null, "Please generate or choose a key");
					return;
				}
				
				// Split query into keywords
				String[] keywords = queryField.getText().trim().toLowerCase().split("[^\\w']+");
				listSet.clear();
				for (String keyword : keywords) {
					if (keyword.isEmpty()) continue;
					
					try {
						listSet.add(cache.get(keyword));
					} catch (ExecutionException ex) {
						// Some error? Do nothing for now
						ex.printStackTrace();
					}
				}
				
				// This triggers event on slider once
				matchSlider.setValueIsAdjusting(true);
				matchSlider.setMaximum(keywords.length);
				matchSlider.setValue(keywords.length);
				matchSlider.setMinimum(1);
				matchSlider.setValueIsAdjusting(false);
				// Perform set intersections on results (Done by above code due to event handler)
				/*Set<String> results = intersect(listSet);
				populateResults(results, list, searchResults);*/
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
	
	public static ChangeListener getMatchHandler(JList<String> list, DefaultListModel<String> searchResults) {
		return new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					int min = source.getValue();
					Set<String> results = intersect(listSet, min);
					populateResults(results, list, searchResults);
				}
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
	
	private static void populateResults(Set<String> results, JList<String> list, DefaultListModel<String> searchResults) {
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
	
	private static Set<String> intersect(List<Set<String>> sets, int min) {
		if (sets.size() < 1) {
			return Collections.emptySet();
		} else if(sets.size() <= min) {
			return intersect(sets);
		}
		
		// Adds each result to multiset and counts
		Multiset<String> bag = HashMultiset.create();
		for (Set<String> set : sets) {
			for (String str : set) {
				bag.add(str);
			}
		}
		
		// Only keep results with a count greater than min
		Set<String> newSet = new HashSet<>();
		for (Entry<String> e : bag.entrySet()) {
			if (e.getCount() >= min) {
				newSet.add(e.getElement());
			}
		}
		
		return newSet;
	}
	
	private static Set<String> intersect(List<Set<String>> sets) {
		if (sets.size() < 1) {
			return Collections.emptySet();
		}
		// Sort sets by size (ascending)
		Collections.sort(sets, new Comparator<Set<String>>() {
			@Override
			public int compare(Set<String> o1, Set<String> o2) {
				return Integer.compare(o1.size(), o2.size());
			}
		});
		
		Set<String> newSet = new HashSet<>(sets.get(0));
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
