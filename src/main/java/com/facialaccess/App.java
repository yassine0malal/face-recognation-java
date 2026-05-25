package com.facialaccess;

import com.facialaccess.dao.DatabaseManager;
import com.facialaccess.util.NavigationUtil;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Point d'entrée principal de l'application Facial Access System.
 * Lance l'interface JavaFX.
 */
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
        // Fermer la connexion à la base de données
        DatabaseManager.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
