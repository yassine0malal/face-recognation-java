package com.facialaccess.service;

import com.facialaccess.dao.DatabaseManager;
import com.facialaccess.dao.UtilisateurDAO;
import com.facialaccess.model.Utilisateur;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires et d'intégration pour UserService.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {
    
    private static UserService userService;
    private static Integer testUserId;
    
    @BeforeAll
    static void setUp() {
        DatabaseManager.getInstance();
        userService = new UserService();
    }
    
    @AfterAll
    static void tearDown() {
        // Nettoyer
        if (testUserId != null) {
            userService.deleteUser(testUserId);
        }
        DatabaseManager.getInstance().close();
    }
    
    @Test
    @Order(1)
    @DisplayName("Test création utilisateur valide")
    void testCreateUserValid() {
        // Act
        boolean created = userService.createUser("Marie Martin", "marie.martin@test.com", "user");
        
        // Assert
        assertTrue(created, "La création devrait réussir");
        
        // Récupérer l'utilisateur créé
        Utilisateur user = userService.getUserByEmail("marie.martin@test.com");
        assertNotNull(user, "L'utilisateur devrait exister");
        testUserId = user.getId();
        
        assertEquals("Marie Martin", user.getFullName());
        assertEquals("marie.martin@test.com", user.getEmail());
        assertEquals("user", user.getRole());
        assertTrue(user.isActive());
    }
    
    @Test
    @Order(2)
    @DisplayName("Test création utilisateur avec nom vide")
    void testCreateUserEmptyName() {
        // Act
        boolean created = userService.createUser("", "test@test.com", "user");
        
        // Assert
        assertFalse(created, "La création devrait échouer avec un nom vide");
    }
    
    @Test
    @Order(3)
    @DisplayName("Test création utilisateur avec email invalide")
    void testCreateUserInvalidEmail() {
        // Act
        boolean created = userService.createUser("Test User", "invalid-email", "user");
        
        // Assert
        assertFalse(created, "La création devrait échouer avec un email invalide");
    }
    
    @Test
    @Order(4)
    @DisplayName("Test création utilisateur avec email existant")
    void testCreateUserDuplicateEmail() {
        // Act
        boolean created = userService.createUser("Autre User", "marie.martin@test.com", "user");
        
        // Assert
        assertFalse(created, "La création devrait échouer avec un email déjà utilisé");
    }
    
    @Test
    @Order(5)
    @DisplayName("Test mise à jour utilisateur")
    void testUpdateUser() {
        // Act
        boolean updated = userService.updateUser(
            testUserId, 
            "Marie Martin Modifiée", 
            "marie.modifiee@test.com", 
            "admin"
        );
        
        // Assert
        assertTrue(updated, "La mise à jour devrait réussir");
        
        Utilisateur user = userService.getUserById(testUserId);
        assertEquals("Marie Martin Modifiée", user.getFullName());
        assertEquals("marie.modifiee@test.com", user.getEmail());
        assertEquals("admin", user.getRole());
    }
    
    @Test
    @Order(6)
    @DisplayName("Test enregistrement vecteur facial")
    void testRegisterFaceVector() {
        // Arrange
        byte[] faceVector = new byte[]{1, 2, 3, 4, 5};
        
        // Act
        boolean registered = userService.registerFaceVector(testUserId, faceVector);
        
        // Assert
        assertTrue(registered, "L'enregistrement devrait réussir");
        assertTrue(userService.hasFaceVector(testUserId), "L'utilisateur devrait avoir un vecteur facial");
    }
    
    @Test
    @Order(7)
    @DisplayName("Test enregistrement QR code")
    void testRegisterQRCode() {
        // Act
        boolean registered = userService.registerQRCode(testUserId, "QR-CODE-123");
        
        // Assert
        assertTrue(registered, "L'enregistrement devrait réussir");
        assertTrue(userService.hasQRCode(testUserId), "L'utilisateur devrait avoir un QR code");
    }
    
    @Test
    @Order(8)
    @DisplayName("Test recherche utilisateurs")
    void testSearchUsers() {
        // Act
        List<Utilisateur> results = userService.searchUsers("Martin");
        
        // Assert
        assertFalse(results.isEmpty(), "La recherche devrait retourner des résultats");
        assertTrue(results.stream().anyMatch(u -> u.getId().equals(testUserId)),
            "L'utilisateur de test devrait être dans les résultats");
    }
    
    @Test
    @Order(9)
    @DisplayName("Test récupération utilisateurs actifs")
    void testGetActiveUsers() {
        // Act
        List<Utilisateur> activeUsers = userService.getActiveUsers();
        int activeCount = userService.countActiveUsers();
        
        // Assert
        assertEquals(activeUsers.size(), activeCount, 
            "Le comptage devrait correspondre à la liste");
        assertTrue(activeUsers.stream().anyMatch(u -> u.getId().equals(testUserId)),
            "L'utilisateur de test devrait être actif");
    }
    
    @Test
    @Order(10)
    @DisplayName("Test désactivation utilisateur")
    void testDeactivateUser() {
        // Act
        boolean deactivated = userService.deactivateUser(testUserId);
        
        // Assert
        assertTrue(deactivated, "La désactivation devrait réussir");
        
        Utilisateur user = userService.getUserById(testUserId);
        assertFalse(user.isActive(), "L'utilisateur devrait être inactif");
        
        // Vérifier qu'il n'est plus dans les actifs
        List<Utilisateur> activeUsers = userService.getActiveUsers();
        assertFalse(activeUsers.stream().anyMatch(u -> u.getId().equals(testUserId)),
            "L'utilisateur ne devrait plus être dans les actifs");
    }
    
    @Test
    @Order(11)
    @DisplayName("Test réactivation utilisateur")
    void testActivateUser() {
        // Act
        boolean activated = userService.activateUser(testUserId);
        
        // Assert
        assertTrue(activated, "La réactivation devrait réussir");
        
        Utilisateur user = userService.getUserById(testUserId);
        assertTrue(user.isActive(), "L'utilisateur devrait être actif");
    }
    
    @Test
    @Order(12)
    @DisplayName("Test comptage utilisateurs")
    void testCountUsers() {
        // Act
        int totalCount = userService.countUsers();
        int activeCount = userService.countActiveUsers();
        
        // Assert
        assertTrue(totalCount > 0, "Il devrait y avoir au moins un utilisateur");
        assertTrue(activeCount > 0, "Il devrait y avoir au moins un utilisateur actif");
        assertTrue(totalCount >= activeCount, "Le total devrait être >= aux actifs");
    }
    
    @Test
    @Order(13)
    @DisplayName("Test suppression utilisateur")
    void testDeleteUser() {
        // Act
        boolean deleted = userService.deleteUser(testUserId);
        
        // Assert
        assertTrue(deleted, "La suppression devrait réussir");
        
        Utilisateur user = userService.getUserById(testUserId);
        assertNull(user, "L'utilisateur ne devrait plus exister");
        
        testUserId = null;
    }
    
    @Test
    @Order(14)
    @DisplayName("Test opérations sur utilisateur inexistant")
    void testOperationsOnNonExistentUser() {
        // Act & Assert
        assertFalse(userService.updateUser(99999, "Test", "test@test.com", "user"),
            "La mise à jour devrait échouer");
        assertFalse(userService.deactivateUser(99999),
            "La désactivation devrait échouer");
        assertFalse(userService.deleteUser(99999),
            "La suppression devrait échouer");
        assertNull(userService.getUserById(99999),
            "Devrait retourner null");
    }
    
    @Test
    @Order(15)
    @DisplayName("Test validation email")
    void testEmailValidation() {
        // Emails valides
        assertTrue(userService.createUser("Test1", "valid@email.com", "user"));
        userService.deleteUser(userService.getUserByEmail("valid@email.com").getId());
        
        assertTrue(userService.createUser("Test2", "user.name@domain.co.uk", "user"));
        userService.deleteUser(userService.getUserByEmail("user.name@domain.co.uk").getId());
        
        // Emails invalides
        assertFalse(userService.createUser("Test3", "invalid", "user"));
        assertFalse(userService.createUser("Test4", "@invalid.com", "user"));
        assertFalse(userService.createUser("Test5", "invalid@", "user"));
    }
}
