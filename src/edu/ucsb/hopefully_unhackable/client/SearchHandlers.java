package edu.ucsb.hopefully_unhackable.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
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
import edu.ucsb.hopefully_unhackable.utils.Stemmer;
import edu.ucsb.hopefully_unhackable.utils.Stopper;
import edu.ucsb.hopefully_unhackable.utils.StringPair;

public class SearchHandlers {
	public static LoadingCache<String, Set<StringPair>> cache = CacheBuilder.newBuilder().maximumSize(100)
			.expireAfterAccess(5, TimeUnit.MINUTES).build(new QueryCacheLoader());
	
	private static List<Set<StringPair>> listSet = new ArrayList<>();
	
	public static ActionListener getSearchHandler(JTextField queryField, JList<StringPair> list, 
			DefaultListModel<StringPair> searchResults, JSlider matchSlider, JCheckBox stem) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (AESCTR.secretKey == null) {
					JOptionPane.showMessageDialog(null, "Please generate or choose a key");
					return;
				}
				
				// Split query into keywords
				String[] keywords = queryField.getText().trim().toLowerCase().split("[^\\w']+");
				Set<String> stemWords = new HashSet<>();
				for (String word : keywords) {
					if (Stopper.isStop(word)) continue;
					if (stem.isSelected()) {
						stemWords.add(Stemmer.getStem(word));
					} else {
						stemWords.add(word);
					}
				}
				System.out.println(" Searching: " + stemWords);
				listSet.clear();
				for (String keyword : stemWords) {
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
				matchSlider.setMaximum(stemWords.size());
				matchSlider.setValue(stemWords.size());
				matchSlider.setMinimum(1);
				matchSlider.setValueIsAdjusting(false);
				// Perform set intersections on results (Done by above code due to event handler)
				/*Set<StringPair> results = intersect(listSet);
				populateResults(results, list, searchResults);*/
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	public static MouseAdapter getListClickHandler() {
		return new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JList<StringPair> list = (JList<StringPair>) e.getSource();
		        if (e.getClickCount() == 2) {
		        	if (list.getSelectedIndex() != -1) {
		        		downloadFromList(list);		        		
		        	}
		        }
		    }
		};
	}
	
	public static ActionListener getDownloadHandler(JList<StringPair> list) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				downloadFromList(list);
			}
		};
	}
	
	public static ChangeListener getMatchHandler(JList<StringPair> list, DefaultListModel<StringPair> searchResults) {
		return new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					int min = source.getValue();
					Set<StringPair> results = intersect(listSet, min);
					populateResults(results, list, searchResults);
				}
			}
		};
	}
	
	private static void downloadFromList(JList<StringPair> list) {
		if (list.getSelectedIndex() >= 0) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setApproveButtonText("Save");
	        fileChooser.setDialogTitle("Choose a location...");
	        fileChooser.setSelectedFile(new File(list.getSelectedValue().getFileName()));
	        
	        // file chooser to save file
	        if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
	        	//JOptionPane.showMessageDialog(null, "Downloading file: " + list.getSelectedValue() + "[" + list.getSelectedIndex() + "]");
	        	String path = fileChooser.getSelectedFile().getAbsolutePath();
				FileUtils.downloadFile(path, list.getSelectedValue().getFileId(), AESCTR.secretKey);
				ClientWindow.writeLog("Downloaded to " + path);
				JOptionPane.showMessageDialog(null, "Downloaded to " + path);
	        }
		} else {
			// maybe produce an error message
			System.out.println("No file selected");
			JOptionPane.showMessageDialog(null, "No file selected");
		}
	}
	
	private static void populateResults(Set<StringPair> results, JList<StringPair> list, DefaultListModel<StringPair> searchResults) {
		// Add results to gui, and set selected
		searchResults.clear();
		if (results.isEmpty()) {
			searchResults.addElement(new StringPair("", "No results..."));
			list.setEnabled(false);
		} else {
			for (StringPair result : results) {
				searchResults.addElement(result);
			}
			list.setSelectedIndex(0);
			list.setEnabled(true);
		}
	}
	
	private static Set<StringPair> intersect(List<Set<StringPair>> sets, int min) {
		if (sets.size() < 1) {
			return Collections.emptySet();
		} else if(sets.size() <= min) {
			return intersect(sets);
		}
		
		// Adds each result to multiset and counts
		Multiset<StringPair> bag = HashMultiset.create();
		for (Set<StringPair> set : sets) {
			for (StringPair str : set) {
				bag.add(str);
			}
		}
		
		// Only keep results with a count greater than min
		Set<StringPair> newSet = new HashSet<>();
		for (Entry<StringPair> e : bag.entrySet()) {
			if (e.getCount() >= min) {
				newSet.add(e.getElement());
			}
		}
		
		return newSet;
	}
	
	private static Set<StringPair> intersect(List<Set<StringPair>> sets) {
		if (sets.size() < 1) {
			return Collections.emptySet();
		}
		// Sort sets by size (ascending)
		Collections.sort(sets, new Comparator<Set<StringPair>>() {
			@Override
			public int compare(Set<StringPair> o1, Set<StringPair> o2) {
				return Integer.compare(o1.size(), o2.size());
			}
		});
		
		Set<StringPair> newSet = new HashSet<>(sets.get(0));
		for (Set<StringPair> set : sets) {
			if (newSet.size() < 1) break;
			if (set == newSet) continue;
			
			Iterator<StringPair> it = newSet.iterator();
			while (it.hasNext()) {
				if (!set.contains(it.next())) {
					it.remove();
				}
			}
		}
		
		return newSet;
	}
}
