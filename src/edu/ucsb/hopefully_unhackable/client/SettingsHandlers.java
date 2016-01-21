package edu.ucsb.hopefully_unhackable.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import edu.ucsb.hopefully_unhackable.crypto.AES;

public class SettingsHandlers {
	public static ActionListener getKeygenHandler(JTextField keyFile) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				
				// Would you like to load or generate key?
				if (JOptionPane.showConfirmDialog(null, "Would you like to generate a new key?", "New Key?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					fileChooser.setApproveButtonText("Save");
			        fileChooser.setDialogTitle("Select a file to save key...");
			        
			        // file chooser to save key
			        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				        SecretKey newKey = AES.generateKey();
				        
				        // Serialize (out)
				        try {
				        	File file = fileChooser.getSelectedFile();
							ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file.getAbsolutePath()));
							out.writeObject(newKey);
							out.close();
							
							// Set key in AES
							AES.secretKey = newKey;
							keyFile.setText(file.getName());
				        } catch (IOException ex) {
							ex.printStackTrace();
						}
			        }
				} else {
					fileChooser.setApproveButtonText("Load");
			        fileChooser.setDialogTitle("Select a file to load key...");
			        
					//file chooser to load key
			        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			        	// Deserialize (in)
			        	try {
							File file = fileChooser.getSelectedFile();
							ObjectInputStream in = new ObjectInputStream(new FileInputStream(file.getAbsolutePath()));
							AES.secretKey = (SecretKey) in.readObject(); // Set secretKey
							in.close();
							
							keyFile.setText(file.getName()); //Should store in file and display filename instead of key
						} catch (IOException | ClassNotFoundException ex) {
							ex.printStackTrace();
							JOptionPane.showMessageDialog(null, "Invalid key file!");
						}
			        }
					
				}
				
				byte[] decoded = AES.secretKey.getEncoded();
				ClientWindow.writeLog("Loaded key: " + Arrays.toString(decoded));
			}
		};
	}
}
