package fr.heneriacore.premium;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameToUuidResolver {
    private final HttpClient client = HttpClient.newHttpClient();
    private final long timeoutMs;

    public NameToUuidResolver(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public CompletableFuture<Optional<String>> nameToUuid(String name) {
        URI uri = URI.create("https://api.mojang.com/users/profiles/minecraft/" + name);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofMillis(timeoutMs))
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .handle((resp, ex) -> {
                    if (ex != null || resp.statusCode() != 200) return Optional.empty();
                    Matcher m = ID_PATTERN.matcher(resp.body());
                    if (m.find()) return Optional.of(m.group(1));
                    return Optional.empty();
                });
    }

    private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*\"([0-9a-fA-F]{32})\"");
}
