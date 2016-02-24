package edu.ucsb.hopefully_unhackable.client;

import edu.ucsb.hopefully_unhackable.crypto.AESCTR;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputDialog;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Optional;


public class SettingsHandlers {
	public static EventHandler<ActionEvent> selectKeyHandler(FXController controller) {
		return e -> {
            Object o = e.getSource();
            if (!(o instanceof ComboBox)) {
                throw new IllegalStateException("selectKeyHandler attached to invalid control");
            }

            ComboBox<KeyItem> box = (ComboBox<KeyItem>) o;
            AESCTR.secretKey =box.getSelectionModel().getSelectedItem().getKey();
            SearchHandlers.cache.invalidateAll();
            controller.writeLog("Successfully loaded key: " + box.getSelectionModel().getSelectedItem());
        };
	}
	
	public static EventHandler<ActionEvent> removeKeyHandler(FXController controller, ComboBox<KeyItem> keyCombo) {
		return e -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("WARNING!");
            a.setHeaderText("WARNING: Delete Key?");
            a.setContentText("Are you sure you want to delete the key: " + keyCombo.getSelectionModel().getSelectedItem() + "?\nThis CANNOT be undone.");
            Optional<ButtonType> result = a.showAndWait();
			if (result.get() == ButtonType.OK) {
				String keyName = keyCombo.getSelectionModel().getSelectedItem().toString();
				File file = new File("keys/" + keyName);
				if (file.delete()) {
                    controller.writeLog("Successfully deleted key: " + keyName);
					keyCombo.getItems().remove(keyCombo.getSelectionModel().getSelectedItem());
				} else {
                    controller.writeLog("Unable to delete file");
				}
			}
		};
	}
	
	public static EventHandler<ActionEvent> getKeygenHandler(FXController controller, ComboBox<KeyItem> keyCombo) {
		return e -> {
            TextInputDialog d = new TextInputDialog("");
            d.setTitle("New Key");
            d.setHeaderText("Please enter a name for your new key.");
            d.setContentText("Key Name:");
            Optional<String> keyName = d.showAndWait();
			if (keyName.isPresent()) {
				SecretKey newKey = AESCTR.generateKey();

				// Serialize (out)
				try {
					File file = new File("keys/" + keyName.get());
					ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file.getAbsolutePath()));
					out.writeObject(newKey);
					out.close();

					// Set key in AES
					AESCTR.secretKey = newKey;

					KeyItem keyItem = new KeyItem(newKey, file.getName());
					keyCombo.getItems().add(keyItem);
					keyCombo.getSelectionModel().select(keyItem);
                    controller.writeLog("Loaded new key: " + file.getName());
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		};
	}
}
