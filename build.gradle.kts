plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
}

tasks.withType<Jar> {
    archiveBaseName.set("Faskin")
    archiveVersion.set(project.version.toString())
    manifest {
        attributes["Created-By"] = "Faskin bootstrap"
        attributes["Built-By"] = "CI"
    }
}
