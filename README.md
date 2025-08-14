# HeneriaCore

Initial skeleton for HeneriaCore plugin (Spigot/Paper 1.21). Version 0.0.3.

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
