package com.facialaccess.dao;

import com.facialaccess.model.Admin;
import com.facialaccess.service.SecurityService;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour AdminDAO.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminDAOTest {
    
    private static AdminDAO adminDAO;
    
    @BeforeAll
    static void setUp() {
        // Initialiser la base de données
        DatabaseManager.getInstance();
        adminDAO = new AdminDAO();
    }
    
    @AfterAll
    static void tearDown() {
        // Fermer la connexion
        DatabaseManager.getInstance().close();
    }
    
    @Test
    @Order(1)
    @DisplayName("Test récupération admin par username")
    void testGetAdminByUsername() {
        // Arrange & Act
        Admin admin = adminDAO.getAdminByUsername("admin");
        
        // Assert
        assertNotNull(admin, "L'admin devrait être trouvé");
        assertEquals("admin", admin.getUsername(), "Le username devrait être 'admin'");
        assertEquals("Administrateur", admin.getFullName(), "Le nom complet devrait être 'Administrateur'");
        assertTrue(admin.isActive(), "L'admin devrait être actif");
    }
    
    @Test
    @Order(2)
    @DisplayName("Test vérification mot de passe")
    void testPasswordVerification() {
        // Arrange
        Admin admin = adminDAO.getAdminByUsername("admin");
        assertNotNull(admin, "L'admin doit exister pour ce test");
        
        // Act
        boolean validPassword = SecurityService.verifyPassword("admin123", admin.getPasswordHash());
        boolean invalidPassword = SecurityService.verifyPassword("wrongpassword", admin.getPasswordHash());
        
        // Assert
        assertTrue(validPassword, "Le mot de passe 'admin123' devrait être valide");
        assertFalse(invalidPassword, "Un mauvais mot de passe devrait être invalide");
    }
    
    @Test
    @Order(3)
    @DisplayName("Test incrémentation tentatives échouées")
    void testIncrementFailedAttempts() {
        // Arrange
        Admin admin = adminDAO.getAdminByUsername("admin");
        assertNotNull(admin, "L'admin doit exister pour ce test");
        
        int initialAttempts = admin.getFailedAttempts();
        admin.incrementFailedAttempts();
        
        // Act
        boolean updated = adminDAO.updateFailedAttempts(
            admin.getId(), 
            admin.getFailedAttempts(), 
            null
        );
        
        // Assert
        assertTrue(updated, "La mise à jour devrait réussir");
        
        // Vérifier en base
        Admin updatedAdmin = adminDAO.getAdminByUsername("admin");
        assertEquals(initialAttempts + 1, updatedAdmin.getFailedAttempts(), 
            "Le nombre de tentatives devrait être incrémenté");
    }
    
    @Test
    @Order(4)
    @DisplayName("Test réinitialisation tentatives échouées")
    void testResetFailedAttempts() {
        // Arrange
        Admin admin = adminDAO.getAdminByUsername("admin");
        assertNotNull(admin, "L'admin doit exister pour ce test");
        
        // Act
        boolean reset = adminDAO.resetFailedAttempts(admin.getId());
        
        // Assert
        assertTrue(reset, "La réinitialisation devrait réussir");
        
        // Vérifier en base
        Admin updatedAdmin = adminDAO.getAdminByUsername("admin");
        assertEquals(0, updatedAdmin.getFailedAttempts(), 
            "Le nombre de tentatives devrait être 0");
        assertNull(updatedAdmin.getLockedUntil(), 
            "La date de verrouillage devrait être null");
    }
    
    @Test
    @Order(5)
    @DisplayName("Test changement de mot de passe")
    void testChangePassword() {
        // Arrange
        Admin admin = adminDAO.getAdminByUsername("admin");
        assertNotNull(admin, "L'admin doit exister pour ce test");
        
        String newPasswordHash = SecurityService.hashPassword("newpassword123");
        
        // Act
        boolean changed = adminDAO.changePassword(admin.getId(), newPasswordHash);
        
        // Assert
        assertTrue(changed, "Le changement de mot de passe devrait réussir");
        
        // Vérifier le nouveau mot de passe
        Admin updatedAdmin = adminDAO.getAdminByUsername("admin");
        assertTrue(SecurityService.verifyPassword("newpassword123", updatedAdmin.getPasswordHash()),
            "Le nouveau mot de passe devrait être valide");
        
        // Restaurer l'ancien mot de passe pour les autres tests
        String oldPasswordHash = SecurityService.hashPassword("admin123");
        adminDAO.changePassword(admin.getId(), oldPasswordHash);
    }
    
    @Test
    @Order(6)
    @DisplayName("Test admin inexistant")
    void testGetNonExistentAdmin() {
        // Act
        Admin admin = adminDAO.getAdminByUsername("nonexistent");
        
        // Assert
        assertNull(admin, "Un admin inexistant devrait retourner null");
    }
}
