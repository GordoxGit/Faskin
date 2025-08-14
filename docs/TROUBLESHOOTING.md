# Troubleshooting

Common issues and their fixes.

## ProtocolLib mismatch
If login packets fail or skins do not apply, ensure the ProtocolLib version matches the server. Use the `withPlib` build or install a compatible ProtocolLib release.

## Packet flicker
Rapid skin changes may cause packet flicker. Update ProtocolLib and avoid other plugins modifying the same packets.

## Tablist issues
Incorrect tablist names or skins usually stem from outdated client information. Clear tablist caches or restart the server.

## Mojang rate limits
When the Mojang API returns rate limit errors, increase `mojang.rateLimit` delay or provide a cache. The plugin will back off automatically but repeated failures may block authentication.
