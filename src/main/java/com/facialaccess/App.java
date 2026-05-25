package com.facialaccess;

import com.facialaccess.dao.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Safe Database Init
            DatabaseManager.getInstance();
            
            // Load the initial interface (e.g., login.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard_parent.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 800, 600);
            
            // Link the global modern design stylesheet directly from resources
            String cssPath = getClass().getResource("/css/global.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            
            primaryStage.setTitle("VigilantCore - Security Portal");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (Exception e) {
            System.err.println("Failed to launch main viewport interface framework context.");
            e.printStackTrace();
        }
    }
    
    @Override
    public void stop() {
        try {
            if (DatabaseManager.getInstance() != null) {
                DatabaseManager.getInstance().close();
            }
        } catch (Exception e) {
            System.err.println("Error gracefully disconnecting database architecture: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}