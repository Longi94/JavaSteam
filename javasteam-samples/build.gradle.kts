plugins {
    alias(libs.plugins.kotlin.jvm)
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

    implementation(libs.bouncyCastle)
    implementation(libs.gson)
    implementation(libs.kotlin.coroutines)
    implementation(libs.protobuf.java) // To access protobufs directly as shown in Sample #2
    implementation(libs.qrCode)

    testImplementation(libs.test.jupiter.api)
}
