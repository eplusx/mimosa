plugins {
    kotlin("jvm") version "1.9.22"
    id("com.google.devtools.ksp") version "1.9.22-1.0.16"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "net.eplusx.mimosa"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-adapters:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:mockwebserver:4.12.0")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")

    testImplementation("io.kotest:kotest-runner-junit5:5.7.2")
    testImplementation("io.kotest:kotest-assertions-core:5.7.2")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("net.eplusx.mimosa.AppKt")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

// Set the Main-Class attribute in the manifest to make a JAR file executable.
// https://qiita.com/T45K/items/116b092960c7595884dd
val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "net.eplusx.mimosa.AppKt"
    }
}