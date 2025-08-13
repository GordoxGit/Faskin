# skinview (Heneria)

Plugin Paper/Spigot **1.21** (Java 21) — gestion future de skins pour serveurs offline/cracked.

## Politique dépôt (OBLIGATOIRE)
- **AUCUN fichier binaire** dans le repository (images, PDF, archives, JAR, etc.)  
- Le dépôt doit rester **100% TEXTE** (Java, YAML, MD, Gradle…)  
- Le build exécute `checkNoBinaries` et **échoue** si un binaire est détecté  
- Les artefacts (JAR…) sont générés dans `build/` et **ne doivent pas** être commit

## Build
```bash
./gradlew clean check   # vérifie l'absence de binaires
./gradlew build         # génère le JAR
```

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
