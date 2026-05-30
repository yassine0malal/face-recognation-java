package com.facialaccess.dao;

import com.facialaccess.model.AccessLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object pour les logs d'accès.
 * Utilise DatabaseManager.getConnection() à chaque appel pour éviter
 * les connexions fermées (même pattern que UtilisateurDAO).
 */
public class AccessLogDAO {

    public AccessLogDAO() {}

    private Connection conn() {
        return DatabaseManager.getInstance().getConnection();
    }

    /** Enregistre un nouveau log d'accès. */
    public boolean addAccessLog(AccessLog log) {
        String sql = "INSERT INTO ACCESS_LOGS (user_id, status, confidence_score, identification_method) " +
                     "VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (log.getUserId() != null) pstmt.setInt(1, log.getUserId());
            else                          pstmt.setNull(1, Types.INTEGER);
            pstmt.setString(2, log.getStatus());
            if (log.getConfidenceScore() != null) pstmt.setDouble(3, log.getConfidenceScore());
            else                                   pstmt.setNull(3, Types.REAL);
            pstmt.setString(4, log.getIdentificationMethod());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) log.setId(keys.getInt(1));
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur addAccessLog: " + e.getMessage());
            return false;
        }
    }

    /** Récupère tous les logs d'accès, triés du plus récent. */
    public List<AccessLog> getAllAccessLogs() {
        List<AccessLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, p.full_name as user_name " +
                     "FROM ACCESS_LOGS al " +
                     "LEFT JOIN UTILISATEUR u ON al.user_id = u.id " +
                     "LEFT JOIN PERSONNE p ON u.id = p.id " +
                     "ORDER BY al.accessed_at DESC";
        try (Statement stmt = conn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) logs.add(map(rs));
        } catch (SQLException e) {
            System.err.println("Erreur getAllAccessLogs: " + e.getMessage());
        }
        return logs;
    }

    /** Récupère les logs d'un utilisateur spécifique. */
    public List<AccessLog> getAccessLogsByUserId(int userId) {
        List<AccessLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, p.full_name as user_name " +
                     "FROM ACCESS_LOGS al " +
                     "LEFT JOIN UTILISATEUR u ON al.user_id = u.id " +
                     "LEFT JOIN PERSONNE p ON u.id = p.id " +
                     "WHERE al.user_id = ? ORDER BY al.accessed_at DESC";
        try (PreparedStatement pstmt = conn().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) logs.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur getAccessLogsByUserId: " + e.getMessage());
        }
        return logs;
    }

    /** Récupère les logs par statut (GRANTED ou DENIED). */
    public List<AccessLog> getAccessLogsByStatus(String status) {
        List<AccessLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, p.full_name as user_name " +
                     "FROM ACCESS_LOGS al " +
                     "LEFT JOIN UTILISATEUR u ON al.user_id = u.id " +
                     "LEFT JOIN PERSONNE p ON u.id = p.id " +
                     "WHERE al.status = ? ORDER BY al.accessed_at DESC";
        try (PreparedStatement pstmt = conn().prepareStatement(sql)) {
            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) logs.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur getAccessLogsByStatus: " + e.getMessage());
        }
        return logs;
    }

    /** Récupère les logs dans une plage de dates. */
    public List<AccessLog> getAccessLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<AccessLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, p.full_name as user_name " +
                     "FROM ACCESS_LOGS al " +
                     "LEFT JOIN UTILISATEUR u ON al.user_id = u.id " +
                     "LEFT JOIN PERSONNE p ON u.id = p.id " +
                     "WHERE al.accessed_at BETWEEN ? AND ? ORDER BY al.accessed_at DESC";
        try (PreparedStatement pstmt = conn().prepareStatement(sql)) {
            pstmt.setString(1, startDate.toString().replace("T", " "));
            pstmt.setString(2, endDate.toString().replace("T", " "));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) logs.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur getAccessLogsByDateRange: " + e.getMessage());
        }
        return logs;
    }

    /** Compte le total des logs. */
    public int countAccessLogs() {
        try (Statement stmt = conn().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ACCESS_LOGS")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur countAccessLogs: " + e.getMessage());
        }
        return 0;
    }

    /** Compte les accès accordés. */
    public int countGrantedAccess() {
        try (Statement stmt = conn().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ACCESS_LOGS WHERE status = 'GRANTED'")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur countGrantedAccess: " + e.getMessage());
        }
        return 0;
    }

    /** Compte les accès refusés. */
    public int countDeniedAccess() {
        try (Statement stmt = conn().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ACCESS_LOGS WHERE status = 'DENIED'")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur countDeniedAccess: " + e.getMessage());
        }
        return 0;
    }

    /** Supprime les logs antérieurs à une date. */
    public boolean deleteOldLogs(LocalDateTime beforeDate) {
        try (PreparedStatement pstmt = conn().prepareStatement(
                "DELETE FROM ACCESS_LOGS WHERE accessed_at < ?")) {
            pstmt.setString(1, beforeDate.toString().replace("T", " "));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur deleteOldLogs: " + e.getMessage());
            return false;
        }
    }

    /** Mappe un ResultSet vers un AccessLog. */
    private AccessLog map(ResultSet rs) throws SQLException {
        AccessLog log = new AccessLog();
        log.setId(rs.getInt("id"));

        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) log.setUserId(userId);

        log.setStatus(rs.getString("status"));

        double score = rs.getDouble("confidence_score");
        if (!rs.wasNull()) log.setConfidenceScore(score);

        log.setIdentificationMethod(rs.getString("identification_method"));

        String accessedAt = rs.getString("accessed_at");
        if (accessedAt != null) {
            log.setAccessedAt(LocalDateTime.parse(accessedAt.replace(" ", "T")));
        }

        String userName = rs.getString("user_name");
        if (userName != null) log.setUserName(userName);

        return log;
    }
}
