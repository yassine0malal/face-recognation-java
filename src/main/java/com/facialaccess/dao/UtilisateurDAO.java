package com.facialaccess.dao;

import com.facialaccess.model.Utilisateur;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object pour les utilisateurs.
 * Gère les opérations CRUD sur les tables PERSONNE et UTILISATEUR.
 */
public class UtilisateurDAO {
    
    private final Connection connection;
    
    public UtilisateurDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }
    
    /**
     * Ajoute un nouvel utilisateur (insertion dans PERSONNE puis UTILISATEUR).
     */
    public boolean addUtilisateur(Utilisateur utilisateur) {
        try {
            connection.setAutoCommit(false);
            
            // 1. Insérer dans PERSONNE
            String sqlPersonne = "INSERT INTO PERSONNE (full_name, email, type, is_active) VALUES (?, ?, 'UTILISATEUR', ?)";
            PreparedStatement pstmtPersonne = connection.prepareStatement(sqlPersonne, Statement.RETURN_GENERATED_KEYS);
            pstmtPersonne.setString(1, utilisateur.getFullName());
            pstmtPersonne.setString(2, utilisateur.getEmail());
            pstmtPersonne.setBoolean(3, utilisateur.isActive());
            
            int affectedRows = pstmtPersonne.executeUpdate();
            if (affectedRows == 0) {
                connection.rollback();
                return false;
            }
            
            // Récupérer l'ID généré
            ResultSet generatedKeys = pstmtPersonne.getGeneratedKeys();
            if (generatedKeys.next()) {
                utilisateur.setId(generatedKeys.getInt(1));
            } else {
                connection.rollback();
                return false;
            }
            
            // 2. Insérer dans UTILISATEUR
            String sqlUtilisateur = "INSERT INTO UTILISATEUR (id, role, face_vector, qr_code_data) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmtUtilisateur = connection.prepareStatement(sqlUtilisateur);
            pstmtUtilisateur.setInt(1, utilisateur.getId());
            pstmtUtilisateur.setString(2, utilisateur.getRole());
            pstmtUtilisateur.setBytes(3, utilisateur.getFaceVector());
            pstmtUtilisateur.setString(4, utilisateur.getQrCodeData());
            
            pstmtUtilisateur.executeUpdate();
            
            connection.commit();
            connection.setAutoCommit(true);
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Erreur lors de l'ajout de l'utilisateur: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Récupère un utilisateur par son ID.
     */
    public Utilisateur getUtilisateurById(int id) {
        String sql = "SELECT p.*, u.role, u.face_vector, u.qr_code_data " +
                     "FROM PERSONNE p " +
                     "INNER JOIN UTILISATEUR u ON p.id = u.id " +
                     "WHERE p.id = ? AND p.type = 'UTILISATEUR'";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUtilisateur(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'utilisateur: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Récupère un utilisateur par son email.
     */
    public Utilisateur getUtilisateurByEmail(String email) {
        String sql = "SELECT p.*, u.role, u.face_vector, u.qr_code_data " +
                     "FROM PERSONNE p " +
                     "INNER JOIN UTILISATEUR u ON p.id = u.id " +
                     "WHERE p.email = ? AND p.type = 'UTILISATEUR'";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUtilisateur(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'utilisateur par email: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Récupère tous les utilisateurs.
     */
    public List<Utilisateur> getAllUtilisateurs() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT p.*, u.role, u.face_vector, u.qr_code_data " +
                     "FROM PERSONNE p " +
                     "INNER JOIN UTILISATEUR u ON p.id = u.id " +
                     "WHERE p.type = 'UTILISATEUR' " +
                     "ORDER BY p.created_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                utilisateurs.add(mapResultSetToUtilisateur(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs: " + e.getMessage());
        }
        return utilisateurs;
    }
    
    /**
     * Récupère tous les utilisateurs actifs.
     */
    public List<Utilisateur> getActiveUtilisateurs() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT p.*, u.role, u.face_vector, u.qr_code_data " +
                     "FROM PERSONNE p " +
                     "INNER JOIN UTILISATEUR u ON p.id = u.id " +
                     "WHERE p.type = 'UTILISATEUR' AND p.is_active = 1 " +
                     "ORDER BY p.full_name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                utilisateurs.add(mapResultSetToUtilisateur(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs actifs: " + e.getMessage());
        }
        return utilisateurs;
    }
    
    /**
     * Recherche des utilisateurs par nom.
     */
    public List<Utilisateur> searchUtilisateursByName(String name) {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT p.*, u.role, u.face_vector, u.qr_code_data " +
                     "FROM PERSONNE p " +
                     "INNER JOIN UTILISATEUR u ON p.id = u.id " +
                     "WHERE p.type = 'UTILISATEUR' AND p.full_name LIKE ? " +
                     "ORDER BY p.full_name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                utilisateurs.add(mapResultSetToUtilisateur(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche d'utilisateurs: " + e.getMessage());
        }
        return utilisateurs;
    }
    
    /**
     * Met à jour un utilisateur existant.
     */
    public boolean updateUtilisateur(Utilisateur utilisateur) {
        try {
            connection.setAutoCommit(false);
            
            // 1. Mettre à jour PERSONNE
            String sqlPersonne = "UPDATE PERSONNE SET full_name = ?, email = ?, is_active = ? WHERE id = ?";
            PreparedStatement pstmtPersonne = connection.prepareStatement(sqlPersonne);
            pstmtPersonne.setString(1, utilisateur.getFullName());
            pstmtPersonne.setString(2, utilisateur.getEmail());
            pstmtPersonne.setBoolean(3, utilisateur.isActive());
            pstmtPersonne.setInt(4, utilisateur.getId());
            pstmtPersonne.executeUpdate();
            
            // 2. Mettre à jour UTILISATEUR
            String sqlUtilisateur = "UPDATE UTILISATEUR SET role = ?, face_vector = ?, qr_code_data = ? WHERE id = ?";
            PreparedStatement pstmtUtilisateur = connection.prepareStatement(sqlUtilisateur);
            pstmtUtilisateur.setString(1, utilisateur.getRole());
            pstmtUtilisateur.setBytes(2, utilisateur.getFaceVector());
            pstmtUtilisateur.setString(3, utilisateur.getQrCodeData());
            pstmtUtilisateur.setInt(4, utilisateur.getId());
            pstmtUtilisateur.executeUpdate();
            
            connection.commit();
            connection.setAutoCommit(true);
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Erreur lors de la mise à jour de l'utilisateur: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Désactive un utilisateur (soft delete).
     */
    public boolean deactivateUtilisateur(int id) {
        String sql = "UPDATE PERSONNE SET is_active = 0 WHERE id = ? AND type = 'UTILISATEUR'";
        
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
     * Grâce à ON DELETE CASCADE, supprime aussi dans UTILISATEUR.
     */
    public boolean deleteUtilisateur(int id) {
        String sql = "DELETE FROM PERSONNE WHERE id = ? AND type = 'UTILISATEUR'";
        
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
    public int countUtilisateurs() {
        String sql = "SELECT COUNT(*) FROM PERSONNE WHERE type = 'UTILISATEUR'";
        
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
    public int countActiveUtilisateurs() {
        String sql = "SELECT COUNT(*) FROM PERSONNE WHERE type = 'UTILISATEUR' AND is_active = 1";
        
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
     * Mappe un ResultSet vers un objet Utilisateur.
     */
    private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
        Utilisateur utilisateur = new Utilisateur();
        
        // Attributs de PERSONNE
        utilisateur.setId(rs.getInt("id"));
        utilisateur.setFullName(rs.getString("full_name"));
        utilisateur.setEmail(rs.getString("email"));
        utilisateur.setActive(rs.getBoolean("is_active"));
        
        // Conversion du timestamp SQLite en LocalDateTime
        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            utilisateur.setCreatedAt(LocalDateTime.parse(createdAtStr.replace(" ", "T")));
        }
        
        // Attributs de UTILISATEUR
        utilisateur.setRole(rs.getString("role"));
        utilisateur.setFaceVector(rs.getBytes("face_vector"));
        utilisateur.setQrCodeData(rs.getString("qr_code_data"));
        
        return utilisateur;
    }
}
