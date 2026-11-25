plugins {
    java
}

group = "dev.tokenshop"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // Change this to match your server version if needed
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

// Builds with any newer JDK (e.g. Java 25) but outputs Java 21 bytecode,
// so the plugin runs on normal Paper 1.21 servers.
tasks.withType<JavaCompile> {
    options.release.set(21)
    options.encoding = "UTF-8"
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.jar {
    archiveFileName.set("TokenShop-${project.version}.jar")
}
