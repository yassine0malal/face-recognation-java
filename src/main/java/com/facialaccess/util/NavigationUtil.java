package com.facialaccess.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Utilitaire pour la navigation entre les écrans.
 */
public class NavigationUtil {

    public static void navigateToWelcome(Stage stage) throws IOException {
        load(stage, "/fxml/welcome.fxml", "BioAccess", 1024, 768, true);
    }

    public static void navigateToCamera(Stage stage) throws IOException {
        load(stage, "/fxml/camera.fxml", "BioAccess - Face Scan", 1024, 768, true);
    }

    public static void navigateToLogin(Stage stage) throws IOException {
        load(stage, "/fxml/login.fxml", "Sentinel Precision - Secure Access", 1024, 768, true);
    }

    public static void navigateToDashboard(Stage stage) throws IOException {
        load(stage, "/fxml/layouts/main_layout.fxml", "VigilantCore - Admin Control Center", 1280, 800, true);
    }

    public static void navigateToUserManagement(Stage stage) throws IOException {
        load(stage, "/fxml/user-management.fxml", "Sentinel Precision - Users", 1280, 800, true);
    }

    public static void navigateToAccessLog(Stage stage) throws IOException {
        load(stage, "/fxml/access-log.fxml", "Sentinel Precision - Access Logs", 1280, 800, true);
    }

    private static void load(Stage stage, String fxml, String title,
            double width, double height, boolean resizable) throws IOException {
        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxml));
        Parent root = loader.load();

        // Retirer les contraintes max de la fenêtre précédente
        stage.setMaxWidth(Double.MAX_VALUE);
        stage.setMaxHeight(Double.MAX_VALUE);

        // Use screen size to adapt layout
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        double actualWidth  = Math.min(width,  screenBounds.getWidth()  * 0.95);
        double actualHeight = Math.min(height, screenBounds.getHeight() * 0.95);

        Scene scene = new Scene(root, actualWidth, actualHeight);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.setResizable(resizable);
        stage.setMinWidth(resizable ? 900 : width);
        stage.setMinHeight(resizable ? 650 : height);
        stage.centerOnScreen();
    }
}
