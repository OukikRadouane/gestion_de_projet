package com.gestionprojet.service;

import com.gestionprojet.dao.ProjectDAO;
import com.gestionprojet.model.Project;
import com.gestionprojet.model.User;
import com.gestionprojet.model.enums.Role;
import com.gestionprojet.dao.UserDAO;
import com.gestionprojet.utils.PasswordUtils;
import com.gestionprojet.utils.ValidationUtils;

import java.util.List;
import java.util.Optional;

public class AuthService {
    private final UserDAO userDAO;
    private final ProjectDAO projectDAO;


    public AuthService(UserDAO userDAO, ProjectDAO projectDAO) {
        this.userDAO = userDAO;
        this.projectDAO = projectDAO;
    }

    public User register(String firstName, String lastName, String username, String email, String password, String confirmPassword, Role role) {
        System.out.println("Début inscription - Username: " + username + ", Email: " + email);
        
        String validationError = ValidationUtils.validateRegistration(username, email, password, confirmPassword);
        if (validationError != null) {
            System.out.println("Erreur validation: " + validationError);
            throw new IllegalArgumentException(validationError);
        }

        if (userDAO.existsByUsername(username)) {
            System.out.println("Username existe déjà: " + username);
            throw new IllegalArgumentException("Ce nom d'utilisateur existe déjà");
        }

        if (userDAO.existsByEmail(email)) {
            System.out.println("Email existe déjà: " + email);
            throw new IllegalArgumentException("Cet email existe déjà");
        }

        String passwordHash = PasswordUtils.hashPassword(password);
        System.out.println("Password hashé: " + passwordHash);

        User user = new User(firstName, lastName, username, email, passwordHash, role != null ? role : Role.USER);

        User savedUser = userDAO.save(user);
        System.out.println("Utilisateur enregistré avec ID: " + savedUser.getId());
        return savedUser;
    }

    public User login(String usernameOrEmail, String password) {
        System.out.println("Tentative de connexion: " + usernameOrEmail);
        
        String validationError = ValidationUtils.validateLogin(usernameOrEmail, password);
        if (validationError != null) {
            System.out.println("Erreur validation: " + validationError);
            throw new IllegalArgumentException(validationError);
        }

        Optional<User> userOptional;

        if (ValidationUtils.isValidEmail(usernameOrEmail)) {
            System.out.println("Recherche par email: " + usernameOrEmail);
            userOptional = userDAO.findByEmail(usernameOrEmail);
        } else {
            System.out.println("Recherche par username: " + usernameOrEmail);
            userOptional = userDAO.findByUsername(usernameOrEmail);
        }

        if (userOptional.isEmpty()) {
            System.out.println("Utilisateur non trouvé");
            throw new IllegalArgumentException("Utilisateur non trouvé");
        }

        User user = userOptional.get();
        System.out.println("Utilisateur trouvé: " + user.getEmail());
        System.out.println("Hash stocké: " + user.getPasswordHash());
        System.out.println("Hash du password fourni: " + PasswordUtils.hashPassword(password));

        // Vérifier si l'utilisateur est actif
        if (!user.isActive()) {
            System.out.println("Utilisateur désactivé");
            throw new IllegalArgumentException("Votre compte a été désactivé. Contactez un administrateur.");
        }

        // Vérifier si l'utilisateur est masqué
        if (user.isHidden()) {
            System.out.println("Utilisateur masqué");
            throw new IllegalArgumentException("Utilisateur non trouvé");
        }

        if (!PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
            System.out.println("Mot de passe incorrect");
            throw new IllegalArgumentException("Mot de passe incorrect");
        }

        System.out.println("Connexion réussie!");
        SessionManager.getInstance().setCurrentUser(user);
        return user;
    }

    public void logout() {
        SessionManager.getInstance().logout();
    }

    public User getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser();
    }

    public boolean isLoggedIn() {
        return SessionManager.getInstance().isLoggedIn();
    }

    public User updateProfile(User user, String newEmail, String newPassword) {
        if (newEmail != null && !newEmail.equals(user.getEmail())) {
            if (!ValidationUtils.isValidEmail(newEmail)) {
                throw new IllegalArgumentException("L'email n'est pas valide");
            }
            if (userDAO.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Cet email existe déjà");
            }
            user.setEmail(newEmail);
        }

        if (newPassword != null && !newPassword.isEmpty()) {
            if (!ValidationUtils.isValidPassword(newPassword)) {
                throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caractères");
            }
            user.setPasswordHash(PasswordUtils.hashPassword(newPassword));
        }

        return userDAO.save(user);
    }
    public List<Project> getAllProjectsOfCurrentUser() {
        User currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            throw new IllegalStateException("Aucun utilisateur connecté");
        }

        return projectDAO.getAllProjectsByUser(currentUser);
    }

}
