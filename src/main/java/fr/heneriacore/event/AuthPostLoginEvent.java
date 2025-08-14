package fr.heneriacore.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class AuthPostLoginEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID uuid;
    private final String username;
    private final String token;

    public AuthPostLoginEvent(UUID uuid, String username, String token) {
        this.uuid = uuid;
        this.username = username;
        this.token = token;
    }

    public UUID getUuid() { return uuid; }
    public String getUsername() { return username; }
    public String getToken() { return token; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
