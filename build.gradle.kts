plugins {
    kotlin("jvm") version "2.1.20"
    id("com.gradleup.shadow") version "8.3.0"
    kotlin("plugin.serialization") version "2.1.20"
}

group = "world.anhgelus.world.architectsland.minecraftscalewayfrontend"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    implementation("org.apache.logging.log4j:log4j-api:2.25.0")
    implementation("org.apache.logging.log4j:log4j-core:2.25.0")

    implementation("net.minestom:minestom-snapshots:e94aaed297")
    implementation("net.lenni0451:MCPing:1.4.4")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21)) // Minestom has a minimum Java version of 21
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "world.anhgelus.world.architectsland.minecraftscalewayfrontend.Main"
        }
    }

    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("") // Prevent the -all suffix on the shadowjar file.
    }
}