# Changelog

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
