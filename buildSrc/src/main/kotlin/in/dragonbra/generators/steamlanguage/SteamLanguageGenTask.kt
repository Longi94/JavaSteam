package `in`.dragonbra.generators.steamlanguage

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.stream.Collectors
import `in`.dragonbra.generators.steamlanguage.parser.LanguageParser
import `in`.dragonbra.generators.steamlanguage.parser.token.TokenAnalyzer
import `in`.dragonbra.generators.steamlanguage.parser.node.EnumNode
import `in`.dragonbra.generators.steamlanguage.parser.node.ClassNode
import `in`.dragonbra.generators.steamlanguage.generator.JavaGen
import `in`.dragonbra.generators.steamlanguage.generator.JavaFileWriter
import java.io.FileInputStream

class SteamLanguageGenTask : DefaultTask() {

    private val outputDir = File(
        project.layout.buildDirectory.get().asFile,
        "generated/source/steamd/main/java/in/dragonbra/javasteam"
    )

    private val inputFile = project.file("src/main/steamd/in/dragonbra/javasteam/steammsg.steamd")

    private val _package = "in.dragonbra.javasteam"

    @InputDirectory
    fun getInputDirectory(): File = project.file("src/main/steamd/")

    @TaskAction
    fun generate() {
        val buffer = IOUtils.toString(FileInputStream(inputFile), "utf-8")
        val tokens = LanguageParser.tokenizeString(buffer, inputFile.name)
        val root = TokenAnalyzer.analyze(tokens, inputFile.parent)
        val enums = root.childNodes.filterIsInstance<EnumNode>().toList()
        val classes = root.childNodes.filterIsInstance<ClassNode>().toList()

        val flagEnums = hashSetOf<String>()

        FileUtils.deleteDirectory(outputDir)

        enums.forEach { enum ->
            val javaGen = JavaGen()
        }

        enums.each {
            in.dragonbra.generators.steamlanguage.parser.node.Node _enum ->
            def javaGen = new in.dragonbra.generators.steamlanguage.generator.JavaGen(
                _enum,
                "${_package}.enums",
                new File (outputDir,
                "enums"
            ), flagEnums)
            javaGen.emit()
            javaGen.flush()
            javaGen.close()
        }

        classes.each {
            in.dragonbra.generators.steamlanguage.parser.node.Node _class ->
            def javaGen = new in.dragonbra.generators.steamlanguage.generator.JavaGen(
                _class,
                "${_package}.generated",
                new File (outputDir,
                "generated"
            ), flagEnums)
            javaGen.emit()
            javaGen.flush()
            javaGen.close()
        }
    }
}