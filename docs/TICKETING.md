# TICKETING — règles Codex

1. **À CHAQUE TICKET** : MAJ **README**, **docs/ROADMAP.md**, **CHANGELOG.md**, fichiers build (bump version si besoin), CI, et toute doc impactée.
2. **Roadmap** : on **ajoute**, on ne **supprime** pas.
3. **Gradle Wrapper** : **NE PAS COMMITTER** de wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/*`). Si généré localement, ces fichiers sont ignorés (.gitignore).
4. Versionning: bump patch (`0.0.x`) par livraison fonctionnelle.
