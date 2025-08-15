# Faskin

Plugin unifié Spigot 1.21 / Java 21 :
1) Auth offline (Étape 1), 2) Auto-login premium (Étape 2), 3) Skins premium en offline (Étape 3).

## Version
`0.0.8` — Timeout d’auth + anti-bruteforce (cooldown, compteur d’échecs, lock DB).

## i18n & couleurs
- `messages_locale` ou `messages.locale` pour choisir la langue (fichiers `messages.yml` / `messages_<locale>.yml`).
- Codes couleur `&` convertis via `ChatColor.translateAlternateColorCodes`. :contentReference[oaicite:4]{index=4}

## Anti-bruteforce
- Cooldown local par joueur (`min_seconds_between_attempts`).
- Compteur d’échecs et **lock** (`max_failed_attempts`, `lock_minutes`) stockés en DB.
- Kick automatique si non authentifié après `timeout_seconds` (configurable).

## Rappels d’auth
- Tâche périodique qui envoie un **ActionBar** aux joueurs non authentifiés. Implémenté via `Player.Spigot#sendMessage(ChatMessageType.ACTION_BAR, ...)`. :contentReference[oaicite:5]{index=5}
- Config : `reminder.enabled`, `reminder.interval_seconds`, `reminder.actionbar`, `reminder.chat_on_join`.

## Configuration (pré-auth)
Clés `preauth.block.*` pour activer/désactiver mouvement/chat/commandes/interactions, etc.
Liste blanche: `preauth.commands.whitelist` (toujours inclut `register`/`login` en plus).

## Dépendances incluses (shaded)
- `org.xerial:sqlite-jdbc:3.50.3.0` (inclus dans le JAR via Shadow).

## Fonctionnement session
- Si `login.allow_ip_session=true` et `session_minutes>0` :
  - même IP + dernier login < TTL ⇒ auto-auth.
  - sinon ⇒ `/login` requis.
- L’IP provient de `Player#getAddress()` (Spigot API). :contentReference[oaicite:4]{index=4}

## Build (sans wrapper)
- Gradle local : `gradle clean build --no-daemon`
- CI : `gradle/actions/setup-gradle` (aucun wrapper requis)
- Plugin Shadow : `com.gradleup.shadow: 9.0.2` (compatible Gradle 9).

## Politique doc
- À **CHAQUE ticket** : mettre à jour **README**, **docs/ROADMAP.md**, **CHANGELOG.md**, fichiers build et toute doc impactée.
- **Roadmap** non destructive : on **ajoute**, on ne **supprime** pas d’items.

## Dépendance
`org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT` (repo Spigot).

## API
- `BukkitScheduler` pour timers (sync). :contentReference[oaicite:10]{index=10}
- `Player#kickPlayer(String)` pour le kick. :contentReference[oaicite:11]{index=11}
