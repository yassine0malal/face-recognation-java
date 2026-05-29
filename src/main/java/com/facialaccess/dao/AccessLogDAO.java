package com.facialaccess.dao;

import com.facialaccess.model.AccessLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object pour les logs d'accès.
 * Gère les opérations CRUD sur la table ACCESS_LOGS.
 */
public class AccessLogDAO {
    
    private final Connection connection;
    
    public AccessLogDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }
    
    /**
     * Enregistre un nouveau log d'accès.
     */
    public boolean addAccessLog(AccessLog log) {
        String sql = "INSERT INTO ACCESS_LOGS (user_id, status, confidence_score, identification_method) " +
                     "VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (log.getUserId() != null) {
                pstmt.setInt(1, log.getUserId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setString(2, log.getStatus());
            
            if (log.getConfidenceScore() != null) {
                pstmt.setDouble(3, log.getConfidenceScore());
            } else {
                pstmt.setNull(3, Types.REAL);
            }
            
            pstmt.setString(4, log.getIdentificationMethod());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        log.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du log d'accès: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Récupère tous les logs d'accès.
     */
    public List<AccessLog> getAllAccessLogs() {
        List<AccessLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, p.full_name as user_name " +
                     "FROM ACCESS_LOGS al " +
                     "LEFT JOIN UTILISATEUR u ON al.user_id = u.id " +
                     "LEFT JOIN PERSONNE p ON u.id = p.id " +
                     "ORDER BY al.accessed_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                logs.add(mapResultSetToAccessLog(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des logs: " + e.getMessage());
        }
        return logs;
    }
    
    /**
     * Récupère les logs d'accès d'un utilisateur spécifique.
     */
    public List<AccessLog> getAccessLogsByUserId(int userId) {
        List<AccessLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, p.full_name as user_name " +
                     "FROM ACCESS_LOGS al " +
                     "LEFT JOIN UTILISATEUR u ON al.user_id = u.id " +
                     "LEFT JOIN PERSONNE p ON u.id = p.id " +
                     "WHERE al.user_id = ? " +
                     "ORDER BY al.accessed_at DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetToAccessLog(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des logs par utilisateur: " + e.getMessage());
        }
        return logs;
    }
    
    /**
     * Récupère les logs d'accès par statut (GRANTED ou DENIED).
     */
    public List<AccessLog> getAccessLogsByStatus(String status) {
        List<AccessLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, p.full_name as user_name " +
                     "FROM ACCESS_LOGS al " +
                     "LEFT JOIN UTILISATEUR u ON al.user_id = u.id " +
                     "LEFT JOIN PERSONNE p ON u.id = p.id " +
                     "WHERE al.status = ? " +
                     "ORDER BY al.accessed_at DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetToAccessLog(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des logs par statut: " + e.getMessage());
        }
        return logs;
    }
    
    /**
     * Récupère les logs d'accès dans une période donnée.
     */
    public List<AccessLog> getAccessLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<AccessLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, p.full_name as user_name " +
                     "FROM ACCESS_LOGS al " +
                     "LEFT JOIN UTILISATEUR u ON al.user_id = u.id " +
                     "LEFT JOIN PERSONNE p ON u.id = p.id " +
                     "WHERE al.accessed_at BETWEEN ? AND ? " +
                     "ORDER BY al.accessed_at DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, startDate.toString().replace("T", " "));
            pstmt.setString(2, endDate.toString().replace("T", " "));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                logs.add(mapResultSetToAccessLog(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des logs par période: " + e.getMessage());
        }
        return logs;
    }
    
    /**
     * Compte le nombre total de logs.
     */
    public int countAccessLogs() {
        String sql = "SELECT COUNT(*) FROM ACCESS_LOGS";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des logs: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Compte les accès accordés.
     */
    public int countGrantedAccess() {
        String sql = "SELECT COUNT(*) FROM ACCESS_LOGS WHERE status = 'GRANTED'";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des accès accordés: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Compte les accès refusés.
     */
    public int countDeniedAccess() {
        String sql = "SELECT COUNT(*) FROM ACCESS_LOGS WHERE status = 'DENIED'";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des accès refusés: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Supprime les logs plus anciens qu'une date donnée.
     */
    public boolean deleteOldLogs(LocalDateTime beforeDate) {
        String sql = "DELETE FROM ACCESS_LOGS WHERE accessed_at < ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, beforeDate.toString().replace("T", " "));
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression des anciens logs: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Mappe un ResultSet vers un objet AccessLog.
     */
    private AccessLog mapResultSetToAccessLog(ResultSet rs) throws SQLException {
        AccessLog log = new AccessLog();
        
        log.setId(rs.getInt("id"));
        
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            log.setUserId(userId);
        }
        
        log.setStatus(rs.getString("status"));
        
        double confidenceScore = rs.getDouble("confidence_score");
        if (!rs.wasNull()) {
            log.setConfidenceScore(confidenceScore);
        }
        
        log.setIdentificationMethod(rs.getString("identification_method"));
        
        String accessedAtStr = rs.getString("accessed_at");
        if (accessedAtStr != null) {
            log.setAccessedAt(LocalDateTime.parse(accessedAtStr.replace(" ", "T")));
        }
        
        // Nom de l'utilisateur (jointure)
        String userName = rs.getString("user_name");
        if (userName != null) {
            log.setUserName(userName);
        }
        
        return log;
    }
}
