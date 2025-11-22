package com.gestionprojet;

import com.gestionprojet.config.HibernateConfig;
import com.gestionprojet.controller.AuthController;
import com.gestionprojet.repository.UserRepository;
import com.gestionprojet.service.AuthService;
import jakarta.persistence.EntityManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    private EntityManager entityManager;

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Démarrage de l'application...");
        entityManager = HibernateConfig.getEntityManager();

        UserRepository userRepository = new UserRepository(entityManager);
        AuthService authService = new AuthService(userRepository);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();

        AuthController controller = loader.getController();
        controller.setAuthService(authService);

        // CORRECTION : Choisissez une seule taille, pas deux
        Scene scene = new Scene(root); // Supprimez les dimensions ici
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(650);
        primaryStage.setWidth(900);
        primaryStage.setHeight(650);
    
        primaryStage.setTitle("Project Manager - Connexion");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
        
        System.out.println("Application démarrée avec succès");
    }

    @Override
    public void stop() {
        System.out.println("Arrêt de l'application...");
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }
        HibernateConfig.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}