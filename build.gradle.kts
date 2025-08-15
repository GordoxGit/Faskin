plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.release.set(17)
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

tasks.withType<Jar> {
    archiveBaseName.set("Faskin")
    archiveVersion.set(project.version.toString())
    manifest {
        attributes["Created-By"] = "Faskin build"
        attributes["Built-By"] = "CI"
    }
}

// Fat jar + relocation pour éviter conflits de classes
tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("") // remplace le jar normal
    relocate("org.sqlite", "com.faskin.libs.sqlite")
}

tasks.build { dependsOn(tasks.shadowJar) }
