plugins {
    id("mimosa.kotlin-application-conventions")
    id("com.github.johnrengelman.shadow")
}

group = "net.eplusx.mimosa"
version = "0.1.0-SNAPSHOT"

dependencies {
    implementation(project(":lib"))

    implementation(platform("io.opentelemetry:opentelemetry-bom:1.34.0"))
    implementation("io.opentelemetry:opentelemetry-api")
    implementation("io.opentelemetry:opentelemetry-sdk")
    implementation("io.opentelemetry:opentelemetry-sdk-metrics")
    implementation("io.opentelemetry:opentelemetry-exporter-logging")
    implementation("io.opentelemetry:opentelemetry-exporter-prometheus:1.34.1-alpha")
    implementation("io.opentelemetry:opentelemetry-semconv:1.30.1-alpha")
    implementation("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure")
    implementation("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure-spi")
}

application {
    mainClass.set("net.eplusx.mimosa.daemon.AppKt")
}

// Set the Main-Class attribute in the manifest to make a JAR file executable.
// https://qiita.com/T45K/items/116b092960c7595884dd
val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "net.eplusx.mimosa.daemon.AppKt"
    }
}
