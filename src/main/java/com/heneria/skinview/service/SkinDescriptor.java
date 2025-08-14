package com.heneria.skinview.service;

import java.net.URI;
import java.util.Objects;

/** Descripteur complet d’un skin Mojang. */
public final class SkinDescriptor {
    private final URI skinUrl;
    private final SkinModel model;
    private final String texturesValueB64;   // propriété "textures".value (Base64)
    private final String texturesSignature;  // propriété "textures".signature (Mojang)

    public SkinDescriptor(URI skinUrl, SkinModel model, String texturesValueB64, String texturesSignature) {
        this.skinUrl = Objects.requireNonNull(skinUrl, "skinUrl");
        this.model = Objects.requireNonNull(model, "model");
        this.texturesValueB64 = texturesValueB64;     // peut être null s’il vient d’une URL directe
        this.texturesSignature = texturesSignature;   // idem
    }

    public URI skinUrl() { return skinUrl; }
    public SkinModel model() { return model; }
    public String texturesValueB64() { return texturesValueB64; }
    public String texturesSignature() { return texturesSignature; }
    public boolean hasSignedTextures() { return texturesValueB64 != null && texturesSignature != null; }

    @Override public String toString() {
        return "SkinDescriptor{url=" + skinUrl + ", model=" + model +
                ", signed=" + hasSignedTextures() + '}';
    }
}
