package `in`.dragonbra.generators.steamlanguage

import `in`.dragonbra.generators.steamlanguage.generator.JavaGen
import `in`.dragonbra.generators.steamlanguage.parser.LanguageParser
import `in`.dragonbra.generators.steamlanguage.parser.node.ClassNode
import `in`.dragonbra.generators.steamlanguage.parser.node.EnumNode
import `in`.dragonbra.generators.steamlanguage.parser.token.TokenAnalyzer
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

abstract class SteamLanguageGenTask : DefaultTask {

    private companion object {
        private const val PKG = "in.dragonbra.javasteam"
    }

    @Input
    abstract fun getInputPkg(): Property<String>

    @InputFile
    abstract fun getInputFile(): RegularFileProperty

    @OutputDirectory
    abstract fun getOutputDir(): DirectoryProperty

    @Inject
    constructor() {
        getInputPkg().convention(PKG)
        getInputFile().convention {
            project.file("src/main/steamd/in/dragonbra/javasteam/steammsg.steamd")
        }
        getOutputDir().convention(
            project.layout.buildDirectory.dir(
                "generated/source/steamd/main/java/in/dragonbra/javasteam"
            )
        )
    }

    @TaskAction
    fun generate() {
        val pkg = getInputPkg().get()
        val inputFile = getInputFile().get().asFile
        val outputDir = getOutputDir().get().asFile

        val buffer = IOUtils.toString(FileInputStream(inputFile), "utf-8")
        val tokens = LanguageParser.tokenizeString(buffer, inputFile.name)

        val root = TokenAnalyzer.analyze(tokens, inputFile.parent)

        val enums = root.childNodes.filterIsInstance<EnumNode>().toList()
        val classes = root.childNodes.filterIsInstance<ClassNode>().toList()

        val flagEnums = hashSetOf<String>()

        FileUtils.deleteDirectory(outputDir)

        println("Steam Language Gen: ${enums.size} enums to process")
        enums.forEach { enum ->
            JavaGen(
                node = enum,
                pkg = "$pkg.enums",
                destination = File(outputDir, "enums"),
                flagEnums = flagEnums
            ).run {
                emit()
                flush()
                close()
            }
        }

        println("Steam Language Gen: ${classes.size} classes to process")
        classes.forEach { clazz ->
            JavaGen(
                node = clazz,
                pkg = "$pkg.generated",
                destination = File(outputDir, "generated"),
                flagEnums = flagEnums
            ).run {
                emit()
                flush()
                close()
            }
        }
    }
}
