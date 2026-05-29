package com.facialaccess.dao;

import com.facialaccess.model.Utilisateur;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour UtilisateurDAO.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UtilisateurDAOTest {
    
    private static UtilisateurDAO utilisateurDAO;
    private static Integer testUserId;
    
    @BeforeAll
    static void setUp() {
        DatabaseManager.getInstance();
        utilisateurDAO = new UtilisateurDAO();
    }
    
    @AfterAll
    static void tearDown() {
        // Nettoyer l'utilisateur de test s'il existe encore
        if (testUserId != null) {
            utilisateurDAO.deleteUtilisateur(testUserId);
        }
        DatabaseManager.getInstance().close();
    }
    
    @Test
    @Order(1)
    @DisplayName("Test ajout utilisateur")
    void testAddUtilisateur() {
        // Arrange
        Utilisateur user = new Utilisateur();
        user.setFullName("Jean Dupont");
        user.setEmail("jean.dupont@test.com");
        user.setRole("user");
        user.setActive(true);
        user.setQrCodeData("QR123456");
        
        // Act
        boolean added = utilisateurDAO.addUtilisateur(user);
        
        // Assert
        assertTrue(added, "L'ajout devrait réussir");
        assertNotNull(user.getId(), "L'ID devrait être généré");
        testUserId = user.getId();
    }
    
    @Test
    @Order(2)
    @DisplayName("Test récupération utilisateur par ID")
    void testGetUtilisateurById() {
        // Arrange
        assertNotNull(testUserId, "Un utilisateur doit avoir été créé");
        
        // Act
        Utilisateur user = utilisateurDAO.getUtilisateurById(testUserId);
        
        // Assert
        assertNotNull(user, "L'utilisateur devrait être trouvé");
        assertEquals("Jean Dupont", user.getFullName());
        assertEquals("jean.dupont@test.com", user.getEmail());
        assertEquals("user", user.getRole());
        assertTrue(user.isActive());
        assertEquals("QR123456", user.getQrCodeData());
    }
    
    @Test
    @Order(3)
    @DisplayName("Test récupération utilisateur par email")
    void testGetUtilisateurByEmail() {
        // Act
        Utilisateur user = utilisateurDAO.getUtilisateurByEmail("jean.dupont@test.com");
        
        // Assert
        assertNotNull(user, "L'utilisateur devrait être trouvé");
        assertEquals(testUserId, user.getId());
        assertEquals("Jean Dupont", user.getFullName());
    }
    
    @Test
    @Order(4)
    @DisplayName("Test mise à jour utilisateur")
    void testUpdateUtilisateur() {
        // Arrange
        Utilisateur user = utilisateurDAO.getUtilisateurById(testUserId);
        assertNotNull(user, "L'utilisateur doit exister");
        
        user.setFullName("Jean Dupont Modifié");
        user.setEmail("jean.modifie@test.com");
        user.setRole("admin");
        
        // Act
        boolean updated = utilisateurDAO.updateUtilisateur(user);
        
        // Assert
        assertTrue(updated, "La mise à jour devrait réussir");
        
        // Vérifier en base
        Utilisateur updatedUser = utilisateurDAO.getUtilisateurById(testUserId);
        assertEquals("Jean Dupont Modifié", updatedUser.getFullName());
        assertEquals("jean.modifie@test.com", updatedUser.getEmail());
        assertEquals("admin", updatedUser.getRole());
    }
    
    @Test
    @Order(5)
    @DisplayName("Test recherche utilisateurs par nom")
    void testSearchUtilisateursByName() {
        // Act
        List<Utilisateur> results = utilisateurDAO.searchUtilisateursByName("Dupont");
        
        // Assert
        assertFalse(results.isEmpty(), "La recherche devrait retourner des résultats");
        assertTrue(results.stream().anyMatch(u -> u.getId().equals(testUserId)),
            "L'utilisateur de test devrait être dans les résultats");
    }
    
    @Test
    @Order(6)
    @DisplayName("Test récupération tous les utilisateurs")
    void testGetAllUtilisateurs() {
        // Act
        List<Utilisateur> users = utilisateurDAO.getAllUtilisateurs();
        
        // Assert
        assertFalse(users.isEmpty(), "Il devrait y avoir au moins un utilisateur");
        assertTrue(users.stream().anyMatch(u -> u.getId().equals(testUserId)),
            "L'utilisateur de test devrait être dans la liste");
    }
    
    @Test
    @Order(7)
    @DisplayName("Test comptage utilisateurs")
    void testCountUtilisateurs() {
        // Act
        int count = utilisateurDAO.countUtilisateurs();
        
        // Assert
        assertTrue(count > 0, "Il devrait y avoir au moins un utilisateur");
    }
    
    @Test
    @Order(8)
    @DisplayName("Test désactivation utilisateur")
    void testDeactivateUtilisateur() {
        // Act
        boolean deactivated = utilisateurDAO.deactivateUtilisateur(testUserId);
        
        // Assert
        assertTrue(deactivated, "La désactivation devrait réussir");
        
        // Vérifier en base
        Utilisateur user = utilisateurDAO.getUtilisateurById(testUserId);
        assertFalse(user.isActive(), "L'utilisateur devrait être inactif");
        
        // Vérifier le comptage des actifs
        int activeCount = utilisateurDAO.countActiveUtilisateurs();
        List<Utilisateur> activeUsers = utilisateurDAO.getActiveUtilisateurs();
        assertEquals(activeCount, activeUsers.size(), 
            "Le comptage devrait correspondre à la liste");
        assertFalse(activeUsers.stream().anyMatch(u -> u.getId().equals(testUserId)),
            "L'utilisateur désactivé ne devrait pas être dans les actifs");
    }
    
    @Test
    @Order(9)
    @DisplayName("Test suppression utilisateur")
    void testDeleteUtilisateur() {
        // Act
        boolean deleted = utilisateurDAO.deleteUtilisateur(testUserId);
        
        // Assert
        assertTrue(deleted, "La suppression devrait réussir");
        
        // Vérifier que l'utilisateur n'existe plus
        Utilisateur user = utilisateurDAO.getUtilisateurById(testUserId);
        assertNull(user, "L'utilisateur ne devrait plus exister");
        
        testUserId = null; // Marquer comme supprimé
    }
    
    @Test
    @Order(10)
    @DisplayName("Test utilisateur inexistant")
    void testGetNonExistentUtilisateur() {
        // Act
        Utilisateur user = utilisateurDAO.getUtilisateurById(99999);
        
        // Assert
        assertNull(user, "Un utilisateur inexistant devrait retourner null");
    }
    
    @Test
    @Order(11)
    @DisplayName("Test méthodes hasFaceVector et hasQrCode")
    void testUtilisateurHelperMethods() {
        // Arrange
        Utilisateur user = new Utilisateur();
        user.setFullName("Test Helper");
        user.setEmail("helper@test.com");
        user.setQrCodeData("QR789");
        user.setFaceVector(new byte[]{1, 2, 3, 4});
        
        // Assert
        assertTrue(user.hasQrCode(), "Devrait avoir un QR code");
        assertTrue(user.hasFaceVector(), "Devrait avoir un vecteur facial");
        
        // Test sans données
        Utilisateur emptyUser = new Utilisateur();
        assertFalse(emptyUser.hasQrCode(), "Ne devrait pas avoir de QR code");
        assertFalse(emptyUser.hasFaceVector(), "Ne devrait pas avoir de vecteur facial");
    }
}
