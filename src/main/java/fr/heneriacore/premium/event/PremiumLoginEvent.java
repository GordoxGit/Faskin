package fr.heneriacore.premium.event;

import fr.heneriacore.premium.GameProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PremiumLoginEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final GameProfile profile;

    public PremiumLoginEvent(Player player, GameProfile profile) {
        this.player = player;
        this.profile = profile;
    }

    public Player getPlayer() { return player; }
    public GameProfile getProfile() { return profile; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
