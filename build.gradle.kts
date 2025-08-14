import java.io.ByteArrayOutputStream

plugins { java }

group = "com.heneria"
version = "0.5.0" // feat: ProtocolLib signed skins applier

repositories {
    mavenCentral()
    // Garder, utile pour Spigot API
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
    // ProtocolLib: AJOUTÉ UNIQUEMENT SI -PwithPlib=true (voir plus bas)
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
    withSourcesJar()
}
tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    filesMatching("plugin.yml") { expand("version" to project.version) }
}

tasks.withType<AbstractCopyTask> { duplicatesStrategy = DuplicatesStrategy.INCLUDE }
tasks.jar { archiveBaseName.set("skinview") }

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

/* ====== CI: scan binaire limité aux fichiers **versionnés** ====== */
val forbiddenBinaryExtensions = listOf(
    "jar","class","war","ear","zip","7z","rar",
    "pdf","png","jpg","jpeg","gif","webp","ico","bmp","svg",
    "exe","dll","so","dylib","bin","dat",
    "mp3","wav","flac","mp4","mov","avi","mkv","webm"
)

tasks.register("checkNoBinariesTracked") {
    group = "verification"
    description = "Echoue si des binaires *versionnés* (git ls-files) sont présents."
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
            logger.warn("git indisponible: fallback scan minimal (src/, resources/, scripts/)")
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
                appendLine("Dépôt 100% TEXTE. Wrappers/artefacts/JARs locaux non commités.")
            }
            throw GradleException(msg)
        }
    }
}

tasks.named("check") { dependsOn("checkNoBinariesTracked") }

/* ====== OPTION ProtocolLib: activée seulement si -PwithPlib=true ====== */
val withPlib = providers.gradleProperty("withPlib").isPresent
val withPlibLocal = providers.gradleProperty("withPlibLocal").orNull // chemin vers un .jar local

sourceSets {
    val main by getting {
        java {
            // Src principal toujours présent
            setSrcDirs(listOf("src/main/java"))
            // Ajoute les classes ProtocolLib uniquement si flag activé
            if (withPlib) srcDir("src/with-plib/java")
        }
        resources.srcDir("src/main/resources")
    }
}

if (withPlib) {
    // Dépendance compileOnly vers ProtocolLib, au choix: repo ou jar local
    if (withPlibLocal != null) {
        println("Using local ProtocolLib jar: $withPlibLocal")
        dependencies { compileOnly(files(withPlibLocal)) }
    } else {
        println("Using remote ProtocolLib repository")
        repositories { maven("https://repo.dmulloy2.net/repository/public/") }
        dependencies { compileOnly("com.comphenix.protocol:ProtocolLib:5.2.0-SNAPSHOT") }
    }
}
