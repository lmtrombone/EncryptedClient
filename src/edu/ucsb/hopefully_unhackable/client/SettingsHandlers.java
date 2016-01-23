package edu.ucsb.hopefully_unhackable.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.crypto.SecretKey;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import edu.ucsb.hopefully_unhackable.crypto.AESCTR;


@SuppressWarnings("unchecked")
public class SettingsHandlers {
	public static ActionListener selectKeyHandler() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object o = e.getSource();
				if (!(o instanceof JComboBox)) {
					throw new IllegalStateException("selectKeyHandler attached to invalid control");
				}
				
				JComboBox<KeyItem> box = (JComboBox<KeyItem>) o;
				AESCTR.secretKey = box.getItemAt(box.getSelectedIndex()).getKey();
				ClientWindow.writeLog("Successfully loaded key: " + box.getSelectedItem());
			}
		};
	}
	
	public static ActionListener removeKeyHandler(JComboBox<KeyItem> keyFile) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] options = { "OK", "CANCEL" };
				if (JOptionPane.showOptionDialog(null, "Are you sure you want to delete the key: " + keyFile.getSelectedItem() + 
						"?\nThis CANNOT be undone.", "WARNING: Delete Key?", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, 
						null, options, options[1]) == JOptionPane.OK_OPTION) {
					String keyName = keyFile.getSelectedItem().toString();
					File file = new File("keys/" + keyName);
					if (file.delete()) {
						ClientWindow.writeLog("Successfully deleted key: " + keyName);
						keyFile.removeItemAt(keyFile.getSelectedIndex());
					} else {
						ClientWindow.writeLog("Unable to delete file");
					}
				}
			}
		};
	}
	
	public static ActionListener getKeygenHandler(JComboBox<KeyItem> keyFile) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String keyName = JOptionPane.showInputDialog("Please enter a name for your key");
				if (keyName != null && !keyName.isEmpty()) {
			        SecretKey newKey = AESCTR.generateKey();
			        
			        // Serialize (out)
			        try {
			        	File file = new File("keys/" + keyName);
						ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file.getAbsolutePath()));
						out.writeObject(newKey);
						out.close();
						
						// Set key in AES
						AESCTR.secretKey = newKey;
						
						KeyItem keyItem = new KeyItem(newKey, file.getName());
						keyFile.addItem(keyItem);
						keyFile.setSelectedItem(keyItem);
						ClientWindow.writeLog("Loaded new key: " + file.getName());
			        } catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		};
	}
}
