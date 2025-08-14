package com.heneria.skinview.service;

import java.net.URI;
import java.util.Objects;

/** Descripteur complet d’un skin : URL textures + modèle + propriété signée. */
public final class SkinDescriptor {
    private final URI skinUrl;
    private final SkinModel model;
    private final String texturesValueB64;
    private final String texturesSignature;

    public SkinDescriptor(URI skinUrl, SkinModel model) {
        this(skinUrl, model, null, null);
    }

    public SkinDescriptor(URI skinUrl, SkinModel model, String texturesValueB64, String texturesSignature) {
        this.skinUrl = Objects.requireNonNull(skinUrl, "skinUrl");
        this.model = Objects.requireNonNull(model, "model");
        this.texturesValueB64 = texturesValueB64;
        this.texturesSignature = texturesSignature;
    }

    public URI skinUrl() { return skinUrl; }
    public SkinModel model() { return model; }
    public String texturesValueBase64() { return texturesValueB64; }
    public String texturesSignature() { return texturesSignature; }

    @Override public String toString() {
        return "SkinDescriptor{url=" + skinUrl + ", model=" + model + '}';
    }
}
