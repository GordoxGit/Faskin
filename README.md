# skinview (Heneria)

Plugin Paper/Spigot **1.21** (Java 21) — gestion future de skins pour serveurs offline/cracked.

## Politique dépôt (OBLIGATOIRE)
- **AUCUN fichier binaire** dans le repository (images, PDF, archives, JAR, etc.)
- Le dépôt doit rester **100% TEXTE** (Java, YAML, MD, Gradle…)
- Le build exécute `checkNoBinaries` et **échoue** si un binaire est détecté
- Les artefacts (JAR…) sont générés dans `build/` et **ne doivent pas** être commit
- **Pas de wrapper Gradle** dans Git (`gradle/`, `gradlew*`). Si un wrapper est nécessaire sur ta machine, génère-le localement et ne le pousse pas.
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
Important : ne pousse aucun des fichiers du wrapper (`gradle/`, `gradlew`, `gradlew.bat`, `gradle-wrapper.jar`). `.gitignore` les ignore et `checkNoBinaries` échouera si un binaire arrive quand même.

## Installation
Copier build/libs/skinview-*.jar dans plugins/ puis démarrer Paper/Spigot 1.21.x.

## Commandes
- `/skinview help` — aide
- `/skinview reload` — recharge config/messages (perm skinview.admin)
- Aliases: `/skin`, `/sv`.

## Permissions
- `skinview.use` (par défaut: true)
- `skinview.admin` (par défaut: op)

## Configs
`config.yml` et `messages.yml` générés à la première exécution.

## Roadmap
Tickets suivants : résolution Mojang (async + cache), application via PlayerProfile, persistance, auto-apply au join, fallback TAB.
## Mises à jour
À chaque ticket, mettre à jour :
- `build.gradle.kts` (dépendances/versions)
- `README.md` (sections concernées)
- `CHANGELOG.md` (entrée détaillée)
