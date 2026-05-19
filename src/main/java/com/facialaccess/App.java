package com.facialaccess;

import com.facialaccess.data.DatabaseManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Point d'entrée principal de l'application Facial Access System.
 * Lance l'interface JavaFX.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialiser la base de données
        DatabaseManager.getInstance();
        
        primaryStage.setTitle("Facial Access System - OK");
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.show();
    }
    
    @Override
    public void stop() {
        // Fermer la connexion à la base de données
        DatabaseManager.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
