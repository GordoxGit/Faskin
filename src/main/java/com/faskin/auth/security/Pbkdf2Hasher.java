package com.faskin.auth.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;

public final class Pbkdf2Hasher {
    private static final int ITERATIONS = 210_000;
    private static final int SALT_LEN = 16;
    private static final int KEY_LEN_BITS = 256; // 32 bytes

    private final SecureRandom rng = new SecureRandom();

    public byte[] newSalt() {
        byte[] s = new byte[SALT_LEN];
        rng.nextBytes(s);
        return s;
    }

    public byte[] hash(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LEN_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("PBKDF2 failure", e);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    public boolean verify(char[] password, byte[] salt, byte[] expected) {
        byte[] h = hash(password, salt);
        return constantTimeEquals(h, expected);
    }

    private boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        int r = 0;
        for (int i = 0; i < a.length; i++) r |= a[i] ^ b[i];
        return r == 0;
    }
}
