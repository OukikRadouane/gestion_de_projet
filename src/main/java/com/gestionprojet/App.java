package com.gestionprojet;

import com.gestionprojet.controller.PrimaryController;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    private static Scene scene;

    @Override
    public void start(Stage stage) {
        PrimaryController controller = new PrimaryController();
        Parent root = controller.createView();
        scene = new Scene(root, 1280, 720);
        stage.setTitle("Gestion de Projet");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
