package com.facialaccess.model;

import java.time.LocalDateTime;

public class Utilisateur extends Personne {
    
    private String role;
    private byte[] faceImage;
    private byte[] faceVector;
    private String qrCodeData;
    
    public Utilisateur() {
        super();
        this.type = "UTILISATEUR";
        this.role = "user";
    }
    
    public Utilisateur(Integer id, String fullName, String email, LocalDateTime createdAt,
                       boolean isActive, String role, byte[] faceImage, byte[] faceVector, String qrCodeData) {
        super(id, fullName, email, createdAt, isActive, "UTILISATEUR");
        this.role = role;
        this.faceImage = faceImage;
        this.faceVector = faceVector;
        this.qrCodeData = qrCodeData;
    }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public byte[] getFaceImage() { return faceImage; }
    public void setFaceImage(byte[] faceImage) { this.faceImage = faceImage; }
    
    public byte[] getFaceVector() { return faceVector; }
    public void setFaceVector(byte[] faceVector) { this.faceVector = faceVector; }
    
    public String getQrCodeData() { return qrCodeData; }
    public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }
    
    public boolean hasFaceImage() { return faceImage != null && faceImage.length > 0; }
    public boolean hasFaceVector() { return faceVector != null && faceVector.length > 0; }
    public boolean hasQrCode() { return qrCodeData != null && !qrCodeData.isEmpty(); }
    
    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", hasFaceVector=" + hasFaceVector() +
                ", isActive=" + isActive +
                '}';
    }
}