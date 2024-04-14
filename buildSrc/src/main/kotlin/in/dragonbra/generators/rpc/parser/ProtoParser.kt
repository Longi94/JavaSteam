package `in`.dragonbra.generators.rpc.parser

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import `in`.dragonbra.generators.rpc.RpcGenTask
import java.io.File
import java.util.*

class ProtoParser(private val outputDir: File) {

    companion object {
        private const val INTERFACE_PACKAGE = "in.dragonbra.javasteam.rpc.interfaces"
        private const val CLASS_PACKAGE = "in.dragonbra.javasteam.rpc.service"

        private val suppressAnnotation = AnnotationSpec.builder(Suppress::class)
            .addMember("%S", "KDocUnresolvedReference") // IntelliJ's seems to get confused with canonical names
            .addMember("%S", "RedundantVisibilityModifier") // KotlinPoet is an explicit API generator
            .addMember("%S", "unused") // All methods could be used.
            .build()

        val classAsyncJobSingle = ClassName(
            "in.dragonbra.javasteam.types",
            "AsyncJobSingle"
        )
        val classServiceMethodResponse = ClassName(
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
        val protoContent = file.readText()

        val serviceRegex = Regex("""service\s+(\w+)\s*\{([^}]*)}""")
        serviceRegex.findAll(protoContent).forEach { serviceMatch ->
            val serviceName = serviceMatch.groupValues[1]
            val methodsContent = serviceMatch.groupValues[2]

            val serviceMethods = mutableListOf<ServiceMethod>() // Method list

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
            buildClass(file, service)
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

    /**
     * Build the [Service] to an interface with all known RPC methods.
     */
    private fun buildInterface(file: File, service: Service) {
        val protoFileName = transformProtoFileName(file.name)
        val interfaceName = "I${service.name}"

        // Interface Builder
        val iBuilder = TypeSpec.interfaceBuilder(interfaceName)
            .addAnnotation(suppressAnnotation)
            .addKdoc(RpcGenTask.kDocClass)

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

            // Make a method
            val funBuilder = FunSpec.builder(methodName)
                .addModifiers(KModifier.ABSTRACT)
                .addParameter("request", requestClassName)

            // Add method kDoc
            if (method.responseType == "NoResponse") {
                funBuilder.addKdoc(kDocNoResponse)
            } else {
                kDocReturns(requestClassName, returnClassName).also(funBuilder::addKdoc)
            }

            // Creates: AsyncJobSingle<ServiceMethodResponse>
            if (method.responseType != "NoResponse") {
                val requestReturnType = classAsyncJobSingle.parameterizedBy(classServiceMethodResponse)
                funBuilder.returns(requestReturnType)
            }

            // Add the function to the interface class.
            iBuilder.addFunction(funBuilder.build())
        }

        // Build everything together
        val fileBuilder = FileSpec.builder(INTERFACE_PACKAGE, interfaceName)
            .addType(iBuilder.build())
            .build()

        // Write the file
        fileBuilder.writeTo(outputDir)
    }

    /**
     * Build the [Service] to a class with all known RPC methods.
     */
    private fun buildClass(file: File, service: Service) {
        val protoFileName = transformProtoFileName(file.name)
        val className = service.name

        val unifiedServiceClass = ClassName(
            "in.dragonbra.javasteam.steam.handlers.steamunifiedmessages",
            "UnifiedService"
        )
        val unifiedMessageClass = ClassName(
            "in.dragonbra.javasteam.steam.handlers.steamunifiedmessages",
            "SteamUnifiedMessages"
        )
        val interfaceType = ClassName("in.dragonbra.javasteam.rpc.interfaces", "I$className")

        // Class Builder
        val cBuilder = TypeSpec.classBuilder(className)
            .addAnnotation(suppressAnnotation)
            .addKdoc(RpcGenTask.kDocClass)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("steamUnifiedMessages", unifiedMessageClass)
                    .build()
            )
            .addSuperclassConstructorParameter("steamUnifiedMessages")
            .superclass(unifiedServiceClass)
            .addSuperinterface(interfaceType)

        // Iterate over found 'rpc' methods.
        service.methods.forEach { method ->
            val methodName = method.methodName
            val requestClassName = ClassName(
                "in.dragonbra.javasteam.protobufs.steamclient.$protoFileName",
                method.requestType
            )
            val returnClassName = ClassName(
                "in.dragonbra.javasteam.protobufs.steamclient.$protoFileName",
                method.responseType
            )

            // Make a method
            val funBuilder = FunSpec.builder(methodName.replaceFirstChar { it.lowercase(Locale.getDefault()) })
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("request", requestClassName)

            // Add method kDoc
            if (method.responseType == "NoResponse") {
                funBuilder.addKdoc(kDocNoResponse)
            } else {
                kDocReturns(requestClassName, returnClassName).also(funBuilder::addKdoc)
            }

            // Creates: AsyncJobSingle<ServiceMethodResponse>
            if (method.responseType != "NoResponse") {
                val requestReturnType = classAsyncJobSingle.parameterizedBy(classServiceMethodResponse)
                funBuilder.returns(requestReturnType)
                funBuilder.addStatement("return sendMessage(request, \"$methodName\")")
            } else {
                funBuilder.addStatement("sendNotification(request, \"$methodName\")")
            }

            cBuilder.addFunction(funBuilder.build())
        }

        // Build everything together
        val fileBuilder = FileSpec.builder(CLASS_PACKAGE, className)
            .addType(cBuilder.build())
            .build()

        // Write the file
        fileBuilder.writeTo(outputDir)
    }
}
