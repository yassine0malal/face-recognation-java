module com.facialaccess {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    
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
    
    // Exports et opens pour JavaFX
    opens com.facialaccess to javafx.fxml;
    opens com.facialaccess.ui to javafx.fxml;
    
    exports com.facialaccess;
    exports com.facialaccess.ui;
    exports com.facialaccess.model;
    exports com.facialaccess.service;
}
