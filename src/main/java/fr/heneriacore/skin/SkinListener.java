package fr.heneriacore.skin;

import fr.heneriacore.event.AuthPostLoginEvent;
import fr.heneriacore.premium.event.PremiumLoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkinListener implements Listener {
    private final SkinService skinService;
    private final Map<UUID, SignedTexture> pending = new ConcurrentHashMap<>();

    public SkinListener(SkinService skinService) {
        this.skinService = skinService;
    }

    @EventHandler
    public void onPremiumLogin(PremiumLoginEvent event) {
        Map<String, String> props = event.getProfile().getProperties();
        String value = props.get("textures.value");
        if (value != null) {
            String sig = props.get("textures.signature");
            pending.put(event.getPlayer().getUniqueId(), new SignedTexture(value, sig, System.currentTimeMillis()));
        }
    }

    @EventHandler
    public void onAuthPostLogin(AuthPostLoginEvent event) {
        SignedTexture st = pending.remove(event.getUuid());
        if (st != null) {
            Player player = Bukkit.getPlayer(event.getUuid());
            if (player != null) {
                skinService.applySigned(player, st);
            }
        }
    }
}
