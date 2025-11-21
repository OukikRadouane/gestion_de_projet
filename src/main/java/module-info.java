module com.gestionprojet.gestiondeprojet {
    requires javafx.controls;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires static lombok;

    exports com.gestionprojet;
    exports com.gestionprojet.controller;
    exports com.gestionprojet.view;
}
