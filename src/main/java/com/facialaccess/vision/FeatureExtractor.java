package com.facialaccess.vision;

import org.bytedeco.opencv.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

import java.nio.ByteBuffer;

/**
 * Extraction des caractéristiques faciales.
 * Génère le vecteur de features pour la reconnaissance.
 */
public class FeatureExtractor {
    
    private static final int FACE_SIZE = 100; // Taille normalisée du visage
    private Mat normalizedFace;
    
    public FeatureExtractor() {
        normalizedFace = new Mat();
        System.out.println("✓ Extracteur de features initialisé");
    }
    
    /**
     * Extrait le vecteur de caractéristiques d'un visage.
     * @param faceImage Image du visage (recadrée)
     * @return Vecteur de features (byte[])
     */
    public byte[] extractFeatures(Mat faceImage) {
        if (faceImage == null || faceImage.empty()) {
            return null;
        }
        
        try {
            // 1. Convertir en niveaux de gris
            Mat grayFace = new Mat();
            if (faceImage.channels() > 1) {
                cvtColor(faceImage, grayFace, COLOR_BGR2GRAY);
            } else {
                grayFace = faceImage.clone();
            }
            
            // 2. Redimensionner à une taille fixe
            resize(grayFace, normalizedFace, new Size(FACE_SIZE, FACE_SIZE));
            
            // 3. Égaliser l'histogramme pour normaliser l'éclairage
            // CLAHE (Contrast Limited Adaptive Histogram Equalization) est meilleur que equalizeHist
            // pour gérer les variations d'éclairage
            equalizeHist(normalizedFace, normalizedFace);
            
            // 4. Normaliser les valeurs de pixels (0-255 → 0-1 → 0-255)
            // Cela aide à réduire l'impact des différences d'exposition
            normalize(normalizedFace, normalizedFace, 0, 255, NORM_MINMAX, -1, null);
            
            // 5. Convertir en byte[]
            byte[] features = matToByteArray(normalizedFace);
            
            grayFace.release();
            
            return features;
            
        } catch (Exception e) {
            System.err.println("✗ Erreur lors de l'extraction des features: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Compare deux vecteurs de features.
     * @param features1 Premier vecteur
     * @param features2 Deuxième vecteur
     * @return Score de similarité (0.0 à 1.0, 1.0 = identique)
     */
    public double compareFeatures(byte[] features1, byte[] features2) {
        if (features1 == null || features2 == null) {
            return 0.0;
        }
        
        if (features1.length != features2.length) {
            return 0.0;
        }
        
        try {
            // Convertir en Mat
            Mat mat1 = byteArrayToMat(features1);
            Mat mat2 = byteArrayToMat(features2);
            
            // Calculer la corrélation normalisée
            Mat result = new Mat();
            matchTemplate(mat1, mat2, result, TM_CCOEFF_NORMED);
            
            // Extraire le score
            double[] minVal = new double[1];
            double[] maxVal = new double[1];
            minMaxLoc(result, minVal, maxVal, null, null, null);
            
            double similarity = maxVal[0];
            
            // Libérer les ressources
            mat1.release();
            mat2.release();
            result.release();
            
            // Normaliser entre 0 et 1
            return Math.max(0.0, Math.min(1.0, similarity));
            
        } catch (Exception e) {
            System.err.println("✗ Erreur lors de la comparaison: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Compare un visage avec une liste de vecteurs stockés.
     * @param faceImage Image du visage à identifier
     * @param storedFeatures Liste des vecteurs stockés en BDD
     * @return Index du meilleur match + score, ou null si aucun match
     */
    public MatchResult findBestMatch(Mat faceImage, java.util.List<byte[]> storedFeatures) {
        byte[] features = extractFeatures(faceImage);
        if (features == null) {
            return null;
        }
        
        double bestScore = 0.0;
        int bestIndex = -1;
        
        for (int i = 0; i < storedFeatures.size(); i++) {
            double score = compareFeatures(features, storedFeatures.get(i));
            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }
        
        if (bestIndex >= 0) {
            return new MatchResult(bestIndex, bestScore);
        }
        
        return null;
    }
    
    /**
     * Convertit un Mat en byte[].
     */
    private byte[] matToByteArray(Mat mat) {
        int size = (int) (mat.total() * mat.elemSize());
        byte[] data = new byte[size];
        ByteBuffer buffer = mat.createBuffer();
        buffer.get(data);
        return data;
    }
    
    /**
     * Convertit un byte[] en Mat.
     */
    private Mat byteArrayToMat(byte[] data) {
        Mat mat = new Mat(FACE_SIZE, FACE_SIZE, CV_8UC1);
        ByteBuffer buffer = mat.createBuffer();
        buffer.put(data);
        return mat;
    }
    
    /**
     * Libère les ressources.
     */
    public void release() {
        if (normalizedFace != null) {
            normalizedFace.release();
        }
    }
    
    /**
     * Classe pour stocker le résultat d'un match.
     */
    public static class MatchResult {
        private final int index;
        private final double score;
        
        public MatchResult(int index, double score) {
            this.index = index;
            this.score = score;
        }
        
        public int getIndex() {
            return index;
        }
        
        public double getScore() {
            return score;
        }
    }
}
