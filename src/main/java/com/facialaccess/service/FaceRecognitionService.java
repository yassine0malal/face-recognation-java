package com.facialaccess.service;

import com.facialaccess.dao.UtilisateurDAO;
import com.facialaccess.model.Utilisateur;
import com.facialaccess.vision.*;
import org.bytedeco.opencv.opencv_core.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de reconnaissance faciale.
 */
public class FaceRecognitionService {
    
    private final FaceDetector faceDetector;
    private final FeatureExtractor featureExtractor;
    private final UtilisateurDAO utilisateurDAO;
    
    private static final double RECOGNITION_THRESHOLD = 0.75;
    
    public FaceRecognitionService() {
        // Extraire le fichier cascade vers un chemin temporaire réel sur le disque
        // (nécessaire car OpenCV ne peut pas lire depuis un JAR ou un chemin URL Windows)
        String cascadePath = extractCascadeToTemp();
        this.faceDetector = new FaceDetector(cascadePath);
        this.featureExtractor = new FeatureExtractor();
        this.utilisateurDAO = new UtilisateurDAO();
        System.out.println("✓ Service de reconnaissance faciale initialisé");
    }

    /**
     * Copie le fichier cascade depuis les ressources vers un fichier temporaire
     * et retourne son chemin absolu utilisable par OpenCV.
     */
    private String extractCascadeToTemp() {
        try (InputStream is = getClass().getResourceAsStream(
                "/haarcascades/haarcascade_frontalface_default.xml")) {

            if (is == null) {
                throw new RuntimeException("Fichier cascade introuvable dans les ressources");
            }

            Path tmp = Files.createTempFile("haarcascade_frontalface_default", ".xml");
            tmp.toFile().deleteOnExit();
            Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✓ Cascade extraite vers: " + tmp.toAbsolutePath());
            return tmp.toAbsolutePath().toString();

        } catch (Exception e) {
            throw new RuntimeException("Impossible d'extraire le fichier cascade: " + e.getMessage(), e);
        }
    }
    
    // Constructeur pour injection de dépendances (tests)
    public FaceRecognitionService(FaceDetector faceDetector, FeatureExtractor featureExtractor, UtilisateurDAO utilisateurDAO) {
        this.faceDetector = faceDetector;
        this.featureExtractor = featureExtractor;
        this.utilisateurDAO = utilisateurDAO;
    }
    
    /**
     * Enregistre le visage d'un utilisateur.
     * @param userId ID de l'utilisateur
     * @param faceImage Image contenant le visage
     * @return true si l'enregistrement a réussi
     */
    public boolean registerFace(int userId, Mat faceImage) {
        if (faceImage == null || faceImage.empty()) {
            System.err.println("Image invalide");
            return false;
        }
        
        // Détecter le visage
        Rect faceRect = faceDetector.detectLargestFace(faceImage);
        if (faceRect == null) {
            System.err.println("Aucun visage détecté");
            return false;
        }
        
        // Extraire la région du visage
        Mat faceRegion = faceDetector.extractFaceRegion(faceImage, faceRect);
        if (faceRegion == null) {
            System.err.println("Impossible d'extraire le visage");
            return false;
        }
        
        // Extraire les features
        byte[] features = featureExtractor.extractFeatures(faceRegion);
        if (features == null) {
            System.err.println("Impossible d'extraire les features");
            faceRegion.release();
            return false;
        }
        
        // Enregistrer dans la base de données
        Utilisateur user = utilisateurDAO.getUtilisateurById(userId);
        if (user == null) {
            System.err.println("Utilisateur introuvable");
            faceRegion.release();
            return false;
        }
        
        user.setFaceVector(features);
        boolean success = utilisateurDAO.updateUtilisateur(user);
        
        faceRegion.release();
        
        if (success) {
            System.out.println("✓ Visage enregistré pour l'utilisateur " + userId);
        }
        
        return success;
    }
    
    /**
     * Identifie un visage dans une image.
     * @param faceImage Image contenant le visage
     * @return Résultat de la reconnaissance (utilisateur + score)
     */
    public RecognitionResult recognizeFace(Mat faceImage) {
        if (faceImage == null || faceImage.empty()) {
            return null;
        }
        
        // Détecter le visage
        Rect faceRect = faceDetector.detectLargestFace(faceImage);
        if (faceRect == null) {
            return new RecognitionResult(null, 0.0, null, "Aucun visage détecté");
        }
        
        // Extraire la région du visage
        Mat faceRegion = faceDetector.extractFaceRegion(faceImage, faceRect);
        if (faceRegion == null) {
            return new RecognitionResult(null, 0.0, faceRect, "Erreur extraction");
        }
        
        // Extraire les features
        byte[] features = featureExtractor.extractFeatures(faceRegion);
        faceRegion.release();
        
        if (features == null) {
            return new RecognitionResult(null, 0.0, faceRect, "Erreur features");
        }
        
        // Récupérer tous les utilisateurs actifs avec vecteur facial
        List<Utilisateur> users = utilisateurDAO.getActiveUtilisateurs();
        List<Utilisateur> usersWithFace = new ArrayList<>();
        List<byte[]> storedFeatures = new ArrayList<>();
        
        for (Utilisateur user : users) {
            if (user.hasFaceVector()) {
                usersWithFace.add(user);
                storedFeatures.add(user.getFaceVector());
            }
        }
        
        if (usersWithFace.isEmpty()) {
            return new RecognitionResult(null, 0.0, faceRect, "Aucun utilisateur enregistré");
        }
        
        // Trouver le meilleur match
        FeatureExtractor.MatchResult match = featureExtractor.findBestMatch(faceImage, storedFeatures);
        
        if (match == null || match.getScore() < RECOGNITION_THRESHOLD) {
            return new RecognitionResult(null, match != null ? match.getScore() : 0.0, faceRect, "Visage non reconnu");
        }
        
        // Utilisateur reconnu
        Utilisateur recognizedUser = usersWithFace.get(match.getIndex());
        return new RecognitionResult(recognizedUser, match.getScore(), faceRect, "Reconnu");
    }
    
    /**
     * Vérifie si un visage correspond à un utilisateur spécifique.
     * @param userId ID de l'utilisateur
     * @param faceImage Image contenant le visage
     * @return Score de correspondance (0.0 à 1.0)
     */
    public double verifyFace(int userId, Mat faceImage) {
        Utilisateur user = utilisateurDAO.getUtilisateurById(userId);
        if (user == null || !user.hasFaceVector()) {
            return 0.0;
        }
        
        Rect faceRect = faceDetector.detectLargestFace(faceImage);
        if (faceRect == null) {
            return 0.0;
        }
        
        Mat faceRegion = faceDetector.extractFaceRegion(faceImage, faceRect);
        if (faceRegion == null) {
            return 0.0;
        }
        
        byte[] features = featureExtractor.extractFeatures(faceRegion);
        faceRegion.release();
        
        if (features == null) {
            return 0.0;
        }
        
        return featureExtractor.compareFeatures(features, user.getFaceVector());
    }
    
    /**
     * Obtient le seuil de reconnaissance.
     */
    public double getRecognitionThreshold() {
        return RECOGNITION_THRESHOLD;
    }
    
    /**
     * Libère les ressources.
     */
    public void release() {
        if (faceDetector != null) {
            faceDetector.release();
        }
        if (featureExtractor != null) {
            featureExtractor.release();
        }
    }
    
    /**
     * Classe pour stocker le résultat d'une reconnaissance.
     */
    public static class RecognitionResult {
        private final Utilisateur user;
        private final double confidence;
        private final Rect faceRect;
        private final String message;
        
        public RecognitionResult(Utilisateur user, double confidence, Rect faceRect, String message) {
            this.user = user;
            this.confidence = confidence;
            this.faceRect = faceRect;
            this.message = message;
        }
        
        public Utilisateur getUser() {
            return user;
        }
        
        public double getConfidence() {
            return confidence;
        }
        
        public Rect getFaceRect() {
            return faceRect;
        }
        
        public String getMessage() {
            return message;
        }
        
        public boolean isRecognized() {
            return user != null;
        }
    }
}
