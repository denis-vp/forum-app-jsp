package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final int SALT_SIZE = 16;

    public static String generateSalt() {
        byte[] salt = new byte[SALT_SIZE];
        RANDOM.nextBytes(salt);
        return ENCODER.encodeToString(salt);
    }

    public static String hashPassword(String password, String salt) {
        String saltedPassword = salt + password;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedPassword = md.digest(saltedPassword.getBytes());
            return ENCODER.encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available.", e);
        }
    }
}
