plugins {
    id("mimosa.kotlin-application-conventions")
    id("com.github.johnrengelman.shadow")
    id("org.jmailen.kotlinter")
}

group = "net.eplusx.mimosa"
version = "0.1.0-SNAPSHOT"

dependencies {
    implementation(project(":lib"))
}

application {
    mainClass.set("net.eplusx.mimosa.cli.AppKt")
}

// Set the Main-Class attribute in the manifest to make a JAR file executable.
// https://qiita.com/T45K/items/116b092960c7595884dd
val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "net.eplusx.mimosa.cli.AppKt"
    }
}
