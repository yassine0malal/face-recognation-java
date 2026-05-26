package com.facialaccess.presentation;

import com.facialaccess.dao.UtilisateurDAO;
import com.facialaccess.model.Utilisateur;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;
import java.util.List;

public class PersonnelDirectoryController {

    @FXML
    private VBox tableRowsContainer;

    private UtilisateurDAO utilisateurDAO;

    @FXML
    public void initialize() {
        utilisateurDAO = new UtilisateurDAO();
        loadData();
    }

    public void loadData() {
        tableRowsContainer.getChildren().clear();
        List<Utilisateur> users = utilisateurDAO.getAllUtilisateurs();
        
        if (users == null || users.isEmpty()) {
            Label emptyLabel = new Label("No personnel entries found in the database.");
            emptyLabel.setStyle("-fx-padding: 32px; -fx-text-fill: #94A3B8;");
            tableRowsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (int i = 0; i < users.size(); i++) {
            Utilisateur u = users.get(i);
            HBox row = createRow(u, i == users.size() - 1);
            tableRowsContainer.getChildren().add(row);
        }
    }

    private HBox createRow(Utilisateur u, boolean isLast) {
        HBox row = new HBox();
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.getStyleClass().add("table-row");
        if (isLast) {
            row.setStyle("-fx-border-color: transparent;");
        }

        // --- Column 1: Avatar & Name/ID (width: 250) ---
        HBox col1 = new HBox();
        col1.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        col1.setSpacing(16.0);
        col1.setPrefWidth(250.0);

        StackPane avatar = new StackPane();
        avatar.setPrefWidth(40);
        avatar.setPrefHeight(40);
        avatar.setStyle("-fx-background-color: #E5E7EB; -fx-background-radius: 8;");
        Label initials = new Label(getInitials(u.getFullName()));
        initials.setStyle("-fx-text-fill: #6B7280; -fx-font-weight: bold;");
        avatar.getChildren().add(initials);

        VBox nameBox = new VBox();
        nameBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        nameBox.setSpacing(2.0);
        Label nameLabel = new Label(u.getFullName());
        nameLabel.getStyleClass().add("row-name");
        Label idLabel = new Label("ID: VC-" + String.format("%04d", u.getId()));
        idLabel.getStyleClass().add("row-id");
        nameBox.getChildren().addAll(nameLabel, idLabel);

        col1.getChildren().addAll(avatar, nameBox);

        // --- Column 2: Clearance Level (width: 220) ---
        HBox col2 = new HBox();
        col2.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        col2.setPrefWidth(220.0);
        
        Label badge = new Label(getRoleLabel(u.getRole()));
        badge.getStyleClass().addAll("badge", getBadgeStyle(u.getRole()));
        col2.getChildren().add(badge);

        // --- Column 3: Last Access (width: 220) ---
        VBox col3 = new VBox();
        col3.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        col3.setSpacing(2.0);
        col3.setPrefWidth(220.0);
        
        // Placeholder since AccessLogs are not mapped to this table yet
        Label timeLabel = new Label("N/A");
        timeLabel.getStyleClass().add("access-time");
        Label locationLabel = new Label("No access records");
        locationLabel.getStyleClass().add("access-location");
        col3.getChildren().addAll(timeLabel, locationLabel);

        // --- Column 4: Status (HGrow ALWAYS) ---
        HBox col4 = new HBox();
        col4.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        col4.setSpacing(8.0);
        HBox.setHgrow(col4, Priority.ALWAYS);
        
        Circle statusDot = new Circle(4);
        Label statusLabel = new Label();
        
        if (u.isActive()) {
            statusDot.setFill(Color.web("#22C55E"));
            statusLabel.setText("ACTIVE");
            statusLabel.getStyleClass().add("status-text-onsite");
        } else {
            statusDot.setFill(Color.web("#DC2626"));
            statusLabel.setText("SUSPENDED");
            statusLabel.getStyleClass().add("status-text-flagged");
        }
        col4.getChildren().addAll(statusDot, statusLabel);

        // Add all columns to row
        row.getChildren().addAll(col1, col2, col3, col4);

        return row;
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private String getRoleLabel(String role) {
        if (role == null) return "UNKNOWN";
        return role.toUpperCase();
    }

    private String getBadgeStyle(String role) {
        if (role == null) return "badge-gray";
        String r = role.toLowerCase();
        if (r.contains("admin") || r.contains("critical") || r.contains("5")) return "badge-purple";
        if (r.contains("senior") || r.contains("4") || r.contains("executive")) return "badge-blue";
        if (r.contains("suspended") || r.contains("blocked")) return "badge-red";
        return "badge-gray";
    }

    @FXML
    private void handleAddEntry(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_personnel_dialog.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("New Personnel Entry");
            stage.initStyle(StageStyle.DECORATED);
            stage.setResizable(false);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.centerOnScreen();

            AddPersonnelController controller = loader.getController();
            controller.setDialogStage(stage);

            stage.showAndWait();
            
            // Refresh table after dialog closes
            if (controller.isSaved()) {
                loadData();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
