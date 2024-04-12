plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" // https://github.com/JLLeitschuh/ktlint-gradle/releases
}

version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())

    // https://mvnrepository.com/artifact/commons-io/commons-io
    implementation("commons-io:commons-io:2.16.0")
}

gradlePlugin {
    plugins {
        create("steamlanguagegen") {
            id = "steamlanguagegen"
            implementationClass = "in.dragonbra.generators.steamlanguage.SteamLanguageGenPlugin"
        }
        create("projectversiongen") {
            id = "projectversiongen"
            implementationClass = "in.dragonbra.generators.versions.VersionGenPlugin"
        }
    }
}
