package edu.ucsb.hopefully_unhackable.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ucsb.hopefully_unhackable.crypto.AESCTR;
import edu.ucsb.hopefully_unhackable.crypto.SSE;
import edu.ucsb.hopefully_unhackable.utils.FileUtils;
import edu.ucsb.hopefully_unhackable.utils.HttpUtil;
import edu.ucsb.hopefully_unhackable.utils.StringPair;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.StageStyle;

import javax.swing.*;
import java.io.File;
import java.util.*;

public class UploadHandlers {
	private static Set<String> lastUpload = new HashSet<>();
	private static List<File> selectedFiles;
	
	public static EventHandler<ActionEvent> getBrowseHandler(FXController controller, TextField filePath) {
		FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file to upload...");
        
		return e -> {
			if (AESCTR.secretKey == null) {
                new Alert(Alert.AlertType.INFORMATION, "Please generate or choose a key", ButtonType.OK).showAndWait();
				return;
			}

			selectedFiles = fileChooser.showOpenMultipleDialog(null);
			if(selectedFiles != null) {
                controller.writeLog("Selected files: ");
				StringBuilder sb = new StringBuilder(1024);

				for(int i = 0; i < selectedFiles.size(); i++) {
					if(i == selectedFiles.size() - 1) {
						sb.append(selectedFiles.get(i).getAbsolutePath());
					} else {
						sb.append(selectedFiles.get(i).getAbsolutePath() + ", ");
					}
                    controller.writeLog(selectedFiles.get(i).getName());
				}
				filePath.setText(sb.toString());
			}
		};
	}
	
	public static EventHandler<ActionEvent> getUploadHandler(FXController controller, ProgressBar progressBar, CheckBox stem) {
		return e -> {
            Alert a;
			if(selectedFiles != null) {
                controller.selectedFiles = new File[selectedFiles.size()];
				for(int i = 0; i < selectedFiles.size(); i++) {
					String filepath = selectedFiles.get(i).getAbsolutePath();
					if(lastUpload.contains(filepath)) {
                        a = new Alert(Alert.AlertType.CONFIRMATION);
                        a.setTitle("Are you sure?");
                        a.setContentText("You just uploaded " + filepath + ", are you sure you want to upload it again?");
                        Optional<ButtonType> result = a.showAndWait();
						if (result.get() != ButtonType.OK) {
							return;
						}
					}
					if(selectedFiles.get(i).isAbsolute() && selectedFiles.get(i).exists()) {
                        controller.selectedFiles[i] = selectedFiles.get(i);
					} else {
                        a = new Alert(Alert.AlertType.INFORMATION, "Invalid path to file.", ButtonType.OK);
                        a.setTitle("Information");
                        a.showAndWait();
						return;
					}
				}
			} else {
                a = new Alert(Alert.AlertType.INFORMATION, "Please select a file.", ButtonType.OK);
                a.initStyle(StageStyle.UTILITY);
                a.setTitle("Information");
                a.showAndWait();
				return;
			}

			progressBar.setProgress(0);
            controller.writeLog("Uploading file(s)...");
			Button source = (Button) e.getSource();
			source.setDisable(true);

			SwingWorker<Boolean, Double> worker = new SwingWorker<Boolean, Double>() {
				@Override
				protected Boolean doInBackground() throws Exception {
					String[] key = new String[FXController.selectedFiles.length];
					publish(0.05);

					for (int i = 0; i < FXController.selectedFiles.length; i++) {
						key[i] = UUID.randomUUID().toString();
						FileUtils.uploadFile(FXController.selectedFiles[i], key[i], AESCTR.secretKey);
					}
					publish(0.4);

					Map<String, ArrayList<StringPair>> map = SSE.EDBSetup(FXController.selectedFiles, AESCTR.secretKey, key, stem.isSelected());
					publish(0.6);

					ObjectMapper mapper = new ObjectMapper();
					try {
						String json = mapper.writeValueAsString(map);
						publish(0.8);
						HttpUtil.HttpPost(json);
					} catch (JsonProcessingException e1) {
						e1.printStackTrace();
						return false;
					}


					publish(1.0);
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
                                    controller.writeLog("Upload successful!");
                                    a = new Alert(Alert.AlertType.INFORMATION, "Upload successful!", ButtonType.OK);
                                    a.setTitle("Success!");
                                    a.showAndWait();
                                    lastUpload.clear();
                                    for(File f : FXController.selectedFiles) {
                                        lastUpload.add(f.getAbsolutePath());
                                    }
                                } else {
                                    controller.writeLog("Upload failed!");
                                    a = new Alert(Alert.AlertType.ERROR, "Upload failed!", ButtonType.OK);
                                    a.setTitle("Error!");
                                    a.showAndWait();
                                }
                            } catch (Exception ex) {
                                a = new Alert(Alert.AlertType.ERROR, "Upload error!", ButtonType.OK);
                                a.setTitle("Error!");
                                a.showAndWait();
                                ex.printStackTrace();
                            }
                            source.setDisable(false);
                        }
                    });
				}

				@Override
				protected void process(List<Double> n) {
					progressBar.setProgress(n.get(n.size() - 1));
				}
			};

			worker.execute();
		};
	}
}
