package `in`.dragonbra.generators.rpc

import `in`.dragonbra.generators.rpc.parser.ProtoParser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class RpcGenTask : DefaultTask() {

    companion object {
        private const val KDOC_AUTHOR = "Lossy"
        private const val KDOC_DATE = "2024-04-10"

        val kDocClass = """
            |@author $KDOC_AUTHOR
            |@since $KDOC_DATE
            """
            .trimMargin()
    }

    private val outputDir = File(
        project.layout.buildDirectory.get().asFile,
        "generated/source/javasteam/main/java/"
    )

    private val protoDirectory = project.file("src/main/proto")

    @TaskAction
    fun generate() {
        println("Generating RPC service methods as interfaces")

        outputDir.mkdirs()

        val protoParser = ProtoParser(outputDir)

        protoDirectory.walkTopDown()
            .filter { it.isFile && it.extension == "proto" }
            .forEach(protoParser::parseFile)
    }
}
