package com.facialaccess;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class TestLoad {
    public static void main(String[] args) {
        Platform.startup(() -> {
            try {
                System.out.println("Attempting to load...");
                FXMLLoader loader = new FXMLLoader(TestLoad.class.getResource("/fxml/personnel_directory_view.fxml"));
                Parent root = loader.load();
                System.out.println("SUCCESS! Loaded parent: " + root);
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}
