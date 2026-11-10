plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "javasteam"

include(":javasteam-cs")
include(":javasteam-deadlock")
include(":javasteam-depotdownloader")
include(":javasteam-dota2")
include(":javasteam-samples")
include(":javasteam-tf")
include(":javasteam-protobufs-webui")
