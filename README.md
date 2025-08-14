# skinview (Heneria)

Plugin Spigot **1.21** (Java 21) — gestion future de skins pour serveurs offline/cracked.

## Politique dépôt
- Dépôt **100% TEXTE** : la tâche `checkNoBinariesTracked` scanne uniquement les fichiers *versionnés* (via `git ls-files`).
- Un wrapper Gradle **local** (non commité) est autorisé. S’il est commité, le build **échoue**.
- Les artefacts (JAR…) sont générés dans `build/` et **ne doivent pas** être commit.
- La CI installe Gradle côté runner.

## Build (Gradle **sans wrapper** par défaut)
Prérequis: **Java 21** et **Gradle 8.10+** installés localement.
```bash
gradle clean check
gradle build
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

## Plateforme
- **Spigot 1.21** (pas Paper). API utilisée : `PlayerProfile` / `PlayerTextures` (aucun NMS).

### Compatibilité API (Spigot vs Paper)
- Spigot 1.21 fournit `PlayerProfile` + `PlayerTextures#setSkin(URL)` pour manipuler des **profils**, pas le profil du **joueur en ligne**.
- L’application live du skin sur le joueur en ligne via `Player#setPlayerProfile(...)` est **spécifique à Paper**.
- Plugin: tente l’apply via **réflexion**. Sur Spigot pur, l’API **ne permet pas** l’apply live → voir Ticket suivant pour un chemin **ProtocolLib/NMS** si nécessaire.
Réfs: Javadocs Spigot (PlayerTextures) et Paper (Player#setPlayerProfile).

## Comportement (auto-apply premium au join)
- Si `apply.update-on-join: true` et que le pseudo du joueur existe chez Mojang,
  le skin est résolu **async** et appliqué **main-thread** via `PlayerProfile`.
- Option `apply.refresh-tablist: true` pour un léger hide/show (meilleure propagation client).

## Config (extrait pertinent)
```yaml
apply:
  update-on-join: true
  refresh-tablist: true
lookups:
  allow-premium-name: true
```

## Résolution de skins
Service interne `SkinResolver` : résolution Mojang (pseudo premium) ou URL `textures.minecraft.net`.
I/O **asynchrones** via `HttpClient` Java 21, cache TTL en mémoire avec purge périodique.
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
- Aliases: `/skin`, `/sv`.

## Permissions
- `skinview.use` (par défaut: true)
- `skinview.admin` (par défaut: op)

## Configs
`config.yml` et `messages.yml` générés à la première exécution.

## Roadmap
Tickets suivants : application via PlayerProfile, persistance, auto-apply au join, fallback TAB.
## Mises à jour
À chaque ticket, mettre à jour :
- `build.gradle.kts` (dépendances/versions)
- `README.md` (sections concernées)
- `CHANGELOG.md` (entrée détaillée)
