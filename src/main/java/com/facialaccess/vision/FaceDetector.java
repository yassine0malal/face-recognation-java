package com.facialaccess.vision;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * Détection de visages dans une image.
 * Utilise les cascades Haar d'OpenCV.
 */
public class FaceDetector {
    
    private CascadeClassifier faceCascade;
    private Mat grayImage;
    
    /**
     * Initialise le détecteur avec le fichier cascade.
     * @param cascadePath Chemin vers haarcascade_frontalface_default.xml
     */
    public FaceDetector(String cascadePath) {
        faceCascade = new CascadeClassifier(cascadePath);
        
        if (faceCascade.empty()) {
            throw new RuntimeException("Impossible de charger le fichier cascade: " + cascadePath);
        }
        
        grayImage = new Mat();
        System.out.println("✓ Détecteur de visages initialisé");
    }
    
    /**
     * Détecte les visages dans une image.
     * @param image Image à analyser (Mat en couleur)
     * @return Liste des rectangles contenant les visages
     */
    public RectVector detectFaces(Mat image) {
        if (image == null || image.empty()) {
            return new RectVector();
        }
        
        // Convertir en niveaux de gris
        cvtColor(image, grayImage, COLOR_BGR2GRAY);
        
        // Égaliser l'histogramme pour améliorer la détection
        equalizeHist(grayImage, grayImage);
        
        // Détecter les visages
        RectVector faces = new RectVector();
        faceCascade.detectMultiScale(
            grayImage,
            faces,
            1.1,        // scaleFactor
            3,          // minNeighbors
            0,          // flags
            new Size(30, 30),  // minSize
            new Size()         // maxSize
        );
        
        return faces;
    }
    
    /**
     * Détecte le visage le plus grand (le plus proche de la caméra).
     * @param image Image à analyser
     * @return Rectangle du visage le plus grand, ou null si aucun visage
     */
    public Rect detectLargestFace(Mat image) {
        RectVector faces = detectFaces(image);
        
        if (faces.size() == 0) {
            return null;
        }
        
        // Trouver le visage le plus grand
        Rect largestFace = null;
        int maxArea = 0;
        
        for (int i = 0; i < faces.size(); i++) {
            Rect face = faces.get(i);
            int area = face.width() * face.height();
            
            if (area > maxArea) {
                maxArea = area;
                largestFace = face;
            }
        }
        
        return largestFace;
    }
    
    /**
     * Vérifie si au moins un visage est détecté.
     * @param image Image à analyser
     * @return true si au moins un visage est détecté
     */
    public boolean hasFace(Mat image) {
        RectVector faces = detectFaces(image);
        return faces.size() > 0;
    }
    
    /**
     * Compte le nombre de visages détectés.
     * @param image Image à analyser
     * @return Nombre de visages
     */
    public int countFaces(Mat image) {
        RectVector faces = detectFaces(image);
        return (int) faces.size();
    }
    
    /**
     * Extrait la région du visage de l'image.
     * @param image Image source
     * @param faceRect Rectangle du visage
     * @return Image du visage recadrée
     */
    public Mat extractFaceRegion(Mat image, Rect faceRect) {
        if (image == null || faceRect == null) {
            return null;
        }
        
        try {
            // Recadrer l'image au rectangle du visage
            Mat faceRegion = new Mat(image, faceRect);
            return faceRegion.clone();
        } catch (Exception e) {
            System.err.println("✗ Erreur lors de l'extraction du visage: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Libère les ressources.
     */
    public void release() {
        if (grayImage != null) {
            grayImage.release();
        }
        if (faceCascade != null) {
            faceCascade.close();
        }
    }
}
