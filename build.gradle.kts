plugins { java }

group = "com.heneria"
version = "0.1.4" // Politique: pas de wrapper généré/commité par Codex

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    // compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
    withSourcesJar()
}

tasks.processResources {
    filesMatching("plugin.yml") { expand("version" to project.version) }
}

tasks.jar { archiveBaseName.set("skinview") }

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

/* ====== Anti-binaires (bloquant) — aucune whitelist ====== */
val forbiddenBinaryExtensions = listOf(
    "jar","class","war","ear","zip","7z","rar",           // archives/classes
    "pdf","png","jpg","jpeg","gif","webp","ico","bmp","svg",
    "exe","dll","so","dylib","bin","dat",
    "mp3","wav","flac","mp4","mov","avi","mkv","webm"
)

tasks.register("checkNoBinaries") {
    group = "verification"
    description = "Échoue si des binaires sont présents dans le repo (y compris tout wrapper Gradle)."
    doLast {
        val exts = forbiddenBinaryExtensions.toSet()
        val offenders = mutableListOf<File>()

        fileTree(project.rootDir) {
            exclude(".git/**", ".gradle/**", ".idea/**", "out/**", "build/**", "target/**")
        }.files.forEach { f ->
            if (!f.isFile) return@forEach
            val ext = f.name.substringAfterLast('.', "").lowercase()
            val byExt = ext in exts
            var byHeur = false
            if (!byExt) {
                val bytes = f.readBytes()
                val limit = kotlin.math.min(bytes.size, 4096)
                var i = 0
                while (i < limit) { if (bytes[i] == 0.toByte()) { byHeur = true; break }; i++ }
            }
            if (byExt || byHeur) offenders += f
        }

        if (offenders.isNotEmpty()) {
            val msg = buildString {
                appendLine("Interdit: fichiers binaires détectés dans le dépôt :")
                offenders.sortedBy { it.path }.forEach { appendLine(" - ${it.path}") }
                appendLine("Politique: dépôt 100% TEXTE. Pas de wrapper généré/commité par Codex.")
                appendLine("Si un wrapper est nécessaire sur un poste, il doit rester LOCAL et NON versionné.")
            }
            throw GradleException(msg)
        }
    }
}
tasks.check { dependsOn("checkNoBinaries") }
