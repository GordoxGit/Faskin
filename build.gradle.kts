plugins {
    java
}

group = "com.heneria"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // API Paper 1.21.x (compileOnly pour éviter d'embarquer l'API)
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    // Option Spigot pur (laisser commenté si non utilisé)
    // compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

tasks.processResources {
    // Injecte la version Gradle dans plugin.yml
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.jar {
    archiveBaseName.set("skinview")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

/* ==== Politique ANTI-BINAIRES (bloquant) ==== */
val forbiddenBinaryExtensions = listOf(
    "jar","class","war","ear","zip","7z","rar","pdf",
    "png","jpg","jpeg","gif","webp","ico",
    "exe","dll","so","dylib","bin","dat",
    "mp3","wav","mp4","mov","avi","mkv"
)

tasks.register("checkNoBinaries") {
    group = "verification"
    description = "Echoue si des fichiers binaires sont présents dans le repo."
    doLast {
        val exts = forbiddenBinaryExtensions.toSet()
        val tree = fileTree(".") {
            exclude(".git/**", "build/**", ".gradle/**", ".idea/**", "out/**")
        }
        val offenders = mutableListOf<File>()
        tree.files.forEach { f ->
            if (!f.isFile) return@forEach
            val name = f.name.lowercase()
            val ext = name.substringAfterLast('.', "")
            if (ext in exts) offenders += f
            else {
                // Heuristique binaire (NUL byte dans les 4 Ko initiaux)
                val bytes = f.readBytes()
                val limit = kotlin.math.min(bytes.size, 4096)
                var hasNul = false
                var i = 0
                while (i < limit) {
                    if (bytes[i] == 0.toByte()) { hasNul = true; break }
                    i++
                }
                if (hasNul) offenders += f
            }
        }
        if (offenders.isNotEmpty()) {
            val msg = buildString {
                appendLine("Interdit: fichiers binaires détectés dans le repository :")
                offenders.sortedBy { it.path }.forEach { appendLine(" - ${it.path}") }
                appendLine("Supprimez-les. Le dépôt doit rester 100% TEXTE.")
            }
            throw GradleException(msg)
        }
    }
}

tasks.check { dependsOn("checkNoBinaries") }
