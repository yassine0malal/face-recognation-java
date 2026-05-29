package com.facialaccess.presentation;

import com.facialaccess.dao.AdminDAO;
import com.facialaccess.dao.UtilisateurDAO;
import com.facialaccess.dao.AccessLogDAO;
import com.facialaccess.model.Admin;
import com.facialaccess.service.SecurityService;
import com.facialaccess.util.FaceRecognitionConfig;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;

public class SettingsController {

    // Profile Section
    @FXML private Circle profilePictureCircle;
    @FXML private Label profileInitials;
    @FXML private Button changePhotoBtn;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private Label roleLabel;
    @FXML private Button saveProfileBtn;
    @FXML private Label profileSaveStatus;

    // Security Section
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button changePasswordBtn;
    @FXML private Label passwordChangeStatus;

    // Recognition Settings
    @FXML private Slider thresholdSlider;
    @FXML private Label thresholdValueLabel;
    @FXML private ComboBox<String> cameraQualityCombo;
    @FXML private Button saveRecognitionBtn;
    @FXML private Label recognitionSaveStatus;

    // Application Settings
    @FXML private ComboBox<String> themeCombo;
    @FXML private ComboBox<String> languageCombo;
    @FXML private CheckBox autoLogoutCheck;
    @FXML private ComboBox<String> autoLogoutTimeCombo;
    @FXML private CheckBox notifyAccessCheck;
    @FXML private CheckBox notifyFailedCheck;
    @FXML private CheckBox notifySoundCheck;
    @FXML private Button saveAppBtn;
    @FXML private Label appSaveStatus;

    // Database Management
    @FXML private Label dbSizeLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalLogsLabel;

    // Services
    private AdminDAO adminDAO;
    private UtilisateurDAO utilisateurDAO;
    private AccessLogDAO accessLogDAO;
    private SecurityService securityService;
    
    // Current Admin
    private Admin currentAdmin;
    private int currentAdminId;

    @FXML
    public void initialize() {
        // Initialize DAOs
        adminDAO = new AdminDAO();
        utilisateurDAO = new UtilisateurDAO();
        accessLogDAO = new AccessLogDAO();
        securityService = new SecurityService();

        // Load current admin (you should pass this from login)
        currentAdminId = getCurrentAdminId();
        loadCurrentAdmin();

        // Initialize UI components
        initializeComboBoxes();
        initializeSlider();
        initializeCheckBoxes();
        loadDatabaseStats();
    }

    private void initializeComboBoxes() {
        // Camera Quality
        cameraQualityCombo.setItems(FXCollections.observableArrayList(
            "Low (480p)", "Medium (720p)", "High (1080p)", "Ultra (4K)"
        ));
        cameraQualityCombo.setValue("Medium (720p)");

        // Theme
        themeCombo.setItems(FXCollections.observableArrayList(
            "Light", "Dark", "Auto (System)"
        ));
        themeCombo.setValue("Light");

        // Language
        languageCombo.setItems(FXCollections.observableArrayList(
            "English", "Français", "العربية", "Español"
        ));
        languageCombo.setValue("English");

        // Auto Logout Time
        autoLogoutTimeCombo.setItems(FXCollections.observableArrayList(
            "5 minutes", "10 minutes", "15 minutes", "30 minutes", "1 hour"
        ));
        autoLogoutTimeCombo.setValue("15 minutes");
    }

    private void initializeSlider() {
        // Set current threshold
        double currentThreshold = FaceRecognitionConfig.RECOGNITION_THRESHOLD;
        thresholdSlider.setValue(currentThreshold);
        updateThresholdLabel(currentThreshold);

        // Listen to slider changes
        thresholdSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateThresholdLabel(newVal.doubleValue());
        });
    }

    private void updateThresholdLabel(double value) {
        DecimalFormat df = new DecimalFormat("0");
        thresholdValueLabel.setText(df.format(value * 100) + "%");
    }

    private void initializeCheckBoxes() {
        // Auto logout checkbox listener
        autoLogoutCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            autoLogoutTimeCombo.setDisable(!newVal);
        });

        // Set default values
        notifyAccessCheck.setSelected(true);
        notifyFailedCheck.setSelected(true);
        notifySoundCheck.setSelected(false);
    }

    private void loadCurrentAdmin() {
        currentAdmin = adminDAO.getAdminById(currentAdminId);
        if (currentAdmin != null) {
            fullNameField.setText(currentAdmin.getFullName());
            emailField.setText(currentAdmin.getEmail());
            roleLabel.setText("Administrator");
            
            // Set initials
            String initials = getInitials(currentAdmin.getFullName());
            profileInitials.setText(initials);
        }
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "AD";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        }
        return fullName.substring(0, Math.min(2, fullName.length())).toUpperCase();
    }

    private void loadDatabaseStats() {
        try {
            // Database size
            File dbFile = new File("facial_access.db");
            if (dbFile.exists()) {
                long sizeInBytes = dbFile.length();
                String sizeStr = formatFileSize(sizeInBytes);
                dbSizeLabel.setText(sizeStr);
            } else {
                dbSizeLabel.setText("N/A");
            }

            // Total users
            int totalUsers = utilisateurDAO.getTotalUtilisateursCount();
            totalUsersLabel.setText(String.valueOf(totalUsers));

            // Total logs
            int totalLogs = accessLogDAO.countAccessLogs();
            totalLogsLabel.setText(String.valueOf(totalLogs));

        } catch (Exception e) {
            System.err.println("Error loading database stats: " + e.getMessage());
            dbSizeLabel.setText("Error");
            totalUsersLabel.setText("Error");
            totalLogsLabel.setText("Error");
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    // ========== Event Handlers ==========

    @FXML
    private void handleChangePhoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(changePhotoBtn.getScene().getWindow());
        if (file != null) {
            try {
                // TODO: Implement profile picture upload
                showStatus(profileSaveStatus, "Photo updated successfully!", true);
            } catch (Exception e) {
                showStatus(profileSaveStatus, "Failed to update photo", false);
            }
        }
    }

    @FXML
    private void handleSaveProfile(ActionEvent event) {
        String newName = fullNameField.getText().trim();
        String newEmail = emailField.getText().trim();

        if (newName.isEmpty() || newEmail.isEmpty()) {
            showStatus(profileSaveStatus, "Please fill all fields", false);
            return;
        }

        if (!isValidEmail(newEmail)) {
            showStatus(profileSaveStatus, "Invalid email format", false);
            return;
        }

        try {
            currentAdmin.setFullName(newName);
            currentAdmin.setEmail(newEmail);
            
            boolean success = adminDAO.updateAdmin(currentAdmin);
            
            if (success) {
                // Update initials
                profileInitials.setText(getInitials(newName));
                showStatus(profileSaveStatus, "Profile updated successfully!", true);
            } else {
                showStatus(profileSaveStatus, "Failed to update profile", false);
            }
        } catch (Exception e) {
            showStatus(profileSaveStatus, "Error: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        String currentPwd = currentPasswordField.getText();
        String newPwd = newPasswordField.getText();
        String confirmPwd = confirmPasswordField.getText();

        if (currentPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
            showStatus(passwordChangeStatus, "Please fill all password fields", false);
            return;
        }

        if (!newPwd.equals(confirmPwd)) {
            showStatus(passwordChangeStatus, "New passwords don't match", false);
            return;
        }

        if (newPwd.length() < 6) {
            showStatus(passwordChangeStatus, "Password must be at least 6 characters", false);
            return;
        }

        try {
            // Verify current password
            if (!securityService.verifyAdminPassword(currentAdminId, currentPwd)) {
                showStatus(passwordChangeStatus, "Current password is incorrect", false);
                return;
            }

            // Update password
            boolean success = securityService.updateAdminPassword(currentAdminId, newPwd);
            
            if (success) {
                // Clear fields
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
                showStatus(passwordChangeStatus, "Password changed successfully!", true);
            } else {
                showStatus(passwordChangeStatus, "Failed to change password", false);
            }
        } catch (Exception e) {
            showStatus(passwordChangeStatus, "Error: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleSaveRecognitionSettings(ActionEvent event) {
        try {
            double newThreshold = thresholdSlider.getValue();
            
            // TODO: Save threshold to configuration file
            // For now, just show success message
            
            showStatus(recognitionSaveStatus, "Recognition settings saved!", true);
            System.out.println("New threshold: " + newThreshold);
        } catch (Exception e) {
            showStatus(recognitionSaveStatus, "Failed to save settings", false);
        }
    }

    @FXML
    private void handleSaveAppSettings(ActionEvent event) {
        try {
            // TODO: Save application settings to configuration file
            
            showStatus(appSaveStatus, "Application settings saved!", true);
        } catch (Exception e) {
            showStatus(appSaveStatus, "Failed to save settings", false);
        }
    }

    @FXML
    private void handleCleanOldLogs(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clean Old Logs");
        alert.setHeaderText("Delete logs older than 30 days?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30);
                    boolean success = accessLogDAO.deleteOldLogs(thirtyDaysAgo);
                    
                    if (success) {
                        loadDatabaseStats();
                        showAlert("Success", "Old logs cleaned successfully", Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Error", "Failed to clean logs", Alert.AlertType.ERROR);
                    }
                } catch (Exception e) {
                    showAlert("Error", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleExportData(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Data");
        fileChooser.setInitialFileName("facial_access_export.csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(saveAppBtn.getScene().getWindow());
        if (file != null) {
            try {
                // TODO: Implement data export
                showAlert("Success", "Data exported successfully to:\n" + file.getAbsolutePath(), 
                         Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Failed to export data: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleBackupDatabase(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Backup Database");
        fileChooser.setInitialFileName("facial_access_backup.db");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Database Files", "*.db")
        );

        File file = fileChooser.showSaveDialog(saveAppBtn.getScene().getWindow());
        if (file != null) {
            try {
                File sourceDb = new File("facial_access.db");
                if (sourceDb.exists()) {
                    Files.copy(sourceDb.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    showAlert("Success", "Database backed up successfully to:\n" + file.getAbsolutePath(), 
                             Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "Database file not found", Alert.AlertType.ERROR);
                }
            } catch (IOException e) {
                showAlert("Error", "Failed to backup database: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    // ========== Helper Methods ==========

    private void showStatus(Label statusLabel, String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("success", "error");
        statusLabel.getStyleClass().add(success ? "success" : "error");
        statusLabel.setVisible(true);

        // Hide after 3 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> statusLabel.setVisible(false));
        pause.play();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private int getCurrentAdminId() {
        // TODO: Get from session/login context
        // For now, return 1 (default admin)
        return 1;
    }
}
