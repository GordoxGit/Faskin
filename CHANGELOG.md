# Changelog

## 0.4.1
- Build: option **`-PwithPlib`** pour compiler/inclure les classes ProtocolLib uniquement à la demande.
- CI: passe sans tenter de résoudre ProtocolLib (aucune dépendance par défaut).
- Doc: ajout du mode **JAR local** (`-PwithPlibLocal`), dossier `local-libs/` ignoré par git.

## 0.4.0
- build: dépendance optionnelle ProtocolLib (repo dmulloy2) + bump version.
- feature: resolver remonte désormais `value` base64 et `signature` signée.
- feature: `SkinApplierProtocolLib` pour apply live des skins premium sur Spigot offline (PLAYER_INFO_UPDATE).
- config: nouveau toggle `apply.protocollib-enable` (par défaut true).
- doc: README mis à jour (ProtocolLib requis sur Spigot, limites & toggles).

## 0.3.2
- build: suppression des annotations JetBrains dans `SkinCommand` (compilation CI OK sans dépendance).
- compat: remplacement de l’appel direct à `Player#setPlayerProfile(...)` par **réflexion** (compile-safe Spigot).
- doc: README mis à jour (limites Spigot vs Paper pour l’apply live).

## 0.3.1
- Spigot 1.21: auto-apply du skin des comptes premium au join (serveur offline)
- Impl: Listener `SkinAutoApplyJoinListener` (async resolve → main-thread apply via PlayerProfile/PlayerTextures)
- Aucun NMS, tick-safe. Logs FINE/INFO.
- build.gradle.kts: dépendance `org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT`

How-to (build & test rapide)

Build (Gradle système, Java 21)

```
gradle clean check
gradle build
```

Déposer build/libs/skinview-0.3.1.jar dans plugins/ (serveur Spigot 1.21 en offline-mode).

Tests manuels

1. démarrer le serveur.
2. Rejoindre avec un pseudo premium réel (existant sur Mojang).
3. Vérifier côté autres joueurs que le skin apparaît (log plugin Applied premium skin ...).
4. Tester toggles : apply.update-on-join=false → aucun changement de skin au join.

Perf/stabilité

- Aucune I/O réseau sur le main thread (résolution via service async existant).
- Application strictement main-thread (Spigot-safe).
- Pas d’allocation massive / pas de scheduler serré (promesses complétées puis 2×2 ticks pour refresh).

## 0.2.1
- build: `checkNoBinariesTracked` remplace `checkNoBinaries` — scan basé sur `git ls-files` (uniquement fichiers versionnés).
- doc: README mis à jour (wrapper local OK si non commité).

## 0.2.0
- Service de résolution de skins Mojang/URL async via `HttpClient`
- Cache TTL mémoire avec purge périodique
- Commande admin `/skinview resolve` pour tester les résolutions
- Bump Gradle version du plugin à 0.2.0

## 0.1.4
- Policy: Gradle conservé mais **Codex ne génère ni ne commit le wrapper**.
- CI: Gradle 8.10.2 installé côté runner (pas de wrapper requis).
- Anti-binaires: check strict sans whitelist (échoue si wrapper ou autre binaire poussé).
- .gitignore: ignore `gradle/`, `gradlew*`.

## 0.1.3
- Gradle sans wrapper + ban total des binaires (CI + check local)

## 0.1.0
- Initialisation du projet skinview (Heneria)
- Gradle Kotlin DSL (Java 21), Paper API 1.21.x
- Politique ANTI-BINAIRES: .gitignore, .gitattributes, tâche Gradle `checkNoBinaries`
- `plugin.yml`, `config.yml`, `messages.yml`
- Commande `/skinview` (help/reload) + tab-complete
- Listeners (join/interact), logs de boot, fail-fast si commande absente
How-to (build, config, commandes, permissions)
Build (Java 21)

```bash
./gradlew clean check
./gradlew build
```

Commandes

/skinview help (toujours return true, pas d’écho)

/skinview reload (perm skinview.admin)

Permissions

skinview.use, skinview.admin

Validation (tests manuels)
./gradlew clean check : doit passer sans lister de binaires.

Démarrage serveur : plugin Enabled, aucun WARN/stacktrace.

/skinview ou /skinview help : affiche l’aide (pas d’“usage”).

/skinview reload : recharge config/messages.

Tab-complete premier arg : help, reload si admin.
