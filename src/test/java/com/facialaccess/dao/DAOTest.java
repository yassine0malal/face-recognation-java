package com.facialaccess.dao;

import com.facialaccess.model.Admin;
import com.facialaccess.model.Utilisateur;
import com.facialaccess.model.AccessLog;
import com.facialaccess.service.SecurityService;

import java.time.LocalDateTime;

/**
 * Classe de test pour vérifier le fonctionnement des DAO.
 * Lance des opérations CRUD sur la base de données.
 */
public class DAOTest {
    
    public static void main(String[] args) {
        System.out.println("=== DÉBUT DES TESTS DAO ===\n");
        
        // Initialiser la base de données
        DatabaseManager.getInstance();
        
        // Tests
        testAdminDAO();
        testUtilisateurDAO();
        testAccessLogDAO();
        
        System.out.println("\n=== FIN DES TESTS DAO ===");
        
        // Fermer la connexion
        DatabaseManager.getInstance().close();
    }
    
    /**
     * Test de AdminDAO.
     */
    private static void testAdminDAO() {
        System.out.println("--- Test AdminDAO ---");
        AdminDAO adminDAO = new AdminDAO();
        
        // 1. Récupérer l'admin par défaut
        Admin admin = adminDAO.getAdminByUsername("admin");
        if (admin != null) {
            System.out.println("✓ Admin trouvé: " + admin);
            
            // 2. Vérifier le mot de passe
            boolean passwordValid = SecurityService.verifyPassword("admin123", admin.getPasswordHash());
            System.out.println("✓ Vérification mot de passe: " + (passwordValid ? "OK" : "ÉCHEC"));
            
            // 3. Tester l'incrémentation des tentatives échouées
            admin.incrementFailedAttempts();
            boolean updated = adminDAO.updateFailedAttempts(admin.getId(), admin.getFailedAttempts(), null);
            System.out.println("✓ Mise à jour tentatives échouées: " + (updated ? "OK" : "ÉCHEC"));
            
            // 4. Réinitialiser les tentatives
            boolean reset = adminDAO.resetFailedAttempts(admin.getId());
            System.out.println("✓ Réinitialisation tentatives: " + (reset ? "OK" : "ÉCHEC"));
        } else {
            System.out.println("✗ Admin non trouvé");
        }
        System.out.println();
    }
    
    /**
     * Test de UtilisateurDAO.
     */
    private static void testUtilisateurDAO() {
        System.out.println("--- Test UtilisateurDAO ---");
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        
        // 1. Créer un nouvel utilisateur
        Utilisateur user = new Utilisateur();
        user.setFullName("Jean Dupont");
        user.setEmail("jean.dupont@test.com");
        user.setRole("user");
        user.setActive(true);
        user.setQrCodeData("QR123456");
        
        boolean added = utilisateurDAO.addUtilisateur(user);
        System.out.println("✓ Ajout utilisateur: " + (added ? "OK (ID=" + user.getId() + ")" : "ÉCHEC"));
        
        if (added) {
            // 2. Récupérer l'utilisateur par ID
            Utilisateur retrieved = utilisateurDAO.getUtilisateurById(user.getId());
            System.out.println("✓ Récupération par ID: " + (retrieved != null ? "OK" : "ÉCHEC"));
            
            // 3. Récupérer par email
            Utilisateur byEmail = utilisateurDAO.getUtilisateurByEmail("jean.dupont@test.com");
            System.out.println("✓ Récupération par email: " + (byEmail != null ? "OK" : "ÉCHEC"));
            
            // 4. Mettre à jour l'utilisateur
            user.setFullName("Jean Dupont Modifié");
            boolean updated = utilisateurDAO.updateUtilisateur(user);
            System.out.println("✓ Mise à jour: " + (updated ? "OK" : "ÉCHEC"));
            
            // 5. Compter les utilisateurs
            int count = utilisateurDAO.countUtilisateurs();
            System.out.println("✓ Nombre d'utilisateurs: " + count);
            
            // 6. Rechercher par nom
            var results = utilisateurDAO.searchUtilisateursByName("Dupont");
            System.out.println("✓ Recherche par nom: " + results.size() + " résultat(s)");
            
            // 7. Désactiver l'utilisateur
            boolean deactivated = utilisateurDAO.deactivateUtilisateur(user.getId());
            System.out.println("✓ Désactivation: " + (deactivated ? "OK" : "ÉCHEC"));
            
            // 8. Supprimer l'utilisateur (nettoyage)
            boolean deleted = utilisateurDAO.deleteUtilisateur(user.getId());
            System.out.println("✓ Suppression: " + (deleted ? "OK" : "ÉCHEC"));
        }
        System.out.println();
    }
    
    /**
     * Test de AccessLogDAO.
     */
    private static void testAccessLogDAO() {
        System.out.println("--- Test AccessLogDAO ---");
        AccessLogDAO accessLogDAO = new AccessLogDAO();
        
        // 1. Créer un log d'accès accordé
        AccessLog logGranted = new AccessLog();
        logGranted.setUserId(null); // Accès sans utilisateur identifié
        logGranted.setStatus("GRANTED");
        logGranted.setConfidenceScore(0.95);
        logGranted.setIdentificationMethod("FACE");
        
        boolean addedGranted = accessLogDAO.addAccessLog(logGranted);
        System.out.println("✓ Ajout log GRANTED: " + (addedGranted ? "OK (ID=" + logGranted.getId() + ")" : "ÉCHEC"));
        
        // 2. Créer un log d'accès refusé
        AccessLog logDenied = new AccessLog();
        logDenied.setUserId(null);
        logDenied.setStatus("DENIED");
        logDenied.setConfidenceScore(0.45);
        logDenied.setIdentificationMethod("FACE");
        
        boolean addedDenied = accessLogDAO.addAccessLog(logDenied);
        System.out.println("✓ Ajout log DENIED: " + (addedDenied ? "OK (ID=" + logDenied.getId() + ")" : "ÉCHEC"));
        
        // 3. Récupérer tous les logs
        var allLogs = accessLogDAO.getAllAccessLogs();
        System.out.println("✓ Nombre total de logs: " + allLogs.size());
        
        // 4. Compter les accès accordés
        int grantedCount = accessLogDAO.countGrantedAccess();
        System.out.println("✓ Accès accordés: " + grantedCount);
        
        // 5. Compter les accès refusés
        int deniedCount = accessLogDAO.countDeniedAccess();
        System.out.println("✓ Accès refusés: " + deniedCount);
        
        // 6. Récupérer par statut
        var grantedLogs = accessLogDAO.getAccessLogsByStatus("GRANTED");
        System.out.println("✓ Logs GRANTED: " + grantedLogs.size());
        
        // 7. Récupérer par période
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        var recentLogs = accessLogDAO.getAccessLogsByDateRange(yesterday, now);
        System.out.println("✓ Logs dernières 24h: " + recentLogs.size());
        
        System.out.println();
    }
}
