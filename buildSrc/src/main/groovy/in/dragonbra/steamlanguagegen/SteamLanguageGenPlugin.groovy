package in.dragonbra.steamlanguagegen

import in.dragonbra.steamlanguagegen.generator.JavaGen
import in.dragonbra.steamlanguagegen.parser.LanguageParser
import in.dragonbra.steamlanguagegen.parser.node.ClassNode
import in.dragonbra.steamlanguagegen.parser.node.EnumNode
import in.dragonbra.steamlanguagegen.parser.node.Node
import in.dragonbra.steamlanguagegen.parser.token.TokenAnalyzer
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.util.stream.Collectors

class SteamLanguageGenPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.task('generateSteamLanguage') {
            doLast {
                def dir = 'src/main/steamd/in/dragonbra/javasteam'
                File file = project.file("$dir/steammsg.steamd")
                def _package = 'in.dragonbra.javasteam'
                def destination = 'generated/source/steamd/main/java/in/dragonbra/javasteam'

                def tokens = LanguageParser.tokenizeString(IOUtils.toString(new FileInputStream(file), 'utf-8'), 'steammsg.steamd')
                def root = TokenAnalyzer.analyze(tokens, dir)

                def enums = root.childNodes.stream().filter({ child -> child instanceof EnumNode }).collect(Collectors.toList())
                def classes = root.childNodes.stream().filter({ child -> child instanceof ClassNode }).collect(Collectors.toList())

                Set<String> flagEnums = new HashSet<>()

                FileUtils.deleteDirectory(new File(project.buildDir, destination))

                enums.each { Node _enum ->
                    def javaGen = new JavaGen(_enum, "${_package}.enums", new File(project.buildDir, "$destination/enums"), flagEnums);
                    javaGen.emit()
                    javaGen.flush()
                    javaGen.close()
                }

                classes.each { Node _class ->
                    def javaGen = new JavaGen(_class, "${_package}.generated", new File(project.buildDir, "$destination/generated"), flagEnums);
                    javaGen.emit()
                    javaGen.flush()
                    javaGen.close()
                }
            }
        }
    }
}