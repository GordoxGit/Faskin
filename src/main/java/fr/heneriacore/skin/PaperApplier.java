package fr.heneriacore.skin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class PaperApplier implements SkinApplier {
    @Override
    public void apply(Plugin plugin, Player target, SignedTexture texture, boolean refreshTablist) throws Exception {
        Class<?> gpClass = Class.forName("com.mojang.authlib.GameProfile");
        Object profile = gpClass.getConstructor(java.util.UUID.class, String.class)
                .newInstance(target.getUniqueId(), target.getName());
        Object props = gpClass.getMethod("getProperties").invoke(profile);
        Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
        Object tex = propertyClass.getConstructor(String.class, String.class, String.class)
                .newInstance("textures", texture.getValue(), texture.getSignature());
        props.getClass().getMethod("put", Object.class, Object.class).invoke(props, "textures", tex);
        Method m = target.getClass().getMethod("setPlayerProfile", gpClass);
        m.invoke(target, profile);
        if (refreshTablist) {
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                viewer.hidePlayer(plugin, target);
                viewer.showPlayer(plugin, target);
            }
        }
    }
}
