package `in`.dragonbra.generators.versions

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.jvm.jvmStatic
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class VersionGenTask : DefaultTask() {

    private val `package` = "in.dragonbra.javasteam.util"

    private val outputDir = File(
        project.layout.buildDirectory.get().asFile,
        "generated/source/javasteam/main/java/${`package`}"
    )

    @TaskAction
    fun generate() {
        val className = "Versions"

        // Make the 'getVersion()' variable
        val versionProperty = PropertySpec.builder("version", String::class)
            .jvmStatic()
            .addModifiers(KModifier.PUBLIC)
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("return %S", project.version.toString())
                    .build()
            )
            .build()

        // Make the companion object
        val companionObject = TypeSpec.companionObjectBuilder()
            .addProperty(versionProperty)
            .build()

        // Make the actual class file
        val classFile = TypeSpec.classBuilder(className)
            .addType(companionObject)
            .build()

        // Build everything together
        val file = FileSpec.builder(`package`, className)
            .addType(classFile)
            .build()

        // Write the file
        file.writeTo(outputDir)
    }
}
