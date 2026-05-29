package com.facialaccess.dao;

import com.facialaccess.model.AccessLog;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour AccessLogDAO.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccessLogDAOTest {
    
    private static AccessLogDAO accessLogDAO;
    private static Integer testLogId1;
    private static Integer testLogId2;
    
    @BeforeAll
    static void setUp() {
        DatabaseManager.getInstance();
        accessLogDAO = new AccessLogDAO();
    }
    
    @AfterAll
    static void tearDown() {
        DatabaseManager.getInstance().close();
    }
    
    @Test
    @Order(1)
    @DisplayName("Test ajout log d'accès accordé")
    void testAddAccessLogGranted() {
        // Arrange
        AccessLog log = new AccessLog();
        log.setUserId(null); // Accès sans utilisateur identifié
        log.setStatus("GRANTED");
        log.setConfidenceScore(0.95);
        log.setIdentificationMethod("FACE");
        
        // Act
        boolean added = accessLogDAO.addAccessLog(log);
        
        // Assert
        assertTrue(added, "L'ajout devrait réussir");
        assertNotNull(log.getId(), "L'ID devrait être généré");
        testLogId1 = log.getId();
    }
    
    @Test
    @Order(2)
    @DisplayName("Test ajout log d'accès refusé")
    void testAddAccessLogDenied() {
        // Arrange
        AccessLog log = new AccessLog();
        log.setUserId(null);
        log.setStatus("DENIED");
        log.setConfidenceScore(0.45);
        log.setIdentificationMethod("FACE");
        
        // Act
        boolean added = accessLogDAO.addAccessLog(log);
        
        // Assert
        assertTrue(added, "L'ajout devrait réussir");
        assertNotNull(log.getId(), "L'ID devrait être généré");
        testLogId2 = log.getId();
    }
    
    @Test
    @Order(3)
    @DisplayName("Test récupération tous les logs")
    void testGetAllAccessLogs() {
        // Act
        List<AccessLog> logs = accessLogDAO.getAllAccessLogs();
        
        // Assert
        assertFalse(logs.isEmpty(), "Il devrait y avoir au moins un log");
        assertTrue(logs.size() >= 2, "Il devrait y avoir au moins 2 logs");
    }
    
    @Test
    @Order(4)
    @DisplayName("Test comptage total des logs")
    void testCountAccessLogs() {
        // Act
        int count = accessLogDAO.countAccessLogs();
        
        // Assert
        assertTrue(count >= 2, "Il devrait y avoir au moins 2 logs");
    }
    
    @Test
    @Order(5)
    @DisplayName("Test comptage accès accordés")
    void testCountGrantedAccess() {
        // Act
        int grantedCount = accessLogDAO.countGrantedAccess();
        
        // Assert
        assertTrue(grantedCount >= 1, "Il devrait y avoir au moins 1 accès accordé");
    }
    
    @Test
    @Order(6)
    @DisplayName("Test comptage accès refusés")
    void testCountDeniedAccess() {
        // Act
        int deniedCount = accessLogDAO.countDeniedAccess();
        
        // Assert
        assertTrue(deniedCount >= 1, "Il devrait y avoir au moins 1 accès refusé");
    }
    
    @Test
    @Order(7)
    @DisplayName("Test récupération logs par statut GRANTED")
    void testGetAccessLogsByStatusGranted() {
        // Act
        List<AccessLog> grantedLogs = accessLogDAO.getAccessLogsByStatus("GRANTED");
        
        // Assert
        assertFalse(grantedLogs.isEmpty(), "Il devrait y avoir des logs GRANTED");
        assertTrue(grantedLogs.stream().allMatch(log -> "GRANTED".equals(log.getStatus())),
            "Tous les logs devraient avoir le statut GRANTED");
    }
    
    @Test
    @Order(8)
    @DisplayName("Test récupération logs par statut DENIED")
    void testGetAccessLogsByStatusDenied() {
        // Act
        List<AccessLog> deniedLogs = accessLogDAO.getAccessLogsByStatus("DENIED");
        
        // Assert
        assertFalse(deniedLogs.isEmpty(), "Il devrait y avoir des logs DENIED");
        assertTrue(deniedLogs.stream().allMatch(log -> "DENIED".equals(log.getStatus())),
            "Tous les logs devraient avoir le statut DENIED");
    }
    
    @Test
    @Order(9)
    @DisplayName("Test récupération logs par période")
    void testGetAccessLogsByDateRange() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime tomorrow = now.plusDays(1);
        
        // Act
        List<AccessLog> recentLogs = accessLogDAO.getAccessLogsByDateRange(yesterday, tomorrow);
        
        // Assert
        assertFalse(recentLogs.isEmpty(), "Il devrait y avoir des logs dans cette période");
        assertTrue(recentLogs.size() >= 2, "Les logs de test devraient être dans cette période");
    }
    
    @Test
    @Order(10)
    @DisplayName("Test récupération logs par utilisateur")
    void testGetAccessLogsByUserId() {
        // Arrange - Créer un utilisateur de test
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        com.facialaccess.model.Utilisateur user = new com.facialaccess.model.Utilisateur();
        user.setFullName("Test User For Logs");
        user.setEmail("testlogs@test.com");
        user.setActive(true);
        
        boolean userAdded = utilisateurDAO.addUtilisateur(user);
        assertTrue(userAdded, "L'utilisateur de test devrait être créé");
        
        // Créer un log pour cet utilisateur
        AccessLog log = new AccessLog();
        log.setUserId(user.getId());
        log.setStatus("GRANTED");
        log.setConfidenceScore(0.88);
        log.setIdentificationMethod("QR");
        
        boolean logAdded = accessLogDAO.addAccessLog(log);
        assertTrue(logAdded, "Le log devrait être ajouté");
        
        // Act
        List<AccessLog> userLogs = accessLogDAO.getAccessLogsByUserId(user.getId());
        
        // Assert
        assertFalse(userLogs.isEmpty(), "Il devrait y avoir des logs pour cet utilisateur");
        assertTrue(userLogs.stream().allMatch(l -> l.getUserId().equals(user.getId())),
            "Tous les logs devraient appartenir à cet utilisateur");
        
        // Nettoyage
        utilisateurDAO.deleteUtilisateur(user.getId());
    }
    
    @Test
    @Order(11)
    @DisplayName("Test suppression anciens logs")
    void testDeleteOldLogs() {
        // Arrange - Créer un log "ancien"
        AccessLog oldLog = new AccessLog();
        oldLog.setUserId(null);
        oldLog.setStatus("GRANTED");
        oldLog.setConfidenceScore(0.75);
        oldLog.setIdentificationMethod("FACE");
        
        accessLogDAO.addAccessLog(oldLog);
        
        int countBefore = accessLogDAO.countAccessLogs();
        
        // Act - Supprimer les logs plus anciens que dans 1 jour
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        boolean deleted = accessLogDAO.deleteOldLogs(tomorrow);
        
        // Assert
        assertTrue(deleted, "La suppression devrait réussir");
        
        int countAfter = accessLogDAO.countAccessLogs();
        assertTrue(countAfter < countBefore, 
            "Le nombre de logs devrait avoir diminué");
    }
    
    @Test
    @Order(12)
    @DisplayName("Test vérification des champs AccessLog")
    void testAccessLogFields() {
        // Arrange
        AccessLog log = new AccessLog();
        log.setUserId(123);
        log.setStatus("GRANTED");
        log.setConfidenceScore(0.92);
        log.setIdentificationMethod("FACE");
        log.setAccessedAt(LocalDateTime.now());
        log.setUserName("Test User");
        
        // Assert
        assertEquals(123, log.getUserId());
        assertEquals("GRANTED", log.getStatus());
        assertEquals(0.92, log.getConfidenceScore());
        assertEquals("FACE", log.getIdentificationMethod());
        assertNotNull(log.getAccessedAt());
        assertEquals("Test User", log.getUserName());
    }
}
