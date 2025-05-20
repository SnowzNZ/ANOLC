plugins {
    java
}

group = "dev.snowz.anolc"
version = "2.0.6"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    // Paper
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    // WorldEdit
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.11-SNAPSHOT")

    // WorldGuard
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.13")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks {
    processResources {
        filesMatching("**/plugin.yml") {
            expand("project" to project)
        }
    }
}
