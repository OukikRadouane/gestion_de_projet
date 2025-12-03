package com.gestionprojet;

import com.gestionprojet.controller.AuthController;
import com.gestionprojet.dao.ProjectDAO;
import com.gestionprojet.dao.UserDAO;
import com.gestionprojet.service.AuthService;
import com.gestionprojet.utils.HibernateUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class App extends Application {

    private SessionFactory sessionFactory;
    private Session session;

    @Override
    public void start(Stage primaryStage) throws Exception {

        System.out.println("Démarrage de l'application...");

        // Initialisation Hibernate
        sessionFactory = HibernateUtil.getSessionFactory();
        session = sessionFactory.openSession();

        // DAO + Service
        UserDAO userDao = new UserDAO();
        ProjectDAO  projectDao = new ProjectDAO();
        AuthService authService = new AuthService(userDao, projectDao);

        // Charger la vue Login.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        Parent root = loader.load();

        // Injection du service dans le controller
        AuthController controller = loader.getController();
        controller.setAuthService(authService);

        // Paramètres fenêtre
        Scene scene = new Scene(root);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(650);
        primaryStage.setWidth(900);
        primaryStage.setHeight(650);

        primaryStage.setTitle("Project Manager - Connexion");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
        primaryStage.show();

        System.out.println("Application démarrée avec succès");
    }

    @Override
    public void stop() {
        System.out.println("Arrêt de l'application...");
        if (session != null && session.isOpen()) {
            session.close();
        }
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
