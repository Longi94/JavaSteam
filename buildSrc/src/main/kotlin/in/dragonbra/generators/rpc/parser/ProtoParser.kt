package `in`.dragonbra.generators.rpc.parser

import com.squareup.kotlinpoet.*
import `in`.dragonbra.generators.rpc.RpcGenTask
import java.io.File
import java.util.*

class ProtoParser(private val outputDir: File) {

    private companion object {
        private const val RPC_PACKAGE = "in.dragonbra.javasteam.rpc"
        private const val INTERFACE_PACKAGE = "$RPC_PACKAGE.interfaces"

        private val suppressAnnotation = AnnotationSpec
            .builder(Suppress::class)
            .addMember("%S", "KDocUnresolvedReference") // IntelliJ's seems to get confused with canonical names
            .addMember("%S", "RedundantVisibilityModifier") // KotlinPoet is an explicit API generator
            .addMember("%S", "unused") // All methods could be used.
            .addMember("%S", "FunctionName") // Service messages might be case-sensitive, preserve it.
            .build()

        private fun kDocReturns(requestClassName: ClassName, returnClassName: ClassName): String = """
                |@param request The request.
                |@see [${requestClassName.simpleName}]
                |@returns [${returnClassName.simpleName}]
                """.trimMargin()
    }

    /**
     * Open a .proto file and find all service interfaces.
     * Then grab the name of the RPC interface name and everything between the curly braces
     * Then loop through all RPC interface methods, destructuring them to name, type, and response and put them in a list.
     * Collect the items into a [Service] and pass it off to [buildInterface]
     */
    fun parseFile(file: File) {
        Regex("""service\s+(\w+)\s*\{([^}]*)}""")
            .findAll(file.readText())
            .forEach { serviceMatch ->
                val serviceMethods = mutableListOf<ServiceMethod>() // Method list

                val serviceName = serviceMatch.groupValues[1]
                val methodsContent = serviceMatch.groupValues[2]

                Regex("""rpc\s+(\w+)\s*\((.*?)\)\s*returns\s*\((.*?)\);""")
                    .findAll(methodsContent)
                    .forEach { methodMatch ->
                        val (methodName, requestType, responseType) = methodMatch.destructured
                        val request = requestType.trim().replace(".", "")
                        val response = responseType.trim().replace(".", "")

                        ServiceMethod(methodName, request, response).also(serviceMethods::add)
                    }

                Service(serviceName, serviceMethods).also { service ->
                    println("[${file.name}] - found \"${service.name}\", which has ${service.methods.size} methods")

                    buildInterface(file, service)
                }
            }
    }

    /**
     * Transforms the .proto file into an import statement.
     * Also handle edge cases if they are discovered.
     *
     * Example: steammessages_contentsystem.steamclient.proto to SteammessagesContentsystemSteamclient
     */
    private fun transformProtoFileName(protoFileName: String): String {
        // Edge cases
        if (protoFileName == "steammessages_remoteclient_service.steamclient.proto") {
            return "SteammessagesRemoteclientServiceMessages"
        }

        return protoFileName
            .removeSuffix(".proto")
            .split("[._]".toRegex())
            .joinToString("") { str ->
                str.replaceFirstChar { char ->
                    if (char.isLowerCase()) {
                        char.titlecase(Locale.getDefault())
                    } else {
                        char.toString()
                    }
                }
            }
    }

    /**
     * Build the [Service] to an interface with all known RPC methods.
     */
    private fun buildInterface(file: File, service: Service) {
        // Interface Builder
        val iBuilder = TypeSpec
            .interfaceBuilder("I${service.name}")
            .addAnnotation(suppressAnnotation)
            .addKdoc(RpcGenTask.kDocClass)

        // Iterate over found 'rpc' methods
        val protoFileName = transformProtoFileName(file.name)
        service.methods.forEach { method ->
            val requestClassName = ClassName(
                packageName = "in.dragonbra.javasteam.protobufs.steamclient.$protoFileName",
                method.requestType
            )

            // Make a method
            val funBuilder = FunSpec
                .builder(method.methodName)
                .addModifiers(KModifier.ABSTRACT)
                .addParameter("request", requestClassName)

            // Add the appropriate return class
            val returnPackageName = if (method.responseType == "NoResponse") {
                "SteammessagesUnifiedBaseSteamclient"
            } else {
                protoFileName
            }
            val returnClassName = ClassName(
                packageName = "in.dragonbra.javasteam.protobufs.steamclient.$returnPackageName",
                method.responseType
            )

            // Add method kDoc
            val kDoc = kDocReturns(requestClassName, returnClassName)
            funBuilder.addKdoc(kDoc)
                .returns(returnClassName)

            // Add the function to the interface class.
            iBuilder.addFunction(funBuilder.build())
        }

        // Build everything together and write it
        FileSpec.builder(INTERFACE_PACKAGE, "I${service.name}")
            .addType(iBuilder.build())
            .build()
            .writeTo(outputDir)
    }
}
