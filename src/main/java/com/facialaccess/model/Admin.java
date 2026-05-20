package com.facialaccess.model;

import java.time.LocalDateTime;

/**
 * Classe représentant un administrateur.
 * Hérite de Personne et correspond à la table ADMIN.
 */
public class Admin extends Personne {
    
    private String username;
    private String passwordHash;
    private int failedAttempts;
    private LocalDateTime lockedUntil;
    
    // Constructeur vide
    public Admin() {
        super();
        this.type = "ADMIN";
    }
    
    // Constructeur complet
    public Admin(Integer id, String fullName, String email, LocalDateTime createdAt, 
                 boolean isActive, String username, String passwordHash, 
                 int failedAttempts, LocalDateTime lockedUntil) {
        super(id, fullName, email, createdAt, isActive, "ADMIN");
        this.username = username;
        this.passwordHash = passwordHash;
        this.failedAttempts = failedAttempts;
        this.lockedUntil = lockedUntil;
    }
    
    // Getters et Setters spécifiques
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public int getFailedAttempts() {
        return failedAttempts;
    }
    
    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }
    
    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }
    
    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }
    
    /**
     * Vérifie si le compte est verrouillé.
     */
    public boolean isLocked() {
        if (lockedUntil == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(lockedUntil);
    }
    
    /**
     * Incrémente le nombre de tentatives échouées.
     */
    public void incrementFailedAttempts() {
        this.failedAttempts++;
    }
    
    /**
     * Réinitialise les tentatives échouées.
     */
    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.lockedUntil = null;
    }
    
    @Override
    public String toString() {
        return "Admin{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", failedAttempts=" + failedAttempts +
                ", isLocked=" + isLocked() +
                ", isActive=" + isActive +
                '}';
    }
}
