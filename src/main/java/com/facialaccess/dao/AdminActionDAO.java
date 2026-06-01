package com.facialaccess.dao;

import com.facialaccess.model.AdminAction;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object pour les actions d'audit des administrateurs.
 */
public class AdminActionDAO {

    public AdminActionDAO() {}

    private Connection conn() {
        return DatabaseManager.getInstance().getConnection();
    }

    /**
     * Enregistre un nouveau log d'action admin.
     */
    public boolean addAdminAction(String adminUsername, String actionType, String details) {
        String sql = "INSERT INTO ADMIN_ACTION_LOGS (admin_username, action_type, details) " +
                     "VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn().prepareStatement(sql)) {
            pstmt.setString(1, adminUsername);
            pstmt.setString(2, actionType);
            pstmt.setString(3, details);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur addAdminAction: " + e.getMessage());
            return false;
        }
    }

    /**
     * Récupère tous les logs d'action admin triés du plus récent.
     */
    public List<AdminAction> getAllAdminActions() {
        List<AdminAction> actions = new ArrayList<>();
        String sql = "SELECT * FROM ADMIN_ACTION_LOGS ORDER BY action_at DESC";
        try (Statement stmt = conn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                AdminAction action = new AdminAction();
                action.setId(rs.getInt("id"));
                action.setAdminUsername(rs.getString("admin_username"));
                action.setActionType(rs.getString("action_type"));
                action.setDetails(rs.getString("details"));
                
                String actionAt = rs.getString("action_at");
                if (actionAt != null) {
                    // SQLite DATETIME format can be "YYYY-MM-DD HH:MM:SS" or ISO
                    String formatted = actionAt.replace(" ", "T");
                    action.setActionAt(LocalDateTime.parse(formatted));
                }
                actions.add(action);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getAllAdminActions: " + e.getMessage());
        }
        return actions;
    }
}
