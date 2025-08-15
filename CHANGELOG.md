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
