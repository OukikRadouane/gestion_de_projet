module com.gestionprojet.gestiondeprojet {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires static lombok;

    opens com.gestionprojet to javafx.fxml;
    opens com.gestionprojet.controller to javafx.fxml;
    opens com.gestionprojet.view to javafx.fxml;

    exports com.gestionprojet;
    exports com.gestionprojet.controller;
    exports com.gestionprojet.view;
}
