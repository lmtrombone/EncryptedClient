package edu.ucsb.hopefully_unhackable.client;

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
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class SearchHandlers {
	public static LoadingCache<String, Set<StringPair>> cache = CacheBuilder.newBuilder().maximumSize(100)
			.expireAfterAccess(5, TimeUnit.MINUTES).build(new QueryCacheLoader());
	
	private static List<Set<StringPair>> listSet = new ArrayList<>();
	
	public static EventHandler<ActionEvent> getSearchHandler(TextField queryField, ListView<StringPair> list, Slider matchSlider, CheckBox stem) {
		return e -> {
			if (AESCTR.secretKey == null) {
                Alert a = new Alert(Alert.AlertType.INFORMATION, "Please generate or choose a key", ButtonType.OK);
                a.setTitle("Information");
                a.showAndWait();
				return;
			}

			Button source = (Button) e.getSource();
			source.setDisable(true);

			SwingWorker<Set<String>, Void> worker = new SwingWorker<Set<String>, Void>() {
				@Override
				protected Set<String> doInBackground() throws Exception {
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
					return stemWords;
				}

				@Override
				protected void done() {
					try {
						Set<String> stemWords = get();
						// This triggers event on slider once
						matchSlider.setValueChanging(true);
						matchSlider.setMax(stemWords.size());
						matchSlider.setValue(stemWords.size());
						matchSlider.setMin(1);
						matchSlider.setValueChanging(false);
						// Perform set intersections on results (Done by above code due to event handler)
						Set<StringPair> results = intersect(listSet);
						populateResults(results, list);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					source.setDisable(false);
				}
			};

			worker.execute();
		};
	}

	public static EventHandler<MouseEvent> getListClickHandler(FXController controller, ProgressBar progressBar) {
		return e -> {
			ListView<StringPair> list = (ListView<StringPair>) e.getSource();
			if (e.getClickCount() == 2) {
				if (list.getSelectionModel().getSelectedIndex() != -1) {
					downloadFromList(controller, progressBar, list);
				}
			}
		};
	}
	
	public static EventHandler<ActionEvent> getDownloadHandler(FXController controller, ProgressBar progressBar, ListView<StringPair> list) {
		return e -> downloadFromList(controller, progressBar, list);
	}
	
	public static ChangeListener<Number> getMatchHandler(ListView<StringPair> list) {
		return (observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                int min = newValue.intValue();
                Set<StringPair> results = intersect(listSet, min);
                populateResults(results, list);
            }
        };
    }
	
	private static void downloadFromList(FXController controller, ProgressBar progressBar, ListView<StringPair> list) {
		if (list.getSelectionModel().getSelectedIndex() >= 0) {
			FileChooser fileChooser = new FileChooser();
	        fileChooser.setTitle("Choose a location...");
	        fileChooser.setInitialFileName(list.getSelectionModel().getSelectedItem().getFileName());
	        
	        // file chooser to save file
            File f = fileChooser.showSaveDialog(null);
	        if (f != null) {
	        	String path = f.getAbsolutePath();
	        	
	        	progressBar.setProgress(0);
                controller.writeLog("Downloading file...");
	        	
				//TODO: Actually report progress
	        	SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
					@Override
					protected Boolean doInBackground() throws Exception {
						try {
							FileUtils.downloadFile(path, list.getSelectionModel().getSelectedItem().getFileId(), AESCTR.secretKey);
							publish(1);
						} catch (Exception ex) {
							ex.printStackTrace();
							return false;
						}
						return true;
					}
					
					@Override
					protected void done() {
                        Platform.runLater(new TimerTask() {
                            @Override
                            public void run() {
                                Alert a;
                                try {
                                    if (get()) {
                                        controller.writeLog("Downloaded to " + path);
                                        a = new Alert(Alert.AlertType.INFORMATION, "Downloaded to " + path, ButtonType.OK);
                                        a.setTitle("Information");
                                        a.showAndWait();
                                    } else {
                                        controller.writeLog("Download failed!");
                                        a = new Alert(Alert.AlertType.ERROR, "Download failed!", ButtonType.OK);
                                        a.setTitle("Error");
                                        a.showAndWait();
                                    }
                                } catch (Exception ex) {
                                    a = new Alert(Alert.AlertType.ERROR, "Download error!", ButtonType.OK);
                                    a.setTitle("Error");
                                    a.showAndWait();
                                    ex.printStackTrace();
                                }
                            }
                        });
					}
					
					@Override
					protected void process(List<Integer> n) {
						progressBar.setProgress(n.get(n.size() - 1));
					}
				};
				progressBar.setProgress(0.05);
				worker.execute();
	        }
		} else {
			// maybe produce an error message
			System.out.println("No file selected");
            Alert a = new Alert(Alert.AlertType.INFORMATION, "No file selected.", ButtonType.OK);
            a.setTitle("Information");
            a.showAndWait();
		}
	}
	
	private static void populateResults(Set<StringPair> results, ListView<StringPair> list) {
        Platform.runLater(new TimerTask() {
            @Override
            public void run() {
                // Add results to gui, and set selected
                list.getItems().clear();
                if (results.isEmpty()) {
                    list.getItems().add(new StringPair("", "No results..."));
                    list.setDisable(true);
                } else {
                    for (StringPair result : results) {
                        list.getItems().add(result);
                    }
                    list.getSelectionModel().select(0);
                    list.setDisable(false);
                }
            }
        });

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
		Collections.sort(sets, (o1, o2) -> Integer.compare(o1.size(), o2.size()));
		
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
