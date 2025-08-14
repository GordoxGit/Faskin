package com.heneria.skinview.service;

import java.net.URI;
import java.util.Objects;

/** Descripteur minimal d’un skin : URL textures + modèle. */
public final class SkinDescriptor {
    private final URI skinUrl;
    private final SkinModel model;

    public SkinDescriptor(URI skinUrl, SkinModel model) {
        this.skinUrl = Objects.requireNonNull(skinUrl, "skinUrl");
        this.model = Objects.requireNonNull(model, "model");
    }

    public URI skinUrl() { return skinUrl; }
    public SkinModel model() { return model; }

    @Override public String toString() {
        return "SkinDescriptor{url=" + skinUrl + ", model=" + model + '}';
    }
}
