// package com.facialaccess.dao;

// import com.facialaccess.model.Utilisateur;
// import java.sql.*;
// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;

// public class UtilisateurDAO {

//     // Constructeur vide (nous ne stockons plus de connexion globale)
//     public UtilisateurDAO() {
//     }

//     /**
//      * ENREGISTREMENT SÉCURISÉ EN DEUX ÉTAPES (Appelé par AddPersonnelController)
//      */
//     public boolean addUtilisateur(Utilisateur u) {
//         String sqlPersonne = "INSERT INTO PERSONNE (full_name, email, type, is_active) VALUES (?, ?, 'UTILISATEUR', ?);";
//         String sqlUtilisateur = "INSERT INTO UTILISATEUR (id, role, face_image, face_vector, qr_code_data) VALUES (?, ?, ?, ?, ?);";

//         Connection conn = null;
//         try {
//             conn = DatabaseManager.getInstance().getConnection();
//             conn.setAutoCommit(false); // Mode transactionnel

//             // Étape A : Insérer dans PERSONNE
//             int generatedId = -1;
//             try (PreparedStatement pstmtP = conn.prepareStatement(sqlPersonne, Statement.RETURN_GENERATED_KEYS)) {
//                 pstmtP.setString(1, u.getFullName());
//                 pstmtP.setString(2, u.getEmail());
//                 pstmtP.setBoolean(3, u.isActive());
//                 pstmtP.executeUpdate();

//                 try (ResultSet rs = pstmtP.getGeneratedKeys()) {
//                     if (rs.next()) {
//                         generatedId = rs.getInt(1);
//                     }
//                 }
//             }

//             if (generatedId == -1) {
//                 conn.rollback();
//                 return false;
//             }

//             // Étape B : Insérer dans UTILISATEUR
//             try (PreparedStatement pstmtU = conn.prepareStatement(sqlUtilisateur)) {
//                 pstmtU.setInt(1, generatedId);
//                 pstmtU.setString(2, u.getRole() != null ? u.getRole() : "user");
//                 pstmtU.setBytes(3, u.getFaceImage());
//                 pstmtU.setBytes(4, u.getFaceVector());
//                 pstmtU.setString(5, u.getQrCodeData());
//                 pstmtU.executeUpdate();
//             }

//             conn.commit();
//             u.setId(generatedId);
//             return true;

//         } catch (SQLException e) {
//             System.err.println("Error adding user: " + e.getMessage());
//             if (conn != null) {
//                 try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
//             }
//             return false;
//         } finally {
//             if (conn != null) {
//                 try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
//             }
//         }
//     }

//     /**
//      * Récupère un utilisateur par son ID.
//      */
//     public Utilisateur getUtilisateurById(int id) {
//         String sql = "SELECT p.*, u.role, u.face_image, u.face_vector, u.qr_code_data " +
//                 "FROM PERSONNE p " +
//                 "INNER JOIN UTILISATEUR u ON p.id = u.id " +
//                 "WHERE p.id = ? AND p.type = 'UTILISATEUR'";

//         try (Connection conn = DatabaseManager.getInstance().getConnection();
//              PreparedStatement pstmt = conn.prepareStatement(sql)) {
//             pstmt.setInt(1, id);
//             try (ResultSet rs = pstmt.executeQuery()) {
//                 if (rs.next()) return mapResultSetToUtilisateur(rs);
//             }
//         } catch (SQLException e) {
//             System.err.println("Erreur lors de la récupération de l'utilisateur: " + e.getMessage());
//         }
//         return null;
//     }

//     /**
//      * Récupère un utilisateur par son email.
//      */
//     public Utilisateur getUtilisateurByEmail(String email) {
//         String sql = "SELECT p.*, u.role, u.face_image, u.face_vector, u.qr_code_data " +
//                 "FROM PERSONNE p " +
//                 "INNER JOIN UTILISATEUR u ON p.id = u.id " +
//                 "WHERE p.email = ? AND p.type = 'UTILISATEUR'";

//         try (Connection conn = DatabaseManager.getInstance().getConnection();
//              PreparedStatement pstmt = conn.prepareStatement(sql)) {
//             pstmt.setString(1, email);
//             try (ResultSet rs = pstmt.executeQuery()) {
//                 if (rs.next()) return mapResultSetToUtilisateur(rs);
//             }
//         } catch (SQLException e) {
//             System.err.println("Erreur lors de la récupération par email: " + e.getMessage());
//         }
//         return null;
//     }

//     /**
//      * Compte le nombre total pour la pagination
//      */
//     public int getTotalUtilisateursCount() {
//         String sql = "SELECT COUNT(*) FROM UTILISATEUR;";
//         try (Connection conn = DatabaseManager.getInstance().getConnection();
//              Statement stmt = conn.createStatement();
//              ResultSet rs = stmt.executeQuery(sql)) {
//             if (rs.next()) return rs.getInt(1);
//         } catch (SQLException e) {
//             System.err.println("Erreur count : " + e.getMessage());
//         }
//         return 0;
//     }

//     /**
//      * RÉCUPÉRATION PAGINÉE AVEC JOINTURE
//      */
//     public List<Utilisateur> getUtilisateursByPage(int limit, int offset) {
//         List<Utilisateur> list = new ArrayList<>();
//         String sql = "SELECT p.id, p.full_name, p.email, p.is_active, p.created_at, u.role, u.face_image, u.face_vector, u.qr_code_data " +
//                 "FROM UTILISATEUR u " +
//                 "JOIN PERSONNE p ON u.id = p.id " +
//                 "LIMIT ? OFFSET ?;";

//         try (Connection conn = DatabaseManager.getInstance().getConnection();
//              PreparedStatement pstmt = conn.prepareStatement(sql)) {
//             pstmt.setInt(1, limit);
//             pstmt.setInt(2, offset);
//             try (ResultSet rs = pstmt.executeQuery()) {
//                 while (rs.next()) list.add(mapResultSetToUtilisateur(rs));
//             }
//         } catch (SQLException e) {
//             System.err.println("Erreur lors de la récupération paginée : " + e.getMessage());
//         }
//         return list;
//     }

//     /**
//      * Récupère tous les utilisateurs (Ancienne méthode, gardée si besoin ailleurs).
//      */
//     public List<Utilisateur> getAllUtilisateurs() {
//         List<Utilisateur> utilisateurs = new ArrayList<>();
//         String sql = "SELECT p.*, u.role, u.face_image, u.face_vector, u.qr_code_data " +
//                 "FROM PERSONNE p " +
//                 "INNER JOIN UTILISATEUR u ON p.id = u.id " +
//                 "WHERE p.type = 'UTILISATEUR' " +
//                 "ORDER BY p.created_at DESC";

//         try (Connection conn = DatabaseManager.getInstance().getConnection();
//              Statement stmt = conn.createStatement();
//              ResultSet rs = stmt.executeQuery(sql)) {
//             while (rs.next()) utilisateurs.add(mapResultSetToUtilisateur(rs));
//         } catch (SQLException e) {
//             System.err.println("Erreur lors de la récupération des utilisateurs: " + e.getMessage());
//         }
//         return utilisateurs;
//     }

//     /**
//      * Récupère tous les utilisateurs actifs.
//      */
//     public List<Utilisateur> getActiveUtilisateurs() {
//         List<Utilisateur> utilisateurs = new ArrayList<>();
//         String sql = "SELECT p.*, u.role, u.face_image, u.face_vector, u.qr_code_data " +
//                 "FROM PERSONNE p " +
//                 "INNER JOIN UTILISATEUR u ON p.id = u.id " +
//                 "WHERE p.type = 'UTILISATEUR' AND p.is_active = 1 " +
//                 "ORDER BY p.full_name";

//         try (Connection conn = DatabaseManager.getInstance().getConnection();
//              Statement stmt = conn.createStatement();
//              ResultSet rs = stmt.executeQuery(sql)) {
//             while (rs.next()) utilisateurs.add(mapResultSetToUtilisateur(rs));
//         } catch (SQLException e) {
//             System.err.println("Erreur utilisateurs actifs: " + e.getMessage());
//         }
//         return utilisateurs;
//     }

//     /**
//      * Recherche des utilisateurs par nom.
//      */
//     public List<Utilisateur> searchUtilisateursByName(String name) {
//         List<Utilisateur> utilisateurs = new ArrayList<>();
//         String sql = "SELECT p.*, u.role, u.face_image, u.face_vector, u.qr_code_data " +
//                 "FROM PERSONNE p " +
//                 "INNER JOIN UTILISATEUR u ON p.id = u.id " +
//                 "WHERE p.type = 'UTILISATEUR' AND p.full_name LIKE ? " +
//                 "ORDER BY p.full_name";

//         try (Connection conn = DatabaseManager.getInstance().getConnection();
//              PreparedStatement pstmt = conn.prepareStatement(sql)) {
//             pstmt.setString(1, "%" + name + "%");
//             try (ResultSet rs = pstmt.executeQuery()) {
//                 while (rs.next()) utilisateurs.add(mapResultSetToUtilisateur(rs));
//             }
//         } catch (SQLException e) {
//             System.err.println("Erreur lors de la recherche: " + e.getMessage());
//         }
//         return utilisateurs;
//     }

//     /**
//      * Met à jour un utilisateur existant.
//      */
//     public boolean updateUtilisateur(Utilisateur utilisateur) {
//         Connection conn = null;
//         try {
//             conn = DatabaseManager.getInstance().getConnection();
//             conn.setAutoCommit(false);

//             // 1. Mettre à jour PERSONNE
//             String sqlPersonne = "UPDATE PERSONNE SET full_name = ?, email = ?, is_active = ? WHERE id = ?";
//             try (PreparedStatement pstmtPersonne = conn.prepareStatement(sqlPersonne)) {
//                 pstmtPersonne.setString(1, utilisateur.getFullName());
//                 pstmtPersonne.setString(2, utilisateur.getEmail());
//                 pstmtPersonne.setBoolean(3, utilisateur.isActive());
//                 pstmtPersonne.setInt(4, utilisateur.getId());
//                 pstmtPersonne.executeUpdate();
//             }

//             // 2. Mettre à jour UTILISATEUR
//             String sqlUtilisateur = "UPDATE UTILISATEUR SET role = ?, face_image = ?, face_vector = ?, qr_code_data = ? WHERE id = ?";
//             try (PreparedStatement pstmtUtilisateur = conn.prepareStatement(sqlUtilisateur)) {
//                 pstmtUtilisateur.setString(1, utilisateur.getRole());
//                 pstmtUtilisateur.setBytes(2, utilisateur.getFaceImage());
//                 pstmtUtilisateur.setBytes(3, utilisateur.getFaceVector());
//                 pstmtUtilisateur.setString(4, utilisateur.getQrCodeData());
//                 pstmtUtilisateur.setInt(5, utilisateur.getId());
//                 pstmtUtilisateur.executeUpdate();
//             }

//             conn.commit();
//             return true;

//         } catch (SQLException e) {
//             if (conn != null) {
//                 try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
//             }
//             System.err.println("Erreur lors de la mise à jour: " + e.getMessage());
//             return false;
//         } finally {
//             if (conn != null) {
//                 try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
//             }
//         }
//     }

//     /**
//      * Désactive un utilisateur (soft delete).
//      */
//     public boolean deactivateUtilisateur(int id) {
//         String sql = "UPDATE PERSONNE SET is_active = 0 WHERE id = ? AND type = 'UTILISATEUR'";
//         try (Connection conn = DatabaseManager.getInstance().getConnection();
//              PreparedStatement pstmt = conn.prepareStatement(sql)) {
//             pstmt.setInt(1, id);
//             return pstmt.executeUpdate() > 0;
//         } catch (SQLException e) {
//             System.err.println("Erreur désactivation: " + e.getMessage());
//             return false;
//         }
//     }

//     /**
//      * Supprime définitivement un utilisateur (hard delete).
//      */
//     public boolean deleteUtilisateur(int id) {
//         String sql = "DELETE FROM PERSONNE WHERE id = ? AND type = 'UTILISATEUR'";
//         try (Connection conn = DatabaseManager.getInstance().getConnection();
//              PreparedStatement pstmt = conn.prepareStatement(sql)) {
//             pstmt.setInt(1, id);
//             return pstmt.executeUpdate() > 0;
//         } catch (SQLException e) {
//             System.err.println("Erreur suppression: " + e.getMessage());
//             return false;
//         }
//     }

//     /**
//      * Compte le nombre d'utilisateurs actifs.
//      */
//     public int countActiveUtilisateurs() {
//         String sql = "SELECT COUNT(*) FROM PERSONNE WHERE type = 'UTILISATEUR' AND is_active = 1";
//         try (Connection conn = DatabaseManager.getInstance().getConnection();
//              Statement stmt = conn.createStatement();
//              ResultSet rs = stmt.executeQuery(sql)) {
//             if (rs.next()) return rs.getInt(1);
//         } catch (SQLException e) {
//             System.err.println("Erreur comptage actifs: " + e.getMessage());
//         }
//         return 0;
//     }

//     /**
//      * Mappe un ResultSet vers un objet Utilisateur.
//      */
//     private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
//         Utilisateur utilisateur = new Utilisateur();

//         utilisateur.setId(rs.getInt("id"));
//         utilisateur.setFullName(rs.getString("full_name"));
//         utilisateur.setEmail(rs.getString("email"));

//         // Gérer le int en boolean depuis SQLite
//         int isActive = 1;
//         try {
//             isActive = rs.getInt("is_active");
//         } catch (SQLException e) {
//             // Ignorer si la colonne n'est pas demandée dans certaines requêtes
//         }
//         utilisateur.setActive(isActive == 1);

//         try {
//             String createdAtStr = rs.getString("created_at");
//             if (createdAtStr != null) {
//                 utilisateur.setCreatedAt(LocalDateTime.parse(createdAtStr.replace(" ", "T")));
//             }
//         } catch (SQLException e) {}

//         utilisateur.setRole(rs.getString("role"));
//         utilisateur.setFaceImage(rs.getBytes("face_image"));

//         // Certaines requêtes paginées ne ramènent pas toujours ces éléments pour alléger, on sécurise
//         try { utilisateur.setFaceVector(rs.getBytes("face_vector")); } catch (SQLException e) {}
//         try { utilisateur.setQrCodeData(rs.getString("qr_code_data")); } catch (SQLException e) {}

//         return utilisateur;
//     }
// }

package com.facialaccess.dao;

import com.facialaccess.model.Utilisateur;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {

    // Constructeur vide (nous ne stockons plus de connexion globale)
    public UtilisateurDAO() {
    }

    /**
     * ENREGISTREMENT SÉCURISÉ EN DEUX ÉTAPES (Appelé par AddPersonnelController)
     */
    public boolean addUtilisateur(Utilisateur u) {
        String sqlPersonne = "INSERT INTO PERSONNE (full_name, email, type, is_active) VALUES (?, ?, 'UTILISATEUR', ?);";
        String sqlUtilisateur = "INSERT INTO UTILISATEUR (id, role, face_image, face_vector, qr_code_data) VALUES (?, ?, ?, ?, ?);";

        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false); // Mode transactionnel

            // Étape A : Insérer dans PERSONNE
            int generatedId = -1;
            try (PreparedStatement pstmtP = conn.prepareStatement(sqlPersonne, Statement.RETURN_GENERATED_KEYS)) {
                pstmtP.setString(1, u.getFullName());
                pstmtP.setString(2, u.getEmail());
                pstmtP.setBoolean(3, u.isActive());
                pstmtP.executeUpdate();

                try (ResultSet rs = pstmtP.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                    }
                }
            }

            if (generatedId == -1) {
                conn.rollback();
                return false;
            }

            // Étape B : Insérer dans UTILISATEUR
            try (PreparedStatement pstmtU = conn.prepareStatement(sqlUtilisateur)) {
                pstmtU.setInt(1, generatedId);
                pstmtU.setString(2, u.getRole() != null ? u.getRole() : "user");
                pstmtU.setBytes(3, u.getFaceImage());
                pstmtU.setBytes(4, u.getFaceVector());
                pstmtU.setString(5, u.getQrCodeData());
                pstmtU.executeUpdate();
            }

            conn.commit();
            u.setId(generatedId);
            return true;

        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Récupère un utilisateur par son ID.
     */
    public Utilisateur getUtilisateurById(int id) {
        String sql = "SELECT p.*, u.role, u.face_image, u.face_vector, u.qr_code_data " +
                "FROM PERSONNE p " +
                "INNER JOIN UTILISATEUR u ON p.id = u.id " +
                "WHERE p.id = ? AND p.type = 'UTILISATEUR'";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
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
        String sql = "SELECT p.*, u.role, u.face_image, u.face_vector, u.qr_code_data " +
                "FROM PERSONNE p " +
                "INNER JOIN UTILISATEUR u ON p.id = u.id " +
                "WHERE p.email = ? AND p.type = 'UTILISATEUR'";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return mapResultSetToUtilisateur(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération par email: " + e.getMessage());
        }
        return null;
    }

    /**
     * Compte le nombre total pour la pagination
     */
    public int getTotalUtilisateursCount() {
        String sql = "SELECT COUNT(*) FROM UTILISATEUR;";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur count : " + e.getMessage());
        }
        return 0;
    }

    /**
     * RÉCUPÉRATION PAGINÉE AVEC JOINTURE
     */
    public List<Utilisateur> getUtilisateursByPage(int limit, int offset) {
        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT p.id, p.full_name, p.email, p.is_active, p.created_at, u.role, u.face_image, u.face_vector, u.qr_code_data "
                +
                "FROM UTILISATEUR u " +
                "JOIN PERSONNE p ON u.id = p.id " +
                "LIMIT ? OFFSET ?;";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next())
                    list.add(mapResultSetToUtilisateur(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération paginée : " + e.getMessage());
        }
        return list;
    }

    /**
     * Récupère tous les utilisateurs (Ancienne méthode, gardée si besoin ailleurs).
     */
    public List<Utilisateur> getAllUtilisateurs() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT p.*, u.role, u.face_image, u.face_vector, u.qr_code_data " +
                "FROM PERSONNE p " +
                "INNER JOIN UTILISATEUR u ON p.id = u.id " +
                "WHERE p.type = 'UTILISATEUR' " +
                "ORDER BY p.created_at DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                utilisateurs.add(mapResultSetToUtilisateur(rs));
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
        String sql = "SELECT p.*, u.role, u.face_image, u.face_vector, u.qr_code_data " +
                "FROM PERSONNE p " +
                "INNER JOIN UTILISATEUR u ON p.id = u.id " +
                "WHERE p.type = 'UTILISATEUR' AND p.is_active = 1 " +
                "ORDER BY p.full_name";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                utilisateurs.add(mapResultSetToUtilisateur(rs));
        } catch (SQLException e) {
            System.err.println("Erreur utilisateurs actifs: " + e.getMessage());
        }
        return utilisateurs;
    }

    /**
     * Recherche des utilisateurs par nom.
     */
    public List<Utilisateur> searchUtilisateursByName(String name) {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT p.*, u.role, u.face_image, u.face_vector, u.qr_code_data " +
                "FROM PERSONNE p " +
                "INNER JOIN UTILISATEUR u ON p.id = u.id " +
                "WHERE p.type = 'UTILISATEUR' AND p.full_name LIKE ? " +
                "ORDER BY p.full_name";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next())
                    utilisateurs.add(mapResultSetToUtilisateur(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche: " + e.getMessage());
        }
        return utilisateurs;
    }

    /**
     * Met à jour un utilisateur existant.
     */
    public boolean updateUtilisateur(Utilisateur utilisateur) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 1. Mettre à jour PERSONNE
            String sqlPersonne = "UPDATE PERSONNE SET full_name = ?, email = ?, is_active = ? WHERE id = ?";
            try (PreparedStatement pstmtPersonne = conn.prepareStatement(sqlPersonne)) {
                pstmtPersonne.setString(1, utilisateur.getFullName());
                pstmtPersonne.setString(2, utilisateur.getEmail());
                pstmtPersonne.setBoolean(3, utilisateur.isActive());
                pstmtPersonne.setInt(4, utilisateur.getId());
                pstmtPersonne.executeUpdate();
            }

            // 2. Mettre à jour UTILISATEUR
            String sqlUtilisateur = "UPDATE UTILISATEUR SET role = ?, face_image = ?, face_vector = ?, qr_code_data = ? WHERE id = ?";
            try (PreparedStatement pstmtUtilisateur = conn.prepareStatement(sqlUtilisateur)) {
                pstmtUtilisateur.setString(1, utilisateur.getRole());
                pstmtUtilisateur.setBytes(2, utilisateur.getFaceImage());
                pstmtUtilisateur.setBytes(3, utilisateur.getFaceVector());
                pstmtUtilisateur.setString(4, utilisateur.getQrCodeData());
                pstmtUtilisateur.setInt(5, utilisateur.getId());
                pstmtUtilisateur.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Erreur lors de la mise à jour: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Désactive un utilisateur (soft delete).
     */
    public boolean deactivateUtilisateur(int id) {
        String sql = "UPDATE PERSONNE SET is_active = 0 WHERE id = ? AND type = 'UTILISATEUR'";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur désactivation: " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprime définitivement un utilisateur (hard delete).
     */
    public boolean deleteUtilisateur(int id) {
        String sql = "DELETE FROM PERSONNE WHERE id = ? AND type = 'UTILISATEUR'";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression: " + e.getMessage());
            return false;
        }
    }

    /**
     * Compte le nombre d'utilisateurs actifs.
     */
    public int countActiveUtilisateurs() {
        String sql = "SELECT COUNT(*) FROM PERSONNE WHERE type = 'UTILISATEUR' AND is_active = 1";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur comptage actifs: " + e.getMessage());
        }
        return 0;
    }

    // ===================== NOUVELLES MÉTHODES POUR LE FILTRAGE
    // =====================

    /**
     * Retourne le nombre total d'utilisateurs selon le filtre.
     * "ADMIN" : role contenant "admin" (insensible à la casse)
     * "UTILISATEUR" : role ne contenant pas "admin"
     * "ALL" ou autre : tous les utilisateurs (utilisez plutôt
     * getTotalUtilisateursCount)
     */
    public int getCountByFilter(String filter) {
        String sql;
        if ("ADMIN".equalsIgnoreCase(filter)) {
            sql = "SELECT COUNT(*) FROM UTILISATEUR u WHERE LOWER(u.role) LIKE '%admin%'";
        } else if ("UTILISATEUR".equalsIgnoreCase(filter)) {
            sql = "SELECT COUNT(*) FROM UTILISATEUR u WHERE LOWER(u.role) NOT LIKE '%admin%' OR u.role IS NULL";
        } else {
            // fallback to all
            return getTotalUtilisateursCount();
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur getCountByFilter: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retourne une page d'utilisateurs filtrés.
     * Même logique que getUtilisateursByPage mais avec une clause WHERE dynamique.
     */
    public List<Utilisateur> getUtilisateursByFilterPage(String filter, int limit, int offset) {
        List<Utilisateur> list = new ArrayList<>();
        String whereClause = "";

        if ("ADMIN".equalsIgnoreCase(filter)) {
            whereClause = "WHERE LOWER(u.role) LIKE '%admin%'";
        } else if ("UTILISATEUR".equalsIgnoreCase(filter)) {
            whereClause = "WHERE LOWER(u.role) NOT LIKE '%admin%' OR u.role IS NULL";
        }

        String sql = "SELECT p.id, p.full_name, p.email, p.is_active, p.created_at, u.role, u.face_image, u.face_vector, u.qr_code_data "
                +
                "FROM UTILISATEUR u " +
                "JOIN PERSONNE p ON u.id = p.id " +
                whereClause + " " +
                "LIMIT ? OFFSET ?;";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next())
                    list.add(mapResultSetToUtilisateur(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur getUtilisateursByFilterPage: " + e.getMessage());
        }
        return list;
    }

    // ===================== FIN DES NOUVELLES MÉTHODES =====================

    /**
     * Mappe un ResultSet vers un objet Utilisateur.
     */
    private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
        Utilisateur utilisateur = new Utilisateur();

        utilisateur.setId(rs.getInt("id"));
        utilisateur.setFullName(rs.getString("full_name"));
        utilisateur.setEmail(rs.getString("email"));

        // Gérer le int en boolean depuis SQLite
        int isActive = 1;
        try {
            isActive = rs.getInt("is_active");
        } catch (SQLException e) {
            // Ignorer si la colonne n'est pas demandée dans certaines requêtes
        }
        utilisateur.setActive(isActive == 1);

        try {
            String createdAtStr = rs.getString("created_at");
            if (createdAtStr != null) {
                utilisateur.setCreatedAt(LocalDateTime.parse(createdAtStr.replace(" ", "T")));
            }
        } catch (SQLException e) {
        }

        utilisateur.setRole(rs.getString("role"));
        utilisateur.setFaceImage(rs.getBytes("face_image"));

        // Certaines requêtes paginées ne ramènent pas toujours ces éléments pour
        // alléger, on sécurise
        try {
            utilisateur.setFaceVector(rs.getBytes("face_vector"));
        } catch (SQLException e) {
        }
        try {
            utilisateur.setQrCodeData(rs.getString("qr_code_data"));
        } catch (SQLException e) {
        }

        return utilisateur;
    }

    //modify the user status
    public boolean activateUtilisateur(int id) {
        String sql = "UPDATE PERSONNE SET is_active = 1 WHERE id = ? AND type = 'UTILISATEUR'";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur activation: " + e.getMessage());
            return false;
        }
    }
}