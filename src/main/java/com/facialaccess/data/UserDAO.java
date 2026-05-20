package com.facialaccess.data;

import com.facialaccess.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object pour les utilisateurs.
 * Gère les opérations CRUD sur la table USERS.
 */
public class UserDAO {
    
    private final Connection connection;
    
    public UserDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }
    
    /**
     * Ajoute un nouvel utilisateur dans la base de données.
     */
    public boolean addUser(User user) {
        String sql = "INSERT INTO USERS (full_name, role, email, face_vector, qr_code_data, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getRole());
            pstmt.setString(3, user.getEmail());
            pstmt.setBytes(4, user.getFaceVector());
            pstmt.setString(5, user.getQrCodeData());
            pstmt.setBoolean(6, user.isActive());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Récupérer l'ID généré
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'utilisateur: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Récupère un utilisateur par son ID.
     */
    public User getUserById(int id) {
        String sql = "SELECT * FROM USERS WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'utilisateur: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Récupère un utilisateur par son email.
     */
    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM USERS WHERE email = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'utilisateur par email: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Récupère tous les utilisateurs.
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM USERS ORDER BY created_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs: " + e.getMessage());
        }
        return users;
    }
    
    /**
     * Récupère tous les utilisateurs actifs.
     */
    public List<User> getActiveUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM USERS WHERE is_active = 1 ORDER BY full_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs actifs: " + e.getMessage());
        }
        return users;
    }
    
    /**
     * Recherche des utilisateurs par nom.
     */
    public List<User> searchUsersByName(String name) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM USERS WHERE full_name LIKE ? ORDER BY full_name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche d'utilisateurs: " + e.getMessage());
        }
        return users;
    }
    
    /**
     * Met à jour un utilisateur existant.
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE USERS SET full_name = ?, role = ?, email = ?, " +
                     "face_vector = ?, qr_code_data = ?, is_active = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getRole());
            pstmt.setString(3, user.getEmail());
            pstmt.setBytes(4, user.getFaceVector());
            pstmt.setString(5, user.getQrCodeData());
            pstmt.setBoolean(6, user.isActive());
            pstmt.setInt(7, user.getId());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de l'utilisateur: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Désactive un utilisateur (soft delete).
     */
    public boolean deactivateUser(int id) {
        String sql = "UPDATE USERS SET is_active = 0 WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la désactivation de l'utilisateur: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Supprime définitivement un utilisateur (hard delete).
     */
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM USERS WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'utilisateur: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Compte le nombre total d'utilisateurs.
     */
    public int countUsers() {
        String sql = "SELECT COUNT(*) FROM USERS";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des utilisateurs: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Compte le nombre d'utilisateurs actifs.
     */
    public int countActiveUsers() {
        String sql = "SELECT COUNT(*) FROM USERS WHERE is_active = 1";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des utilisateurs actifs: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Mappe un ResultSet vers un objet User.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        user.setEmail(rs.getString("email"));
        user.setFaceVector(rs.getBytes("face_vector"));
        user.setQrCodeData(rs.getString("qr_code_data"));
        
        // Conversion du timestamp SQLite en LocalDateTime
        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            user.setCreatedAt(LocalDateTime.parse(createdAtStr.replace(" ", "T")));
        }
        
        user.setActive(rs.getBoolean("is_active"));
        
        return user;
    }
}
