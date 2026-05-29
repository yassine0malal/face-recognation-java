package com.facialaccess.dao;

import com.facialaccess.model.Admin;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * Data Access Object pour les administrateurs.
 * Gère les opérations CRUD sur les tables PERSONNE et ADMIN.
 */
public class AdminDAO {
    
    private final Connection connection;
    
    public AdminDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }
    
    /**
     * Récupère un admin par son ID.
     */
    public Admin getAdminById(int id) {
        String sql = "SELECT p.*, a.username, a.password_hash, a.failed_attempts, a.locked_until " +
                     "FROM PERSONNE p " +
                     "INNER JOIN ADMIN a ON p.id = a.id " +
                     "WHERE p.id = ? AND p.type = 'ADMIN'";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToAdmin(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'admin: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Met à jour les informations d'un admin.
     */
    public boolean updateAdmin(Admin admin) {
        String sqlPersonne = "UPDATE PERSONNE SET full_name = ?, email = ?, is_active = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sqlPersonne)) {
            pstmt.setString(1, admin.getFullName());
            pstmt.setString(2, admin.getEmail());
            pstmt.setBoolean(3, admin.isActive());
            pstmt.setInt(4, admin.getId());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de l'admin: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Récupère un admin par son username.
     */
    public Admin getAdminByUsername(String username) {
        String sql = "SELECT p.*, a.username, a.password_hash, a.failed_attempts, a.locked_until " +
                     "FROM PERSONNE p " +
                     "INNER JOIN ADMIN a ON p.id = a.id " +
                     "WHERE a.username = ? AND p.type = 'ADMIN'";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToAdmin(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'admin: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Alias pour getAdminByUsername.
     */
    public Admin findByUsername(String username) {
        return getAdminByUsername(username);
    }
    
    /**
     * Met à jour les tentatives échouées d'un admin.
     */
    public boolean updateFailedAttempts(int adminId, int failedAttempts, LocalDateTime lockedUntil) {
        String sql = "UPDATE ADMIN SET failed_attempts = ?, locked_until = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, failedAttempts);
            
            if (lockedUntil != null) {
                pstmt.setString(2, lockedUntil.toString().replace("T", " "));
            } else {
                pstmt.setNull(2, Types.VARCHAR);
            }
            
            pstmt.setInt(3, adminId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour des tentatives: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Réinitialise les tentatives échouées d'un admin.
     */
    public boolean resetFailedAttempts(int adminId) {
        return updateFailedAttempts(adminId, 0, null);
    }
    
    /**
     * Change le mot de passe d'un admin.
     */
    public boolean changePassword(int adminId, String newPasswordHash) {
        String sql = "UPDATE ADMIN SET password_hash = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newPasswordHash);
            pstmt.setInt(2, adminId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du changement de mot de passe: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Mappe un ResultSet vers un objet Admin.
     */
    private Admin mapResultSetToAdmin(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        
        // Attributs de PERSONNE
        admin.setId(rs.getInt("id"));
        admin.setFullName(rs.getString("full_name"));
        admin.setEmail(rs.getString("email"));
        admin.setActive(rs.getBoolean("is_active"));
        
        // Conversion du timestamp SQLite en LocalDateTime
        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            admin.setCreatedAt(LocalDateTime.parse(createdAtStr.replace(" ", "T")));
        }
        
        // Attributs de ADMIN
        admin.setUsername(rs.getString("username"));
        admin.setPasswordHash(rs.getString("password_hash"));
        admin.setFailedAttempts(rs.getInt("failed_attempts"));
        
        String lockedUntilStr = rs.getString("locked_until");
        if (lockedUntilStr != null) {
            admin.setLockedUntil(LocalDateTime.parse(lockedUntilStr.replace(" ", "T")));
        }
        
        return admin;
    }
}
