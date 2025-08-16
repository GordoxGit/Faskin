# Faskin

Plugin unifié Spigot 1.21 / Java 21 :
1) Auth offline (Étape 1), 2) Auto-login premium (Étape 2), 3) Skins premium en offline (Étape 3).

## Version
`0.1.0-SNAPSHOT` — Étape 2 amorcée : ossature auto-login premium.

## Admin
- `/faskin status [player]` : état runtime + méta compte (IP, lastLogin, compteur d’échecs, lock).
- `/faskin unlock <player>` : reset failed_count + locked_until.
- `/faskin stats` : comptes totaux, locks actifs, online AUTH / non-AUTH.

## i18n & couleurs
- `messages_locale` ou `messages.locale` pour choisir la langue (`messages.yml` / `messages_<locale>.yml`).
- Codes couleur `&` convertis via l’API Spigot.

## Anti-bruteforce
- Cooldown local par joueur (`min_seconds_between_attempts`).
- Compteur d’échecs + lock (`max_failed_attempts`, `lock_minutes`) persistés.
- Kick si non authentifié après `timeout_seconds`.

## Rappels d’auth
- Tâche périodique (ActionBar + chat) configurable : `reminder.*`.

## Pré-auth (blocages)
- `preauth.block.*` + liste blanche `preauth.commands.whitelist` (inclut toujours `register` / `login`).

## Dépendances shaded
- `org.xerial:sqlite-jdbc:3.50.3.0` (inclus via Shadow).

## Sessions par IP
- Voir `login.allow_ip_session` et `session_minutes`.

## Mode PROXY_SAFE recommandé
Faskin privilégie un proxy en **online-mode** avec [player information forwarding](https://docs.papermc.io/velocity/player-information-forwarding/) activé. Cela permet de transmettre UUID, IP et propriétés signées pour un auto-login premium sécurisé. Sans forwarding, aucun bypass n'est effectué. Réfs : [FastLogin](https://www.spigotmc.org/resources/fastlogin.14153/), [Velocity](https://docs.papermc.io/velocity/player-information-forwarding/).

## Pré-requis Étape 2 (PROXY_SAFE)
Pour prouver qu'un joueur est premium, le proxy doit transférer son identité complète au backend.

```toml
# velocity.toml
player-info-forwarding-mode = "modern"
forwarding-secret = "<secret>"
```

Le même secret doit être défini côté backend (`velocity.toml` de Paper/Waterfall). Sans forwarding **IP/UUID/properties**, Faskin ne réalise aucune détection premium.

L'API Paper 1.21 expose `Player#getPlayerProfile()` et `PlayerProfile#getTextures()` afin de récupérer les textures signées (skin). Toute modification manuelle invalide ces attributs signés.

## Build local (sans wrapper)
```bash
gradle clean build --no-daemon
```

## CI

* GitHub Actions avec **setup-java@v4** (cache Gradle) et **Gradle 9.0.0** installé à la volée.
* Le job **clean** puis **build shadowJar** pour éviter le bug `META-INF` connu sur Shadow en CI.
  Réfs : guide Gradle sur Actions, cache Gradle et setup-java. ([GitHub Docs][2], [GitHub][1])

## Publier une release

1. Choisir la version plugin dans `gradle.properties` (ex: `0.0.10`), commit & push.
2. **Tag local** puis push:

   ```bash
   git tag v0.0.10
   git push origin v0.0.10
   ```

   *ou* lancer le workflow **Release** en `workflow_dispatch` avec `tag = v0.0.10`.
3. Le workflow crée/maj la **Release GitHub** avec:

   * `Faskin-<version>.jar`
   * `SHA256SUMS.txt` (SHA-256)
   * Notes de release générées

> Le runner installe Gradle, pas de wrapper requis. Installer de nouveaux paquets via `apt`/scripts est supporté par Actions. ([GitHub Docs][3])

## Politique doc

* À CHAQUE ticket : README, docs/ROADMAP.md, CHANGELOG.md, build & workflows **mis à jour**.
* La **roadmap n’est jamais nettoyée** : on ajoute seulement.

## Licence

MIT

[1]: https://github.com/actions/setup-java
[2]: https://docs.github.com/en/actions/tutorials/building-and-testing-java-with-gradle
[3]: https://docs.github.com/actions/using-github-hosted-runners/customizing-github-hosted-runners
