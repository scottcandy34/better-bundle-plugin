plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "com.grok"
version = "1.0.0"
description = "Better Bundles - Upgradable bundles plugin for PaperMC"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("26.1.2.build.+")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    processResources {
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(
                "version" to project.version,
                "name" to project.name
            )
        }
    }

    jar {
        archiveFileName.set("BetterBundles-${project.version}.jar")
    }
}