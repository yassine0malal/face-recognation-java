package com.facialaccess.presentation;

import com.facialaccess.util.NavigationUtil;
import com.facialaccess.vision.CameraCapture;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
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

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Contrôleur pour l'écran de capture caméra.
 * Affiche le flux vidéo réel dans le cercle de scan.
 */
public class CameraController {

    @FXML private StackPane cameraCirclePane;
    @FXML private Circle outerCircle;
    @FXML private Circle bgCircle;
    @FXML private ImageView cameraView;
    @FXML private javafx.scene.Node placeholderIcon;
    @FXML private Label statusLabel;
    @FXML private Label statusTopLabel;
    @FXML private Label instructionLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private HBox actionButtons;
    @FXML private Button retryButton;
    @FXML private Button cancelButton;

    private CameraCapture cameraCapture;
    private Java2DFrameConverter frameConverter;
    private Timeline frameGrabberTimeline;
    private SequentialTransition pulseAnimation;
    private RotateTransition rotateAnimation;
    private boolean cameraActive = false;

    @FXML
    public void initialize() {
        // Le clip doit être centré sur l'ImageView.
        // fitWidth/fitHeight = 356, donc le centre est à (178, 178)
        double radius = 178;
        Circle clip = new Circle(radius, radius, radius);
        cameraView.setClip(clip);

        // Centrer l'ImageView dans le StackPane
        StackPane.setAlignment(cameraView, javafx.geometry.Pos.CENTER);

        startPulseAnimation();
        startCamera();
    }

    // ─── Caméra ──────────────────────────────────────────────────────────────

    private void startCamera() {
        updateTopStatus("INITIALIZING...", false);

        new Thread(() -> {
            try {
                cameraCapture = new CameraCapture();
                frameConverter = new Java2DFrameConverter();
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

                    // Démarrer la boucle de capture sur le thread JavaFX
                    frameGrabberTimeline = new Timeline(
                        new KeyFrame(Duration.millis(33), e -> grabFrame())
                    );
                    frameGrabberTimeline.setCycleCount(Timeline.INDEFINITE);
                    frameGrabberTimeline.play();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    updateTopStatus("CAMERA ERROR", false);
                    updateStatus("Camera error: " + e.getMessage(), ScanStatus.ERROR);
                    showActionButtons();
                });
            }
        }, "camera-thread").start();
    }

    private void grabFrame() {
        if (!cameraActive || cameraCapture == null) return;

        try {
            Frame frame = cameraCapture.getGrabber().grab();
            if (frame == null || frame.image == null) return;

            BufferedImage buffered = frameConverter.convert(frame);
            if (buffered == null) return;

            // Recadrer en carré centré (crop du centre) pour remplir le cercle
            int w = buffered.getWidth();
            int h = buffered.getHeight();
            int size = Math.min(w, h);
            int x = (w - size) / 2;
            int y = (h - size) / 2;
            BufferedImage square = buffered.getSubimage(x, y, size, size);

            WritableImage fxImage = SwingFXUtils.toFXImage(square, null);
            Platform.runLater(() -> cameraView.setImage(fxImage));

        } catch (Exception e) {
            // Frame manquée — on continue
        }
    }

    // ─── Animations ──────────────────────────────────────────────────────────

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

    // ─── UI helpers ──────────────────────────────────────────────────────────

    private void updateStatus(String message, ScanStatus status) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            outerCircle.getStyleClass().removeAll("success-circle", "error-circle", "warning-circle");
            statusLabel.getStyleClass().removeAll("success-text", "error-text", "warning-text");

            switch (status) {
                case SUCCESS -> {
                    outerCircle.getStyleClass().add("success-circle");
                    statusLabel.getStyleClass().add("success-text");
                    instructionLabel.setVisible(false);
                }
                case ERROR -> {
                    outerCircle.getStyleClass().add("error-circle");
                    statusLabel.getStyleClass().add("error-text");
                    instructionLabel.setText("Please try again");
                }
                case WARNING -> {
                    outerCircle.getStyleClass().add("warning-circle");
                    statusLabel.getStyleClass().add("warning-text");
                }
                default -> { /* SCANNING — bleu par défaut */ }
            }
        });
    }

    private void updateTopStatus(String text, boolean ready) {
        Platform.runLater(() -> {
            statusTopLabel.setText(text);
            // Réutilise le style du login.css
            if (ready) {
                statusTopLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555; -fx-font-weight: 600;");
            } else {
                statusTopLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e65100; -fx-font-weight: 600;");
            }
        });
    }

    private void showActionButtons() {
        Platform.runLater(() -> {
            actionButtons.setVisible(true);
            actionButtons.setManaged(true);
        });
    }

    // ─── Handlers ────────────────────────────────────────────────────────────

    @FXML
    private void handleRetry() {
        actionButtons.setVisible(false);
        actionButtons.setManaged(false);
        instructionLabel.setVisible(true);
        instructionLabel.setText("Position your face within the circle");
        outerCircle.getStyleClass().removeAll("success-circle", "error-circle", "warning-circle");
        statusLabel.getStyleClass().removeAll("success-text", "error-text", "warning-text");

        stopCamera();
        startCamera();
    }

    @FXML
    private void handleCancel() {
        stopCamera();
        try {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            NavigationUtil.navigateToWelcome(stage);
        } catch (IOException e) {
            System.err.println("Erreur navigation: " + e.getMessage());
        }
    }

    private void stopCamera() {
        cameraActive = false;
        if (frameGrabberTimeline != null) frameGrabberTimeline.stop();
        if (pulseAnimation != null) pulseAnimation.stop();
        if (rotateAnimation != null) rotateAnimation.stop();
        if (cameraCapture != null) cameraCapture.stop();
    }

    private enum ScanStatus { SCANNING, SUCCESS, ERROR, WARNING }
}
