package com.facialaccess.model;

import java.time.LocalDateTime;

/**
 * Classe représentant un utilisateur du système.
 * Hérite de Personne et correspond à la table UTILISATEUR.
 */
public class Utilisateur extends Personne {
    
    private String role;
    private byte[] faceVector;
    private String qrCodeData;
    
    // Constructeur vide
    public Utilisateur() {
        super();
        this.type = "UTILISATEUR";
        this.role = "user";
    }
    
    // Constructeur complet
    public Utilisateur(Integer id, String fullName, String email, LocalDateTime createdAt,
                       boolean isActive, String role, byte[] faceVector, String qrCodeData) {
        super(id, fullName, email, createdAt, isActive, "UTILISATEUR");
        this.role = role;
        this.faceVector = faceVector;
        this.qrCodeData = qrCodeData;
    }
    
    // Getters et Setters spécifiques
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
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
    
    /**
     * Vérifie si l'utilisateur a un vecteur facial enregistré.
     */
    public boolean hasFaceVector() {
        return faceVector != null && faceVector.length > 0;
    }
    
    /**
     * Vérifie si l'utilisateur a un QR code.
     */
    public boolean hasQrCode() {
        return qrCodeData != null && !qrCodeData.isEmpty();
    }
    
    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", hasFaceVector=" + hasFaceVector() +
                ", hasQrCode=" + hasQrCode() +
                ", isActive=" + isActive +
                '}';
    }
}
