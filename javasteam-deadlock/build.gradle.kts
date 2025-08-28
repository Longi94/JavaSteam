plugins {
    alias(libs.plugins.protobuf.gradle)
    id("java")
    id("maven-publish")
    id("signing")
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    withSourcesJar()
    withJavadocJar()
}

/* Protobufs */
protobuf.protoc {
    artifact = libs.protobuf.protoc.get().toString()
}

/* Java Docs */
tasks.javadoc {
    exclude("**/in/dragonbra/javasteam/protobufs/**")
}

dependencies {
    implementation(libs.protobuf.java)
}

// TODO promote to actual lib?
