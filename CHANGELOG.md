# Changelog

## [0.0.1] - 2025-08-15
### Added
- Bootstrap projet Gradle (Java 21), Spigot API 1.21.
- CI GitHub Actions (build + artefact).
- Squelette plugin: FaskinPlugin + Config/Messages.
- Docs initiales: ROADMAP, ARCHITECTURE, TICKETING.
### Note
- **Aucun gradle-wrapper** n'est committé. Le wrapper pourra être généré plus tard si nécessaire.

## [0.0.2] - 2025-08-15
### Added
- Registre de services (`AuthServiceRegistry`), états joueurs (`PlayerAuthState`).
- DAO in-memory (`InMemoryAccountRepository`) + hasher PBKDF2.
- Commandes: /register, /login, /logout, /changepassword, /faskin (reload|status).
### Changed
- Messages: suppression de MiniMessage, envoi en String (compat Spigot).
### Notes
- Persistance SQLite arrive TICKET-102.

## [0.0.3] - 2025-08-15
### Added
- Persistance **SQLite** (`SqliteAccountRepository`) avec création auto du schéma.
- Exécution **async** des commandes sensibles (DB hors main-thread).
- Intégration **Shadow** pour embarquer `sqlite-jdbc` dans le JAR.
### Changed
- Sélection du backend via `storage.driver` (SQLITE | SQLITE_INMEMORY | INMEMORY).

## [0.0.4] - 2025-08-15
### Fixed
- Échec `:shadowJar` sous Gradle 9 corrigé par migration vers **Shadow 9.0.2** (`com.gradleup.shadow`).
- CI : étape `Clean` dédiée avant `Build` pour un pipeline déterministe.
### Notes
- Gradle 9 requiert Java 17+, conforme à notre toolchain Java 21.
- Shadow 8.x présente des soucis avec Gradle 9 ; migration recommandée par les mainteneurs.
