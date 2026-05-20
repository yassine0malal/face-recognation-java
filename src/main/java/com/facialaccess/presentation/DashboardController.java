package com.facialaccess.presentation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class DashboardController {

    @FXML
    private StackPane mainContentArea; // Tied directly to the fx:id="mainContentArea" in dashboard.fxml

    @FXML
    public void initialize() {
        // Automatically load your card overview view into the window on application launch
        navigateTo("/fxml/user-management.fxml");
    }

    @FXML
    private void handleShowLogsView() {
        navigateTo("/fxml/access-log.fxml");
    }

    @FXML
    private void handleShowCameraView() {
        navigateTo("/fxml/camera.fxml");
    }

    @FXML
    private void handleShowUserManagementView() {
        navigateTo("/fxml/user-management.fxml");
    }

    /**
     * Helper routine to clean the display area and safely load a sub-view context.
     */
    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            mainContentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Critical error compiling UI sub-resource scene mapping: " + fxmlPath);
            e.printStackTrace();
        }
    }
}