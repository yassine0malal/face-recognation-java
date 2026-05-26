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

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;

import org.bytedeco.opencv.opencv_core.Mat;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_COLOR;

public class EditPersonnelController {

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

    private Utilisateur currentUser;
    private byte[] originalImageData;
    private boolean imageChanged = false;

    @FXML
    public void initialize() {
        utilisateurDAO = new UtilisateurDAO();
        featureExtractor = new FeatureExtractor();
        roleComboBox.setItems(FXCollections.observableArrayList("admin", "employe"));
    }

    public void setParentController(PersonnelDirectoryController parentController) {
        this.parentController = parentController;
    }

    /**
     * Pre-fill the form with an existing user's data.
     * Must be called before showing the form.
     */
    public void loadUserData(Utilisateur user) {
        this.currentUser = user;
        nameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        roleComboBox.setValue(user.getRole() != null ? user.getRole() : "employe");
        activeCheckBox.setSelected(user.isActive());

        if (user.hasFaceImage()) {
            originalImageData = user.getFaceImage();
            try {
                Image img = new Image(new ByteArrayInputStream(originalImageData));
                selectedImageView.setImage(img);
                imagePlaceholder.setVisible(false);
                removeImageBtn.setVisible(true);
                removeImageBtn.setManaged(true);
            } catch (Exception e) {
                showError("Failed to load existing image.");
            }
        } else {
            originalImageData = null;
            imagePlaceholder.setVisible(true);
            selectedImageView.setImage(null);
            removeImageBtn.setVisible(false);
            removeImageBtn.setManaged(false);
        }

        imageChanged = false;
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
            imageChanged = true;
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
        imageChanged = true;
    }

    @FXML
    public void handleUpdateEntry(ActionEvent event) {
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

        if (imageChanged) {
            if (selectedImageFile != null && selectedImageFile.exists()) {
                try {
                    imageData = Files.readAllBytes(selectedImageFile.toPath());
                    faceImage = imread(selectedImageFile.getAbsolutePath(), IMREAD_COLOR);
                    if (faceImage != null && !faceImage.empty()) {
                        faceVector = featureExtractor.extractFeatures(faceImage);
                        if (faceVector == null) {
                            showError("Could not extract facial features. Please select a clearer photo.");
                            return;
                        }
                    } else {
                        showError("OpenCV could not read the image file.");
                        return;
                    }
                } catch (IOException e) {
                    showError("Failed to read image file: " + e.getMessage());
                    return;
                } finally {
                    if (faceImage != null) faceImage.release();
                }
            } else {
                // image explicitly removed
                imageData = null;
                faceVector = null;
            }
        } else {
            // no change, keep original
            imageData = originalImageData;
            faceVector = currentUser.getFaceVector();
        }

        currentUser.setFullName(name);
        currentUser.setEmail(email);
        currentUser.setRole(role);
        currentUser.setActive(isActive);
        currentUser.setFaceImage(imageData);
        currentUser.setFaceVector(faceVector);
        // qr_code_data left untouched

        boolean success = utilisateurDAO.updateUtilisateur(currentUser);
        if (success) {
            clearForm();
            if (parentController != null) {
                parentController.hideEditEntryForm();
                parentController.loadData();
            }
        } else {
            showError("Failed to update user. Email might already exist.");
        }
    }

    public void clearForm() {
        nameField.clear();
        emailField.clear();
        roleComboBox.setValue(null);
        activeCheckBox.setSelected(true);
        selectedImageFile = null;
        selectedImageView.setImage(null);
        imagePlaceholder.setVisible(true);
        removeImageBtn.setVisible(false);
        removeImageBtn.setManaged(false);
        errorLabel.setVisible(false);
        imageChanged = false;
        originalImageData = null;
        currentUser = null;
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        clearForm();
        if (parentController != null) {
            parentController.hideEditEntryForm();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}