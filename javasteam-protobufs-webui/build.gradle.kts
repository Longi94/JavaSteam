import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    alias(libs.plugins.kotlin.dokka)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kotlinter)
    alias(libs.plugins.protobuf.gradle)
    id("maven-publish")
    id("signing")
    rpcinterfacegen
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    withSourcesJar()
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

/* Source Sets */
sourceSets.main {
    java.srcDirs(
        // builtBy() fixes gradle warning "Execution optimizations have been disabled for task"
        files("build/generated/source/javasteam/main/java").builtBy("generateRpcMethods")
    )
}

/* Tasks */
tasks["compileJava"].dependsOn("generateRpcMethods")
tasks["compileKotlin"].dependsOn("generateRpcMethods")
tasks["generateRpcMethods"].dependsOn("extractProto", "extractIncludeProto")

/* Testing */
tasks.test {
    useJUnitPlatform()
}

/* Java-Kotlin Docs */
dokka {
    moduleName.set("JavaSteam")
    dokkaSourceSets.main {
        suppressGeneratedFiles.set(false) // Allow generated files to be documented.
        sourceRoots.setFrom(
            "src/main/java",
            files(layout.buildDirectory.dir("generated/source/javasteam/main/java")).builtBy("generateRpcMethods"),
        )
        classpath.from(tasks.compileJava.map { it.destinationDirectory })
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
    val generatedFile = "${File.separator}build${File.separator}generated"
    exclude { it.file.path.contains(generatedFile) }
}

tasks.withType<FormatTask> {
    val generatedFile = "${File.separator}build${File.separator}generated"
    exclude { it.file.path.contains(generatedFile) }
}

/* Jar */
tasks.jar {
    exclude("**/*.proto")
}

dependencies {
    api(rootProject)

    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.stdib)
    implementation(libs.protobuf.java)

    testImplementation(platform(libs.tests.junit.bom))
    testImplementation(libs.tests.junit.jupiter)
    testRuntimeOnly(libs.tests.junit.platform)
}

/* Artifact publishing */
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(javadocJar)
            pom {
                name = "JavaSteam-protobufs-webui"
                packaging = "jar"
                description = "Webui protobuf classes and services for JavaSteam."
                url = "https://github.com/Longi94/JavaSteam"
                inceptionYear = "2026"
                scm {
                    connection = "scm:git:git://github.com/Longi94/JavaSteam.git"
                    developerConnection = "scm:git:ssh://github.com:Longi94/JavaSteam.git"
                    url = "https://github.com/Longi94/JavaSteam/tree/master"
                }
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://www.opensource.org/licenses/mit-license.php"
                    }
                }
                developers {
                    developer {
                        id = "Longi"
                        name = "Long Tran"
                        email = "lngtrn94@gmail.com"
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
