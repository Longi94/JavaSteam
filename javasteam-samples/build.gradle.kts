plugins {
    `java-library`
    application
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

    implementation(libs.bouncyCastle)
    implementation(libs.gson)
    implementation(libs.kotlin.coroutines)
    implementation(libs.okHttp)
    implementation(libs.protobuf.java) // To access protobufs directly as shown in Sample #2
    implementation(libs.qrCode)
    implementation(libs.zstd) // Content Downloading.
    implementation(libs.xz) // Content Downloading.
}

// Allow running samples from command line
// Usage: ./gradlew :javasteam-samples:run -PmainClass=<fully.qualified.ClassName> --args="username password"
application {
    mainClass.set(project.findProperty("mainClass") as String? ?: "in.dragonbra.javasteamsamples._031_get_categories_games.SampleGameCategories")
}

// Enable stdin for interactive 2FA input
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
