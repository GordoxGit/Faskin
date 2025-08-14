import java.io.ByteArrayOutputStream

plugins { java }

group = "fr.heneriacore"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

    dependencies {
        compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
        compileOnly("net.kyori:adventure-api:4.17.0")
        compileOnly("net.kyori:adventure-platform-bukkit:4.3.3")
    // ProtocolLib: activé seulement si -PwithPlib=true (voir plus bas)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    implementation("org.xerial:sqlite-jdbc:3.46.0.0")
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
    withSourcesJar()
}

tasks.processResources {
    filesMatching("plugin.yml") { expand("version" to project.version) }
}

tasks.jar { archiveBaseName.set("heneriacore") }

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.test { useJUnitPlatform() }

/* ====== Anti-binaires: scanne uniquement les fichiers *versionnés* par Git ====== */
val forbiddenBinaryExtensions = listOf(
    "jar","class","war","ear","zip","7z","rar",
    "pdf","png","jpg","jpeg","gif","webp","ico","bmp","svg",
    "exe","dll","so","dylib","bin","dat",
    "mp3","wav","flac","mp4","mov","avi","mkv","webm"
)

tasks.register("checkNoBinariesTracked") {
    group = "verification"
    description = "Échoue si des binaires *versionnés* (git ls-files) sont présents."
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
                appendLine("Dépôt 100% TEXTE. Wrappers/artefacts locaux non commités.")
            }
            throw GradleException(msg)
        }
    }
}
tasks.named("check") { dependsOn("checkNoBinariesTracked") }

/* ====== OPTION ProtocolLib (build serveur) ====== */
val withPlib = providers.gradleProperty("withPlib").isPresent
val withPlibLocal = providers.gradleProperty("withPlibLocal").orNull

sourceSets {
    val main by getting {
        java {
            setSrcDirs(listOf("src/main/java"))
            if (withPlib) srcDir("src/with-plib/java")
        }
        resources.setSrcDirs(listOf("src/main/resources"))
    }
}

if (withPlib) {
    if (withPlibLocal != null) {
        println("Using local ProtocolLib jar: $withPlibLocal")
        dependencies { compileOnly(files(withPlibLocal)) }
    } else {
        println("Using remote ProtocolLib repository")
        repositories { maven("https://repo.dmulloy2.net/repository/public/") }
        dependencies { compileOnly("com.comphenix.protocol:ProtocolLib:5.2.0-SNAPSHOT") }
    }
}

