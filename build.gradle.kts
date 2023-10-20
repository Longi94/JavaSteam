import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `maven-publish`
    alias(libs.plugins.kotlin.dokka)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.protobuf.gradle)
    id("jacoco")
    id("projectversiongen")
    id("signing")
    id("steamlanguagegen")
}

allprojects {
    group = "in.dragonbra"
    version = "1.3.0"
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    withSourcesJar()
}

/* Protobufs */
protobuf.protoc {
    artifact = libs.protobuf.protoc.get().toString()
}

/* Testing */
tasks.test {
    useJUnitPlatform()
    testLogging {
        events = setOf(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED,
        )
    }
}

/* Test Reporting */
jacoco.toolVersion = libs.versions.jacoco.get()
tasks.jacocoTestReport {
    reports {
        xml.required = false
        html.required = false
    }
}

/* Java-Kotlin Docs */
tasks.dokkaJavadoc {
    dokkaSourceSets {
        configureEach {
            suppressGeneratedFiles.set(false) // Allow generated files to be documented.
            perPackageOption {
                // Deny most of the generated files.
                matchingRegex.set("in.dragonbra.javasteam.(protobufs|enums|generated).*")
                suppress.set(true)
            }
        }
    }
}

val dokkaJavadocJar by tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

/* Configuration */
configurations {
    configureEach {
        // Only allow junit 5
        exclude("junit", "junit")
        exclude("org.junit.vintage", "junit-vintage-engine")
    }
}

/* Source Sets */
sourceSets.main {
    java.srcDirs(
        // builtBy() fixes gradle warning "Execution optimizations have been disabled for task"
        files("build/generated/source/steamd/main/java").builtBy("generateSteamLanguage"),
        files("build/generated/source/javasteam/main/java").builtBy("generateProjectVersion")
    )
}

/* Dependencies */
tasks["check"].dependsOn("jacocoTestReport")
tasks["compileJava"].dependsOn("generateSteamLanguage")
tasks["compileJava"].dependsOn("generateProjectVersion")
tasks["build"].finalizedBy(dokkaJavadocJar)

dependencies {
    implementation(libs.commons.io)
    implementation(libs.commons.lang3)
    implementation(libs.commons.validator)
    implementation(libs.gson)
    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.stdib)
    implementation(libs.okHttp)
    implementation(libs.protobuf.java)
    implementation(libs.webSocket)

    testImplementation(libs.bundles.testing)
}

/* Artifact publishing */
nexusPublishing {
    repositories {
        sonatype()
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name = "JavaSteam"
                packaging = "jar"
                description = "Java library to interact with Valve\"s Steam network."
                url = "https://github.com/Longi94/JavaSteam"
                inceptionYear = "2018"
                scm {
                    connection = "scm:git:git://github.com/Longi94/JavaSteam.git"
                    developerConnection = "scm:git:ssh://github.com:Longi94/JavaSteam.git"
                    url = "http://github.com/Longi94/JavaSteam/tree/master"
                }
                licenses {
                    license {
                        name = "MIT License"
                        url = "http://www.opensource.org/licenses/mit-license.php"
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
    repositories {
        maven {
            val snapshotRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            val stagingRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotRepoUrl else stagingRepoUrl
            credentials {
                val ossrhUsername: String by project
                val ossrhPassword: String by project
                username = ossrhUsername
                password = ossrhPassword
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
