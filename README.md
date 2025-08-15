# Faskin

Plugin unifié Spigot 1.21 / Java 21 :
1) Auth offline (Étape 1), 2) Auto-login premium (Étape 2), 3) Skins premium en offline (Étape 3).

## Version
`0.0.6` — Gardien **pré-auth** (blocage global avant authentification) + whitelist de commandes.

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
