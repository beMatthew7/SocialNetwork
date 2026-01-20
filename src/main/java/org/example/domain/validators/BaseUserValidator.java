package org.example.domain.validators;

import org.example.domain.User;

public abstract class BaseUserValidator {

    /**
     * Valideaza atributele de baza (din clasa User).
     * NU arunca exceptie, ci returneaza un string cu erorile,
     * pentru a permite validatorilor specifici sa adauge propriile erori.
     */
    protected String getBaseUserErrors(User user) {
        String errors = "";

        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            errors += "Numele de utilizator (username) nu poate fi gol!\n";
        } else if (user.getUsername().length() < 3) {
            errors += "Numele de utilizator (username) trebuie sa aiba minim 3 caractere!\n";
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            errors += "Email-ul nu poate fi gol!\n";
        } else if (!user.getEmail().contains("@") || !user.getEmail().contains(".")) {
            errors += "Email-ul are un format invalid!\n";
        }

        return errors;
    }
}