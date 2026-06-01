package com.facialaccess.presentation;

import com.facialaccess.dao.AccessLogDAO;
import com.facialaccess.dao.UtilisateurDAO;
import com.facialaccess.model.AccessLog;
import com.facialaccess.model.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DashboardViewController {

    // ── Stat card labels ──
    @FXML private Label authorizedCountLabel;
    @FXML private Label authorizedTrendLabel;
    @FXML private Label deniedCountLabel;
    @FXML private Label deniedTrendLabel;
    @FXML private Label unknownCountLabel;
    @FXML private Label unknownAlertLabel;
    @FXML private Label activeNodesLabel;

    // ── Chart ──
    @FXML private BarChart<String, Number> activityChart;
    @FXML private CategoryAxis chartXAxis;
    @FXML private NumberAxis chartYAxis;
    @FXML private Button btn12h;
    @FXML private Button btn7d;

    // ── Recent activity ──
    @FXML private VBox recentActivityList;

    // ── Table ──
    @FXML private TableView<AccessLog> logsTable;
    @FXML private TableColumn<AccessLog, String> colUserName;
    @FXML private TableColumn<AccessLog, String> colMethod;
    @FXML private TableColumn<AccessLog, String> colTime;
    @FXML private TableColumn<AccessLog, String> colStatus;
    @FXML private SplitMenuButton exportBtn;

    private final AccessLogDAO accessLogDAO = new AccessLogDAO();
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private static final DateTimeFormatter HOUR_FMT = DateTimeFormatter.ofPattern("HH:00");

    @FXML
    public void initialize() {
        loadStatCards();
        loadActivityChart();
        loadRecentActivity();
        loadLogsTable();
    }

    // ── 1. STAT CARDS ────────────────────────────────────────────────────────

    private void loadStatCards() {
        List<AccessLog> allLogs = accessLogDAO.getAllAccessLogs();

        long granted = allLogs.stream().filter(l -> "GRANTED".equals(l.getStatus())).count();
        long denied  = allLogs.stream().filter(l -> "DENIED".equals(l.getStatus())).count();
        // Unknown = DENIED logs where user_id is null (face not identified)
        long unknown = allLogs.stream()
                .filter(l -> "DENIED".equals(l.getStatus()) && l.getUserId() == null)
                .count();
        int activeUsers = utilisateurDAO.countActiveUtilisateurs();

        authorizedCountLabel.setText(String.valueOf(granted));
        deniedCountLabel.setText(String.valueOf(denied));
        unknownCountLabel.setText(String.format("%02d", unknown));
        activeNodesLabel.setText(String.valueOf(activeUsers));

        // Trend: compare today vs yesterday (UTC to match SQLite)
        LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);
        LocalDateTime startToday     = now.toLocalDate().atStartOfDay();
        LocalDateTime startYesterday = startToday.minusDays(1);

        long grantedToday     = allLogs.stream().filter(l -> "GRANTED".equals(l.getStatus())
                && l.getAccessedAt() != null && l.getAccessedAt().isAfter(startToday)).count();
        long grantedYesterday = allLogs.stream().filter(l -> "GRANTED".equals(l.getStatus())
                && l.getAccessedAt() != null
                && l.getAccessedAt().isAfter(startYesterday)
                && l.getAccessedAt().isBefore(startToday)).count();

        long deniedToday     = allLogs.stream().filter(l -> "DENIED".equals(l.getStatus())
                && l.getAccessedAt() != null && l.getAccessedAt().isAfter(startToday)).count();
        long deniedYesterday = allLogs.stream().filter(l -> "DENIED".equals(l.getStatus())
                && l.getAccessedAt() != null
                && l.getAccessedAt().isAfter(startYesterday)
                && l.getAccessedAt().isBefore(startToday)).count();

        authorizedTrendLabel.setText(formatTrend(grantedToday, grantedYesterday));
        deniedTrendLabel.setText(formatTrend(deniedToday, deniedYesterday));

        // Color the denied trend label negative
        if (deniedToday > deniedYesterday) {
            deniedTrendLabel.getStyleClass().setAll("trend-negative");
        }
        if (unknown > 0) {
            unknownAlertLabel.setText("Alert");
        } else {
            unknownAlertLabel.setText("Clear");
            unknownAlertLabel.getStyleClass().setAll("trend-stable");
        }
    }

    private String formatTrend(long today, long yesterday) {
        if (yesterday == 0) return today > 0 ? "+100%" : "0%";
        long diff = today - yesterday;
        double pct = (diff * 100.0) / yesterday;
        return String.format("%+.0f%%", pct);
    }

    // ── 2. BAR CHART ─────────────────────────────────────────────────────────

    private void loadActivityChart() {
        renderChart(false); // default: 12h
    }

    @FXML
    private void handleChart12h() {
        btn12h.getStyleClass().setAll("toggle-btn-active");
        btn7d.getStyleClass().setAll("toggle-btn");
        renderChart(false);
    }

    @FXML
    private void handleChart7d() {
        btn7d.getStyleClass().setAll("toggle-btn-active");
        btn12h.getStyleClass().setAll("toggle-btn");
        renderChart(true);
    }

    private void renderChart(boolean sevenDays) {
        activityChart.getData().clear();
        List<AccessLog> allLogs = accessLogDAO.getAllAccessLogs();
        LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        if (sevenDays) {
            // 7 days — one bar per day
            DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("MM/dd");
            List<String> days = new ArrayList<>();
            Map<String, Integer> countByDay = new LinkedHashMap<>();
            for (int i = 6; i >= 0; i--) {
                String label = now.minusDays(i).format(dayFmt);
                days.add(label);
                countByDay.put(label, 0);
            }
            for (AccessLog log : allLogs) {
                if (log.getAccessedAt() != null && log.getAccessedAt().isAfter(now.minusDays(7))) {
                    String key = log.getAccessedAt().format(dayFmt);
                    countByDay.computeIfPresent(key, (k, v) -> v + 1);
                }
            }
            for (String day : days) {
                series.getData().add(new XYChart.Data<>(day, countByDay.get(day)));
            }
        } else {
            // 12 hours — one bar per hour
            List<String> hours = new ArrayList<>();
            Map<String, Integer> countByHour = new LinkedHashMap<>();
            for (int i = 11; i >= 0; i--) {
                String label = now.minusHours(i).format(HOUR_FMT);
                hours.add(label);
                countByHour.put(label, 0);
            }
            for (AccessLog log : allLogs) {
                if (log.getAccessedAt() != null && log.getAccessedAt().isAfter(now.minusHours(12))) {
                    String key = log.getAccessedAt().format(HOUR_FMT);
                    countByHour.computeIfPresent(key, (k, v) -> v + 1);
                }
            }
            for (String hour : hours) {
                series.getData().add(new XYChart.Data<>(hour, countByHour.get(hour)));
            }
        }

        activityChart.getData().add(series);
        activityChart.setAnimated(false);

        // Style bars
        series.getData().forEach(d -> {
            if (d.getNode() != null) d.getNode().setStyle("-fx-bar-fill: #BFDBFE;");
            d.nodeProperty().addListener((obs, o, node) -> {
                if (node != null) node.setStyle("-fx-bar-fill: #BFDBFE;");
            });
        });
    }

    // ── 3. RECENT ACTIVITY ───────────────────────────────────────────────────

    private void loadRecentActivity() {
        List<AccessLog> recent = accessLogDAO.getAllAccessLogs().stream()
                .limit(5)
                .collect(Collectors.toList());

        recentActivityList.getChildren().clear();

        if (recent.isEmpty()) {
            Label empty = new Label("No recent activity");
            empty.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px;");
            recentActivityList.getChildren().add(empty);
            return;
        }

        for (AccessLog log : recent) {
            recentActivityList.getChildren().add(buildActivityItem(log));
        }
    }

    private HBox buildActivityItem(AccessLog log) {
        HBox row = new HBox(10);
        row.getStyleClass().add("activity-item");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Avatar with initials
        String name = log.getUserName() != null ? log.getUserName() : "Unknown";
        String initials = getInitials(name);
        String avatarColor = "GRANTED".equals(log.getStatus()) ? "#10B981" : "#EF4444";

        StackPane avatar = buildActivityAvatar(log, avatarColor, initials);

        // Text info
        VBox info = new VBox(2);
        // Truncate long names
        String displayName = name.length() > 18 ? name.substring(0, 16) + "..." : name;
        Label nameLabel = new Label(displayName);
        nameLabel.getStyleClass().add("activity-name");

        String statusText = "GRANTED".equals(log.getStatus()) ? "Authorized Access" : "Denied Access";
        String timeAgo = log.getAccessedAt() != null ? formatTimeAgo(log.getAccessedAt()) : "";
        Label metaLabel = new Label(statusText + " • " + timeAgo);
        metaLabel.getStyleClass().add("activity-meta");

        info.getChildren().addAll(nameLabel, metaLabel);
        row.getChildren().addAll(avatar, info);
        return row;
    }

    // ── 4. LOGS TABLE ────────────────────────────────────────────────────────

    private void loadLogsTable() {
        // User name column with avatar
        colUserName.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                AccessLog log = (AccessLog) getTableRow().getItem();
                String name = log.getUserName() != null ? log.getUserName() : "Unknown";
                String initials = getInitials(name);
                String color = avatarColorForName(name);

                StackPane avatar = buildTableAvatar(log, color, initials);

                Label nameLbl = new Label(name);
                nameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #1E293B;");

                HBox cell = new HBox(10, avatar, nameLbl);
                cell.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                setGraphic(cell);
            }
        });
        colUserName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getUserName()));

        // Method column
        colMethod.setCellValueFactory(data -> {
            String method = data.getValue().getIdentificationMethod();
            return new javafx.beans.property.SimpleStringProperty(method != null ? method : "—");
        });

        // Time column — convert UTC from DB to local time for display
        colTime.setCellValueFactory(data -> {
            LocalDateTime dt = data.getValue().getAccessedAt();
            if (dt == null) return new javafx.beans.property.SimpleStringProperty("—");
            // SQLite stores in UTC, convert to system local time
            LocalDateTime local = dt.atOffset(java.time.ZoneOffset.UTC)
                    .atZoneSameInstant(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
            return new javafx.beans.property.SimpleStringProperty(local.format(TIME_FMT));
        });

        // Status column with chip
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                AccessLog log = (AccessLog) getTableRow().getItem();
                Label chip = new Label("GRANTED".equals(log.getStatus()) ? "Success" : "Denied");
                chip.getStyleClass().add("GRANTED".equals(log.getStatus()) ? "chip-success" : "chip-denied");
                setGraphic(chip);
            }
        });
        colStatus.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));

        // Load data — last 20 logs
        List<AccessLog> logs = accessLogDAO.getAllAccessLogs().stream()
                .limit(20)
                .collect(Collectors.toList());

        ObservableList<AccessLog> items = FXCollections.observableArrayList(logs);
        logsTable.setItems(items);
        logsTable.setPlaceholder(new Label("No access logs found"));
    }

    // ── 5. EXPORT ────────────────────────────────────────────────────────────

    @FXML
    private void handleExportLog() { handleExportCSV(); }

    @FXML
    private void handleExportCSV() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Access Log — CSV");
        chooser.setInitialFileName("access_log_export.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showSaveDialog(logsTable.getScene().getWindow());
        if (file == null) return;

        List<AccessLog> allLogs = accessLogDAO.getAllAccessLogs();
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("ID,User Name,Status,Method,Confidence,Accessed At\n");
            for (AccessLog log : allLogs) {
                fw.write(String.format("%s,%s,%s,%s,%s,%s\n",
                        log.getId(),
                        csvEscape(log.getUserName() != null ? log.getUserName() : "Unknown"),
                        log.getStatus(),
                        log.getIdentificationMethod() != null ? log.getIdentificationMethod() : "",
                        log.getConfidenceScore() != null ? String.format("%.2f", log.getConfidenceScore()) : "",
                        log.getAccessedAt() != null ? log.getAccessedAt().toString() : ""
                ));
            }
            showAlert(Alert.AlertType.INFORMATION, "Export successful",
                    "CSV exported to:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Export failed", e.getMessage());
        }
    }

    @FXML
    private void handleExportExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Access Log — Excel");
        chooser.setInitialFileName("access_log_export.xlsx");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = chooser.showSaveDialog(logsTable.getScene().getWindow());
        if (file == null) return;

        List<AccessLog> allLogs = accessLogDAO.getAllAccessLogs();

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Access Logs");

            // Header style
            CellStyle headerStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header row
            Row header = sheet.createRow(0);
            String[] cols = {"ID", "User Name", "Status", "Method", "Confidence", "Accessed At"};
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowIdx = 1;
            for (AccessLog log : allLogs) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(log.getId() != null ? log.getId() : 0);
                row.createCell(1).setCellValue(log.getUserName() != null ? log.getUserName() : "Unknown");
                row.createCell(2).setCellValue(log.getStatus() != null ? log.getStatus() : "");
                row.createCell(3).setCellValue(log.getIdentificationMethod() != null ? log.getIdentificationMethod() : "");
                row.createCell(4).setCellValue(log.getConfidenceScore() != null ? log.getConfidenceScore() : 0.0);
                row.createCell(5).setCellValue(log.getAccessedAt() != null ? log.getAccessedAt().toString() : "");
            }

            // Auto-size columns
            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
            showAlert(Alert.AlertType.INFORMATION, "Export successful",
                    "Excel exported to:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Export failed", e.getMessage());
        }
    }

    private String csvEscape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private String formatTimeAgo(LocalDateTime dt) {
        // SQLite stores datetime('now') in UTC — compare against UTC now
        long minutes = java.time.Duration.between(dt, java.time.LocalDateTime.now(java.time.ZoneOffset.UTC)).toMinutes();
        if (minutes < 1)  return "just now";
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24)   return hours + "h ago";
        return (hours / 24) + "d ago";
    }

    private static final String[] AVATAR_COLORS = {
        "#3B5BDB", "#7C3AED", "#0891B2", "#059669", "#D97706",
        "#DC2626", "#DB2777", "#2563EB", "#16A34A", "#9333EA"
    };

    private String avatarColorForName(String name) {
        if (name == null) return AVATAR_COLORS[0];
        return AVATAR_COLORS[Math.abs(name.hashCode()) % AVATAR_COLORS.length];
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Utilisateur resolveUtilisateur(AccessLog log) {
        if (log == null) return null;
        if (log.getUserId() != null) {
            return utilisateurDAO.getUtilisateurById(log.getUserId());
        }

        String name = log.getUserName();
        if (name == null || name.isBlank()) return null;

        List<Utilisateur> matches = utilisateurDAO.searchUtilisateursByName(name.trim());
        if (matches.isEmpty()) return null;

        for (Utilisateur u : matches) {
            if (u.getFullName() != null && u.getFullName().equalsIgnoreCase(name.trim())) {
                return u;
            }
        }

        return matches.get(0);
    }

    private StackPane buildActivityAvatar(AccessLog log, String avatarColor, String initials) {
        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("activity-avatar");
        avatar.setStyle("-fx-background-color: " + avatarColor + "; -fx-background-radius: 50%; "
                + "-fx-min-width: 36px; -fx-min-height: 36px; -fx-max-width: 36px; -fx-max-height: 36px;");

        Utilisateur user = resolveUtilisateur(log);
        if (user != null && user.hasFaceImage()) {
            try {
                Image img = new Image(new ByteArrayInputStream(user.getFaceImage()), 36, 36, true, true);
                ImageView imageView = new ImageView(img);
                imageView.setFitWidth(36);
                imageView.setFitHeight(36);
                imageView.setPreserveRatio(true);

                Rectangle clip = new Rectangle(36, 36);
                clip.setArcWidth(36);
                clip.setArcHeight(36);
                imageView.setClip(clip);

                avatar.getChildren().add(imageView);
                return avatar;
            } catch (Exception e) {
                // Fallback to initials on any image error
            }
        }

        Label initialsLabel = new Label(initials);
        initialsLabel.getStyleClass().add("avatar-label");
        avatar.getChildren().add(initialsLabel);
        return avatar;
    }

    private StackPane buildTableAvatar(AccessLog log, String avatarColor, String initials) {
        StackPane avatar = new StackPane();
        avatar.setStyle("-fx-background-color: " + avatarColor + "; -fx-background-radius: 50%; "
                + "-fx-min-width: 32px; -fx-min-height: 32px; -fx-max-width: 32px; -fx-max-height: 32px;");

        Utilisateur user = resolveUtilisateur(log);
        if (user != null && user.hasFaceImage()) {
            try {
                Image img = new Image(new ByteArrayInputStream(user.getFaceImage()), 32, 32, true, true);
                ImageView imageView = new ImageView(img);
                imageView.setFitWidth(32);
                imageView.setFitHeight(32);
                imageView.setPreserveRatio(true);

                Rectangle clip = new Rectangle(32, 32);
                clip.setArcWidth(32);
                clip.setArcHeight(32);
                imageView.setClip(clip);

                avatar.getChildren().add(imageView);
                return avatar;
            } catch (Exception e) {
                // Fallback to initials on any image error
            }
        }

        Label lbl = new Label(initials);
        lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: white;");
        avatar.getChildren().add(lbl);
        return avatar;
    }
}
