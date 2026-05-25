module com.facialaccess {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    
    // Base de données
    requires java.sql;
    
    // Logging
    requires org.slf4j;
    
    // Email
    requires jakarta.mail;
    requires jakarta.activation;
    
    // QR Code
    requires com.google.zxing;
    requires com.google.zxing.javase;
    
    // JavaCV et OpenCV
    requires org.bytedeco.javacv;
    requires org.bytedeco.opencv;
    
    // Ikonli for Icons
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign2;
    
    // Exports et opens pour JavaFX
    opens com.facialaccess to javafx.fxml;
    opens com.facialaccess.presentation to javafx.fxml;
    
    exports com.facialaccess;
    exports com.facialaccess.presentation;
    exports com.facialaccess.model;
    exports com.facialaccess.service;
    exports com.facialaccess.dao;
    exports com.facialaccess.util;
    exports com.facialaccess.vision;
}
