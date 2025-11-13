package com.gestionprojet.controller;

import com.gestionprojet.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;



public class AuthController {
    @FXML private TextField name;
    @FXML private TextField lastName;
    @FXML private TextField mail;
    @FXML private PasswordField mdp;
    @FXML private PasswordField mdpConfirm;
    @FXML private ChoiceBox<String> roleField;
    @FXML private Label errorLabel;


    @FXML
    public void handleSigneup(ActionEvent event){
        String nom = name.getText();
        String prenom = lastName.getText();
        String password = mdp.getText();
        String passwordConfirm = mdpConfirm.getText();
        String role = roleField.getValue();
        String email = mail.getText();
        if(nom.trim().isEmpty() || !nom.trim().matches("^[A-Za-z]+( [A-Za-z]+)*$")){
            //layoutX="77.0" layoutY="95.0"
            errorLabel.setLayoutX(77.0);
            errorLabel.setLayoutY(118.0);
            errorLabel.setText("le nom est invalid");
            return;
        }
        if(prenom.trim().isEmpty() || !prenom.trim().matches("^[A-Za-z]+( [A-Za-z]+)*$")){
            //layoutX="77.0" layoutY="147.0"
            errorLabel.setLayoutX(77.0);
            errorLabel.setLayoutY(170.0);
            errorLabel.setText("le prenom est invalid");
            return;
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
            //layoutX="77.0" layoutY="197.0"
            errorLabel.setLayoutX(100.0);
            errorLabel.setLayoutY(220.0);
            errorLabel.setText("Invalid email address");
            return ;
        }
        if(!password.equals(passwordConfirm) || password.trim().isEmpty() ){
            //layoutX="77.0" layoutY="243.0"
            errorLabel.setLayoutX(70.0);
            errorLabel.setLayoutY(266.0);
            errorLabel.setText("Les mots de passe ne correspondent pas ou elle est vide!");
            return;
        }

        User user = new User();
        user.setUsername(nom.trim() + " " + prenom.trim()); // exemple
        user.setPasswordHash(password);
        user.setRole(role);
        user.setEmail(email);
        try{
            Configuration config = new Configuration()
                    .addAnnotatedClass(User.class)
                    .configure();
            SessionFactory factory = config.buildSessionFactory();
            Session session = factory.openSession();
            Transaction transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
