package com.gestionprojet.utils;
import java.util.regex.Pattern;

public class ValidationUtils {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidUsername(String username) {
        return username != null && username.length() >= 3 && username.length() <= 50;
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static String validateRegistration(String username, String email, String password, String confirmPassword) {
        if (username == null || username.trim().isEmpty()) {
            return "Le nom d'utilisateur est requis";
        }
        if (!isValidUsername(username)) {
            return "Le nom d'utilisateur doit contenir entre 3 et 50 caractères";
        }
        if (email == null || email.trim().isEmpty()) {
            return "L'email est requis";
        }
        if (!isValidEmail(email)) {
            return "L'email n'est pas valide";
        }
        if (password == null || password.isEmpty()) {
            return "Le mot de passe est requis";
        }
        if (!isValidPassword(password)) {
            return "Le mot de passe doit contenir au moins 6 caractères";
        }
        if (!password.equals(confirmPassword)) {
            return "Les mots de passe ne correspondent pas";
        }
        return null;
    }

    public static String validateLogin(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            return "Le nom d'utilisateur ou l'email est requis";
        }
        if (password == null || password.isEmpty()) {
            return "Le mot de passe est requis";
        }
        return null;
    }
}