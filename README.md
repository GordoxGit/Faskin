# Faskin

Plugin unifié Spigot 1.21 / Java 21 :
1) Auth offline (Étape 1), 2) Auto-login premium (Étape 2), 3) Skins premium en offline (Étape 3).

## Build
- **Sans Gradle Wrapper** (non committé). Installer Gradle en local, puis :  
  `gradle clean build`
- CI utilise `gradle/actions/setup-gradle` (aucun wrapper requis).

## Politique doc
- À **CHAQUE ticket** : mettre à jour **README**, **docs/ROADMAP.md**, **CHANGELOG.md**, fichiers build et toute doc impactée.
- **Roadmap** non destructive : on **ajoute**, on ne **supprime** pas d’items.

## Dépendance
`org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT` (repo Spigot).
