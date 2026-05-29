package com.facialaccess.presentation;

import com.facialaccess.dao.UtilisateurDAO;
import com.facialaccess.model.Utilisateur;
import com.facialaccess.vision.FeatureExtractor;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;

import org.bytedeco.opencv.opencv_core.Mat;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_COLOR;

public class AddPersonnelController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private CheckBox activeCheckBox;
    @FXML private Label errorLabel;
    @FXML private StackPane imagePreview;
    @FXML private Label imagePlaceholder;
    @FXML private ImageView selectedImageView;
    @FXML private Button removeImageBtn;

    private PersonnelDirectoryController parentController;
    private File selectedImageFile;
    private UtilisateurDAO utilisateurDAO;
    private FeatureExtractor featureExtractor;

    @FXML
    public void initialize() {
        utilisateurDAO = new UtilisateurDAO();
        featureExtractor = new FeatureExtractor();
        roleComboBox.setItems(FXCollections.observableArrayList("stagiaire","securite", "employe"));
    }

    public void setParentController(PersonnelDirectoryController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void handleBrowseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select User Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(nameField.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            try (FileInputStream inputStream = new FileInputStream(file)) {
                Image image = new Image(inputStream);
                selectedImageView.setImage(image);
                imagePlaceholder.setVisible(false);
                removeImageBtn.setVisible(true);
                removeImageBtn.setManaged(true);
            } catch (IOException e) {
                showError("Failed to load image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRemoveImage(ActionEvent event) {
        selectedImageFile = null;
        selectedImageView.setImage(null);
        imagePlaceholder.setVisible(true);
        removeImageBtn.setVisible(false);
        removeImageBtn.setManaged(false);
    }

    @FXML
    public void handleSaveEntry(ActionEvent event) {
        String name = nameField.getText();
        String email = emailField.getText();
        String role = roleComboBox.getValue();
        boolean isActive = activeCheckBox.isSelected();

        if (name == null || name.trim().isEmpty()) {
            showError("Full Name is required.");
            return;
        }
        if (email == null || email.trim().isEmpty()) {
            showError("Email Address is required.");
            return;
        }
        if (role == null || role.trim().isEmpty()) {
            showError("Please select a User Type.");
            return;
        }

        byte[] imageData = null;
        byte[] faceVector = null;
        Mat faceImage = null;

        if (selectedImageFile != null && selectedImageFile.exists()) {
            try {
                // 1. Save the raw image bytes for the UI
                imageData = Files.readAllBytes(selectedImageFile.toPath());
                
                // 2. Read image using OpenCV to generate the vector
                faceImage = imread(selectedImageFile.getAbsolutePath(), IMREAD_COLOR);
                
                if (faceImage != null && !faceImage.empty()) {
                    // Extract feature vector using FeatureExtractor
                    faceVector = featureExtractor.extractFeatures(faceImage);
                    
                    if (faceVector == null) {
                        showError("Could not extract facial features. Please select a clearer photo.");
                        return;
                    }
                    System.out.println("✓ Feature vector extracted: " + faceVector.length + " bytes");
                } else {
                    showError("OpenCV could not read the image file.");
                    return;
                }
            } catch (IOException e) {
                showError("Failed to read image file: " + e.getMessage());
                return;
            } finally {
                // CRITICAL: Always release OpenCV Mat objects to prevent C++ memory leaks!
                if (faceImage != null) {
                    faceImage.release();
                }
            }
        }

        String qrCode = ""; // Generate QR later if needed

        Utilisateur newUser = new Utilisateur(
                null, name, email, LocalDateTime.now(), isActive, role, imageData, faceVector, qrCode
        );

        boolean success = utilisateurDAO.addUtilisateur(newUser);
        
        if (success) {
            clearForm();
            if (parentController != null) {
                parentController.hideAddEntryForm();
                parentController.loadData();
            }
        } else {
            showError("Failed to save to database. Email might already exist.");
        }
    }

    public void clearForm() {
        nameField.clear();
        emailField.clear();
        roleComboBox.setValue(null);
        activeCheckBox.setSelected(true);
        handleRemoveImage(null);
        errorLabel.setVisible(false);
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        clearForm();
        if (parentController != null) {
            parentController.hideAddEntryForm();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}