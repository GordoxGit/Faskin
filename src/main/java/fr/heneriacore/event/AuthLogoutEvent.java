package fr.heneriacore.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class AuthLogoutEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID uuid;
    private final String token;

    public AuthLogoutEvent(UUID uuid, String token) {
        this.uuid = uuid;
        this.token = token;
    }

    public UUID getUuid() { return uuid; }
    public String getToken() { return token; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
