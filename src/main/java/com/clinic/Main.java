package com.clinic;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Change "main-view.fxml" to "login-view.fxml"
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 350, 300);
        stage.setTitle("Clinic Security - Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}