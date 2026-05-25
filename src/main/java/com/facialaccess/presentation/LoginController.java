package com.facialaccess.presentation;

import com.facialaccess.service.SecurityService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;

/**
 * Contrôleur pour l'écran de connexion administrateur.
 * Gère l'authentification et la sécurité anti-brute force.
 */
public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button togglePasswordBtn;
    @FXML private FontIcon eyeIcon;
    @FXML private Label errorLabel;
    
    private SecurityService securityService;
    private TextField visiblePasswordField;
    private boolean passwordVisible = false;
    
    @FXML
    public void initialize() {
        securityService = new SecurityService();
        
        // Create hidden text field for password visibility toggle
        visiblePasswordField = new TextField();
        visiblePasswordField.setStyle(passwordField.getStyle());
        visiblePasswordField.setPromptText(passwordField.getPromptText());
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);
        
        // Bind text properties
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        
        // Add enter key support
        usernameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin());
        
        // Add hover effect to login button
        loginButton.setOnMouseEntered(e -> 
            loginButton.setStyle(loginButton.getStyle() + "-fx-background-color: #1976d2;")
        );
        loginButton.setOnMouseExited(e -> 
            loginButton.setStyle(loginButton.getStyle().replace("-fx-background-color: #1976d2;", "-fx-background-color: #1565c0;"))
        );
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Clear previous error
        hideError();
        
        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and access key");
            return;
        }
        
        // Disable button during authentication
        loginButton.setDisable(true);
        loginButton.setText("Authenticating...");
        
        // Perform authentication in background
        new Thread(() -> {
            try {
                boolean authenticated = securityService.authenticate(username, password);
                
                Platform.runLater(() -> {
                    if (authenticated) {
                        navigateToDashboard();
                    } else {
                        int remainingAttempts = securityService.getRemainingAttempts(username);
                        if (remainingAttempts > 0) {
                            showError("Invalid credentials. " + remainingAttempts + " attempts remaining.");
                        } else {
                            showError("Account locked due to multiple failed attempts. Contact system administrator.");
                        }
                        loginButton.setDisable(false);
                        loginButton.setText("Initialize Session");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Authentication error: " + e.getMessage());
                    loginButton.setDisable(false);
                    loginButton.setText("Initialize Session");
                });
            }
        }).start();
    }
    
    @FXML
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        
        if (passwordVisible) {
            // Show password
            eyeIcon.setIconLiteral("mdi2e-eye-off-outline");
            
            // Replace password field with text field
            var parent = passwordField.getParent();
            if (parent instanceof HBox hbox) {
                int index = hbox.getChildren().indexOf(passwordField);
                passwordField.setVisible(false);
                passwordField.setManaged(false);
                
                visiblePasswordField.setVisible(true);
                visiblePasswordField.setManaged(true);
                if (!hbox.getChildren().contains(visiblePasswordField)) {
                    hbox.getChildren().add(index, visiblePasswordField);
                }
            }
        } else {
            // Hide password
            eyeIcon.setIconLiteral("mdi2e-eye-outline");
            
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            
            passwordField.setVisible(true);
            passwordField.setManaged(true);
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    
    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
    
    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Sentinel Precision - Dashboard");
            stage.centerOnScreen();
            
        } catch (IOException e) {
            showError("Failed to load dashboard: " + e.getMessage());
            loginButton.setDisable(false);
            loginButton.setText("Initialize Session");
        }
    }
}
