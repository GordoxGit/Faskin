package fr.heneriacore.skin;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public interface SkinApplier {
    void apply(Plugin plugin, Player target, SignedTexture texture, boolean refreshTablist) throws Exception;
    default void shutdown() {}
}
