plugins {
    kotlin("jvm") version "1.9.10"
    application
}

group = "net.eplusx.logger"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("io.kotest:kotest-runner-junit5:5.7.2")
    testImplementation("io.kotest:kotest-assertions-core:5.7.2")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("net.eplusx.logger.AppKt")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}