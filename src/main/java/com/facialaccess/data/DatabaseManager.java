package com.facialaccess.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Gestionnaire de connexion à la base de données SQLite.
 * Initialise le schéma et fournit les connexions.
 */
public class DatabaseManager {
    
    private static final String DB_URL = "jdbc:sqlite:facial_access.db";
    private static DatabaseManager instance;
    private Connection connection;
    
    private DatabaseManager() {
        try {
            // Charger le driver SQLite
            Class.forName("org.sqlite.JDBC");
            // Créer la connexion
            connection = DriverManager.getConnection(DB_URL);
            // Initialiser le schéma
            initializeSchema();
            System.out.println("✓ Connexion à la base de données établie");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("✗ Erreur lors de la connexion à la base de données: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Récupère l'instance unique du DatabaseManager (Singleton).
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Retourne la connexion active.
     */
    public Connection getConnection() {
        try {
            // Vérifier si la connexion est toujours valide
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            System.err.println("✗ Erreur lors de la récupération de la connexion: " + e.getMessage());
        }
        return connection;
    }
    
    /**
     * Initialise le schéma de la base de données depuis schema.sql.
     */
    private void initializeSchema() {
        try {
            // Vérifier si les tables existent déjà
            if (tablesExist()) {
                System.out.println("✓ Tables déjà existantes, schéma non réinitialisé");
                return;
            }
            
            InputStream is = getClass().getResourceAsStream("/db/schema.sql");
            if (is == null) {
                System.err.println("✗ Fichier schema.sql introuvable dans /db/");
                return;
            }
            
            // Lire le contenu du fichier SQL ligne par ligne
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder currentCommand = new StringBuilder();
            String line;
            int executed = 0;
            
            Statement stmt = connection.createStatement();
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Ignorer les lignes vides et les commentaires
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                
                // Ajouter la ligne à la commande courante
                currentCommand.append(line).append(" ");
                
                // Si la ligne se termine par un point-virgule, exécuter la commande
                if (line.endsWith(";")) {
                    String command = currentCommand.toString().trim();
                    // Retirer le point-virgule final
                    command = command.substring(0, command.length() - 1);
                    
                    try {
                        stmt.execute(command);
                        executed++;
                        System.out.println("✓ Exécuté: " + command.substring(0, Math.min(60, command.length())) + "...");
                    } catch (SQLException e) {
                        System.err.println("✗ Erreur SQL: " + e.getMessage());
                        System.err.println("   Commande: " + command.substring(0, Math.min(100, command.length())));
                    }
                    
                    // Réinitialiser pour la prochaine commande
                    currentCommand = new StringBuilder();
                }
            }
            
            reader.close();
            stmt.close();
            System.out.println("✓ Schema de base de donnees initialisé (" + executed + " commandes executees)");
            
        } catch (Exception e) {
            System.err.println("✗ Erreur lors de l'initialisation du schéma: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Vérifie si les tables existent déjà.
     */
    private boolean tablesExist() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeQuery("SELECT 1 FROM USERS LIMIT 1");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Ferme la connexion à la base de données.
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Connexion a la base de donnees fermee");
            }
        } catch (SQLException e) {
            System.err.println("✗ Erreur lors de la fermeture de la connexion: " + e.getMessage());
        }
    }
}
