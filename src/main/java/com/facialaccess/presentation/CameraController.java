package com.facialaccess.presentation;

import com.facialaccess.model.Utilisateur;
import com.facialaccess.service.AccessService;
import com.facialaccess.service.FaceRecognitionService;
import com.facialaccess.util.NavigationUtil;
import com.facialaccess.vision.CameraCapture;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Contrôleur de l'écran de scan facial.
 *
 * Pipeline :
 *  1. Affichage du flux caméra en temps réel dans le cercle
 *  2. Toutes les N frames → capture d'un Mat OpenCV
 *  3. FaceRecognitionService.recognizeFace(mat) → détection + extraction + comparaison
 *  4. Si reconnu → AccessService.logFaceAccess(userId, score) → GRANTED
 *     Sinon       → AccessService.logFaceAccess(null, score)  → DENIED
 *  5. Mise à jour de l'UI avec le résultat
 */
public class CameraController {

    // ── FXML ──────────────────────────────────────────────────────────────────
    @FXML private StackPane cameraCirclePane;
    @FXML private Circle    outerCircle;
    @FXML private Circle    bgCircle;
    @FXML private ImageView cameraView;
    @FXML private javafx.scene.Node placeholderIcon;
    @FXML private Label     statusLabel;
    @FXML private Label     statusTopLabel;
    @FXML private Label     instructionLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private HBox      actionButtons;
    @FXML private Button    retryButton;
    @FXML private Button    cancelButton;

    // ── Services ──────────────────────────────────────────────────────────────
    private FaceRecognitionService recognitionService;
    private AccessService          accessService;

    // ── Vision ────────────────────────────────────────────────────────────────
    private CameraCapture          cameraCapture;
    private Java2DFrameConverter   frameConverter;
    private OpenCVFrameConverter   opencvConverter;

    // ── Animations ────────────────────────────────────────────────────────────
    private SequentialTransition pulseAnimation;
    private RotateTransition     rotateAnimation;
    private Timeline             frameTimeline;

    // ── État ──────────────────────────────────────────────────────────────────
    private boolean cameraActive   = false;
    private boolean recognitionDone = false;

    /** Analyser 1 frame sur RECOGNITION_INTERVAL pour ne pas surcharger le CPU */
    private static final int RECOGNITION_INTERVAL = 15;
    private int frameCounter = 0;

    // ── Classe interne pour convertir Frame → Mat ─────────────────────────────
    private static class OpenCVFrameConverter {
        private final org.bytedeco.javacv.OpenCVFrameConverter.ToMat converter =
                new org.bytedeco.javacv.OpenCVFrameConverter.ToMat();

        Mat convert(Frame frame) {
            return converter.convert(frame);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Initialisation
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        // Clip circulaire centré sur l'ImageView (fitWidth/Height = 356)
        double r = 178;
        Circle clip = new Circle(r, r, r);
        cameraView.setClip(clip);
        StackPane.setAlignment(cameraView, Pos.CENTER);

        // Initialiser les services
        try {
            recognitionService = new FaceRecognitionService();
            accessService      = new AccessService();
        } catch (Exception e) {
            System.err.println("Erreur init services: " + e.getMessage());
        }

        startPulseAnimation();
        startCamera();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Caméra
    // ─────────────────────────────────────────────────────────────────────────

    private void startCamera() {
        updateTopStatus("INITIALIZING...", false);
        recognitionDone = false;
        frameCounter    = 0;

        new Thread(() -> {
            try {
                cameraCapture  = new CameraCapture();
                frameConverter = new Java2DFrameConverter();
                opencvConverter = new OpenCVFrameConverter();

                boolean started = cameraCapture.start(0);

                if (!started) {
                    Platform.runLater(() -> {
                        updateTopStatus("CAMERA ERROR", false);
                        updateStatus("Camera not available", ScanStatus.ERROR);
                        showActionButtons();
                    });
                    return;
                }

                cameraActive = true;

                Platform.runLater(() -> {
                    placeholderIcon.setVisible(false);
                    placeholderIcon.setManaged(false);
                    updateTopStatus("CAMERA ACTIVE", true);
                    updateStatus("Scanning face...", ScanStatus.SCANNING);

                    // Boucle principale : affichage + reconnaissance
                    frameTimeline = new Timeline(
                        new KeyFrame(Duration.millis(33), e -> processFrame())
                    );
                    frameTimeline.setCycleCount(Timeline.INDEFINITE);
                    frameTimeline.play();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    updateTopStatus("CAMERA ERROR", false);
                    updateStatus("Camera error: " + e.getMessage(), ScanStatus.ERROR);
                    showActionButtons();
                });
            }
        }, "camera-init-thread").start();
    }

    /**
     * Appelée ~30 fois/seconde par le Timeline.
     * Affiche la frame et, toutes les RECOGNITION_INTERVAL frames,
     * lance la reconnaissance en arrière-plan.
     */
    private void processFrame() {
        if (!cameraActive || cameraCapture == null) return;

        try {
            Frame frame = cameraCapture.getGrabber().grab();
            if (frame == null || frame.image == null) return;

            // ── 1. Affichage du flux ──────────────────────────────────────────
            BufferedImage buffered = frameConverter.convert(frame);
            if (buffered != null) {
                int w = buffered.getWidth(), h = buffered.getHeight();
                int size = Math.min(w, h);
                BufferedImage square = buffered.getSubimage((w - size) / 2, (h - size) / 2, size, size);
                WritableImage fxImage = SwingFXUtils.toFXImage(square, null);
                cameraView.setImage(fxImage);
            }

            // ── 2. Reconnaissance (toutes les N frames) ───────────────────────
            if (!recognitionDone && ++frameCounter >= RECOGNITION_INTERVAL) {
                frameCounter = 0;
                // Empêcher plusieurs threads simultanés
                recognitionDone = true;

                Mat mat = opencvConverter.convert(frame);
                if (mat != null && !mat.empty()) {
                    runRecognition(mat.clone());
                } else {
                    recognitionDone = false; // retry si mat invalide
                }
            }

        } catch (Exception e) {
            // Frame manquée — on continue sans bloquer
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Reconnaissance faciale
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Lance la reconnaissance dans un thread séparé pour ne pas bloquer l'UI.
     */
    private void runRecognition(Mat mat) {
        if (recognitionService == null) {
            recognitionDone = false;
            mat.release();
            return;
        }

        new Thread(() -> {
            try {
                FaceRecognitionService.RecognitionResult result =
                        recognitionService.recognizeFace(mat);
                mat.release();

                if (result == null) {
                    Platform.runLater(() -> recognitionDone = false);
                    return;
                }

                Platform.runLater(() -> handleRecognitionResult(result));

            } catch (Exception e) {
                System.err.println("Erreur reconnaissance: " + e.getMessage());
                mat.release();
                Platform.runLater(() -> recognitionDone = false);
            }
        }, "recognition-thread").start();
    }

    /**
     * Traite le résultat et met à jour l'UI + logs.
     */
    private void handleRecognitionResult(FaceRecognitionService.RecognitionResult result) {

        // Aucun visage détecté → cercle orange, on continue à scanner
        if (result.getFaceRect() == null) {
            setCircleColor("#ff9800"); // orange
            statusLabel.setText("No face detected — keep scanning...");
            recognitionDone = false;  // permettre une nouvelle tentative
            return;
        }

        // Visage détecté → arrêter les analyses répétées
        if (result.isRecognized()) {
            // ── ACCÈS ACCORDÉ ─────────────────────────────────────────────────
            Utilisateur user = result.getUser();
            double score     = result.getConfidence();
            String scoreStr  = String.format("%.0f%%", score * 100);

            if (accessService != null) {
                accessService.logFaceAccess(user.getId(), score);
            }

            // Cercle vert + message de bienvenue
            setCircleColor("#28a745");
            stopAnimations();
            statusLabel.setText("Welcome, " + user.getFullName() + "!");
            statusLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #28a745;");
            instructionLabel.setText("Access granted  •  Confidence: " + scoreStr);
            instructionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #28a745;");
            instructionLabel.setVisible(true);

            System.out.println("✓ GRANTED — " + user.getFullName() + " (" + scoreStr + ")");
            showActionButtons();

        } else {
            // ── ACCÈS REFUSÉ ──────────────────────────────────────────────────
            double score = result.getConfidence();

            if (accessService != null) {
                accessService.logFaceAccess(null, score);
            }

            // Cercle rouge + message de refus
            setCircleColor("#dc3545");
            stopAnimations();
            statusLabel.setText("Access Denied");
            statusLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #dc3545;");
            instructionLabel.setText("Face not recognized  •  " + result.getMessage());
            instructionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc3545;");
            instructionLabel.setVisible(true);

            System.out.println("✗ DENIED — " + result.getMessage()
                    + " (score: " + String.format("%.2f", score) + ")");
            showActionButtons();
        }
    }

    /** Change la couleur du cercle directement (bypass CSS inline). */
    private void setCircleColor(String hexColor) {
        outerCircle.setStroke(javafx.scene.paint.Color.web(hexColor));
        outerCircle.setEffect(new javafx.scene.effect.DropShadow(
                20, javafx.scene.paint.Color.web(hexColor + "88")));
    }

    /** Arrête les animations de rotation/pulsation après un résultat. */
    private void stopAnimations() {
        if (pulseAnimation  != null) pulseAnimation.stop();
        if (rotateAnimation != null) rotateAnimation.stop();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Animations
    // ─────────────────────────────────────────────────────────────────────────

    private void startPulseAnimation() {
        ScaleTransition up = new ScaleTransition(Duration.seconds(1.5), outerCircle);
        up.setFromX(1.0); up.setFromY(1.0);
        up.setToX(1.04);  up.setToY(1.04);

        ScaleTransition down = new ScaleTransition(Duration.seconds(1.5), outerCircle);
        down.setFromX(1.04); down.setFromY(1.04);
        down.setToX(1.0);    down.setToY(1.0);

        pulseAnimation = new SequentialTransition(up, down);
        pulseAnimation.setCycleCount(Timeline.INDEFINITE);
        pulseAnimation.play();

        rotateAnimation = new RotateTransition(Duration.seconds(5), outerCircle);
        rotateAnimation.setByAngle(360);
        rotateAnimation.setCycleCount(Timeline.INDEFINITE);
        rotateAnimation.setInterpolator(Interpolator.LINEAR);
        rotateAnimation.play();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void updateStatus(String message, ScanStatus status) {
        statusLabel.setText(message);
        outerCircle.getStyleClass().removeAll("success-circle", "error-circle", "warning-circle");
        statusLabel.getStyleClass().removeAll("success-text", "error-text", "warning-text");

        switch (status) {
            case SUCCESS -> {
                outerCircle.getStyleClass().add("success-circle");
                statusLabel.getStyleClass().add("success-text");
            }
            case ERROR -> {
                outerCircle.getStyleClass().add("error-circle");
                statusLabel.getStyleClass().add("error-text");
            }
            case WARNING -> {
                outerCircle.getStyleClass().add("warning-circle");
                statusLabel.getStyleClass().add("warning-text");
            }
            default -> { /* SCANNING — bleu par défaut */ }
        }
    }

    private void updateTopStatus(String text, boolean ready) {
        statusTopLabel.setText(text);
        statusTopLabel.setStyle(ready
            ? "-fx-font-size: 11px; -fx-text-fill: #555; -fx-font-weight: 600;"
            : "-fx-font-size: 11px; -fx-text-fill: #e65100; -fx-font-weight: 600;");
    }

    private void showActionButtons() {
        actionButtons.setVisible(true);
        actionButtons.setManaged(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Handlers boutons
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    private void handleRetry() {
        actionButtons.setVisible(false);
        actionButtons.setManaged(false);
        instructionLabel.setText("Position your face within the circle");
        instructionLabel.setVisible(true);
        instructionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");
        statusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 500; -fx-text-fill: #2c3e50;");
        // Remettre le cercle bleu
        outerCircle.setStroke(javafx.scene.paint.Color.web("#5c7cfa"));
        outerCircle.setEffect(null);

        stopCamera();

        // Recréer le service car release() a libéré les ressources OpenCV
        try {
            recognitionService = new FaceRecognitionService();
        } catch (Exception e) {
            System.err.println("Erreur recréation service: " + e.getMessage());
        }

        startPulseAnimation();
        startCamera();
    }

    @FXML
    private void handleCancel() {
        stopCamera();
        // Libérer les ressources OpenCV proprement à la sortie
        if (recognitionService != null) recognitionService.release();
        try {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            NavigationUtil.navigateToWelcome(stage);
        } catch (IOException e) {
            System.err.println("Erreur navigation: " + e.getMessage());
        }
    }

    private void stopCamera() {
        cameraActive = false;
        recognitionDone = false;
        if (frameTimeline   != null) frameTimeline.stop();
        if (pulseAnimation  != null) pulseAnimation.stop();
        if (rotateAnimation != null) rotateAnimation.stop();
        if (cameraCapture   != null) cameraCapture.stop();
        // Ne pas appeler recognitionService.release() ici —
        // le service est réutilisé ou recréé explicitement au Retry
    }

    private enum ScanStatus { SCANNING, SUCCESS, ERROR, WARNING }
}
