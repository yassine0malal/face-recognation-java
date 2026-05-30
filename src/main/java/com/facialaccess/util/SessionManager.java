package com.facialaccess.util;

/**
 * Gestionnaire de session pour suivre l'administrateur connecté.
 */
public class SessionManager {
    
    private static String loggedInAdmin = "admin"; // Valeur par défaut / fallback

    public static synchronized String getLoggedInAdmin() {
        return loggedInAdmin;
    }

    public static synchronized void setLoggedInAdmin(String admin) {
        if (admin != null && !admin.trim().isEmpty()) {
            loggedInAdmin = admin.trim();
        }
    }
    
    public static synchronized void clearSession() {
        loggedInAdmin = "admin";
    }
}
