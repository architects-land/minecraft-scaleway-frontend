plugins {
    kotlin("jvm") version "2.1.20"
}

group = "world.anhgelus.world.architectsland"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("net.minestom:minestom-snapshots:e94aaed297")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}