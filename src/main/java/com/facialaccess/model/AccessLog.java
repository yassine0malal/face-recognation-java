package com.facialaccess.model;

import java.time.LocalDateTime;

/**
 * Modèle représentant un log d'accès.
 * Correspond à la table ACCESS_LOGS.
 */
public class AccessLog {
    
    private Integer id;
    private Integer userId;
    private String status; // 'GRANTED' ou 'DENIED'
    private Double confidenceScore;
    private String identificationMethod; // 'FACE' ou 'QR'
    private LocalDateTime accessedAt;
    
    // Pour affichage (jointure avec User)
    private String userName;
    
    // Constructeur vide
    public AccessLog() {
    }
    
    // Constructeur complet
    public AccessLog(Integer id, Integer userId, String status, Double confidenceScore,
                     String identificationMethod, LocalDateTime accessedAt) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.confidenceScore = confidenceScore;
        this.identificationMethod = identificationMethod;
        this.accessedAt = accessedAt;
    }
    
    // Getters et Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Double getConfidenceScore() {
        return confidenceScore;
    }
    
    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
    
    public String getIdentificationMethod() {
        return identificationMethod;
    }
    
    public void setIdentificationMethod(String identificationMethod) {
        this.identificationMethod = identificationMethod;
    }
    
    public LocalDateTime getAccessedAt() {
        return accessedAt;
    }
    
    public void setAccessedAt(LocalDateTime accessedAt) {
        this.accessedAt = accessedAt;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    @Override
    public String toString() {
        return "AccessLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", status='" + status + '\'' +
                ", confidenceScore=" + confidenceScore +
                ", identificationMethod='" + identificationMethod + '\'' +
                ", accessedAt=" + accessedAt +
                '}';
    }
}
