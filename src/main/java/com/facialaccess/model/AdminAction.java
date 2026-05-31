package com.facialaccess.model;

import java.time.LocalDateTime;

/**
 * Modèle représentant un log d'action administrateur.
 * Correspond à la table ADMIN_ACTION_LOGS.
 */
public class AdminAction {
    
    private Integer id;
    private String adminUsername;
    private String actionType;
    private String details;
    private LocalDateTime actionAt;
    
    // Constructeur vide
    public AdminAction() {
    }
    
    // Constructeur complet
    public AdminAction(Integer id, String adminUsername, String actionType, String details, LocalDateTime actionAt) {
        this.id = id;
        this.adminUsername = adminUsername;
        this.actionType = actionType;
        this.details = details;
        this.actionAt = actionAt;
    }
    
    // Getters et Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getAdminUsername() {
        return adminUsername;
    }
    
    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }
    
    public String getActionType() {
        return actionType;
    }
    
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    public LocalDateTime getActionAt() {
        return actionAt;
    }
    
    public void setActionAt(LocalDateTime actionAt) {
        this.actionAt = actionAt;
    }
    
    @Override
    public String toString() {
        return "AdminAction{" +
                "id=" + id +
                ", adminUsername='" + adminUsername + '\'' +
                ", actionType='" + actionType + '\'' +
                ", details='" + details + '\'' +
                ", actionAt=" + actionAt +
                '}';
    }
}
