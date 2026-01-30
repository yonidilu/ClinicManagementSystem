package com.clinic;

import com.clinic.model.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/choice-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Clinic Management System");
        stage.setScene(scene);

        // ADD THIS LINE HERE
        stage.setMaximized(true);

        stage.show();
    }

    public static void main(String[] args) {
        // 1. THIS IS THE KEY: Run the setup before the window opens
        DatabaseManager.initializeDatabase();

        launch(args);
    }
}