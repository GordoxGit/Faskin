import java.io.ByteArrayOutputStream

plugins { java }

group = "com.heneria"
version = "0.2.1" // fix: scan seulement des fichiers git-trackés

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

/* ====== Anti-binaires (scan fichiers *versionnés* uniquement) ====== */
val forbiddenBinaryExtensions = listOf(
    "jar","class","war","ear","zip","7z","rar",
    "pdf","png","jpg","jpeg","gif","webp","ico","bmp","svg",
    "exe","dll","so","dylib","bin","dat",
    "mp3","wav","flac","mp4","mov","avi","mkv","webm"
)

tasks.register("checkNoBinariesTracked") {
    group = "verification"
    description = "Échoue si des binaires sont *versionnés* (git ls-files)."

    doLast {
        val exts = forbiddenBinaryExtensions.toSet()
        val offenders = mutableListOf<String>()

        fun scanPaths(paths: List<String>) {
            paths.filter { it.isNotBlank() }.forEach { rel ->
                val f = project.file(rel)
                if (!f.isFile) return@forEach
                val ext = rel.substringAfterLast('.', "").lowercase()
                val byExt = ext in exts
                var byHeur = false
                if (!byExt) {
                    val bytes = f.readBytes()
                    val limit = kotlin.math.min(bytes.size, 4096)
                    var i = 0
                    while (i < limit) { if (bytes[i] == 0.toByte()) { byHeur = true; break }; i++ }
                }
                if (byExt || byHeur) offenders += rel
            }
        }

        // 1) Essaye d'utiliser 'git ls-files --cached -z' (ne liste *que* les fichiers suivis)
        try {
            val out = ByteArrayOutputStream()
            project.exec {
                commandLine("git", "ls-files", "--cached", "-z")
                isIgnoreExitValue = false
                standardOutput = out
            }
            val files = out.toString("UTF-8").split('\u0000')
            scanPaths(files)
        } catch (e: Exception) {
            // 2) Fallback (sans git) : avertit et scanne *répertoire* minimal (src/resources/scripts)
            logger.warn("git indisponible: fallback scan workspace (src/, resources/, scripts/) — installez Git pour un contrôle strict.")
            val roots = listOf("src", "resources", "scripts").map { project.file(it) }.filter { it.exists() }
            roots.forEach { root ->
                root.walkTopDown().forEach { f ->
                    val rel = project.rootDir.toPath().relativize(f.toPath()).toString().replace('\\','/')
                    scanPaths(listOf(rel))
                }
            }
        }

        if (offenders.isNotEmpty()) {
            val msg = buildString {
                appendLine("Interdit: fichiers binaires *versionnés* détectés :")
                offenders.sorted().forEach { appendLine(" - $it") }
                appendLine("Le dépôt doit rester 100% TEXTE. Gardez vos wrappers/artefacts en local, non commité.")
            }
            throw GradleException(msg)
        }
    }
}

tasks.named("check") { dependsOn("checkNoBinariesTracked") }
