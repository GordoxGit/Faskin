# Faskin

Plugin unifié Spigot 1.21 / Java 21 :
1) Auth offline (Étape 1), 2) Auto-login premium (Étape 2), 3) Skins premium en offline (Étape 3).

## Version
`0.0.3` — Persistance **SQLite** + hash **PBKDF2**, commandes async.

## Dépendances incluses (shaded)
- `org.xerial:sqlite-jdbc:3.50.3.0` (inclus dans le JAR via Shadow).

## Build (sans wrapper)
- Installer Gradle localement, puis `gradle clean build` (CI via setup-gradle).

## Politique doc
- À **CHAQUE ticket** : mettre à jour **README**, **docs/ROADMAP.md**, **CHANGELOG.md**, fichiers build et toute doc impactée.
- **Roadmap** non destructive : on **ajoute**, on ne **supprime** pas d’items.

## Dépendance
`org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT` (repo Spigot).
