package edu.ucsb.hopefully_unhackable.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class FXClientWindow extends Application {
    private static final double WIDTH = 543 + 20;
    private static final double HEIGHT = 355 + 37;

    @Override
    public void start(Stage stage) {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(getClass().getResource("gui.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, WIDTH, HEIGHT);
            stage.setWidth(WIDTH);
            stage.setHeight(HEIGHT);
            stage.setTitle("Grum Guard - Secure File Client");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
