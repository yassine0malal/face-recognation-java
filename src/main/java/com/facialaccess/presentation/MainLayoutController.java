package com.facialaccess.presentation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class MainLayoutController {

    @FXML
    private StackPane mainContentArea;

    @FXML
    public void initialize() {
        // Loads the default initial view into the center area
        navigateTo("/fxml/dashboard.fxml");
    }

    @FXML
    private void handleShowDashboardView(ActionEvent event) {
        updateActiveNavigationState(event);
        navigateTo("/fxml/dashboard.fxml");
    }

    @FXML
    private void handleShowLogsView(ActionEvent event) {
        updateActiveNavigationState(event);
        navigateTo("/fxml/audit_logs_view.fxml");
    }

    @FXML
    private void handleShowCameraView(ActionEvent event) {
        updateActiveNavigationState(event);
        navigateTo("/fxml/biometric_kiosk.fxml");
    }

    @FXML
    private void handleShowUserManagementView(ActionEvent event) {
        updateActiveNavigationState(event);
        navigateTo("/fxml/personnel_directory_view.fxml");
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            mainContentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Error rendering view scene frame mapping: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void updateActiveNavigationState(ActionEvent event) {
        Object source = event.getSource();
        if (source instanceof Button) {
            Button clickedButton = (Button) source;
            Parent sidebar = clickedButton.getParent();
            if (sidebar instanceof VBox) {
                for (Node node : sidebar.getChildrenUnmodifiable()) {
                    if (node instanceof Button) {
                        node.getStyleClass().remove("sidebar-button-active");
                        if (!node.getStyleClass().contains("sidebar-button")) {
                            node.getStyleClass().add("sidebar-button");
                        }
                        // Reset icon to gray
                        if (((Button) node).getGraphic() instanceof javafx.scene.shape.SVGPath svg) {
                            svg.setFill(javafx.scene.paint.Color.web("#6B7084"));
                        }
                    }
                }
                clickedButton.getStyleClass().remove("sidebar-button");
                clickedButton.getStyleClass().add("sidebar-button-active");
                // Set active icon to white
                if (clickedButton.getGraphic() instanceof javafx.scene.shape.SVGPath svg) {
                    svg.setFill(javafx.scene.paint.Color.WHITE);
                }
            }
        }
    }
}
