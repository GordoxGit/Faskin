package com.heneria.skinview.service;

import org.bukkit.entity.Player;

/** Service d'application live d'un skin. */
public interface SkinApplier {

    void apply(Player player, SkinDescriptor descriptor);

    void clear(Player player);

    default void shutdown() {}
}

