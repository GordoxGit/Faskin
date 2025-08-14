# skinview (Heneria)

![Version](https://img.shields.io/badge/version-0.5.3-blue)

Plugin Spigot **1.21** (Java 21) — gestion future de skins pour serveurs offline/cracked.

## Politique dépôt
- Dépôt **100% TEXTE** : la tâche `checkNoBinariesTracked` scanne uniquement les fichiers *versionnés* (via `git ls-files`).
- Un wrapper Gradle **local** (non commité) est autorisé. S’il est commité, le build **échoue**.
- Les artefacts (JAR…) sont générés dans `build/` et **ne doivent pas** être commit.
- La CI installe Gradle côté runner.

## Builds
Prérequis: **Java 21** et **Gradle 8.10+** installés localement.

- **CI (par défaut)** — sans ProtocolLib :
  ```bash
  gradle clean check
  gradle build
  ```

- **Build local (ProtocolLib via repo)** :
  ```bash
  gradle -PwithPlib=true clean build
  ```

- **Build local (ProtocolLib via JAR local non commité)** :
  1. placer `ProtocolLib.jar` dans `local-libs/` (ignoré par git)
  2. ```bash
     gradle -PwithPlib=true -PwithPlibLocal=local-libs/ProtocolLib.jar clean build
     ```

### Option: wrapper local (non versionné)
Si tu veux un wrapper sur ta machine, tu peux le générer puis **ne pas** le committer:
```bash
gradle wrapper --gradle-version 8.10.2
./gradlew clean check
./gradlew build
```
Important : ne pousse aucun des fichiers du wrapper (`gradle/`, `gradlew`, `gradlew.bat`, `gradle-wrapper.jar`). `.gitignore` les ignore et `checkNoBinariesTracked` échouera si un binaire arrive quand même.

## Installation
Copier build/libs/skinview-*.jar dans plugins/ puis démarrer Spigot 1.21.x.

### Serveur
Spigot 1.21 + offline-mode : pour l’apply live des skins, installe ProtocolLib côté serveur.
L’applier ProtocolLib réécrit le `GameProfile` (textures `value` + `signature`) envoyé aux autres joueurs.
Le JAR buildé avec `-PwithPlib=true` active automatiquement le chemin ProtocolLib (softdepend), sinon fallback.

## Plateforme
- **Spigot 1.21** (pas Paper). API utilisée : `PlayerProfile` / `PlayerTextures` (aucun NMS).

### Compatibilité API (Spigot vs Paper)
- Spigot 1.21 fournit `PlayerProfile` + `PlayerTextures#setSkin(URL)` pour manipuler des **profils**, pas le profil du **joueur en ligne**.
- L’application live du skin sur le joueur en ligne via `Player#setPlayerProfile(...)` est **spécifique à Paper**.
- Pour Spigot pur, l’apply live nécessite **ProtocolLib** (`apply.protocollib-enable: true` et plugin présent). Sans ProtocolLib → log info, aucun apply live.
- Plugin: tente l’apply via **réflexion** (Paper) ou via **ProtocolLib**.
  Réfs: Javadocs Spigot (PlayerTextures), Paper (Player#setPlayerProfile) et ProtocolLib.

## Comportement (auto-apply premium au join)
- Si `apply.update-on-join: true` et que le pseudo du joueur existe chez Mojang,
  le skin est résolu **async** et appliqué **main-thread**.
- À chaque apply réussi, l'entrée est **persistée** dans `storage.file` (YAML) avec TTL.
- À la reconnexion, si l'entrée du store est valide, le skin est appliqué immédiatement sans recontacter Mojang.
- Spigot: nécessite ProtocolLib (voir ci-dessus) pour que le skin soit visible par les autres joueurs.
- Option `apply.refresh-tablist: true` pour un léger hide/show (meilleure propagation client).

## Config (extrait pertinent)
```yaml
apply:
  update-on-join: true
  refresh-tablist: true
  protocollib-enable: true
cache:
  ttl-seconds: 3600
  max-entries: 500
lookups:
  allow-premium-name: true
  allow-textures-url: true
  allow-unsigned: true
  url:
    resolve-signature: true
    signature-ttl-seconds: 86400
storage:
  type: YAML
  file: "data/skins.yml"
  ttl-seconds: 86400
```

## Résolution de skins
Service interne `SkinResolver` : résolution Mojang (pseudo premium) ou URL `textures.minecraft.net`.
I/O **asynchrones** via `HttpClient` Java 21, cache TTL mémoire + **persistance YAML**.
`/skinview url` tente de récupérer la propriété `value+signature` signée via Mojang (best-effort) si `lookups.url.resolve-signature` est activé.
Commande d'admin pour tester :

```
/skinview resolve name <Premium>
/skinview resolve url <textures.minecraft.net/...>
```

Tout est async, aucun blocage du tick.

## Commandes
- `/skinview help` — aide
- `/skinview reload` — recharge config/messages (perm skinview.admin)
- `/skinview resolve name <Premium>` — test résolution Mojang (perm skinview.admin)
- `/skinview resolve url <textures.minecraft.net/...>` — test résolution par URL (perm skinview.admin)
- `/skinview use <Premium> [joueur]` — applique un skin premium et le persiste
- `/skinview url <textures.minecraft.net/...> [joueur]` — applique via URL
- `/skinview clear [joueur]` — supprime l'entrée de cache
- `/skinview cache get [joueur]` — applique depuis le store
- `/skinview cache clear [joueur]` — efface l'entrée du store
- `/skinview optout [joueur]` — désactive l'apply auto
- `/skinview optin [joueur]` — réactive l'apply auto
- `/skinview debug` — affiche les infos de debug
- Aliases: `/skin`, `/sv`.

## Permissions
- `skinview.use` (par défaut: true)
- `skinview.admin` (par défaut: op)

## Configs
`config.yml` et `messages.yml` générés à la première exécution.

## Roadmap
Tickets suivants : application via PlayerProfile, persistance, auto-apply au join, fallback TAB.
### Notes techniques
- 0.4.2: correction des chaînes regex dans `JsonUtils` (échappement Java corrigé).

## Mises à jour
À chaque ticket, mettre à jour :
- `build.gradle.kts` (dépendances/versions)
- `README.md` (sections concernées)
- `CHANGELOG.md` (entrée détaillée)

## Changelog

- **0.5.3**
  - `/skinview url` tente de récupérer `value+signature` signée via Mojang (UUID → sessionserver).
  - Nouveaux réglages `lookups.url.resolve-signature` et `signature-ttl-seconds`.
  - Fallback clair si signature introuvable.
- **0.5.2**
  - Ajout : rate-limiting, backoff, circuit-breaker et métriques pour Mojang.
- **0.5.1**
  - Ajout opt-in / opt-out par joueur pour l’application auto des skins.
  - Ajout commande `/skinview debug` (état applier, cache, opt-outs, hits).
