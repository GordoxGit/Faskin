# Release Checklist

1. **Version**
   - Mettre `gradle.properties: version=x.y.z` (ex: `0.0.10`)
   - Commit: `chore(release): bump to x.y.z`

2. **Tag**
   - Créer le tag **annoté**:
     ```bash
     git tag -a v0.0.10 -m "Faskin 0.0.10"
     git push origin v0.0.10
     ```
   - _ou_ lancer le workflow **Release** en manuel (`workflow_dispatch`) avec `tag=v0.0.10`.

3. **CI Release**
   - Le workflow build `gradle clean build shadowJar --no-daemon`
   - Génére `SHA256SUMS.txt`, crée la **GitHub Release** et **upload** des artefacts.

4. **Vérifs post-release**
   - Télécharger le JAR depuis la Release, vérifier signature:
     ```bash
     sha256sum -c SHA256SUMS.txt
     ```
   - Tester le JAR sur un Paper 1.21 (Java 21) vierge.

> Notes:
> - **Ne jamais committer** `gradle/wrapper/gradle-wrapper.jar`.
> - setup-java@v4 fournit le **cache Gradle**; installation Gradle faite par le workflow (SDKMAN).
