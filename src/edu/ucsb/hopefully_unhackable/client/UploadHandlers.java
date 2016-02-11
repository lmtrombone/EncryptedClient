package edu.ucsb.hopefully_unhackable.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Map;
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
	private static String lastUpload = "";
	
	public static ActionListener getBrowseHandler(JTextField filePath) {
		JFileChooser fileChooser = new JFileChooser();
        fileChooser.setApproveButtonText("Select");
        fileChooser.setDialogTitle("Select a file to upload...");
        
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (AESCTR.secretKey == null) {
					JOptionPane.showMessageDialog(null, "Please generate or choose a key");
					return;
				}
				if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
	            {
	                System.out.println(fileChooser.getSelectedFile());
	                // TODO: This will be bugged if a user types a path in instead of using the browse button
	                ClientWindow.selectedFile = fileChooser.getSelectedFile();
	                filePath.setText(ClientWindow.selectedFile.getAbsolutePath());
	                ClientWindow.writeLog("Selected file: " + ClientWindow.selectedFile.getName());
	                ((HintTextField) filePath).setShowingHint(false);
	            }
			}
		};
	}
	
	public static ActionListener getUploadHandler(JProgressBar progressBar, JTextField filePath, JCheckBox stem) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String filename = filePath.getText();
				if (filename != "") {
					if (filename.equals(lastUpload)) {
						if (JOptionPane.showConfirmDialog(null, "You just uploaded " + filename + ", are you sure you want to upload it again?", 
								"Upload", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
							return;
						}
					}
					
					File fileFromType = new File(filename);
					if(fileFromType.isAbsolute() && fileFromType.exists()){
						ClientWindow.selectedFile = fileFromType;
					} else {
						JOptionPane.showMessageDialog(null, "Invalid path to file");
						return;
					}
				} else {
					JOptionPane.showMessageDialog(null, "Please select a file");
					return;
				}
				progressBar.setValue(0);
				ClientWindow.writeLog("Uploading file...");
				JButton source = (JButton) e.getSource();
				source.setEnabled(false);
				
				SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
					@Override
					protected Boolean doInBackground() throws Exception {
						String key = UUID.randomUUID().toString();
						publish(5);
						Map<String, StringPair> map = SSE.EDBSetup(ClientWindow.selectedFile, AESCTR.secretKey, key, stem.isSelected());
						publish(20);
						ObjectMapper mapper = new ObjectMapper();
		                try {
							String json = mapper.writeValueAsString(map);
							publish(50);
							HttpUtil.HttpPost(json);
							publish(60);
						} catch (JsonProcessingException e1) {
							e1.printStackTrace();
							return false;
						}
		                
		                FileUtils.uploadFile(ClientWindow.selectedFile, key, AESCTR.secretKey);
		                publish(100);
						return true;
					}
					
					@Override
					protected void done() {
						try {
							if (get()) {
								ClientWindow.writeLog("Upload successful!");
								JOptionPane.showMessageDialog(null, "Upload successful!");
								lastUpload = filename;
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
