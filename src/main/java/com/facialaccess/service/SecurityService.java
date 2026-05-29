package com.facialaccess.service;

import com.facialaccess.dao.AdminDAO;
import com.facialaccess.model.Admin;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service de sécurité.
 * Gère le hachage des mots de passe et la protection anti-brute force.
 */
public class SecurityService {
    
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;
    
    private final AdminDAO adminDAO;
    private final Map<String, LoginAttempt> loginAttempts;
    
    public SecurityService() {
        this.adminDAO = new AdminDAO();
        this.loginAttempts = new HashMap<>();
    }
    
    /**
     * Authentifie un administrateur.
     * 
     * @param username Le nom d'utilisateur
     * @param password Le mot de passe en clair
     * @return true si l'authentification réussit
     */
    public boolean authenticate(String username, String password) {
        // Vérifier si le compte est verrouillé
        if (isAccountLocked(username)) {
            return false;
        }
        
        // Récupérer l'admin depuis la base de données
        Admin admin = adminDAO.findByUsername(username);
        
        if (admin == null) {
            recordFailedAttempt(username);
            return false;
        }
        
        // Vérifier le mot de passe
        boolean authenticated = verifyPassword(password, admin.getPasswordHash());
        
        if (authenticated) {
            clearLoginAttempts(username);
        } else {
            recordFailedAttempt(username);
        }
        
        return authenticated;
    }
    
    /**
     * Obtient le nombre de tentatives restantes avant verrouillage.
     * 
     * @param username Le nom d'utilisateur
     * @return Le nombre de tentatives restantes
     */
    public int getRemainingAttempts(String username) {
        LoginAttempt attempt = loginAttempts.get(username);
        if (attempt == null) {
            return MAX_ATTEMPTS;
        }
        
        // Si le compte est verrouillé, retourner 0
        if (isAccountLocked(username)) {
            return 0;
        }
        
        return Math.max(0, MAX_ATTEMPTS - attempt.attemptCount);
    }
    
    /**
     * Vérifie si un compte est verrouillé.
     * 
     * @param username Le nom d'utilisateur
     * @return true si le compte est verrouillé
     */
    private boolean isAccountLocked(String username) {
        LoginAttempt attempt = loginAttempts.get(username);
        if (attempt == null || attempt.attemptCount < MAX_ATTEMPTS) {
            return false;
        }
        
        // Vérifier si la période de verrouillage est expirée
        LocalDateTime unlockTime = attempt.lastAttempt.plusMinutes(LOCKOUT_DURATION_MINUTES);
        if (LocalDateTime.now().isAfter(unlockTime)) {
            // Réinitialiser les tentatives
            clearLoginAttempts(username);
            return false;
        }
        
        return true;
    }
    
    /**
     * Enregistre une tentative de connexion échouée.
     * 
     * @param username Le nom d'utilisateur
     */
    private void recordFailedAttempt(String username) {
        LoginAttempt attempt = loginAttempts.getOrDefault(username, new LoginAttempt());
        attempt.attemptCount++;
        attempt.lastAttempt = LocalDateTime.now();
        loginAttempts.put(username, attempt);
    }
    
    /**
     * Efface les tentatives de connexion pour un utilisateur.
     * 
     * @param username Le nom d'utilisateur
     */
    private void clearLoginAttempts(String username) {
        loginAttempts.remove(username);
    }
    
    /**
     * Vérifie le mot de passe d'un admin.
     */
    public boolean verifyAdminPassword(int adminId, String password) {
        Admin admin = adminDAO.getAdminById(adminId);
        if (admin == null) {
            return false;
        }
        return verifyPassword(password, admin.getPasswordHash());
    }
    
    /**
     * Met à jour le mot de passe d'un admin.
     */
    public boolean updateAdminPassword(int adminId, String newPassword) {
        String newPasswordHash = hashPassword(newPassword);
        return adminDAO.changePassword(adminId, newPasswordHash);
    }
    
    /**
     * Hash un mot de passe avec SHA-256.
     * 
     * @param password Le mot de passe en clair
     * @return Le hash SHA-256 en hexadécimal
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Convertir en hexadécimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur lors du hashage du mot de passe", e);
        }
    }
    
    /**
     * Vérifie si un mot de passe correspond à son hash.
     * 
     * @param password Le mot de passe en clair
     * @param hash Le hash stocké en base
     * @return true si le mot de passe correspond
     */
    public static boolean verifyPassword(String password, String hash) {
        String passwordHash = hashPassword(password);
        return passwordHash.equals(hash);
    }
    
    /**
     * Classe interne pour suivre les tentatives de connexion.
     */
    private static class LoginAttempt {
        int attemptCount = 0;
        LocalDateTime lastAttempt = LocalDateTime.now();
    }
    
    /**
     * Génère le hash pour le mot de passe admin par défaut.
     * Utilisé pour vérifier le hash dans schema.sql.
     */
    public static void main(String[] args) {
        // Test : génère le hash de "admin123"
        String password = "admin123";
        String hash = hashPassword(password);
        System.out.println("Mot de passe: " + password);
        System.out.println("Hash SHA-256: " + hash);
        
        // Vérification
        System.out.println("Vérification: " + verifyPassword(password, hash));
    }
}
