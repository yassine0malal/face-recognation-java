package com.facialaccess.model;

import java.time.LocalDateTime;

/**
 * Modèle représentant un utilisateur du système.
 * Correspond à la table USERS.
 */
public class User {
    
    private Integer id;
    private String fullName;
    private String role;
    private String email;
    private byte[] faceVector;
    private String qrCodeData;
    private LocalDateTime createdAt;
    private boolean isActive;
    
    // Constructeur vide
    public User() {
    }
    
    // Constructeur complet
    public User(Integer id, String fullName, String role, String email, 
                byte[] faceVector, String qrCodeData, LocalDateTime createdAt, boolean isActive) {
        this.id = id;
        this.fullName = fullName;
        this.role = role;
        this.email = email;
        this.faceVector = faceVector;
        this.qrCodeData = qrCodeData;
        this.createdAt = createdAt;
        this.isActive = isActive;
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
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public byte[] getFaceVector() {
        return faceVector;
    }
    
    public void setFaceVector(byte[] faceVector) {
        this.faceVector = faceVector;
    }
    
    public String getQrCodeData() {
        return qrCodeData;
    }
    
    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
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
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", email='" + email + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
