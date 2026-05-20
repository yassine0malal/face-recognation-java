package com.facialaccess.model;

import java.time.LocalDateTime;

/**
 * Classe mère représentant une personne dans le système.
 * Correspond à la table PERSONNE.
 */
public abstract class Personne {
    
    protected Integer id;
    protected String fullName;
    protected String email;
    protected LocalDateTime createdAt;
    protected boolean isActive;
    protected String type; // 'ADMIN' ou 'UTILISATEUR'
    
    // Constructeur vide
    public Personne() {
    }
    
    // Constructeur avec paramètres
    public Personne(Integer id, String fullName, String email, 
                    LocalDateTime createdAt, boolean isActive, String type) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.createdAt = createdAt;
        this.isActive = isActive;
        this.type = type;
    }
    
    // Getters et Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return "Personne{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", type='" + type + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
