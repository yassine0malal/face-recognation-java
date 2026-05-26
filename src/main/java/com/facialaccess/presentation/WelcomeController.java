package com.facialaccess.presentation;

import com.facialaccess.util.NavigationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Contrôleur pour l'écran d'accueil.
 * Permet de choisir entre le scan facial public et la connexion admin.
 */
public class WelcomeController {
    
    @FXML private Button scanButton;
    @FXML private Button adminButton;
    @FXML private Button qrButton;
    
    @FXML
    public void initialize() {
        System.out.println("✓ Welcome screen initialized");
    }
    
    /**
     * Gère le clic sur "Face Scan Access".
     * Redirige vers l'écran de scan facial.
     */
    @FXML
    private void handleFaceScan() {
        try {
            System.out.println("🎥 Navigation vers le scan facial...");
            Stage stage = (Stage) scanButton.getScene().getWindow();
            NavigationUtil.navigateToCamera(stage);
        } catch (IOException e) {
            System.err.println("❌ Erreur de navigation vers le scan: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gère le clic sur "Admin Access".
     * Redirige vers l'écran de connexion administrateur.
     */
    @FXML
    private void handleAdminLogin() {
        try {
            System.out.println("🔐 Navigation vers le login admin...");
            Stage stage = (Stage) adminButton.getScene().getWindow();
            NavigationUtil.navigateToLogin(stage);
        } catch (IOException e) {
            System.err.println("❌ Erreur de navigation vers le login: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gère le clic sur "QR Code Access" (optionnel).
     * Redirige vers l'écran de scan QR.
     */
    @FXML
    private void handleQRScan() {
        try {
            System.out.println("📱 Navigation vers le scan QR...");
            // TODO: Implémenter l'écran de scan QR
            Stage stage = (Stage) qrButton.getScene().getWindow();
            // NavigationUtil.navigateToQRScan(stage);
        } catch (Exception e) {
            System.err.println("❌ Erreur de navigation vers le QR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gère le clic sur le bouton de fermeture.
     */
    @FXML
    private void handleClose() {
        System.out.println("👋 Fermeture de l'application...");
        Stage stage = (Stage) scanButton.getScene().getWindow();
        stage.close();
    }
}
