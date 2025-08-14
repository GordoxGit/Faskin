package fr.heneriacore.skin;

import java.util.Objects;

public class SignedTexture {
    private final String value;
    private final String signature;
    private final long fetchedAt;

    public SignedTexture(String value, String signature, long fetchedAt) {
        this.value = value;
        this.signature = signature;
        this.fetchedAt = fetchedAt;
    }

    public String getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }

    public long getFetchedAt() {
        return fetchedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SignedTexture that = (SignedTexture) o;
        return Objects.equals(value, that.value) && Objects.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, signature);
    }
}
