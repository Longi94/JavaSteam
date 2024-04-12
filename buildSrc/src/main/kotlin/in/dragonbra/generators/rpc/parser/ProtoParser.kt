package `in`.dragonbra.generators.rpc.parser

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import java.util.*

class ProtoParser(private val outputDir: File) {

    companion object {
        private const val PACKAGE = "in.dragonbra.javasteam.rpc.interfaces"
        private const val KDOC_AUTHOR = "Lossy"
        private const val KDOC_DATE = "2024-04-10"
    }

    fun parseFile(file: File) {
        val protoContent = file.readText()

        val serviceRegex = Regex("""service\s+(\w+)\s*\{([^}]*)}""")
        serviceRegex.findAll(protoContent).forEach { serviceMatch ->
            val serviceName = serviceMatch.groupValues[1]
            val methodsContent = serviceMatch.groupValues[2]

            val serviceMethods = mutableListOf<ServiceMethod>()
            val serviceMethodRegex = Regex("""rpc\s+(\w+)\s*\((.*?)\)\s*returns\s*\((.*?)\);""")
            serviceMethodRegex.findAll(methodsContent).forEach { methodMatch ->
                val (methodName, requestType, responseType) = methodMatch.destructured
                serviceMethods.add(
                    ServiceMethod(
                        methodName,
                        requestType.trim().replace(".", ""),
                        responseType.trim().replace(".", "")
                    )
                )
            }

            val service = Service(serviceName, serviceMethods)

            buildInterface(file, service)
        }
    }

    /**
     * Transforms the .proto file into an import statement. Also handles edge cases
     *
     * Example:
     * steammessages_contentsystem.steamclient.proto to SteammessagesContentsystemSteamclient
     */
    private fun transformProtoFileName(protoFileName: String): String {
        // Edge cases
        if (protoFileName == "steammessages_remoteclient_service.steamclient.proto") {
            return "SteammessagesRemoteclientServiceMessages"
        }

        val regex = Regex("[._]")
        val importName = protoFileName
            .removeSuffix(".proto")
            .split(regex)
            .joinToString("") { str ->
                str.replaceFirstChar { char ->
                    if (char.isLowerCase()) {
                        char.titlecase(Locale.getDefault())
                    } else {
                        char.toString()
                    }
                }
            }

        return importName
    }

    private fun buildInterface(file: File, service: Service) {
        val protoFileName = transformProtoFileName(file.name)
        val interfaceName = "I${service.name}"

        // kDoc
        val kDoc = """
            |@author $KDOC_AUTHOR
            |@since $KDOC_DATE
            """
            .trimMargin()

        // Suppress
        val suppressAnnotation = AnnotationSpec.builder(Suppress::class)
            .addMember("%S", "KDocUnresolvedReference") // IntelliJ's seems to get confused with canonical names
            .addMember("%S", "RedundantVisibilityModifier") // KotlinPoet is an explicit API generator
            .addMember("%S", "unused") // All methods could be used.
            .build()

        // Interface Builder
        val iBuilder = TypeSpec.interfaceBuilder(interfaceName)
            .addAnnotation(suppressAnnotation)
            .addKdoc(kDoc)

        // Iterate over found 'rpc' methods
        service.methods.forEach { method ->
            val methodName = method.methodName.replaceFirstChar { it.lowercase(Locale.getDefault()) }
            val requestClassName = ClassName(
                "in.dragonbra.javasteam.protobufs.steamclient.$protoFileName",
                method.requestType
            )

            val returnClassName = ClassName(
                "in.dragonbra.javasteam.protobufs.steamclient.$protoFileName",
                method.responseType
            )

            val kDocReturns = """
                |@param request The request.
                |@see [${requestClassName.simpleName}]
                |@returns [${returnClassName.canonicalName}]
                """
                .trimMargin()

            val kDocVoid = """
                |No return value.
                """
                .trimMargin()

            val funBuilder = FunSpec.builder(methodName)
                .addModifiers(KModifier.ABSTRACT)
                .addParameter("request", requestClassName)

            if (method.responseType == "NoResponse") {
                funBuilder.addKdoc(kDocVoid)
            } else {
                funBuilder.addKdoc(kDocReturns)
            }

            if (method.responseType != "NoResponse") {
                // Creates: AsyncJobSingle<ServiceMethodResponse>
                val requestReturnType = ClassName(
                    "in.dragonbra.javasteam.types",
                    "AsyncJobSingle"
                ).parameterizedBy(
                    ClassName(
                        "in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback",
                        "ServiceMethodResponse"
                    )
                )

                funBuilder.returns(requestReturnType)
            }

            iBuilder.addFunction(funBuilder.build())
        }

        // Build everything together
        val fileBuilder = FileSpec.builder(PACKAGE, interfaceName)
            .addType(iBuilder.build())
            .build()

        // Write the file
        fileBuilder.writeTo(outputDir)
    }
}
