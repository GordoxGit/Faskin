package fr.heneriacore.premium;

import java.time.Instant;
import java.util.Map;

public class GameProfile {
    private final String uuid;
    private final String name;
    private final Map<String, String> properties;
    private final Instant fetchedAt;

    public GameProfile(String uuid, String name, Map<String, String> properties, Instant fetchedAt) {
        this.uuid = uuid;
        this.name = name;
        this.properties = properties;
        this.fetchedAt = fetchedAt;
    }

    public String getUuid() { return uuid; }
    public String getName() { return name; }
    public Map<String, String> getProperties() { return properties; }
    public Instant getFetchedAt() { return fetchedAt; }
}
