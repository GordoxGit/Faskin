package com.heneria.skinview.store;

import com.heneria.skinview.service.SkinModel;

import java.net.URI;
import java.util.Objects;

/** Entrée persistée du cache de skin. */
public final class SkinRecord {
    private final URI skinUrl;
    private final SkinModel model;
    private final String texturesValueB64;   // peut être null si source=URL
    private final String texturesSignature;  // idem
    private final long savedAtEpochSec;

    public SkinRecord(URI skinUrl, SkinModel model, String texturesValueB64, String texturesSignature, long savedAtEpochSec) {
        this.skinUrl = Objects.requireNonNull(skinUrl, "skinUrl");
        this.model = Objects.requireNonNull(model, "model");
        this.texturesValueB64 = texturesValueB64;
        this.texturesSignature = texturesSignature;
        this.savedAtEpochSec = savedAtEpochSec;
    }

    public URI skinUrl() { return skinUrl; }
    public SkinModel model() { return model; }
    public String texturesValueB64() { return texturesValueB64; }
    public String texturesSignature() { return texturesSignature; }
    public long savedAtEpochSec() { return savedAtEpochSec; }

    public boolean hasSignedTextures() {
        return texturesValueB64 != null && texturesSignature != null;
    }
}

