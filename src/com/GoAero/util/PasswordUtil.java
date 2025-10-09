package com.GoAero.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification
 * Uses SHA-256 with salt for secure password storage
 */
public class PasswordUtil {
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    private static final String DELIMITER = ":";

    /**
     * Generates a random salt
     * @return Base64 encoded salt
     */
    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a password with a salt
     * @param password The plain text password
     * @param salt The salt to use
     * @return The hashed password
     */
    private static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Hashes a password with a randomly generated salt
     * @param password The plain text password
     * @return The salt and hashed password combined (salt:hash)
     */
    public static String hashPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);
        return salt + DELIMITER + hashedPassword;
    }

    /**
     * Verifies a password against a stored hash
     * @param password The plain text password to verify
     * @param storedHash The stored hash (salt:hash format)
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }

        try {
            String[] parts = storedHash.split(DELIMITER, 2);
            if (parts.length != 2) {
                return false;
            }

            String salt = parts[0];
            String hash = parts[1];
            String hashedPassword = hashPassword(password, salt);
            
            return hash.equals(hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates password strength
     * @param password The password to validate
     * @return true if password meets minimum requirements
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        // Check for at least one letter and one number
        boolean hasLetter = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        
        return hasLetter && hasDigit;
    }

    /**
     * Gets password requirements message
     * @return String describing password requirements
     */
    public static String getPasswordRequirements() {
        return "Password must be at least 6 characters long and contain at least one letter and one number.";
    }

    /**
     * Generates a random password
     * @param length The length of the password to generate
     * @return A randomly generated password
     */
    public static String generateRandomPassword(int length) {
        if (length < 6) {
            length = 6;
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one letter and one number
        password.append(chars.charAt(random.nextInt(26))); // Uppercase letter
        password.append(chars.charAt(random.nextInt(26) + 26)); // Lowercase letter
        password.append(chars.charAt(random.nextInt(10) + 52)); // Number
        
        // Fill the rest randomly
        for (int i = 3; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        // Shuffle the password
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }
}
