package com.facialaccess.vision;

import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;

/**
 * Gestion de la capture vidéo depuis la webcam.
 * Utilise JavaCV pour accéder à la caméra.
 */
public class CameraCapture {
    
    private FrameGrabber grabber;
    private OpenCVFrameConverter.ToMat converter;
    private boolean isRunning;
    
    public CameraCapture() {
        this.converter = new OpenCVFrameConverter.ToMat();
        this.isRunning = false;
    }
    
    /**
     * Démarre la capture vidéo.
     * @param deviceId ID de la caméra (0 = caméra intégrée du PC)
     * @return true si le démarrage a réussi
     */
    public boolean start(int deviceId) {
        try {
            // Créer le grabber pour la caméra
            grabber = new OpenCVFrameGrabber(deviceId);
            
            // Configurer la résolution
            grabber.setImageWidth(640);
            grabber.setImageHeight(480);
            grabber.setFrameRate(30);
            
            // Démarrer la capture
            grabber.start();
            isRunning = true;
            
            System.out.println("✓ Caméra démarrée (Device " + deviceId + ")");
            return true;
            
        } catch (FrameGrabber.Exception e) {
            System.err.println("✗ Erreur lors du démarrage de la caméra: " + e.getMessage());
            isRunning = false;
            return false;
        }
    }
    
    /**
     * Démarre la caméra par défaut (Device 0).
     */
    public boolean start() {
        return start(0);
    }
    
    /**
     * Capture une frame (image) depuis la caméra.
     * @return Mat (image OpenCV) ou null si erreur
     */
    public Mat captureFrame() {
        if (!isRunning || grabber == null) {
            return null;
        }
        
        try {
            Frame frame = grabber.grab();
            if (frame == null) {
                return null;
            }
            
            // Convertir Frame en Mat
            Mat mat = converter.convert(frame);
            return mat;
            
        } catch (FrameGrabber.Exception e) {
            System.err.println("✗ Erreur lors de la capture: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Arrête la capture vidéo et libère les ressources.
     */
    public void stop() {
        if (grabber != null) {
            try {
                grabber.stop();
                grabber.release();
                isRunning = false;
                System.out.println("✓ Caméra arrêtée");
            } catch (FrameGrabber.Exception e) {
                System.err.println("✗ Erreur lors de l'arrêt de la caméra: " + e.getMessage());
            }
        }
    }
    
    /**
     * Vérifie si la caméra est active.
     */
    public boolean isActive() {
        return isRunning;
    }
    
    /**
     * Obtient la largeur de l'image.
     */
    public int getWidth() {
        return grabber != null ? grabber.getImageWidth() : 0;
    }
    
    /**
     * Obtient la hauteur de l'image.
     */
    public int getHeight() {
        return grabber != null ? grabber.getImageHeight() : 0;
    }
    
    /**
     * Obtient le frame rate.
     */
    public double getFrameRate() {
        return grabber != null ? grabber.getFrameRate() : 0;
    }
}
