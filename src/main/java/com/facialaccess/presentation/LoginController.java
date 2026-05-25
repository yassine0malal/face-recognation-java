package com.facialaccess.presentation;

import com.facialaccess.service.SecurityService;
import com.facialaccess.util.NavigationUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Contrôleur pour l'écran de connexion administrateur.
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

        visiblePasswordField = new TextField();
        visiblePasswordField.setStyle(passwordField.getStyle());
        visiblePasswordField.setPromptText(passwordField.getPromptText());
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        usernameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            NavigationUtil.navigateToWelcome(stage);
        } catch (Exception e) {
            System.err.println("Erreur navigation: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        hideError();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and access key");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("Authenticating...");

        new Thread(() -> {
            try {
                boolean authenticated = securityService.authenticate(username, password);

                Platform.runLater(() -> {
                    if (authenticated) {
                        navigateToDashboard();
                    } else {
                        int remaining = securityService.getRemainingAttempts(username);
                        if (remaining > 0) {
                            showError("Invalid credentials. " + remaining + " attempts remaining.");
                        } else {
                            showError("Account locked. Contact system administrator.");
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
            eyeIcon.setIconLiteral("mdi2e-eye-off-outline");
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
            Stage stage = (Stage) loginButton.getScene().getWindow();
            NavigationUtil.navigateToDashboard(stage);
        } catch (Exception e) {
            showError("Failed to load dashboard: " + e.getMessage());
            loginButton.setDisable(false);
            loginButton.setText("Initialize Session");
        }
    }
}
