plugins {
    java
    id("com.gradleup.shadow") version "9.0.2" // Shadow 9, compatible Gradle 9
}

version = "0.1.1"

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
    implementation("org.xerial:sqlite-jdbc:3.50.3.0")
}

// JAR classique (manifest)
tasks.withType<Jar> {
    archiveBaseName.set("Faskin")
    archiveVersion.set(project.version.toString())
    manifest {
        attributes["Created-By"] = "Faskin build"
        attributes["Built-By"] = "CI"
    }
}

// Shadow 9: configuration Kotlin DSL idiomatique
tasks.shadowJar {
    // remplace le jar normal
    archiveClassifier.set("")
    // relocation pour éviter conflits si Paper charge la lib (plugin.yml:libraries)
    relocate("org.sqlite", "com.faskin.libs.sqlite")
}

// build = shadowJar
tasks.build { dependsOn(tasks.shadowJar) }
