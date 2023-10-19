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
                name = "JavaSteam-tf"
                packaging = "jar"
                description = "TF classes for JavaSteam."
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
