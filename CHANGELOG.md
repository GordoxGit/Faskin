# Changelog

## [0.1.0] - 2025-08-16
### Added
- Migration DB `002_step2_premium.sql` pour champs premium.
- Services squelettes (PremiumDetector, AuthBypassService) et listeners.
- Config/messages: blocs `premium.*`.
- CI: artefact `Faskin-0.1.0-SNAPSHOT.jar`.
### Changed
- Bump version plugin à `0.1.0`.

## [0.0.10] - 2025-08-16
### Added
- Pipeline **release** taggée (`.github/workflows/release.yml`): build clean + shadowJar, SHA256, publication Release GitHub avec artefacts (softprops/action-gh-release).
### Changed
- Bump version plugin à `0.0.10`.
- README: section "Publier une release".
- RELEASE_CHECKLIST mis à jour.
- ROADMAP: Étape 1 marquée 100% (done), backlog Étape 2 enrichi.

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

## [0.0.5] - 2025-08-15
### Added
- Auto-login par **session IP** (TTL) côté `PlayerJoinEvent`.
- Mise à jour `last_ip` / `last_login` sur login réussi.
- Listener `JoinQuitListener` + purge état sur quit.
### Changed
- `AccountRepository` : API session (`getSessionMeta`, `updateLastLoginAndIp`).
- Implémentations RAM/SQLite mises à jour.

## [0.0.6] - 2025-08-15
### Added
- Listener **PreAuthGuardListener**: blocage mouvement, chat, commandes (whitelist), interactions,
  inventaire, drop/pickup, swap main/offhand, dégâts (vers/de), faim.
- Config `preauth.*` avec toggles fins et whitelist de commandes.
### Notes
- Respect strict des règles thread Bukkit: pas d'appel API depuis des threads async.

## [0.0.7] - 2025-08-15
### Added
- i18n (fallback par locale), **prefix** et **couleurs &** pour les messages.
- Rappels périodiques d’auth (ActionBar/Chat) tant que non AUTHENTICATED.
- **Rate-limit** des messages de blocage (chat/command).
- `/faskin help` + alias `/l` et `/reg`.
### Changed
- Unifie l’envoi de messages via `messages.prefixed(key)`.

## [0.0.8] - 2025-08-15
### Added
- Timeout d’authentification: kick/message après `login.timeout_seconds`.
- Anti-bruteforce: cooldown tentatives, compteur d’échecs, verrou `locked_until`.
- Repository API: `isLocked`, `registerFailedAttempt`, `resetFailures`, `lockRemainingSeconds`.
### Changed
- `LoginCommand`: vérifie lock/cooldown, reset les échecs sur succès, maj session.
- `JoinQuitListener`: planifie/cancel le timeout selon l’état.

## [0.0.9] - 2025-08-15
### Added
- Commande admin: `/faskin status [player]`, `/faskin unlock <player>`, `/faskin stats`.
- API repository: `countAccounts`, `countLockedActive`, `adminInfo`.
- `AuthServiceRegistry#countStates()` pour compter les états online.
### Changed
- Messages i18n: clés admin_* pour rendu formaté.
