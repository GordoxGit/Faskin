# HeneriaCore

Initial skeleton for HeneriaCore plugin (Spigot/Paper 1.21). Version 0.0.6.

Important:
- Do NOT commit gradle-wrapper.jar (gradle/wrapper/gradle-wrapper.jar).
- CI installs Gradle using gradle/setup-gradle.
- Development: install Gradle 8.x locally or run `gradle wrapper` locally (do NOT commit generated jar).

### Testing Mojang API locally

For premium detection you may want to mock Mojang endpoints. A simple way is to run a tiny HTTP server locally and adjust hosts or URLs during tests. Example using Python:

```bash
python -m http.server 8080
```

Serve JSON responses matching the Mojang API formats for `/users/profiles/minecraft/{name}` and `/session/minecraft/profile/{uuid}` to simulate premium accounts.

### Skins

HC-04 introduces a basic `SkinService` able to apply signed textures either via ProtocolLib or Paper reflection as a fallback.

### HC-05 network layer

Adds an asynchronous `HttpClientWrapper` with token bucket rate limiting, exponential backoff, circuit breaker and metrics collection. Configuration lives under `mojang` in `config.yml`.

### Claim flow

HC-06 introduces a simple claim system to prove ownership of a Mojang account. Use `/heneria claim start <name>` to generate a tokenized skin image and `/heneria claim check <id>` to verify.
