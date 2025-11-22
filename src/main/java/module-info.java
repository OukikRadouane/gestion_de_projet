module com.gestionprojet {
    // Modules requis
    requires static lombok;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.naming;
    
    // Ouvrir les packages pour la réflexion (FXML et Hibernate)
    opens com.gestionprojet to javafx.fxml;
    opens com.gestionprojet.controller to javafx.fxml;
    opens com.gestionprojet.model to org.hibernate.orm.core, javafx.base;
    opens com.gestionprojet.model.enums to org.hibernate.orm.core;
    opens com.gestionprojet.service to javafx.fxml; 
    
    // Exporter les packages
    exports com.gestionprojet;
    exports com.gestionprojet.controller;
    exports com.gestionprojet.model;
    exports com.gestionprojet.model.enums;
    exports com.gestionprojet.service; 
    exports com.gestionprojet.repository;
    exports com.gestionprojet.config;
    exports com.gestionprojet.utils;
}