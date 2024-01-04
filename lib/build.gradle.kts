plugins {
    id("mimosa.kotlin-library-conventions")
    id("com.google.devtools.ksp") version "1.9.22-1.0.16"
}

group = "net.eplusx.mimosa"
version = "0.1.0-SNAPSHOT"

dependencies {
    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-adapters:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:mockwebserver:4.12.0")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
}
