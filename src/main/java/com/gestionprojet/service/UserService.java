package com.gestionprojet.service;

import com.gestionprojet.dao.UserDAO;
import com.gestionprojet.model.User;
import com.gestionprojet.model.enums.Role;
import com.gestionprojet.utils.PasswordUtils;
import com.gestionprojet.utils.ValidationUtils;

import java.util.List;
import java.util.Optional;

public class UserService {
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User createUser(String firstName, String lastName, String username, String email, String password, Role role) {
        validateUserData(firstName, lastName, username, email, password);
        
        if (userDAO.existsByUsername(username)) {
            throw new RuntimeException("Ce nom d'utilisateur existe déjà");
        }
        
        if (userDAO.existsByEmail(email)) {
            throw new RuntimeException("Cette adresse email existe déjà");
        }

        String hashedPassword = PasswordUtils.hashPassword(password);
        User user = new User(firstName, lastName, username, email, hashedPassword, role);
        return userDAO.save(user);
    }

    public User updateUser(User user, String firstName, String lastName, String email, Role role) {
        validateBasicData(firstName, lastName, email);
        
        // Vérifier si l'email existe déjà pour un autre utilisateur
        Optional<User> existingUser = userDAO.findByEmail(email);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
            throw new RuntimeException("Cette adresse email existe déjà");
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRole(role);
        
        return userDAO.save(user);
    }

    public void changePassword(User user, String newPassword) {
        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new RuntimeException("Le mot de passe doit contenir au moins 6 caractères");
        }
        
        user.setPasswordHash(PasswordUtils.hashPassword(newPassword));
        userDAO.save(user);
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public List<User> getAllUsersIncludingHidden() {
        return userDAO.findAllIncludingHidden();
    }

    public List<User> getActiveUsers() {
        return userDAO.findActiveUsers();
    }

    public List<User> getInactiveUsers() {
        return userDAO.findInactiveUsers();
    }

    public List<User> getHiddenUsers() {
        return userDAO.findHiddenUsers();
    }

    public void toggleUserStatus(User user) {
        userDAO.toggleUserStatus(user);
    }

    public void hideUser(User user) {
        userDAO.hideUser(user);
    }

    public void deleteUserPermanently(User user) {
        userDAO.delete(user);
    }

    public boolean canManageUsers(User currentUser) {
        return currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.SUPER_ADMIN;
    }

    public boolean canViewHiddenUsers(User currentUser) {
        return currentUser.getRole() == Role.SUPER_ADMIN;
    }

    public boolean canDeletePermanently(User currentUser) {
        return currentUser.getRole() == Role.SUPER_ADMIN;
    }

    private void validateUserData(String firstName, String lastName, String username, String email, String password) {
        validateBasicData(firstName, lastName, email);
        
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Le nom d'utilisateur est obligatoire");
        }
        
        if (password == null || password.trim().length() < 6) {
            throw new RuntimeException("Le mot de passe doit contenir au moins 6 caractères");
        }
    }

    private void validateBasicData(String firstName, String lastName, String email) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new RuntimeException("Le prénom est obligatoire");
        }
        
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new RuntimeException("Le nom est obligatoire");
        }
        
        if (!ValidationUtils.isValidEmail(email)) {
            throw new RuntimeException("L'adresse email n'est pas valide");
        }
    }
}