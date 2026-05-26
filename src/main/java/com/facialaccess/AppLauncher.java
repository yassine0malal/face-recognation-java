package com.facialaccess;

import com.facialaccess.dao.DatabaseManager;
import com.facialaccess.util.NavigationUtil;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Lanceur d'application avec choix de l'écran de démarrage.
 * 
 * Usage:
 * - mvn javafx:run                           → Lance le login (par défaut)
 * - mvn javafx:run -Dstart=camera            → Lance l'écran de scan
 * - mvn javafx:run -Dstart=dashboard         → Lance le dashboard
 */
public class AppLauncher extends Application {
    
    private static final String DEFAULT_SCREEN = "login";
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialiser la base de données
            DatabaseManager.getInstance();
            
            // Récupérer l'écran de démarrage depuis les paramètres
            String startScreen = System.getProperty("start", DEFAULT_SCREEN);
            
            // Configurer la fenêtre
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(650);
            
            // Naviguer vers l'écran demandé
            switch (startScreen.toLowerCase()) {
                case "camera":
                case "scan":
                    System.out.println(" Lancement de l'écran de scan facial...");
                    NavigationUtil.navigateToCamera(primaryStage);
                    break;
                    
                case "dashboard":
                    System.out.println(" Lancement du dashboard...");
                    NavigationUtil.navigateToDashboard(primaryStage);
                    break;
                    
                case "users":
                case "user-management":
                    System.out.println(" Lancement de la gestion des utilisateurs...");
                    NavigationUtil.navigateToUserManagement(primaryStage);
                    break;
                    
                case "logs":
                case "access-log":
                    System.out.println(" Lancement des logs d'accès...");
                    NavigationUtil.navigateToAccessLog(primaryStage);
                    break;
                    
                case "login":
                default:
                    System.out.println(" Lancement de l'écran de connexion...");
                    NavigationUtil.navigateToLogin(primaryStage);
                    break;
            }
            
            primaryStage.show();
            
        } catch (Exception e) {
            System.err.println(" Erreur lors du chargement de l'application: " + e.getMessage());
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
