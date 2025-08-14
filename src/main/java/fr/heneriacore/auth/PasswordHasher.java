package fr.heneriacore.auth;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Arrays;

public class PasswordHasher {
    private final String algorithm;
    private final int iterations = 65536;
    private final int keyLength = 256;

    public PasswordHasher(String algorithm) {
        this.algorithm = algorithm.equalsIgnoreCase("argon2") ? "Argon2" : "PBKDF2WithHmacSHA256";
    }

    public String hash(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(this.algorithm);
            byte[] hash = skf.generateSecret(spec).getEncoded();
            Arrays.fill(password, '\0');
            return iterations + ":" + Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Hashing failure", e);
        }
    }

    public boolean verify(char[] password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            int iters = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] hash = Base64.getDecoder().decode(parts[2]);
            PBEKeySpec spec = new PBEKeySpec(password, salt, iters, hash.length * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(this.algorithm);
            byte[] test = skf.generateSecret(spec).getEncoded();
            Arrays.fill(password, '\0');
            return Arrays.equals(hash, test);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return false;
        }
    }

    public static byte[] generateSalt(int len) {
        byte[] salt = new byte[len];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
}
