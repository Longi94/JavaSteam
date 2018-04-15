package in.dragonbra.steamlanguagegen

import in.dragonbra.steamlanguagegen.generator.JavaGen
import in.dragonbra.steamlanguagegen.parser.LanguageParser
import in.dragonbra.steamlanguagegen.parser.node.ClassNode
import in.dragonbra.steamlanguagegen.parser.node.EnumNode
import in.dragonbra.steamlanguagegen.parser.node.Node
import in.dragonbra.steamlanguagegen.parser.token.TokenAnalyzer
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import java.util.stream.Collectors

class SteamLanguageGenTask extends DefaultTask {

    private File outputDir = new File(project.buildDir, 'generated/source/steamd/main/java/in/dragonbra/javasteam')

    private File inputFile = project.file('src/main/steamd/in/dragonbra/javasteam/steammsg.steamd')

    private String _package = 'in.dragonbra.javasteam'

    @OutputDirectory
    File getOutputDir() {
        return outputDir
    }

    @SuppressWarnings('GroovyUnusedDeclaration')
    @Input
    File getInputFile() {
        return inputFile
    }

    @Input
    String get_package() {
        return _package
    }

    @SuppressWarnings('GroovyUnusedDeclaration')
    @TaskAction
    def generate() {
        def tokens = LanguageParser.tokenizeString(IOUtils.toString(new FileInputStream(inputFile), 'utf-8'), inputFile.getName())
        def root = TokenAnalyzer.analyze(tokens, inputFile.getParent())

        def enums = root.childNodes.stream().filter({ child -> child instanceof EnumNode }).collect(Collectors.<Node>toList())
        def classes = root.childNodes.stream().filter({ child -> child instanceof ClassNode }).collect(Collectors.<Node>toList())

        Set<String> flagEnums = new HashSet<>()

        FileUtils.deleteDirectory(outputDir)

        enums.each { Node _enum ->
            def javaGen = new JavaGen(_enum, "${_package}.enums", new File(outputDir, 'enums'), flagEnums)
            javaGen.emit()
            javaGen.flush()
            javaGen.close()
        }

        classes.each { Node _class ->
            def javaGen = new JavaGen(_class, "${_package}.generated", new File(outputDir, 'generated'), flagEnums)
            javaGen.emit()
            javaGen.flush()
            javaGen.close()
        }
    }
}