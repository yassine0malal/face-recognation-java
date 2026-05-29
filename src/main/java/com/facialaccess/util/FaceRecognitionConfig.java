package com.facialaccess.util;

/**
 * Configuration centralisée pour la reconnaissance faciale.
 * Toutes les constantes liées à la reconnaissance sont définies ici
 * pour éviter les incohérences entre les services.
 */
public class FaceRecognitionConfig {
    
    /**
     * Seuil de confiance minimum pour la reconnaissance faciale.
     * 
     * Valeurs recommandées :
     * - 0.80 (80%) : Très strict, peu de faux positifs, mais peut rejeter des vrais utilisateurs
     * - 0.70 (70%) : Strict, bon équilibre sécurité/convivialité
     * - 0.60 (60%) : Modéré, plus tolérant aux variations d'éclairage
     * - 0.50 (50%) : Permissif, risque de faux positifs
     * 
     * Valeur actuelle : 0.60 (60%)
     * Ajustez selon vos besoins de sécurité vs. convivialité
     */
    public static final double RECOGNITION_THRESHOLD = 0.60;
    
    /**
     * Taille normalisée du visage pour l'extraction des features (en pixels).
     * Plus la taille est grande, plus les détails sont préservés,
     * mais plus le calcul est lent.
     */
    public static final int FACE_SIZE = 100;
    
    /**
     * Intervalle de frames pour la reconnaissance (1 frame sur N).
     * Plus l'intervalle est grand, moins le CPU est sollicité,
     * mais la reconnaissance est plus lente.
     */
    public static final int RECOGNITION_INTERVAL = 15;
    
    /**
     * Paramètres de détection de visage (Haar Cascade).
     */
    public static final double SCALE_FACTOR = 1.1;
    public static final int MIN_NEIGHBORS = 3;
    public static final int MIN_FACE_SIZE = 30;
    
    /**
     * Timeout pour la caméra (en secondes).
     */
    public static final int CAMERA_TIMEOUT = 30;
    
    /**
     * Vérifie si un score de confiance est suffisant pour accorder l'accès.
     * 
     * @param confidenceScore Score de confiance (0.0 à 1.0)
     * @return true si l'accès doit être accordé
     */
    public static boolean shouldGrantAccess(double confidenceScore) {
        return confidenceScore >= RECOGNITION_THRESHOLD;
    }
    
    /**
     * Formate un score de confiance en pourcentage.
     * 
     * @param confidenceScore Score de confiance (0.0 à 1.0)
     * @return String formatée (ex: "74%")
     */
    public static String formatConfidence(double confidenceScore) {
        return String.format("%.0f%%", confidenceScore * 100);
    }
    
    /**
     * Formate un score de confiance en pourcentage avec décimales.
     * 
     * @param confidenceScore Score de confiance (0.0 à 1.0)
     * @return String formatée (ex: "74.47%")
     */
    public static String formatConfidenceDetailed(double confidenceScore) {
        return String.format("%.2f%%", confidenceScore * 100);
    }
    
    // Empêcher l'instanciation
    private FaceRecognitionConfig() {
        throw new AssertionError("Cette classe ne doit pas être instanciée");
    }
}
