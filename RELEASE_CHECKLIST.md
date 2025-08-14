# Release Checklist

Before publishing a release ensure the following:

- [ ] CI is green on `main` or `develop`.
- [ ] `./gradlew test` passes locally.
- [ ] Manual smoke test on:
  - [ ] Spigot with ProtocolLib installed.
  - [ ] Paper without ProtocolLib (reflection fallback).
- [ ] Migration scripts and database schema version verified.
- [ ] Example `config.production.yml` reviewed and updated.
- [ ] `CHANGELOG.md` updated for the target version.
- [ ] `release-with-plib` workflow ran and produced `HeneriaCore-withPlib-<version>.jar` and checksum.
- [ ] Tag `v<version>` created and GitHub release drafted with artifact and SHA256.
- [ ] Announced to server admins and internal registries updated.
- [ ] Previous release artifact stored for rollback and any tokens/URLs used for ProtocolLib are rotated.
