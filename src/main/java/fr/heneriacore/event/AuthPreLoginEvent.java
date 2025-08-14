package fr.heneriacore.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class AuthPreLoginEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID uuid;
    private final String username;

    public AuthPreLoginEvent(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    public UUID getUuid() { return uuid; }
    public String getUsername() { return username; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
