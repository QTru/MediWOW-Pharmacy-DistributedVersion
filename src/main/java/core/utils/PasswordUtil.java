package core.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.security.SecureRandom;

/**
 * @author Tô Thanh Hậu
 * Utility class for password hashing and verification using BCrypt.
 */
public class PasswordUtil {

    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int BCRYPT_LOG_ROUNDS = 12;

    // Utility class — no instantiation
    private PasswordUtil() {}

    // ─── Password Generation ───────────────────────────────────────────────────

    public static String generatePassword() {
        return generateTemporaryPassword();
    }

    public static String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            password.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }

    // ─── BCrypt Hashing ────────────────────────────────────────────────────────

    public static String hashPassword(String password) {
        if (password == null) throw new IllegalArgumentException("password cannot be null");
        return BCrypt.withDefaults().hashToString(BCRYPT_LOG_ROUNDS, password.toCharArray());
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) return false;
        try {
            BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
            return result.verified;
        } catch (Exception e) {
            return false;
        }
    }

    // Testing the utility
    public static void main(String[] args) {
        PasswordUtil util = new PasswordUtil();
        String tempPassword = "firstPassword";
        String hashed = util.hashPassword(tempPassword);
        System.out.println("Plain: " + tempPassword);
        System.out.println("Hashed: " + hashed);
        System.out.println("Verify correct: " + util.verifyPassword(tempPassword, "$2a$12$rv..T5tR/8RjuI1HUpZNfu.ilHlyZoLJJzWZvumJ7kd/WAdC75R.."));
    }
}
