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

/* Artifact publishing */
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name = "JavaSteam-cs"
                packaging = "jar"
                description = "CS classes for JavaSteam."
                url = "https://github.com/Longi94/JavaSteam"
                inceptionYear = "2025"
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
