package `in`.dragonbra.generators.rpc.parser

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import java.util.*

class ProtoParser(private val outputDir: File) {

    private companion object {
        private const val RPC_PACKAGE = "in.dragonbra.javasteam.rpc"
        private const val SERVICE_PACKAGE = "${RPC_PACKAGE}.service"

        private val suppressAnnotation = AnnotationSpec
            .builder(Suppress::class)
            .addMember("%S", "KDocUnresolvedReference") // IntelliJ's seems to get confused with canonical names
            .addMember("%S", "RedundantVisibilityModifier") // KotlinPoet is an explicit API generator
            .addMember("%S", "unused") // All methods could be used.
            .build()
    }

    /**
     * Open a .proto file and find all service interfaces.
     * Then grab the name of the RPC interface name and everything between the curly braces
     * Then loop through all RPC interface methods, destructuring them to name, type, and response and put them in a list.
     * Collect the items into a [Service] and pass it off to [buildClass]
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

                    buildClass(file, service)
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
     * Build the [Service] to a class with all known RPC methods.
     */
    private fun buildClass(file: File, service: Service) {
        val protoFileName = transformProtoFileName(file.name)

        // Class Builder
        val steamUnifiedMessagesClassName = ClassName(
            "in.dragonbra.javasteam.steam.handlers.steamunifiedmessages",
            "SteamUnifiedMessages"
        )
        val cBuilder = TypeSpec
            .classBuilder(service.name)
            .addAnnotation(suppressAnnotation)
            .addKdoc("This class is auto-generated")
            .superclass(
                ClassName(
                    packageName = "in.dragonbra.javasteam.steam.handlers.steamunifiedmessages",
                    "UnifiedService"
                )
            )
            .addFunction(
                FunSpec.constructorBuilder()
                    .addParameter("unifiedMessages", steamUnifiedMessagesClassName)
                    .callSuperConstructor("unifiedMessages")
                    .build()
            )

        // override val serviceName: String
        val serviceNameGetter = FunSpec.getterBuilder().apply {
            addStatement("return %S", service.name)
        }.build()
        val serviceNameProperty = PropertySpec.builder("serviceName", String::class).apply {
            addModifiers(KModifier.OVERRIDE)
            getter(serviceNameGetter)
        }.build()
        cBuilder.addProperty(serviceNameProperty)

        // Loop through the methods and sort them between a response and notification list.
        var numNotification = 0 // Ehhh. This stops an empty 'when' block. Could be better.
        var numResponse = 0 // Ehhh. This stops an empty 'when' block. Could be better.
        val responseBlock = CodeBlock.builder()
        val notificationBlock = CodeBlock.builder()
        responseBlock.beginControlFlow("when (methodName)")
        notificationBlock.beginControlFlow("when (methodName)")
        service.methods.forEach { method ->
            if (method.responseType != "NoResponse") {
                // HAS Response
                numResponse++
                val className = ClassName(
                    packageName = "in.dragonbra.javasteam.protobufs.steamclient.$protoFileName",
                    method.responseType
                )
                responseBlock.addStatement(
                    "\"${method.methodName}\" -> unifiedMessages!!.handleResponseMsg<%T.Builder>(\n%T::class.java,\npacketMsg\n)",
                    className,
                    className
                )
            } else {
                // NO Response
                numNotification++
                val className = ClassName(
                    packageName = "in.dragonbra.javasteam.protobufs.steamclient.$protoFileName",
                    method.requestType
                )
                notificationBlock.addStatement(
                    "\"${method.methodName}\" -> unifiedMessages!!.handleNotificationMsg<%T.Builder>(\n%T::class.java,\npacketMsg\n)",
                    className,
                    className
                )
            }
        }

        responseBlock.endControlFlow()
        notificationBlock.endControlFlow()

        // override fun handleResponseMsg(methodName: String, packetMsg: PacketClientMsgProtobuf)
        val funcHandleResponseMsg = FunSpec.builder("handleResponseMsg").apply {
            addModifiers(KModifier.OVERRIDE)
            addParameter("methodName", String::class)
            addParameter("packetMsg", ClassName("in.dragonbra.javasteam.base", "PacketClientMsgProtobuf"))
            if (numResponse > 0) {
                addCode(responseBlock.build())
            }
        }.build()
        cBuilder.addFunction(funcHandleResponseMsg)

        // override fun handleNotificationMsg(methodName: String, packetMsg: PacketClientMsgProtobuf)
        val funcHandleNotificationMsg = FunSpec.builder("handleNotificationMsg").apply {
            addModifiers(KModifier.OVERRIDE)
            addParameter("methodName", String::class)
            addParameter("packetMsg", ClassName("in.dragonbra.javasteam.base", "PacketClientMsgProtobuf"))
            if (numNotification > 0) {
                addCode(notificationBlock.build())
            }
        }.build()
        cBuilder.addFunction(funcHandleNotificationMsg)

        // Public Methods
        service.methods.forEach { method ->
            val funcBuilder =
                FunSpec.builder(method.methodName.replaceFirstChar { it.lowercase(Locale.getDefault()) })
                    .addModifiers(KModifier.PUBLIC)
                    .addKdoc(
                        """
                    |@param request The request.
                    |@see [${method.requestType}]
                    |@returns [AsyncJobSingle]<[ServiceMethodResponse]<[${method.responseType}]>>
                """.trimMargin() // wow
                    )
                    .addParameter(
                        "request",
                        ClassName(
                            packageName = "in.dragonbra.javasteam.protobufs.steamclient.$protoFileName",
                            method.requestType
                        )
                    )

            if (method.responseType != "NoResponse") {
                funcBuilder.returns(
                    ClassName(
                        packageName = "in.dragonbra.javasteam.types",
                        "AsyncJobSingle"
                    ).parameterizedBy(
                        ClassName(
                            packageName = "in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback",
                            "ServiceMethodResponse"
                        ).parameterizedBy(
                            ClassName.bestGuess("in.dragonbra.javasteam.protobufs.steamclient.$protoFileName.${method.responseType}.Builder")
                        )
                    )
                )
                funcBuilder.addStatement(
                    format = "return unifiedMessages!!.sendMessage(\n%T.Builder::class.java,\n%S,\nrequest\n)",
                    ClassName(
                        packageName = "in.dragonbra.javasteam.protobufs.steamclient.$protoFileName",
                        method.responseType
                    ),
                    "${service.name}.${method.methodName}#1"
                )
            } else {
                funcBuilder.addStatement(
                    format = "unifiedMessages!!.sendNotification<%T.Builder>(\n%S,\nrequest\n)",
                    ClassName(
                        packageName = "in.dragonbra.javasteam.protobufs.steamclient.$protoFileName",
                        method.requestType
                    ),
                    "${service.name}.${method.methodName}#1"
                )
            }

            cBuilder.addFunction(funcBuilder.build())
        }

        // Build everything together and write it
        FileSpec.builder(SERVICE_PACKAGE, service.name)
            .addType(cBuilder.build())
            .build()
            .writeTo(outputDir)
    }
}
