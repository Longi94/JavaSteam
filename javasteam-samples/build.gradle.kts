plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
}

dependencies {
    implementation(rootProject)
    implementation(project(":javasteam-cs"))
    implementation(project(":javasteam-depotdownloader"))

    implementation(libs.bouncyCastle)
    implementation(libs.gson)
    implementation(libs.kotlin.coroutines)
    implementation(libs.okHttp)
    implementation(libs.protobuf.java) // Protobuf access
    implementation(libs.qrCode)
    implementation(libs.zstd) // Content Downloading.
    implementation(libs.xz) // Content Downloading.
}
