package `in`.dragonbra.generators.rpc.parser

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import java.util.*

class ProtoParser(private val outputDir: File, private val protoRoot: File) {

    private companion object {
        private const val RPC_PACKAGE = "in.dragonbra.javasteam.rpc"
        private const val SERVICE_PACKAGE = "${RPC_PACKAGE}.service"
        private const val WEBUI_SERVICE_PACKAGE = "${SERVICE_PACKAGE}.webui"

        private val suppressAnnotation = AnnotationSpec
            .builder(Suppress::class)
            .addMember("%S", "KDocUnresolvedReference") // IntelliJ's seems to get confused with canonical names
            .addMember("%S", "RemoveRedundantQualifierName") // Full Qualifier names are fine
            .addMember("%S", "RedundantVisibilityModifier") // KotlinPoet is an explicit API generator
            .addMember("%S", "unused") // All methods could be used.
            .build()

        private val topLevelMessageRegex = Regex("""(?m)^message\s+(\w+)""")
        private val importRegex = Regex("""(?m)^import\s+(?:public\s+)?"([^"]+)"\s*;""")
    }

    // Top-level message names declared by each .proto file.
    private val messagesByFile = mutableMapOf<File, Set<String>>()

    // Files imported by each .proto file, limited to the ones that live under [protoRoot].
    private val importsByFile = mutableMapOf<File, List<File>>()

    private fun messagesOf(file: File): Set<String> = messagesByFile.getOrPut(file) {
        topLevelMessageRegex.findAll(file.readText()).map { it.groupValues[1] }.toSet()
    }

    private fun importsOf(file: File): List<File> = importsByFile.getOrPut(file) {
        importRegex.findAll(file.readText())
            .map { File(protoRoot, it.groupValues[1]).canonicalFile }
            .filter { it.isFile }
            .toList()
    }

    /**
     * Find the .proto file that declares [type], starting at [file] and then walking its imports breadth first.
     * A service can use messages declared in an imported file (e.g. webui services and common.proto),
     * and the generated Java class is nested in the outer class of the declaring file.
     * Falls back to [file] if no declaration is found.
     */
    private fun findDeclaringFile(file: File, type: String): File {
        val start = file.canonicalFile
        val visited = mutableSetOf(start)
        val queue = ArrayDeque<File>().apply { add(start) }

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (type in messagesOf(current)) {
                return current
            }
            importsOf(current).filter(visited::add).forEach(queue::add)
        }

        return start
    }

    /**
     * The generated protobuf class of [type], as referenced from [file].
     */
    private fun messageClassName(file: File, type: String): ClassName {
        val declaringFile = findDeclaringFile(file, type)
        return ClassName(
            packageName = "in.dragonbra.javasteam.protobufs." +
                "${declaringFile.parentFile.name}.${transformProtoFileName(declaringFile.name)}",
            type
        )
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
        val parentPathName = file.parentFile.name

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
                val className = messageClassName(file, method.responseType)
                responseBlock.addStatement(
                    "\"${method.methodName}\" -> postResponseMsg<%T.Builder>(\n%T::class.java,\npacketMsg\n)",
                    className,
                    className
                )
            } else {
                // NO Response
                numNotification++
                val className = messageClassName(file, method.requestType)
                notificationBlock.addStatement(
                    "\"${method.methodName}\" -> postNotificationMsg<%T.Builder>(\n%T::class.java,\npacketMsg\n)",
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

        val asyncJobSingleClass = ClassName(
            packageName = "in.dragonbra.javasteam.types",
            "AsyncJobSingle"
        )
        val serviceMethodResponseClass = ClassName(
            packageName = "in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback",
            "ServiceMethodResponse"
        )
        val noResponseClass = ClassName(
            packageName = "in.dragonbra.javasteam.protobufs.steamclient.SteammessagesUnifiedBaseSteamclient",
            "NoResponse"
        )

        // Public Methods
        service.methods.forEach { method ->
            val funcBuilder =
                FunSpec.builder(method.methodName.replaceFirstChar { it.lowercase(Locale.getDefault()) })
                    .addModifiers(KModifier.PUBLIC)
                    .addParameter(
                        "request",
                        messageClassName(file, method.requestType)
                    )

            if (method.responseType != "NoResponse") {
                // I can't find a way to suppress or fix dokka warnings for this.
                //funcBuilder.addKdoc(
                //    "@param request The request.\n@see [${method.requestType}]\n@returns [%T]<[%T]<[${method.responseType}]>>\n",
                //    asyncJobSingleClass,
                //    serviceMethodResponseClass
                //)
                funcBuilder.returns(
                    asyncJobSingleClass.parameterizedBy(
                        serviceMethodResponseClass.parameterizedBy(
                            messageClassName(file, method.responseType).nestedClass("Builder")
                        )
                    )
                )
                funcBuilder.addStatement(
                    format = "return unifiedMessages!!.sendMessage(\n%T.Builder::class.java,\n%S,\nrequest\n)",
                    messageClassName(file, method.responseType),
                    "${service.name}.${method.methodName}#1"
                )
            } else {
                funcBuilder.addKdoc(
                    "@param request The request.\n@returns [%T]<[%T]<[%T]>>\n",
                    asyncJobSingleClass,
                    serviceMethodResponseClass,
                    noResponseClass
                )
                funcBuilder.addStatement(
                    format = "unifiedMessages!!.sendNotification<%T.Builder>(\n%S,\nrequest\n)",
                    messageClassName(file, method.requestType),
                    "${service.name}.${method.methodName}#1"
                )
            }

            cBuilder.addFunction(funcBuilder.build())
        }

        // Build everything together and write it
        // Services from .proto files under "webui" go to their own sub-package to
        // avoid clashing with same-named steamclient services (e.g. Store).
        val servicePackage = if (parentPathName == "webui") WEBUI_SERVICE_PACKAGE else SERVICE_PACKAGE
        FileSpec.builder(servicePackage, service.name)
            .addType(cBuilder.build())
            .build()
            .writeTo(outputDir)
    }
}
