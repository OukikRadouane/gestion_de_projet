module com.gestionprojet.gestiondeprojet {
    requires javafx.controls;
    requires javafx.fxml;          // <-- AJOUTER CECI
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires static lombok;

    exports com.gestionprojet;
    exports com.gestionprojet.controller;
}
