package com.heneria.skinview.service;

/** Modèle de skin : STEVE (classique) ou ALEX (slim). */
public enum SkinModel {
    STEVE, ALEX;

    public static SkinModel fromMetadata(String meta) {
        if (meta == null) return STEVE;
        return "slim".equalsIgnoreCase(meta) ? ALEX : STEVE;
    }
}
