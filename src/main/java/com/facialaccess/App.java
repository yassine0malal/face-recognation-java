/*  juste une comnt pour tester */
package com.facialaccess;

import com.facialaccess.dao.DatabaseManager;
import com.facialaccess.util.NavigationUtil;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            DatabaseManager.getInstance();
            NavigationUtil.navigateToWelcome(primaryStage);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'application: " + e.getMessage());
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