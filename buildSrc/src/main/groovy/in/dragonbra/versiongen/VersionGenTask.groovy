package in.dragonbra.versiongen

import in.dragonbra.versiongen.generator.JavaGen
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class VersionGenTask extends DefaultTask {

    private String _package = 'in.dragonbra.javasteam.util'

    private File outputDir = new File(project.buildDir, "generated/source/javasteam/main/java/$_package")

    @OutputDirectory
    File getOutputDir() {
        return outputDir
    }

    @TaskAction
    def generate() {
        def javaGen = new JavaGen(_package, outputDir)
        javaGen.emit("Versions", project.version.toString())
        javaGen.flush()
        javaGen.close()
    }
}
