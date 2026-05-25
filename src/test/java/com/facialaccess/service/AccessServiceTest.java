package com.facialaccess.service;

import com.facialaccess.dao.DatabaseManager;
import com.facialaccess.model.AccessLog;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires et d'intégration pour AccessService.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccessServiceTest {
    
    private static AccessService accessService;
    
    @BeforeAll
    static void setUp() {
        DatabaseManager.getInstance();
        accessService = new AccessService();
    }
    
    @AfterAll
    static void tearDown() {
        DatabaseManager.getInstance().close();
    }
    
    @Test
    @Order(1)
    @DisplayName("Test seuil de confiance minimum")
    void testMinConfidenceThreshold() {
        // Act
        double threshold = accessService.getMinConfidenceThreshold();
        
        // Assert
        assertEquals(0.75, threshold, "Le seuil devrait être 0.75");
    }
    
    @Test
    @Order(2)
    @DisplayName("Test vérification accès accordé")
    void testShouldGrantAccessAboveThreshold() {
        // Act & Assert
        assertTrue(accessService.shouldGrantAccess(0.80), 
            "Devrait accorder l'accès avec 0.80");
        assertTrue(accessService.shouldGrantAccess(0.95), 
            "Devrait accorder l'accès avec 0.95");
        assertTrue(accessService.shouldGrantAccess(0.75), 
            "Devrait accorder l'accès avec 0.75 (seuil)");
    }
    
    @Test
    @Order(3)
    @DisplayName("Test vérification accès refusé")
    void testShouldGrantAccessBelowThreshold() {
        // Act & Assert
        assertFalse(accessService.shouldGrantAccess(0.74), 
            "Devrait refuser l'accès avec 0.74");
        assertFalse(accessService.shouldGrantAccess(0.50), 
            "Devrait refuser l'accès avec 0.50");
        assertFalse(accessService.shouldGrantAccess(0.20), 
            "Devrait refuser l'accès avec 0.20");
    }
    
    @Test
    @Order(4)
    @DisplayName("Test enregistrement accès facial accordé")
    void testLogFaceAccessGranted() {
        // Act
        boolean logged = accessService.logFaceAccess(null, 0.85);
        
        // Assert
        assertTrue(logged, "L'enregistrement devrait réussir");
        
        // Vérifier que le log existe
        List<AccessLog> grantedLogs = accessService.getAccessLogsByStatus("GRANTED");
        assertTrue(grantedLogs.stream().anyMatch(log -> 
            "FACE".equals(log.getIdentificationMethod()) && 
            log.getConfidenceScore() == 0.85
        ), "Le log devrait exister");
    }
    
    @Test
    @Order(5)
    @DisplayName("Test enregistrement accès facial refusé")
    void testLogFaceAccessDenied() {
        // Act
        boolean logged = accessService.logFaceAccess(null, 0.60);
        
        // Assert
        assertTrue(logged, "L'enregistrement devrait réussir");
        
        // Vérifier que le log existe
        List<AccessLog> deniedLogs = accessService.getAccessLogsByStatus("DENIED");
        assertTrue(deniedLogs.stream().anyMatch(log -> 
            "FACE".equals(log.getIdentificationMethod()) && 
            log.getConfidenceScore() == 0.60
        ), "Le log devrait exister");
    }
    
    @Test
    @Order(6)
    @DisplayName("Test enregistrement accès QR valide")
    void testLogQRAccessValid() {
        // Act
        boolean logged = accessService.logQRAccess(null, true);
        
        // Assert
        assertTrue(logged, "L'enregistrement devrait réussir");
        
        // Vérifier que le log existe
        List<AccessLog> grantedLogs = accessService.getAccessLogsByStatus("GRANTED");
        assertTrue(grantedLogs.stream().anyMatch(log -> 
            "QR".equals(log.getIdentificationMethod()) && 
            log.getConfidenceScore() == 1.0
        ), "Le log QR devrait exister");
    }
    
    @Test
    @Order(7)
    @DisplayName("Test enregistrement accès QR invalide")
    void testLogQRAccessInvalid() {
        // Act
        boolean logged = accessService.logQRAccess(null, false);
        
        // Assert
        assertTrue(logged, "L'enregistrement devrait réussir");
        
        // Vérifier que le log existe
        List<AccessLog> deniedLogs = accessService.getAccessLogsByStatus("DENIED");
        assertTrue(deniedLogs.stream().anyMatch(log -> 
            "QR".equals(log.getIdentificationMethod()) && 
            log.getConfidenceScore() == 0.0
        ), "Le log QR refusé devrait exister");
    }
    
    @Test
    @Order(8)
    @DisplayName("Test comptage total des accès")
    void testCountTotalAccess() {
        // Act
        int total = accessService.countTotalAccess();
        
        // Assert
        assertTrue(total >= 4, "Il devrait y avoir au moins 4 logs");
    }
    
    @Test
    @Order(9)
    @DisplayName("Test comptage accès accordés et refusés")
    void testCountGrantedAndDenied() {
        // Act
        int granted = accessService.countGrantedAccess();
        int denied = accessService.countDeniedAccess();
        int total = accessService.countTotalAccess();
        
        // Assert
        assertTrue(granted > 0, "Il devrait y avoir des accès accordés");
        assertTrue(denied > 0, "Il devrait y avoir des accès refusés");
        assertEquals(total, granted + denied, 
            "Le total devrait être la somme des accordés et refusés");
    }
    
    @Test
    @Order(10)
    @DisplayName("Test calcul taux d'accès accordés")
    void testGetGrantedAccessRate() {
        // Act
        double rate = accessService.getGrantedAccessRate();
        
        // Assert
        assertTrue(rate >= 0 && rate <= 100, 
            "Le taux devrait être entre 0 et 100");
    }
    
    @Test
    @Order(11)
    @DisplayName("Test récupération logs récents (24h)")
    void testGetRecentAccessLogs() {
        // Act
        List<AccessLog> recentLogs = accessService.getRecentAccessLogs();
        
        // Assert
        assertFalse(recentLogs.isEmpty(), 
            "Il devrait y avoir des logs récents");
    }
    
    @Test
    @Order(12)
    @DisplayName("Test récupération logs hebdomadaires")
    void testGetWeeklyAccessLogs() {
        // Act
        List<AccessLog> weeklyLogs = accessService.getWeeklyAccessLogs();
        
        // Assert
        assertFalse(weeklyLogs.isEmpty(), 
            "Il devrait y avoir des logs de la semaine");
    }
    
    @Test
    @Order(13)
    @DisplayName("Test récupération logs par période")
    void testGetAccessLogsByDateRange() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime tomorrow = now.plusDays(1);
        
        // Act
        List<AccessLog> logs = accessService.getAccessLogsByDateRange(yesterday, tomorrow);
        
        // Assert
        assertFalse(logs.isEmpty(), 
            "Il devrait y avoir des logs dans cette période");
    }
    
    @Test
    @Order(14)
    @DisplayName("Test récupération logs avec dates invalides")
    void testGetAccessLogsByDateRangeInvalid() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        
        // Act - Date de début après date de fin
        List<AccessLog> logs = accessService.getAccessLogsByDateRange(now, yesterday);
        
        // Assert
        assertTrue(logs.isEmpty(), 
            "Devrait retourner une liste vide avec des dates invalides");
    }
    
    @Test
    @Order(15)
    @DisplayName("Test récupération logs par statut invalide")
    void testGetAccessLogsByInvalidStatus() {
        // Act
        List<AccessLog> logs = accessService.getAccessLogsByStatus("INVALID");
        
        // Assert
        assertTrue(logs.isEmpty(), 
            "Devrait retourner une liste vide avec un statut invalide");
    }
    
    @Test
    @Order(16)
    @DisplayName("Test récupération tous les logs")
    void testGetAllAccessLogs() {
        // Act
        List<AccessLog> allLogs = accessService.getAllAccessLogs();
        
        // Assert
        assertFalse(allLogs.isEmpty(), "Il devrait y avoir des logs");
        assertEquals(accessService.countTotalAccess(), allLogs.size(),
            "Le nombre devrait correspondre au comptage");
    }
    
    @Test
    @Order(17)
    @DisplayName("Test nettoyage anciens logs")
    void testCleanOldLogs() {
        // Arrange - Créer un log "ancien"
        accessService.logFaceAccess(null, 0.88);
        int countBefore = accessService.countTotalAccess();
        
        // Act - Supprimer les logs plus anciens que demain
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        boolean cleaned = accessService.cleanOldLogs(tomorrow);
        
        // Assert
        assertTrue(cleaned, "Le nettoyage devrait réussir");
        
        int countAfter = accessService.countTotalAccess();
        assertTrue(countAfter < countBefore, 
            "Le nombre de logs devrait avoir diminué");
    }
    
    @Test
    @Order(18)
    @DisplayName("Test intégration avec UserService")
    void testIntegrationWithUserService() {
        // Arrange - Créer un utilisateur
        UserService userService = new UserService();
        userService.createUser("Test Access", "access@test.com", "user");
        var user = userService.getUserByEmail("access@test.com");
        assertNotNull(user, "L'utilisateur devrait être créé");
        
        // Act - Enregistrer des accès pour cet utilisateur
        accessService.logFaceAccess(user.getId(), 0.92);
        accessService.logQRAccess(user.getId(), true);
        
        // Assert - Vérifier les logs de l'utilisateur
        List<AccessLog> userLogs = accessService.getUserAccessLogs(user.getId());
        assertEquals(2, userLogs.size(), 
            "L'utilisateur devrait avoir 2 logs");
        
        // Nettoyage
        userService.deleteUser(user.getId());
    }
}
