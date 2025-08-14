package fr.heneriacore.skin;

import java.util.Objects;

public final class SkinUtils {
    private SkinUtils() {}

    public static boolean same(SignedTexture a, SignedTexture b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return Objects.equals(a.getValue(), b.getValue()) && Objects.equals(a.getSignature(), b.getSignature());
    }
}
