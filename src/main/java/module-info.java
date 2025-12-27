module gestiondeprojet {
    requires jakarta.persistence;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires static lombok;
    requires org.hibernate.orm.core;
    requires java.desktop;
    requires java.naming;
    requires java.prefs; 

    opens com.gestionprojet to javafx.fxml;
    exports com.gestionprojet;
    opens com.gestionprojet.controller to javafx.fxml;
    exports com.gestionprojet.controller;
    opens com.gestionprojet.model to org.hibernate.orm.core;
}