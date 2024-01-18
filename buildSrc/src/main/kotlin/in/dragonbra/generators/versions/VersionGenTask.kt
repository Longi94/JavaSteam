package `in`.dragonbra.generators.versions

import `in`.dragonbra.generators.versions.generator.JavaGen
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class VersionGenTask : DefaultTask() {

    private companion object {
        private const val CLASS_NAME = "Versions"
        private const val PACKAGE = "in.dragonbra.javasteam.util"
    }

    private val outputDir = File(
        project.layout.buildDirectory.get().asFile,
        "generated/source/javasteam/main/java/$PACKAGE"
    )

    @TaskAction
    fun generate() {
        println("Generating version class")
        JavaGen(PACKAGE, outputDir).run {
            emit(CLASS_NAME, project.version.toString())
            flush()
            close()
        }
    }
}
