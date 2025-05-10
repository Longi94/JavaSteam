package `in`.dragonbra.generators.rpc

import `in`.dragonbra.generators.rpc.parser.ProtoParser
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class RpcGenTask : DefaultTask {

    companion object {
        private const val KDOC_AUTHOR = "Lossy"
        private const val KDOC_DATE = "2024-04-10"

        val kDocClass = """
            |@author $KDOC_AUTHOR
            |@since $KDOC_DATE
            """
            .trimMargin()
    }


    @InputDirectory
    abstract fun getProtoDirectory(): DirectoryProperty

    @OutputDirectory
    abstract fun getOutputDir(): DirectoryProperty

    @Inject
    constructor() {
        getOutputDir().convention(
            project.layout.buildDirectory.dir(
                "generated/source/javasteam/main/java/"
            )
        )
        getProtoDirectory().convention(
            project.layout.projectDirectory.dir(
                "src/main/proto"
            )
        )
    }

    @TaskAction
    fun generate() {
        println("Generating RPC service methods as interfaces")
        val outputDir = getOutputDir().get().asFile
        val protoDirectory = getProtoDirectory().get().asFile

        outputDir.mkdirs()

        val protoParser = ProtoParser(outputDir)

        protoDirectory.walkTopDown()
            .filter { it.isFile && it.extension == "proto" }
            .forEach(protoParser::parseFile)
    }
}
