package edu.ucsb.hopefully_unhackable.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.UUID;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ucsb.hopefully_unhackable.crypto.AES;
import edu.ucsb.hopefully_unhackable.crypto.SSE;
import edu.ucsb.hopefully_unhackable.utils.FileUtils;
import edu.ucsb.hopefully_unhackable.utils.HttpUtil;

public class UploadHandlers {
	public static ActionListener getBrowseHandler(JTextField filePath) {
		JFileChooser fileChooser = new JFileChooser();
        fileChooser.setApproveButtonText("Select");
        fileChooser.setDialogTitle("Select a file to upload...");
        
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (AES.secretKey == null) {
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
	            }
			}
		};
	}
	
	public static ActionListener getUploadHandler() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (ClientWindow.selectedFile == null) {
					JOptionPane.showMessageDialog(null, "Please select a file");
					return;
				}
				ClientWindow.writeLog("Encrypting file...");
				//for now uses same key to encrypt keywords
				String key = UUID.randomUUID().toString();
				Map<String, String> map = SSE.EDBSetup(ClientWindow.selectedFile, AES.secretKey, key);
                ObjectMapper mapper = new ObjectMapper();
                try {
					String json = mapper.writeValueAsString(map);
					System.out.println(json);
					ClientWindow.writeLog("Indexing file...");
					HttpUtil.HttpPost(json);
				} catch (JsonProcessingException e1) {
					e1.printStackTrace();
					ClientWindow.writeLog("Upload failed!");
					return;
				}
                ClientWindow.writeLog("Uploading file...");
                FileUtils.uploadFile(ClientWindow.selectedFile, key);
                ClientWindow.writeLog("Upload successful!");
                JOptionPane.showMessageDialog(null, "Upload successfull!");
			}
		};
	}
}
