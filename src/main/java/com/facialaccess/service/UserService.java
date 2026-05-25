package com.facialaccess.service;

import com.facialaccess.dao.UtilisateurDAO;
import com.facialaccess.model.Utilisateur;

import java.util.List;

/**
 * Service métier pour la gestion des utilisateurs.
 * Gère l'ajout, modification, suppression et recherche.
 */
public class UserService {
    
    private final UtilisateurDAO utilisateurDAO;
    
    public UserService() {
        this.utilisateurDAO = new UtilisateurDAO();
    }
    
    // Constructeur pour injection de dépendances (tests)
    public UserService(UtilisateurDAO utilisateurDAO) {
        this.utilisateurDAO = utilisateurDAO;
    }
    
    /**
     * Crée un nouvel utilisateur avec validation.
     */
    public boolean createUser(String fullName, String email, String role) {
        // Validation
        if (fullName == null || fullName.trim().isEmpty()) {
            System.err.println("Le nom complet est obligatoire");
            return false;
        }
        
        if (email != null && !isValidEmail(email)) {
            System.err.println("Format d'email invalide");
            return false;
        }
        
        // Vérifier si l'email existe déjà
        if (email != null && utilisateurDAO.getUtilisateurByEmail(email) != null) {
            System.err.println("Un utilisateur avec cet email existe déjà");
            return false;
        }
        
        // Créer l'utilisateur
        Utilisateur user = new Utilisateur();
        user.setFullName(fullName.trim());
        user.setEmail(email);
        user.setRole(role != null ? role : "user");
        user.setActive(true);
        
        return utilisateurDAO.addUtilisateur(user);
    }
    
    /**
     * Met à jour les informations d'un utilisateur.
     */
    public boolean updateUser(int userId, String fullName, String email, String role) {
        Utilisateur user = utilisateurDAO.getUtilisateurById(userId);
        if (user == null) {
            System.err.println("Utilisateur introuvable");
            return false;
        }
        
        // Validation
        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName.trim());
        }
        
        if (email != null) {
            if (!isValidEmail(email)) {
                System.err.println("Format d'email invalide");
                return false;
            }
            
            // Vérifier si l'email est déjà utilisé par un autre utilisateur
            Utilisateur existingUser = utilisateurDAO.getUtilisateurByEmail(email);
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                System.err.println("Cet email est déjà utilisé");
                return false;
            }
            
            user.setEmail(email);
        }
        
        if (role != null) {
            user.setRole(role);
        }
        
        return utilisateurDAO.updateUtilisateur(user);
    }
    
    /**
     * Enregistre le vecteur facial d'un utilisateur.
     */
    public boolean registerFaceVector(int userId, byte[] faceVector) {
        if (faceVector == null || faceVector.length == 0) {
            System.err.println("Le vecteur facial est invalide");
            return false;
        }
        
        Utilisateur user = utilisateurDAO.getUtilisateurById(userId);
        if (user == null) {
            System.err.println("Utilisateur introuvable");
            return false;
        }
        
        user.setFaceVector(faceVector);
        return utilisateurDAO.updateUtilisateur(user);
    }
    
    /**
     * Enregistre le QR code d'un utilisateur.
     */
    public boolean registerQRCode(int userId, String qrCodeData) {
        if (qrCodeData == null || qrCodeData.trim().isEmpty()) {
            System.err.println("Les données du QR code sont invalides");
            return false;
        }
        
        Utilisateur user = utilisateurDAO.getUtilisateurById(userId);
        if (user == null) {
            System.err.println("Utilisateur introuvable");
            return false;
        }
        
        user.setQrCodeData(qrCodeData.trim());
        return utilisateurDAO.updateUtilisateur(user);
    }
    
    /**
     * Désactive un utilisateur.
     */
    public boolean deactivateUser(int userId) {
        Utilisateur user = utilisateurDAO.getUtilisateurById(userId);
        if (user == null) {
            System.err.println("Utilisateur introuvable");
            return false;
        }
        
        return utilisateurDAO.deactivateUtilisateur(userId);
    }
    
    /**
     * Réactive un utilisateur.
     */
    public boolean activateUser(int userId) {
        Utilisateur user = utilisateurDAO.getUtilisateurById(userId);
        if (user == null) {
            System.err.println("Utilisateur introuvable");
            return false;
        }
        
        user.setActive(true);
        return utilisateurDAO.updateUtilisateur(user);
    }
    
    /**
     * Supprime définitivement un utilisateur.
     */
    public boolean deleteUser(int userId) {
        Utilisateur user = utilisateurDAO.getUtilisateurById(userId);
        if (user == null) {
            System.err.println("Utilisateur introuvable");
            return false;
        }
        
        return utilisateurDAO.deleteUtilisateur(userId);
    }
    
    /**
     * Récupère un utilisateur par son ID.
     */
    public Utilisateur getUserById(int userId) {
        return utilisateurDAO.getUtilisateurById(userId);
    }
    
    /**
     * Récupère un utilisateur par son email.
     */
    public Utilisateur getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return utilisateurDAO.getUtilisateurByEmail(email.trim());
    }
    
    /**
     * Récupère tous les utilisateurs.
     */
    public List<Utilisateur> getAllUsers() {
        return utilisateurDAO.getAllUtilisateurs();
    }
    
    /**
     * Récupère tous les utilisateurs actifs.
     */
    public List<Utilisateur> getActiveUsers() {
        return utilisateurDAO.getActiveUtilisateurs();
    }
    
    /**
     * Recherche des utilisateurs par nom.
     */
    public List<Utilisateur> searchUsers(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllUsers();
        }
        return utilisateurDAO.searchUtilisateursByName(name.trim());
    }
    
    /**
     * Compte le nombre total d'utilisateurs.
     */
    public int countUsers() {
        return utilisateurDAO.countUtilisateurs();
    }
    
    /**
     * Compte le nombre d'utilisateurs actifs.
     */
    public int countActiveUsers() {
        return utilisateurDAO.countActiveUtilisateurs();
    }
    
    /**
     * Vérifie si un utilisateur a un vecteur facial enregistré.
     */
    public boolean hasFaceVector(int userId) {
        Utilisateur user = utilisateurDAO.getUtilisateurById(userId);
        return user != null && user.hasFaceVector();
    }
    
    /**
     * Vérifie si un utilisateur a un QR code enregistré.
     */
    public boolean hasQRCode(int userId) {
        Utilisateur user = utilisateurDAO.getUtilisateurById(userId);
        return user != null && user.hasQrCode();
    }
    
    /**
     * Valide le format d'un email.
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // Regex simple pour validation email
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}
