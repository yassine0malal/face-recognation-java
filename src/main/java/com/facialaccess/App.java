package com.facialaccess;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Point d'entrée principal de l'application Facial Access System.
 * Lance l'interface JavaFX.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Facial Access System - OK");
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
