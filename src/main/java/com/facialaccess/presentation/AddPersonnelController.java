package com.facialaccess.presentation;

import com.facialaccess.dao.UtilisateurDAO;
import com.facialaccess.model.Utilisateur;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class AddPersonnelController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private CheckBox activeCheckBox;

    @FXML
    private Label errorLabel;

    private Stage dialogStage;
    private boolean saved = false;
    private UtilisateurDAO utilisateurDAO;

    @FXML
    public void initialize() {
        utilisateurDAO = new UtilisateurDAO();
        roleComboBox.setItems(FXCollections.observableArrayList(
                "Level 1 • Visitor",
                "Level 2 • Staff",
                "Level 3 • Security",
                "Level 4 • Senior",
                "Level 5 • Critical Admin"
        ));
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave(ActionEvent event) {
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
            showError("Please select a Clearance Level.");
            return;
        }

        // Default empty face/qr for now
        byte[] emptyFace = new byte[0];
        String emptyQr = "";

        Utilisateur newUser = new Utilisateur(
                null, name, email, LocalDateTime.now(), isActive, role, emptyFace, emptyQr
        );

        boolean success = utilisateurDAO.addUtilisateur(newUser);
        
        if (success) {
            saved = true;
            dialogStage.close();
        } else {
            showError("Failed to save to database.");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        dialogStage.close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
