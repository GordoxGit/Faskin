package com.heneria.skinview.service;

import java.util.concurrent.CompletableFuture;

/** API de résolution de skins. Toutes les opérations sont **async**. */
public interface SkinResolver {
    CompletableFuture<SkinDescriptor> resolveByPremiumName(String name);
    CompletableFuture<SkinDescriptor> resolveByTexturesUrl(String url);
    void reloadSettings();
    void shutdown();
}
