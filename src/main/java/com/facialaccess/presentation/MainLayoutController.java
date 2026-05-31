package com.facialaccess.presentation;

import com.facialaccess.model.Admin;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class MainLayoutController {

    @FXML
    private StackPane mainContentArea;

    // Header user info
    @FXML
    private Label headerUserName;
    // @FXML
    // private Label headerUserRole;

    // Sidebar footer user info
    @FXML
    private Label sidebarOperatorName;
    @FXML
    private Label sidebarStatusLabel;
    @FXML
    private Circle sidebarStatusDot;

    // Logout button
    @FXML
    private Button logoutButton;

    private Stage primaryStage;
    private Admin currentAdmin;

    @FXML
    public void initialize() {
        // Load default view
        navigateTo("/fxml/dashboard_view.fxml");
    }

    /**
     * Pass the logged‑in admin and update the UI.
     */
    public void setCurrentAdmin(Admin admin) {
        this.currentAdmin = admin;
        updateUserInterface();
    }

    /**
     * Store the stage so we can change scenes on logout.
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    private void updateUserInterface() {
        if (currentAdmin != null) {
            // Header
            // if (headerUserName != null) {
            //     headerUserName.setText(currentAdmin.getFullName());
            // }
            // if (headerUserRole != null) {
                // headerUserRole.setText(currentAdmin.getUsername() != null ? currentAdmin.getUsername() : "Admin");
            // }

            // Sidebar footer
            if (headerUserName != null) {
                sidebarOperatorName.setText(headerUserName != null ? currentAdmin.getFullName() : "Operator");
            }
            if (sidebarStatusLabel != null && sidebarStatusDot != null) {
                boolean active = currentAdmin.isActive();
                sidebarStatusLabel.setText(active ? "SYSTEM SECURE" : "SYSTEM LOCKED");
                sidebarStatusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (active ? "#10B981" : "#DC2626") + "; -fx-font-weight: 700;");
                sidebarStatusDot.setFill(active ? Color.web("#10B981") : Color.web("#DC2626"));
            }
        }
    }

    // ======================== LOGOUT (FIXED) ========================
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Attempt to load welcome_view.fxml first
            URL fxmlUrl = getClass().getResource("/fxml/welcome.fxml");

            // If not found, try the admin login view (common fallback)
            if (fxmlUrl == null) {
                System.err.println("welcome_view.fxml not found, trying login_admin_view.fxml...");
                fxmlUrl = getClass().getResource("/fxml/login_admin_view.fxml");
            }

            // If still not found, exit
            if (fxmlUrl == null) {
                System.err.println("No valid view found for logout. Closing application.");
                javafx.application.Platform.exit();
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage;
            if (primaryStage != null) {
                stage = primaryStage;
            } else {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            }
            stage.setScene(new Scene(root));
            stage.setTitle("VigilantCore");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ======================== NAVIGATION (unchanged) ========================
    @FXML
    private void handleShowDashboardView(ActionEvent event) {
        updateActiveNavigationState(event);
        navigateTo("/fxml/dashboard_view.fxml");
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

    @FXML
    private void handleShowSettingsView(ActionEvent event) {
        updateActiveNavigationState(event);
        navigateTo("/fxml/settings_view.fxml");
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                System.err.println("FXML not found: " + fxmlPath);
                return;
            }
            Parent view = loader.load();
            mainContentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Error rendering view: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void updateActiveNavigationState(ActionEvent event) {
        Object source = event.getSource();
        if (source instanceof Button) {
            Button clickedButton = (Button) source;
            Parent sidebar = clickedButton.getParent();
            if (sidebar instanceof VBox) {
                for (Node node : ((VBox) sidebar).getChildrenUnmodifiable()) {
                    if (node instanceof Button) {
                        node.getStyleClass().remove("sidebar-button-active");
                        if (!node.getStyleClass().contains("sidebar-button")) {
                            node.getStyleClass().add("sidebar-button");
                        }
                        if (((Button) node).getGraphic() instanceof javafx.scene.shape.SVGPath svg) {
                            svg.setFill(Color.web("#6B7084"));
                        }
                    }
                }
                clickedButton.getStyleClass().remove("sidebar-button");
                clickedButton.getStyleClass().add("sidebar-button-active");
                if (clickedButton.getGraphic() instanceof javafx.scene.shape.SVGPath svg) {
                    svg.setFill(Color.WHITE);
                }
            }
        }
    }
}