package edu.ucsb.hopefully_unhackable.client;

import edu.ucsb.hopefully_unhackable.crypto.AESCTR;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javax.crypto.SecretKey;
import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class FXController implements Initializable {
    public static File[] selectedFiles;

    @FXML
    private TextField pathInput;
    @FXML
    private Button browseBtn;
    @FXML
    private Button uploadBtn;
    @FXML
    private TextArea logText;
    @FXML
    private ProgressBar uploadBar;

    @FXML
    private TextField searchInput;
    @FXML
    private Button searchBtn;
    @FXML
    private Button downloadBtn;
    @FXML
    private Slider matchSlider;
    @FXML
    private ListView resultList;
    @FXML
    private ProgressBar downloadBar;

    @FXML
    private ComboBox<KeyItem> keyCombo;
    @FXML
    private Button removeBtn;
    @FXML
    private Button newBtn;
    @FXML
    private CheckBox stemBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load all keys
        File folder = new File("keys");
        folder.mkdirs();
        File[] files = folder.listFiles();
        boolean hasDefaultKey = false;
        for (File file : files) {
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(file.getAbsolutePath()));
                SecretKey kS = (SecretKey) in.readObject(); // Set secretKey
                in.close();
                keyCombo.getItems().add(new KeyItem(kS, file.getName()));
                if (file.getName().equals("defaultkey")) {
                    hasDefaultKey = true;
                }
            } catch (IOException | ClassNotFoundException ex) {
                // Not a key file, don't add to list
            }
        }

        // Load default key
        if (hasDefaultKey) {
            keyCombo.getSelectionModel().select(new KeyItem(null, "defaultkey"));
            AESCTR.secretKey = keyCombo.getSelectionModel().getSelectedItem().getKey();
            System.out.println("Successfully loaded key: defaultkey");
        } else {
            System.out.println("No default key found, generating new one");
            File file = new File("keys/defaultkey");
            SecretKey newKey = AESCTR.generateKey();
            // Serialize (out)
            try {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file.getAbsolutePath()));
                out.writeObject(newKey);
                out.close();

                AESCTR.secretKey = newKey; // Set secretKey

                KeyItem keyItem = new KeyItem(newKey, file.getName());
                keyCombo.getItems().add(keyItem);
                keyCombo.getSelectionModel().select(keyItem);
            } catch (IOException ex2) {
                System.out.println("Failed to generate a default key");
                ex2.printStackTrace();
            }
        }

        browseBtn.setOnAction(UploadHandlers.getBrowseHandler(this, pathInput));
        uploadBtn.setOnAction(UploadHandlers.getUploadHandler(this, uploadBar, stemBox));

        searchBtn.setOnAction(SearchHandlers.getSearchHandler(searchInput, resultList, matchSlider, stemBox));
        matchSlider.valueProperty().addListener(SearchHandlers.getMatchHandler(resultList));
        downloadBtn.setOnAction(SearchHandlers.getDownloadHandler(this, downloadBar, resultList));
        resultList.setOnMouseClicked(SearchHandlers.getListClickHandler(this, downloadBar));

        keyCombo.setOnAction(SettingsHandlers.selectKeyHandler(this));
        removeBtn.setOnAction(SettingsHandlers.removeKeyHandler(this, keyCombo));
        newBtn.setOnAction(SettingsHandlers.getKeygenHandler(this, keyCombo));
    }

    public void writeLog(String msg) {
        logText.appendText(msg + '\n');
    }
}
