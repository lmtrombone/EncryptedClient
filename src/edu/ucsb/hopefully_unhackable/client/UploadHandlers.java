package edu.ucsb.hopefully_unhackable.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ucsb.hopefully_unhackable.crypto.AESCTR;
import edu.ucsb.hopefully_unhackable.crypto.SSE;
import edu.ucsb.hopefully_unhackable.utils.FileUtils;
import edu.ucsb.hopefully_unhackable.utils.HttpUtil;
import edu.ucsb.hopefully_unhackable.utils.StringPair;

public class UploadHandlers {
	private static Set<String> lastUpload = new HashSet<>();
	private static File[] selectedFiles;
	
	public static ActionListener getBrowseHandler(JTextField filePath) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setApproveButtonText("Select");
        fileChooser.setDialogTitle("Select a file to upload...");
        
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (AESCTR.secretKey == null) {
					JOptionPane.showMessageDialog(null, "Please generate or choose a key");
					return;
				}
				
				if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
	                selectedFiles = fileChooser.getSelectedFiles();
	                ClientWindow.writeLog("Selected files: ");
	                StringBuilder sb = new StringBuilder(1024);
	               
	                for(int i = 0; i < selectedFiles.length; i++) {
	                	if(i == selectedFiles.length - 1) {
	                		sb.append(selectedFiles[i].getAbsolutePath());
	                	} else {
	                		sb.append(selectedFiles[i].getAbsolutePath() + ", ");
	                	}
	                	ClientWindow.writeLog(selectedFiles[i].getName());
	                }
	                filePath.setText(sb.toString());
	                ((HintTextField) filePath).setShowingHint(false);
	            }
			}
		};
	}
	
	public static ActionListener getUploadHandler(JProgressBar progressBar, JCheckBox stem) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(selectedFiles != null) {
					ClientWindow.selectedFiles = new File[selectedFiles.length];
					for(int i = 0; i < selectedFiles.length; i++) {
						String filepath = selectedFiles[i].getAbsolutePath();
						if(lastUpload.contains(filepath)) {
							if (JOptionPane.showConfirmDialog(null, "You just uploaded " + filepath + ", are you sure you want to upload it again?", 
									"Upload", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
								return;
							}
						}
						if(selectedFiles[i].isAbsolute() && selectedFiles[i].exists()) {
							ClientWindow.selectedFiles[i] = selectedFiles[i];
						} else {
							JOptionPane.showMessageDialog(null, "Invalid path to file");
							return;
						}
					}
				} else {
					JOptionPane.showMessageDialog(null, "Please select a file");
					return;
				}
				
				progressBar.setValue(0);
				ClientWindow.writeLog("Uploading file(s)...");
				JButton source = (JButton) e.getSource();
				source.setEnabled(false);
				
				SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
					@Override
					protected Boolean doInBackground() throws Exception {
						String[] key = new String[ClientWindow.selectedFiles.length];
						publish(5);
						
						for (int i = 0; i < ClientWindow.selectedFiles.length; i++) {
							key[i] = UUID.randomUUID().toString();
		                	FileUtils.uploadFile(ClientWindow.selectedFiles[i], key[i], AESCTR.secretKey);
		                }
						publish(40);
						
						Map<String, ArrayList<StringPair>> map = SSE.EDBSetup(ClientWindow.selectedFiles, AESCTR.secretKey, key, stem.isSelected());
						publish(60);
						
						ObjectMapper mapper = new ObjectMapper();
		                try {
							String json = mapper.writeValueAsString(map);
							publish(80);
							HttpUtil.HttpPost(json);
						} catch (JsonProcessingException e1) {
							e1.printStackTrace();
							return false;
						}
		                
		                
		                publish(100);
						return true;
					}
					
					@Override
					protected void done() {
						try {
							if (get()) {
								ClientWindow.writeLog("Upload successful!");
								JOptionPane.showMessageDialog(null, "Upload successful!");
								lastUpload.clear();
								for(File f : ClientWindow.selectedFiles) {
									lastUpload.add(f.getAbsolutePath());
								}
							} else {
								ClientWindow.writeLog("Upload failed!");
								JOptionPane.showMessageDialog(null, "Upload failed!");
							}
						} catch (Exception ex) {
							JOptionPane.showMessageDialog(null, "Upload error!");
							ex.printStackTrace();
						}
						source.setEnabled(true);
					}
					
					@Override
					protected void process(List<Integer> n) {
						progressBar.setValue(n.get(n.size() - 1));
					}
				};
				
				worker.execute();
			}
		};
	}
}
