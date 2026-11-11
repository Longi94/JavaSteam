package `in`.dragonbra.generators.versions

import `in`.dragonbra.generators.versions.generator.JavaGen
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class VersionGenTask : DefaultTask {

    private companion object {
        private const val CLASS_NAME = "Versions"
        private const val PACKAGE = "in.dragonbra.javasteam.util"
    }

    @Input
    abstract fun getClassName(): Property<String>

    @Input
    abstract fun getPackage(): Property<String>

    // Tracked as an input so a version bump regenerates the class
    @Input
    abstract fun getVersion(): Property<String>

    @OutputDirectory
    abstract fun getOutputDir(): DirectoryProperty

    @Inject
    constructor() {
        getClassName().convention(CLASS_NAME)
        getPackage().convention(PACKAGE)
        getVersion().convention(project.provider { project.version.toString() })
        // Kept apart from the RPC generator's output, overlapping outputs can't be cached
        getOutputDir().convention(
            project.layout.buildDirectory.dir(
                "generated/source/version/main/java/in/dragonbra/javasteam/util"
            )
        )
    }

    @TaskAction
    fun generate() {
        println("Generating version class")
        JavaGen(getPackage().get(), getOutputDir().get().asFile).run {
            emit(getClassName().get(), getVersion().get())
            flush()
            close()
        }
    }
}
