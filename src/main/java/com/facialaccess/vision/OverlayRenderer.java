package com.facialaccess.vision;

import org.bytedeco.opencv.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * Rendu des overlays sur le flux vidéo.
 * Dessine les rectangles de détection et les informations.
 */
public class OverlayRenderer {
    
    // Couleurs prédéfinies (BGR format)
    private static final Scalar COLOR_GREEN = new Scalar(0, 255, 0, 0);
    private static final Scalar COLOR_RED = new Scalar(0, 0, 255, 0);
    private static final Scalar COLOR_BLUE = new Scalar(255, 0, 0, 0);
    private static final Scalar COLOR_YELLOW = new Scalar(0, 255, 255, 0);
    private static final Scalar COLOR_WHITE = new Scalar(255, 255, 255, 0);
    private static final Scalar COLOR_BLACK = new Scalar(0, 0, 0, 0);
    
    /**
     * Dessine un rectangle autour d'un visage.
     * @param image Image sur laquelle dessiner
     * @param faceRect Rectangle du visage
     * @param color Couleur du rectangle
     * @param thickness Épaisseur du trait
     */
    public void drawFaceRectangle(Mat image, Rect faceRect, Scalar color, int thickness) {
        if (image == null || faceRect == null) {
            return;
        }
        
        rectangle(image, faceRect, color, thickness, LINE_8, 0);
    }
    
    /**
     * Dessine du texte sur l'image.
     * @param image Image sur laquelle dessiner
     * @param text Texte à afficher
     * @param position Position du texte
     * @param color Couleur du texte
     */
    public void drawText(Mat image, String text, Point position, Scalar color) {
        drawText(image, text, position, color, 0.6, 2);
    }
    
    /**
     * Dessine du texte avec taille et épaisseur personnalisées.
     */
    public void drawText(Mat image, String text, Point position, Scalar color, double scale, int thickness) {
        if (image == null || text == null || position == null) {
            return;
        }
        
        // Dessiner un fond noir pour meilleure lisibilité
        int[] baseline = new int[1];
        org.bytedeco.javacpp.IntPointer baselinePtr = new org.bytedeco.javacpp.IntPointer(baseline);
        Size textSize = getTextSize(text, FONT_HERSHEY_SIMPLEX, scale, thickness, baselinePtr);
        
        rectangle(image,
            new Point(position.x(), position.y() - textSize.height() - 5),
            new Point(position.x() + textSize.width(), position.y() + 5),
            COLOR_BLACK,
            FILLED,
            LINE_8,
            0
        );
        
        // Dessiner le texte
        putText(image, text, position, FONT_HERSHEY_SIMPLEX, scale, color, thickness, LINE_AA, false);
    }
    
    /**
     * Dessine les informations de reconnaissance.
     * @param image Image sur laquelle dessiner
     * @param userName Nom de l'utilisateur reconnu
     * @param confidence Score de confiance (0.0 à 1.0)
     * @param faceRect Rectangle du visage
     */
    public void drawRecognitionInfo(Mat image, String userName, double confidence, Rect faceRect) {
        if (image == null || faceRect == null) {
            return;
        }
        
        // Choisir la couleur selon le score de confiance
        Scalar color = confidence >= 0.75 ? COLOR_GREEN : COLOR_RED;
        
        // Dessiner le rectangle autour du visage
        drawFaceRectangle(image, faceRect, color, 3);
        
        // Préparer le texte
        String confidenceText = String.format("%.0f%%", confidence * 100);
        String displayText = userName != null ? userName + " (" + confidenceText + ")" : "Inconnu (" + confidenceText + ")";
        
        // Dessiner le nom et la confiance au-dessus du rectangle
        Point textPosition = new Point(faceRect.x(), faceRect.y() - 10);
        drawText(image, displayText, textPosition, color, 0.7, 2);
    }
    
    /**
     * Dessine un indicateur de statut d'accès.
     * @param image Image sur laquelle dessiner
     * @param status Statut ("GRANTED" ou "DENIED")
     * @param position Position de l'indicateur
     */
    public void drawAccessStatus(Mat image, String status, Point position) {
        if (image == null || status == null) {
            return;
        }
        
        Scalar color;
        String text;
        
        if ("GRANTED".equals(status)) {
            color = COLOR_GREEN;
            text = "ACCES ACCORDE";
        } else {
            color = COLOR_RED;
            text = "ACCES REFUSE";
        }
        
        // Dessiner un rectangle de fond
        org.bytedeco.javacpp.IntPointer baselinePtr = new org.bytedeco.javacpp.IntPointer(1);
        Size textSize = getTextSize(text, FONT_HERSHEY_SIMPLEX, 1.2, 3, baselinePtr);
        rectangle(image,
            new Point(position.x() - 10, position.y() - textSize.height() - 10),
            new Point(position.x() + textSize.width() + 10, position.y() + 10),
            color,
            FILLED,
            LINE_8,
            0
        );
        
        // Dessiner le texte en blanc
        putText(image, text, position, FONT_HERSHEY_SIMPLEX, 1.2, COLOR_WHITE, 3, LINE_AA, false);
    }
    
    /**
     * Dessine un compteur FPS.
     * @param image Image sur laquelle dessiner
     * @param fps Frames par seconde
     */
    public void drawFPS(Mat image, int fps) {
        if (image == null) {
            return;
        }
        
        String fpsText = "FPS: " + fps;
        Point position = new Point(10, 30);
        drawText(image, fpsText, position, COLOR_YELLOW, 0.6, 2);
    }
    
    /**
     * Dessine un message d'information.
     * @param image Image sur laquelle dessiner
     * @param message Message à afficher
     * @param position Position du message
     */
    public void drawInfoMessage(Mat image, String message, Point position) {
        if (image == null || message == null) {
            return;
        }
        
        drawText(image, message, position, COLOR_BLUE, 0.6, 2);
    }
    
    /**
     * Dessine un indicateur "Aucun visage détecté".
     * @param image Image sur laquelle dessiner
     */
    public void drawNoFaceDetected(Mat image) {
        if (image == null) {
            return;
        }
        
        String message = "Aucun visage detecte";
        int centerX = image.cols() / 2 - 150;
        int centerY = image.rows() / 2;
        
        Point position = new Point(centerX, centerY);
        drawText(image, message, position, COLOR_YELLOW, 1.0, 2);
    }
    
    /**
     * Dessine un indicateur "Positionnez votre visage".
     * @param image Image sur laquelle dessiner
     */
    public void drawPositionGuide(Mat image) {
        if (image == null) {
            return;
        }
        
        // Dessiner un cadre guide au centre
        int centerX = image.cols() / 2;
        int centerY = image.rows() / 2;
        int guideSize = 200;
        
        Rect guideRect = new Rect(
            centerX - guideSize / 2,
            centerY - guideSize / 2,
            guideSize,
            guideSize
        );
        
        rectangle(image, guideRect, COLOR_BLUE, 2, LINE_8, 0);
        
        String message = "Positionnez votre visage ici";
        Point textPos = new Point(centerX - 120, centerY - guideSize / 2 - 10);
        drawText(image, message, textPos, COLOR_BLUE, 0.6, 2);
    }
    
    /**
     * Dessine des statistiques sur l'image.
     * @param image Image sur laquelle dessiner
     * @param stats Tableau de statistiques [label, valeur]
     */
    public void drawStats(Mat image, String[][] stats) {
        if (image == null || stats == null) {
            return;
        }
        
        int y = 60;
        for (String[] stat : stats) {
            String text = stat[0] + ": " + stat[1];
            drawText(image, text, new Point(10, y), COLOR_WHITE, 0.5, 1);
            y += 25;
        }
    }
}
