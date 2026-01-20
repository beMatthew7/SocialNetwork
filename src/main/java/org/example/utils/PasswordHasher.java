package org.example.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHasher {

    private static final int SALT_LENGTH = 16;

    /**
     * Hashes a password with a randomly generated salt.
     * Format: salt:hash
     */
    public static String hash(String password) {
        byte[] salt = generateSalt();
        String hash = hashWithSalt(password, salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + hash;
    }

    /**
     * Verifies a password against a stored hash (salt:hash).
     */
    public static boolean check(String password, String stored) {
        if (stored == null || !stored.contains(":")) {
            // Fallback for legacy plain-text passwords
            return password.equals(stored);
        }

        String[] parts = stored.split(":");
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        String hash = parts[1];

        String computedHash = hashWithSalt(password, salt);
        return computedHash.equals(hash);
    }

    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    private static String hashWithSalt(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
