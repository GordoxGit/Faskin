package fr.heneriacore.premium;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SessionProfileResolver {
    private final HttpClient client = HttpClient.newHttpClient();
    private final long timeoutMs;
    private final boolean fetchSignature;

    public SessionProfileResolver(long timeoutMs, boolean fetchSignature) {
        this.timeoutMs = timeoutMs;
        this.fetchSignature = fetchSignature;
    }

    public CompletableFuture<Optional<GameProfile>> fetchProfileForUuid(String uuid) {
        String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + (fetchSignature ? "?unsigned=false" : "?unsigned=true");
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMs))
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .handle((resp, ex) -> {
                    if (ex != null || resp.statusCode() != 200) return Optional.empty();
                    String body = resp.body();
                    Matcher nameMatcher = NAME_PATTERN.matcher(body);
                    if (!nameMatcher.find()) return Optional.empty();
                    String name = nameMatcher.group(1);
                    Map<String, String> props = new HashMap<>();
                    Matcher propMatcher = PROPERTY_PATTERN.matcher(body);
                    while (propMatcher.find()) {
                        String propName = propMatcher.group(1);
                        String value = propMatcher.group(2);
                        props.put(propName + ".value", value);
                        String sig = propMatcher.group(3);
                        if (sig != null) props.put(propName + ".signature", sig);
                    }
                    return Optional.of(new GameProfile(uuid, name, props, Instant.now()));
                });
    }

    private static final Pattern NAME_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\{\"name\":\"([^\"]+)\",\"value\":\"([^\"]+)\"(?:,\"signature\":\"([^\"]+)\")?\\}");
}
