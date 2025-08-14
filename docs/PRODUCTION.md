# Production Guide

## Server Selection

HeneriaCore supports Spigot and Paper for Minecraft 1.21. Paper is recommended for performance and for the reflection fallback when ProtocolLib is missing. Spigot works but may require ProtocolLib for advanced features.

1. Download either [Spigot](https://www.spigotmc.org/) or [Paper](https://papermc.io/).
2. Place `HeneriaCore.jar` in the server `plugins/` directory.
3. Provide ProtocolLib:
   - Use the regular artifact and install the ProtocolLib plugin manually.
   - Or use the `withPlib` build produced by the release workflow which bundles ProtocolLib and avoids the extra dependency.

## Prerequisites

- Java 21 runtime.
- Network access to Mojang APIs.
- Optional SQLite database backup strategy.

## Recommended Configuration

Copy the default `config.yml` and adjust for production:

- Tune `mojang.rateLimit` to stay under Mojang limits.
- Enable database backups and logging according to your policy.
- Review `preferences` defaults for opt‑in behaviour.

## Admin Commands

- `/heneria claim start <name>` – begin a claim flow.
- `/heneria claim check <id>` – verify claim.
- `/heneria optin` / `/heneria optout` – player preferences.
- `/heneria prefs` – show current preferences.
- `/heneria debug` – inspect internal state.

## FAQ

**Spoofing / premium detection**
: The plugin queries Mojang's session servers. Spoofed names are rejected before login completes.

**Opt‑in / opt‑out**
: Players control automatic skin and claim features with `/heneria optin` and `/heneria optout`. Admins can override via permissions.

**Claim flow**
: Claims prove account ownership. Start with `/heneria claim start <name>` which generates a tokenised skin. The player applies it and you verify with `/heneria claim check <id>`.

**Spigot vs Paper**
: Paper includes extra API and async improvements. When ProtocolLib is absent Paper uses a reflection fallback for skin changes. On Spigot you must provide ProtocolLib unless using the `withPlib` release.
