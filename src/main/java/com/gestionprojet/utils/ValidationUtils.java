package com.gestionprojet.utils;

import java.util.regex.Pattern;

public class ValidationUtils {
    
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MIN_USERNAME_LENGTH = 3;
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }
    
    public static boolean isValidUsername(String username) {
        return username != null && 
               username.trim().length() >= MIN_USERNAME_LENGTH && 
               username.matches("^[a-zA-Z0-9_]+$");
    }
    
    public static String validateRegistration(String username, String email, String password, String confirmPassword) {
        if (username == null || username.trim().isEmpty()) {
            return "Le nom d'utilisateur est obligatoire";
        }
        
        if (username.trim().length() < MIN_USERNAME_LENGTH) {
            return "Le nom d'utilisateur doit contenir au moins " + MIN_USERNAME_LENGTH + " caractères";
        }
        
        if (!isValidUsername(username)) {
            return "Le nom d'utilisateur ne peut contenir que des lettres, chiffres et underscores";
        }
        
        if (email == null || email.trim().isEmpty()) {
            return "L'email est obligatoire";
        }
        
        if (!isValidEmail(email)) {
            return "L'email n'est pas valide";
        }
        
        if (password == null || password.isEmpty()) {
            return "Le mot de passe est obligatoire";
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "Le mot de passe doit contenir au moins " + MIN_PASSWORD_LENGTH + " caractères";
        }
        
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            return "Veuillez confirmer le mot de passe";
        }
        
        if (!password.equals(confirmPassword)) {
            return "Les mots de passe ne correspondent pas";
        }
        
        return null;
    }
    
    public static String validateLogin(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            return "Le nom d'utilisateur ou l'email est obligatoire";
        }
        
        if (password == null || password.isEmpty()) {
            return "Le mot de passe est obligatoire";
        }
        
        return null;
    }
}