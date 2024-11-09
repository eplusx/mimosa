plugins {
    // Shadow JAR (uber JAR, fat JAR) support.
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    // ktlint support.
    id("org.jmailen.kotlinter") version "4.4.1" apply false
}

repositories {
    gradlePluginPortal()
}
