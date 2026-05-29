package com.facialaccess.service;

import com.facialaccess.dao.AccessLogDAO;
import com.facialaccess.model.AccessLog;
import com.facialaccess.util.FaceRecognitionConfig;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service de gestion des accès.
 * Enregistre les tentatives d'accès et gère les autorisations.
 */
public class AccessService {
    
    private final AccessLogDAO accessLogDAO;
    
    public AccessService() {
        this.accessLogDAO = new AccessLogDAO();
    }
    
    // Constructeur pour injection de dépendances (tests)
    public AccessService(AccessLogDAO accessLogDAO) {
        this.accessLogDAO = accessLogDAO;
    }
    
    /**
     * Enregistre une tentative d'accès par reconnaissance faciale.
     */
    public boolean logFaceAccess(Integer userId, double confidenceScore) {
        String status = determineAccessStatus(confidenceScore);
        
        AccessLog log = new AccessLog();
        log.setUserId(userId);
        log.setStatus(status);
        log.setConfidenceScore(confidenceScore);
        log.setIdentificationMethod("FACE");
        
        boolean logged = accessLogDAO.addAccessLog(log);
        
        if (logged) {
            System.out.println("Accès " + status + " - Confiance: " + 
                String.format("%.2f%%", confidenceScore * 100));
        }
        
        return logged;
    }
    
    /**
     * Enregistre une tentative d'accès par QR code.
     */
    public boolean logQRAccess(Integer userId, boolean isValid) {
        String status = isValid ? "GRANTED" : "DENIED";
        
        AccessLog log = new AccessLog();
        log.setUserId(userId);
        log.setStatus(status);
        log.setConfidenceScore(isValid ? 1.0 : 0.0);
        log.setIdentificationMethod("QR");
        
        boolean logged = accessLogDAO.addAccessLog(log);
        
        if (logged) {
            System.out.println("Accès QR " + status);
        }
        
        return logged;
    }
    
    /**
     * Vérifie si l'accès doit être accordé selon le score de confiance.
     */
    public boolean shouldGrantAccess(double confidenceScore) {
        return FaceRecognitionConfig.shouldGrantAccess(confidenceScore);
    }
    
    /**
     * Détermine le statut d'accès selon le score de confiance.
     */
    private String determineAccessStatus(double confidenceScore) {
        return shouldGrantAccess(confidenceScore) ? "GRANTED" : "DENIED";
    }
    
    /**
     * Récupère tous les logs d'accès.
     */
    public List<AccessLog> getAllAccessLogs() {
        return accessLogDAO.getAllAccessLogs();
    }
    
    /**
     * Récupère les logs d'accès d'un utilisateur.
     */
    public List<AccessLog> getUserAccessLogs(int userId) {
        return accessLogDAO.getAccessLogsByUserId(userId);
    }
    
    /**
     * Récupère les logs d'accès par statut.
     */
    public List<AccessLog> getAccessLogsByStatus(String status) {
        if (!"GRANTED".equals(status) && !"DENIED".equals(status)) {
            System.err.println("Statut invalide. Utilisez 'GRANTED' ou 'DENIED'");
            return List.of();
        }
        return accessLogDAO.getAccessLogsByStatus(status);
    }
    
    /**
     * Récupère les logs d'accès dans une période.
     */
    public List<AccessLog> getAccessLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            System.err.println("Les dates de début et fin sont obligatoires");
            return List.of();
        }
        
        if (startDate.isAfter(endDate)) {
            System.err.println("La date de début doit être antérieure à la date de fin");
            return List.of();
        }
        
        return accessLogDAO.getAccessLogsByDateRange(startDate, endDate);
    }
    
    /**
     * Récupère les logs d'accès des dernières 24 heures.
     */
    public List<AccessLog> getRecentAccessLogs() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        return accessLogDAO.getAccessLogsByDateRange(yesterday, now);
    }
    
    /**
     * Récupère les logs d'accès de la semaine en cours.
     */
    public List<AccessLog> getWeeklyAccessLogs() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);
        return accessLogDAO.getAccessLogsByDateRange(weekAgo, now);
    }
    
    /**
     * Compte le nombre total de logs.
     */
    public int countTotalAccess() {
        return accessLogDAO.countAccessLogs();
    }
    
    /**
     * Compte les accès accordés.
     */
    public int countGrantedAccess() {
        return accessLogDAO.countGrantedAccess();
    }
    
    /**
     * Compte les accès refusés.
     */
    public int countDeniedAccess() {
        return accessLogDAO.countDeniedAccess();
    }
    
    /**
     * Calcule le taux d'accès accordés (en pourcentage).
     */
    public double getGrantedAccessRate() {
        int total = countTotalAccess();
        if (total == 0) {
            return 0.0;
        }
        int granted = countGrantedAccess();
        return (granted * 100.0) / total;
    }
    
    /**
     * Supprime les logs plus anciens qu'une date donnée.
     */
    public boolean cleanOldLogs(LocalDateTime beforeDate) {
        if (beforeDate == null) {
            System.err.println("La date est obligatoire");
            return false;
        }
        
        return accessLogDAO.deleteOldLogs(beforeDate);
    }
    
    /**
     * Supprime les logs de plus de 30 jours.
     */
    public boolean cleanLogsOlderThan30Days() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return cleanOldLogs(thirtyDaysAgo);
    }
    
    /**
     * Obtient le seuil de confiance minimum.
     */
    public double getMinConfidenceThreshold() {
        return FaceRecognitionConfig.RECOGNITION_THRESHOLD;
    }
}
