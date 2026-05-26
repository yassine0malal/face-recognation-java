
package com.facialaccess.presentation;

import com.facialaccess.dao.UtilisateurDAO;
import com.facialaccess.model.Utilisateur;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

public class PersonnelDirectoryController {

    @FXML private Button filterAll;
    @FXML private Button filterAdmins;
    @FXML private Button filterUsers;

    private String currentFilter = "ALL";

    @FXML private VBox addEntryForm;
    @FXML private VBox editEntryForm;                          // NEW
    @FXML private VBox mainTableView;
    @FXML private VBox tableRowsContainer;
    @FXML private AddPersonnelController addEntryFormController;
    @FXML private EditPersonnelController editEntryFormController;  // NEW

    @FXML private Label footerPaginationLabel;
    @FXML private Label lblCurrentPage;
    @FXML private Button btnPrevPage;
    @FXML private Button btnNextPage;

    private UtilisateurDAO utilisateurDAO;

    private final int ITEMS_PER_PAGE = 5;
    private int totalItems = 0;
    private int totalPages = 1;

    @FXML private HBox paginationContainer;
    private int currentPage = 1;
    private int itemsPerPage = 5;

    @FXML
    public void initialize() {
        utilisateurDAO = new UtilisateurDAO();
        if (addEntryFormController != null)
            addEntryFormController.setParentController(this);
        if (editEntryFormController != null)
            editEntryFormController.setParentController(this);    // NEW
        goToPage(1);
    }

    public void loadData() {
        tableRowsContainer.getChildren().clear();

        if ("ALL".equals(currentFilter)) {
            totalItems = utilisateurDAO.getTotalUtilisateursCount();
        } else {
            totalItems = utilisateurDAO.getCountByFilter(currentFilter);
        }

        totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;

        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        int offset = (currentPage - 1) * ITEMS_PER_PAGE;

        List<Utilisateur> users;
        if ("ALL".equals(currentFilter)) {
            users = utilisateurDAO.getUtilisateursByPage(ITEMS_PER_PAGE, offset);
        } else {
            users = utilisateurDAO.getUtilisateursByFilterPage(currentFilter, ITEMS_PER_PAGE, offset);
        }

        if (users == null || users.isEmpty()) {
            Label emptyLabel = new Label("No personnel entries found in the database.");
            emptyLabel.setStyle("-fx-padding: 32px; -fx-text-fill: #94A3B8;");
            tableRowsContainer.getChildren().add(emptyLabel);
            updatePaginationUI();
            return;
        }

        for (int i = 0; i < users.size(); i++) {
            Utilisateur u = users.get(i);
            HBox row = createRow(u, i == users.size() - 1);
            tableRowsContainer.getChildren().add(row);
        }

        updatePaginationUI();
    }

    @FXML
    private void handleFilterChange(ActionEvent event) {
        Button source = (Button) event.getSource();

        filterAll.getStyleClass().remove("filter-chip-active");
        filterAdmins.getStyleClass().remove("filter-chip-active");
        filterUsers.getStyleClass().remove("filter-chip-active");
        filterAll.getStyleClass().add("filter-chip");
        filterAdmins.getStyleClass().add("filter-chip");
        filterUsers.getStyleClass().add("filter-chip");

        source.getStyleClass().remove("filter-chip");
        source.getStyleClass().add("filter-chip-active");

        if (source == filterAdmins)
            currentFilter = "ADMIN";
        else if (source == filterUsers)
            currentFilter = "UTILISATEUR";
        else
            currentFilter = "ALL";

        currentPage = 1;
        loadFilteredData();
    }

    private void loadFilteredData() {
        goToPage(1);
    }

    private void renderPaginationButtons(int totalPages) {
        paginationContainer.getChildren().clear();

        btnPrevPage.setDisable(currentPage == 1);
        paginationContainer.getChildren().add(btnPrevPage);

        for (int i = 1; i <= totalPages; i++) {
            Button pageBtn = new Button(String.valueOf(i));
            pageBtn.getStyleClass().add("page-btn");
            if (i == currentPage) {
                pageBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white;");
            }
            final int pageNum = i;
            pageBtn.setOnAction(e -> goToPage(pageNum));
            paginationContainer.getChildren().add(pageBtn);
        }

        btnNextPage.setDisable(currentPage >= totalPages);
        paginationContainer.getChildren().add(btnNextPage);
    }

    private void updatePaginationUI() {
        if (lblCurrentPage != null) {
            lblCurrentPage.setText(String.valueOf(currentPage));
        }

        if (footerPaginationLabel != null) {
            int startItem = totalItems == 0 ? 0 : (currentPage - 1) * ITEMS_PER_PAGE + 1;
            int endItem = Math.min(currentPage * ITEMS_PER_PAGE, totalItems);
            footerPaginationLabel
                    .setText(String.format("Showing %d-%d of %d personnel entries", startItem, endItem, totalItems));
        }

        if (btnPrevPage != null)
            btnPrevPage.setDisable(currentPage == 1);
        if (btnNextPage != null)
            btnNextPage.setDisable(currentPage >= totalPages);
    }

    private void renderPagination() {
        paginationContainer.getChildren().clear();
        paginationContainer.getChildren().add(btnPrevPage);

        int totalItems = utilisateurDAO.getTotalUtilisateursCount();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);

        for (int i = 1; i <= totalPages; i++) {
            Button pageBtn = new Button(String.valueOf(i));
            pageBtn.getStyleClass().add("page-btn");

            if (i == currentPage) {
                pageBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white;");
            }

            final int pageNum = i;
            pageBtn.setOnAction(e -> goToPage(pageNum));
            paginationContainer.getChildren().add(pageBtn);
        }

        paginationContainer.getChildren().add(btnNextPage);
        footerPaginationLabel.setText("Showing " + totalItems + " personnel entries");
    }

    public void goToPage(int page) {
        int totalItems;
        if ("ALL".equals(currentFilter)) {
            totalItems = utilisateurDAO.getTotalUtilisateursCount();
        } else {
            totalItems = utilisateurDAO.getCountByFilter(currentFilter);
        }

        int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;

        this.currentPage = Math.max(1, Math.min(page, totalPages));
        this.totalItems = totalItems;
        this.totalPages = totalPages;

        int offset = (currentPage - 1) * ITEMS_PER_PAGE;
        List<Utilisateur> users;
        if ("ALL".equals(currentFilter)) {
            users = utilisateurDAO.getUtilisateursByPage(ITEMS_PER_PAGE, offset);
        } else {
            users = utilisateurDAO.getUtilisateursByFilterPage(currentFilter, ITEMS_PER_PAGE, offset);
        }

        tableRowsContainer.getChildren().clear();
        if (users == null || users.isEmpty()) {
            tableRowsContainer.getChildren().add(new Label("No personnel entries found."));
        } else {
            for (int i = 0; i < users.size(); i++) {
                tableRowsContainer.getChildren().add(createRow(users.get(i), i == users.size() - 1));
            }
        }

        renderPaginationButtons(totalPages);
        updateFooter(totalItems);
    }

    private void updateFooter(int totalItems) {
        int start = (currentPage - 1) * ITEMS_PER_PAGE + 1;
        int end = Math.min(currentPage * ITEMS_PER_PAGE, totalItems);
        footerPaginationLabel.setText(String.format("Showing %d-%d of %d personnel entries",
                totalItems == 0 ? 0 : start, end, totalItems));
    }

    @FXML
    private void handleNextPage() {
        goToPage(currentPage + 1);
    }

    @FXML
    private void handlePrevPage() {
        goToPage(currentPage - 1);
    }

    // ======================== SINGLE createRow WITH IMPROVED HOVER ========================
    private HBox createRow(Utilisateur u, boolean isLast) {
        HBox row = new HBox();
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.getStyleClass().add("table-row");
        if (isLast) {
            row.setStyle("-fx-border-color: transparent;");
        }

        // --- Column 1: Avatar & Name/ID ---
        HBox col1 = new HBox();
        col1.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        col1.setSpacing(16.0);
        col1.setPrefWidth(250.0);

        StackPane avatar = new StackPane();
        avatar.setMinWidth(40);
        avatar.setMinHeight(40);
        avatar.setMaxWidth(40);
        avatar.setMaxHeight(40);
        avatar.setStyle("-fx-background-color: #E5E7EB; -fx-background-radius: 8px;");

        if (u.hasFaceImage()) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(u.getFaceImage());
                Image img = new Image(bis, 40, 40, true, true);
                ImageView imageView = new ImageView(img);
                imageView.setFitWidth(40);
                imageView.setFitHeight(40);
                imageView.setPreserveRatio(true);

                Rectangle clip = new Rectangle(40, 40);
                clip.setArcWidth(16);
                clip.setArcHeight(16);
                imageView.setClip(clip);

                avatar.getChildren().add(imageView);
            } catch (Exception e) {
                Label fallbackInitials = new Label(getInitials(u.getFullName()));
                fallbackInitials.setStyle("-fx-text-fill: #6B7280; -fx-font-weight: bold;");
                avatar.getChildren().add(fallbackInitials);
            }
        } else {
            Label initials = new Label(getInitials(u.getFullName()));
            initials.setStyle("-fx-text-fill: #6B7280; -fx-font-weight: bold;");
            avatar.getChildren().add(initials);
        }

        VBox nameBox = new VBox();
        nameBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        nameBox.setSpacing(2.0);
        Label nameLabel = new Label(u.getFullName());
        nameLabel.getStyleClass().add("row-name");
        Label idLabel = new Label("ID: VC-" + String.format("%04d", u.getId()));
        idLabel.getStyleClass().add("row-id");
        nameBox.getChildren().addAll(nameLabel, idLabel);

        col1.getChildren().addAll(avatar, nameBox);

        // --- Column 2: Clearance Level ---
        HBox col2 = new HBox();
        col2.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        col2.setPrefWidth(220.0);

        Label badge = new Label(getRoleLabel(u.getRole()));
        badge.getStyleClass().addAll("badge", getBadgeStyle(u.getRole()));
        col2.getChildren().add(badge);

        // --- Column 3: Last Access ---
        VBox col3 = new VBox();
        col3.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        col3.setSpacing(2.0);
        col3.setPrefWidth(220.0);

        Label timeLabel = new Label("N/A");
        timeLabel.getStyleClass().add("access-time");
        Label locationLabel = new Label("No access records");
        locationLabel.getStyleClass().add("access-location");
        col3.getChildren().addAll(timeLabel, locationLabel);

        // --- Column 4: Status ---
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

        // --- Column 5: Action Button (⋮) with improved hover persistence ---
        Button moreButton = new Button("\u22EE"); // ⋮
        moreButton.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-border-color: transparent; " +
            "-fx-font-size: 20px; " +
            "-fx-text-fill: #6B7280; " +
            "-fx-cursor: hand;"
        );
        moreButton.setVisible(false);

        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> handleEditUser(u));

        String toggleText = u.isActive() ? "Deactivate" : "Activate";
        MenuItem toggleActiveItem = new MenuItem(toggleText);
        toggleActiveItem.setOnAction(e -> handleToggleActive(u));

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> handleDeleteUser(u));

        contextMenu.getItems().addAll(editItem, toggleActiveItem, deleteItem);

        moreButton.setOnAction(e -> {
            contextMenu.show(moreButton, javafx.geometry.Side.BOTTOM, 0, 0);
        });

        final boolean[] mouseOver = {false};

        row.setOnMouseEntered(e -> {
            mouseOver[0] = true;
            moreButton.setVisible(true);
        });

        row.setOnMouseExited(e -> {
            mouseOver[0] = false;
            if (!contextMenu.isShowing()) {
                moreButton.setVisible(false);
            }
        });

        contextMenu.setOnHidden(e -> {
            if (!mouseOver[0]) {
                moreButton.setVisible(false);
            }
        });

        row.getChildren().addAll(col1, col2, col3, col4, moreButton);
        return row;
    }

    // ======================== ACTION HANDLERS ========================
    private void handleEditUser(Utilisateur u) {
        // Open edit form pre-filled with user data
        if (editEntryFormController != null) {
            editEntryFormController.loadUserData(u);
            showEditEntryForm();
        }
    }

    private void handleToggleActive(Utilisateur u) {
        boolean newStatus = !u.isActive();
        String action = newStatus ? "activate" : "deactivate";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm " + action);
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to " + action + " " + u.getFullName() + "?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success;
            if (newStatus) {
                success = utilisateurDAO.activateUtilisateur(u.getId());
            } else {
                success = utilisateurDAO.deactivateUtilisateur(u.getId());
            }
            if (success) {
                loadData();
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setContentText("Failed to " + action + " user.");
                error.showAndWait();
            }
        }
    }

    private void handleDeleteUser(Utilisateur u) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete User");
        confirm.setHeaderText(null);
        confirm.setContentText("Permanently delete " + u.getFullName() + "? This cannot be undone.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = utilisateurDAO.deleteUtilisateur(u.getId());
            if (success) {
                loadData();
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setContentText("Failed to delete user.");
                error.showAndWait();
            }
        }
    }

    // ======================== FORM VISIBILITY HELPERS ========================
    @FXML
    private void handleAddEntry(ActionEvent event) {
        showAddEntryForm();
    }

    public void showAddEntryForm() {
        mainTableView.setVisible(false);
        mainTableView.setManaged(false);
        addEntryForm.setVisible(true);
        addEntryForm.setManaged(true);
        if (editEntryForm != null) {
            editEntryForm.setVisible(false);
            editEntryForm.setManaged(false);
        }
        if (addEntryFormController != null) {
            addEntryFormController.clearForm();
        }
    }

    public void hideAddEntryForm() {
        addEntryForm.setVisible(false);
        addEntryForm.setManaged(false);
        mainTableView.setVisible(true);
        mainTableView.setManaged(true);
    }

    public void showEditEntryForm() {
        mainTableView.setVisible(false);
        mainTableView.setManaged(false);
        editEntryForm.setVisible(true);
        editEntryForm.setManaged(true);
        if (addEntryForm != null) {
            addEntryForm.setVisible(false);
            addEntryForm.setManaged(false);
        }
    }

    public void hideEditEntryForm() {
        editEntryForm.setVisible(false);
        editEntryForm.setManaged(false);
        mainTableView.setVisible(true);
        mainTableView.setManaged(true);
    }

    // ======================== HELPER METHODS ========================
    private String getInitials(String name) {
        if (name == null || name.isEmpty())
            return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private String getRoleLabel(String role) {
        return role == null ? "UNKNOWN" : role.toUpperCase();
    }

    private String getBadgeStyle(String role) {
        if (role == null)
            return "badge-gray";
        String r = role.toLowerCase();
        if (r.contains("securite") || r.contains("critical") || r.contains("5"))
            return "badge-purple";
        if (r.contains("stagiaire") || r.contains("4") || r.contains("executive"))
            return "badge-blue";
        if (r.contains("suspended") || r.contains("blocked"))
            return "badge-red";
        return "badge-gray";
    }
}