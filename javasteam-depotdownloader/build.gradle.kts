import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    alias(libs.plugins.kotlin.dokka)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kotlinter)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.protobuf.gradle)
    id("maven-publish")
    id("signing")
}

repositories {
    mavenCentral()
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.java.get()))
    }
}

/* Protobufs */
protobuf.protoc {
    artifact = libs.protobuf.protoc.get().toString()
}


java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    withSourcesJar()
}

/* Java-Kotlin Docs */
dokka {
    moduleName.set("JavaSteam")
    dokkaSourceSets.main {
        suppressGeneratedFiles.set(false) // Allow generated files to be documented.
        perPackageOption {
            // Deny most of the generated files.
            matchingRegex.set("in.dragonbra.javasteam.(protobufs|enums|generated).*")
            suppress.set(true)
        }
    }
}

// Make sure Maven Publishing gets javadoc
val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaGenerate)
    archiveClassifier.set("javadoc")
    from(layout.buildDirectory.dir("dokka/html"))
}
artifacts {
    archives(javadocJar)
}

/* Kotlinter */
tasks.withType<LintTask> {
    this.source = this.source.minus(fileTree("build/generated")).asFileTree
}
tasks.withType<FormatTask> {
    this.source = this.source.minus(fileTree("build/generated")).asFileTree
}

dependencies {
    implementation(rootProject) // TODO verify if this causes something like a circular dependency.

    implementation("com.squareup.okio:okio:3.16.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    implementation(libs.bundles.ktor)
    implementation(libs.commons.lang3)
    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.stdib)
    implementation(libs.protobuf.java)
}
