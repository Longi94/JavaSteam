plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("org.jmailen.kotlinter") version "4.3.0" // https://plugins.gradle.org/plugin/org.jmailen.kotlinter
}

version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())

    // https://mvnrepository.com/artifact/commons-io/commons-io
    implementation("commons-io:commons-io:2.16.0")
    // https://mvnrepository.com/artifact/com.squareup/kotlinpoet
    implementation("com.squareup:kotlinpoet:1.16.0")
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
        create("rpcinterfacegen") {
            id = "rpcinterfacegen"
            implementationClass = "in.dragonbra.generators.rpc.RpcGenPlugin"
        }
    }
}
