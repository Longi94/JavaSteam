package `in`.dragonbra.generators.rpc.parser

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import `in`.dragonbra.generators.rpc.RpcGenTask
import java.io.File
import java.util.*

class ProtoParser(private val outputDir: File) {

    private companion object {
        private const val RPC_PACKAGE = "in.dragonbra.javasteam.rpc"

        private val suppressAnnotation = AnnotationSpec
            .builder(Suppress::class)
            .addMember("%S", "KDocUnresolvedReference") // IntelliJ's seems to get confused with canonical names
            .addMember("%S", "RedundantVisibilityModifier") // KotlinPoet is an explicit API generator
            .addMember("%S", "unused") // All methods could be used.
            .build()

        private val classAsyncJobSingle = ClassName(
            "in.dragonbra.javasteam.types",
            "AsyncJobSingle"
        )
        private val classServiceMethodResponse = ClassName(
            "in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback",
            "ServiceMethodResponse"
        )

        private val kDocNoResponse = """|No return value.""".trimMargin()
        private fun kDocReturns(requestClassName: ClassName, returnClassName: ClassName): String = """
                |@param request The request.
                |@see [${requestClassName.simpleName}]
                |@returns [${returnClassName.canonicalName}]
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
                    buildInterface(file, service)
                    buildClass(file, service)
                }
            }
    }

    /**
     * Transforms the .proto file into an import statement.
     * Also handles `some` edge cases
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
                .builder(method.methodName.replaceFirstChar { it.lowercase(Locale.getDefault()) })
                .addModifiers(KModifier.ABSTRACT)
                .addParameter("request", requestClassName)

            // Add method kDoc
            // Add `AsyncJobSingle<ServiceMethodResponse>` if there is a response
            if (method.responseType == "NoResponse") {
                funBuilder.addKdoc(kDocNoResponse)
            } else {
                val returnClassName = ClassName(
                    packageName = "in.dragonbra.javasteam.protobufs.steamclient.$protoFileName",
                    method.responseType
                )
                val kDoc = kDocReturns(requestClassName, returnClassName)
                funBuilder.addKdoc(kDoc)
                    .returns(classAsyncJobSingle.parameterizedBy(classServiceMethodResponse))
            }

            // Add the function to the interface class.
            iBuilder.addFunction(funBuilder.build())
        }

        // Build everything together and write it
        FileSpec.builder("$RPC_PACKAGE.interfaces", "I${service.name}")
            .addType(iBuilder.build())
            .build()
            .writeTo(outputDir)
    }

    /**
     * Build the [Service] to a class with all known RPC methods.
     */
    private fun buildClass(file: File, service: Service) {
        // Class Builder
        val cBuilder = TypeSpec
            .classBuilder(service.name)
            .addAnnotation(suppressAnnotation)
            .addKdoc(RpcGenTask.kDocClass)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        name = "steamUnifiedMessages",
                        type = ClassName(
                            packageName = "in.dragonbra.javasteam.steam.handlers.steamunifiedmessages",
                            "SteamUnifiedMessages"
                        )
                    )
                    .build()
            )
            .addSuperclassConstructorParameter("steamUnifiedMessages")
            .superclass(
                ClassName(
                    packageName = "in.dragonbra.javasteam.steam.handlers.steamunifiedmessages",
                    "UnifiedService"
                )
            )
            .addSuperinterface(
                ClassName(
                    packageName = "in.dragonbra.javasteam.rpc.interfaces",
                    "I${service.name}"
                )
            )

        // Iterate over found 'rpc' methods.
        val protoFileName = transformProtoFileName(file.name)
        service.methods.forEach { method ->
            val requestClassName = ClassName(
                packageName = "in.dragonbra.javasteam.protobufs.steamclient.$protoFileName",
                method.requestType
            )

            // Make a method
            val funBuilder = FunSpec
                .builder(method.methodName.replaceFirstChar { it.lowercase(Locale.getDefault()) })
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("request", requestClassName)

            // Add method kDoc
            // Add `AsyncJobSingle<ServiceMethodResponse>` if there is a response
            if (method.responseType == "NoResponse") {
                funBuilder.addKdoc(kDocNoResponse)
                    .addStatement("sendNotification(request, \"${method.methodName}\")")
            } else {
                val returnClassName = ClassName(
                    packageName = "in.dragonbra.javasteam.protobufs.steamclient.$protoFileName",
                    method.responseType
                )
                val kDoc = kDocReturns(requestClassName, returnClassName)
                funBuilder.addKdoc(kDoc)
                    .returns(classAsyncJobSingle.parameterizedBy(classServiceMethodResponse))
                    .addStatement("return sendMessage(request, \"${method.methodName}\")")
            }

            cBuilder.addFunction(funBuilder.build())
        }

        // Build everything together and write it
        FileSpec.builder("$RPC_PACKAGE.service", service.name)
            .addType(cBuilder.build())
            .build()
            .writeTo(outputDir)
    }
}
