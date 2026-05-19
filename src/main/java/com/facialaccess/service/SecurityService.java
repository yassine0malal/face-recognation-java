package com.facialaccess.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Service de sécurité.
 * Gère le hachage des mots de passe et la protection anti-brute force.
 */
public class SecurityService {
    
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
