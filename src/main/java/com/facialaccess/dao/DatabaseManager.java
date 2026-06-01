package com.facialaccess.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestionnaire central de la base de données SQLite.
 * Initialise les tables en respectant l'ordre d'héritage : PERSONNE -> UTILISATEUR / ADMIN.
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:facial_access.db";

    private DatabaseManager() {
        // L'initialisation se fait désormais via getConnection() pour garantir une connexion fraîche
        getConnection();
        initializeSchema();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * LE FIX EST ICI : Vérifie l'état de la connexion.
     * Si elle est fermée (par un try-with-resources du DAO), on la recrée.
     */
    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
                
                // Réactiver les clés étrangères à chaque nouvelle connexion
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du rétablissement de la connexion : " + e.getMessage());
        }
        return connection;
    }

    /**
     * Crée les tables dans l'ordre de dépendance hiérarchique.
     */
    private void initializeSchema() {
        // Demander une connexion valide pour l'initialisation
        Connection conn = getConnection(); 
        if (conn == null) return;

        try (Statement stmt = conn.createStatement()) {
            
            // ÉTAPE 1 : Table Mère (PERSONNE)
            stmt.execute("CREATE TABLE IF NOT EXISTS PERSONNE (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "full_name TEXT NOT NULL, " +
                    "email TEXT UNIQUE, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "is_active INTEGER DEFAULT 1, " +
                    "type TEXT NOT NULL CHECK(type IN ('ADMIN', 'UTILISATEUR'))" +
                    ");");
            System.out.println("✓ Étape 1 : Table mère PERSONNE initialisée.");

            // ÉTAPE 2 : Table Fille 1 (UTILISATEUR)
            stmt.execute("CREATE TABLE IF NOT EXISTS UTILISATEUR (" +
                    "id INTEGER PRIMARY KEY, " +
                    "role TEXT DEFAULT 'user', " +
                    "face_image BLOB, " +
                    "face_vector BLOB, " +
                    "qr_code_data TEXT, " +
                    "FOREIGN KEY (id) REFERENCES PERSONNE(id) ON DELETE CASCADE" +
                    ");");
            System.out.println("✓ Étape 2 : Table fille UTILISATEUR initialisée.");

            // ÉTAPE 3 : Table Fille 2 (ADMIN)
            stmt.execute("CREATE TABLE IF NOT EXISTS ADMIN (" +
                    "id INTEGER PRIMARY KEY, " +
                    "username TEXT NOT NULL UNIQUE, " +
                    "password_hash TEXT NOT NULL, " +
                    "failed_attempts INTEGER DEFAULT 0, " +
                    "locked_until DATETIME, " +
                    "FOREIGN KEY (id) REFERENCES PERSONNE(id) ON DELETE CASCADE" +
                    ");");
            System.out.println("✓ Étape 3 : Table fille ADMIN initialisée.");

            // ÉTAPE 4 : Table dépendante des logs (ACCESS_LOGS)
            stmt.execute("CREATE TABLE IF NOT EXISTS ACCESS_LOGS (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "status TEXT NOT NULL CHECK(status IN ('GRANTED', 'DENIED')), " +
                    "confidence_score REAL, " +
                    "identification_method TEXT CHECK(identification_method IN ('FACE', 'QR')), " +
                    "accessed_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (user_id) REFERENCES UTILISATEUR(id) ON DELETE CASCADE" +
                    ");");
            System.out.println("✓ Étape 4 : Table ACCESS_LOGS initialisée.");

            // ÉTAPE 4b : Table des logs d'actions administrateur (ADMIN_ACTION_LOGS)
            stmt.execute("CREATE TABLE IF NOT EXISTS ADMIN_ACTION_LOGS (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "admin_username TEXT NOT NULL, " +
                    "action_type TEXT NOT NULL, " +
                    "details TEXT NOT NULL, " +
                    "action_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");");
            System.out.println("✓ Étape 4b : Table ADMIN_ACTION_LOGS initialisée.");

            // ÉTAPE 5 : Création des index de performance
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_personne_email ON PERSONNE(email);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_personne_type ON PERSONNE(type);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_admin_username ON ADMIN(username);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_utilisateur_id ON UTILISATEUR(id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_access_logs_user_id ON ACCESS_LOGS(user_id);");
            System.out.println("✓ Étape 5 : Index de performance configurés.");

            // ÉTAPE 6 : Insertion sécurisée du compte Admin par défaut
            conn.setAutoCommit(false);
            
            stmt.execute("INSERT OR IGNORE INTO PERSONNE (id, full_name, email, type, is_active) " +
                    "VALUES (1, 'Administrateur', 'admin@facialaccess.com', 'ADMIN', 1);");
            
            stmt.execute("INSERT OR IGNORE INTO ADMIN (id, username, password_hash, failed_attempts, locked_until) " +
                    "VALUES (1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 0, NULL);");
            
            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("✓ Étape 6 : Compte administrateur initial par défaut injecté avec succès.");

        } catch (SQLException e) {
            System.err.println("Erreur lors de la construction ordonnée du schéma : " + e.getMessage());
            try {
                if (conn != null && !conn.isClosed() && !conn.getAutoCommit()) {
                    conn.rollback();
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Ferme proprement la connexion globale (généralement appelé à l'arrêt de l'application).
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Connexion à la base de données fermée proprement.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la base de données : " + e.getMessage());
        }
    }
}