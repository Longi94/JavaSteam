package `in`.dragonbra.generators.steamlanguage.generator

import `in`.dragonbra.generators.steamlanguage.parser.node.ClassNode
import `in`.dragonbra.generators.steamlanguage.parser.node.EnumNode
import `in`.dragonbra.generators.steamlanguage.parser.node.Node
import `in`.dragonbra.generators.steamlanguage.parser.node.PropNode
import `in`.dragonbra.generators.steamlanguage.parser.symbol.StrongSymbol
import `in`.dragonbra.generators.steamlanguage.parser.symbol.Symbol
import `in`.dragonbra.generators.steamlanguage.parser.symbol.WeakSymbol
import `in`.dragonbra.generators.util.JavaFileWriter
import java.io.Closeable
import java.io.File
import java.io.Flushable
import java.io.IOException
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashSet

class JavaGen(
    private var node: Node,
    private var pkg: String,
    private var destination: File,
    private var flagEnums: MutableSet<String>
) : Closeable, Flushable {

    private var writer: JavaFileWriter? = null

    companion object {
        private val NUMBER_PATTERN: Pattern = Pattern.compile("^-?[0-9].*?L?")

        private const val DEFAULT_TYPE = "uint"

        private val WEAK_TYPES: Map<String, TypeInfo> = mapOf(
            "byte" to TypeInfo(1, "byte"),
            "short" to TypeInfo(2, "short"),
            "ushort" to TypeInfo(2, "short"),
            "int" to TypeInfo(4, "int"),
            "uint" to TypeInfo(4, "int"),
            "long" to TypeInfo(8, "long"),
            "ulong" to TypeInfo(8, "long")
        )
    }

    @Throws(IOException::class)
    fun emit() {
        if (node is ClassNode && !(node as ClassNode).emit) {
            return
        }

        if (!destination.exists() && !destination.isDirectory() && !destination.mkdirs()) {
            throw IllegalStateException("Could not create folders")
        }

        val file = File(destination, "${node.name}.java")

        writer = JavaFileWriter(file)
        writePackage(pkg)

        writer?.writeln()
        writeImports()

        writer?.writeln()
        writeClass(node)
    }

    @Throws(IOException::class)
    private fun writeImports() {
        if (node is ClassNode) {
            val imports = HashSet<String>()

            imports.add("java.io.IOException")
            imports.add("java.io.InputStream")
            imports.add("java.io.OutputStream")
            imports.add("in.dragonbra.javasteam.util.stream.BinaryReader")
            imports.add("in.dragonbra.javasteam.util.stream.BinaryWriter")

            if ((node as ClassNode).ident != null) {
                if (node.name.contains("MsgGC")) {
                    imports.add("in.dragonbra.javasteam.base.IGCSerializableMessage")
                } else {
                    imports.add("in.dragonbra.javasteam.base.ISteamSerializableMessage")
                    imports.add("in.dragonbra.javasteam.enums.EMsg")
                }
            } else if (node.name.contains("Hdr")) {
                if (node.name.contains("MsgGC")) {
                    imports.add("in.dragonbra.javasteam.base.IGCSerializableHeader")
                } else {
                    imports.add("in.dragonbra.javasteam.base.ISteamSerializableHeader")
                }
            } else {
                imports.add("in.dragonbra.javasteam.base.ISteamSerializable")
            }

            node.childNodes.forEach { child ->
                val prop = child as PropNode
                val typeStr = getType(prop.type)

                if (flagEnums.contains(typeStr)) {
                    imports.add("java.util.EnumSet")
                }

                if ("steamidmarshal" == prop.flags && "long" == typeStr) {
                    imports.add("in.dragonbra.javasteam.types.SteamID")
                } else if ("gameidmarshal" == prop.flags && "long" == typeStr) {
                    imports.add("in.dragonbra.javasteam.types.GameID")
                } else if (prop.flags == "proto") {
                    imports.add("in.dragonbra.javasteam.protobufs.steamclient.SteammessagesBase.CMsgProtoBufHeader")
                } else if (prop.flags == "protomask") {
                    imports.add("in.dragonbra.javasteam.util.MsgUtil")
                }

                if (prop.type is StrongSymbol) {
                    if ((prop.type as StrongSymbol).clazz is EnumNode) {
                        imports.add("in.dragonbra.javasteam.enums.${(prop.type as StrongSymbol).clazz.name}")
                    }
                }
            }

            val sortedImports = imports.map { it }.sorted()
            var currentGroup: String? = null

            sortedImports.forEach { imp ->
                val group = imp.substring(0, imp.indexOf("."))

                if (group != currentGroup) {
                    if (currentGroup != null) {
                        writer?.writeln()
                    }

                    currentGroup = group
                }

                writer?.writeln("import $imp;")
            }
        } else if (node is EnumNode) {
            if ("flags" == (node as EnumNode).flags) {
                writer?.writeln("import java.util.EnumSet;")
            }
        }
    }

    @Throws(IOException::class)
    private fun writePackage(`package`: String) {
        writer?.writeln("package ${`package`};")
    }

    @Throws(IOException::class)
    private fun writeClass(node: Node) {
        if (node is ClassNode && node.emit) {
            writeMessageClass(node)
        } else if (node is EnumNode) {
            writeEnumClass(node)
        }
    }

    @Throws(IOException::class)
    private fun writeMessageClass(node: ClassNode) {
        writeClassDef(node)

        writer?.indent()

        writeClassConstructor(node)
        writeClassProperties(node)
        writeClassIdentity(node)
        writeSetterGetter(node)
        writeSerializationMethods(node)

        writer?.unindent()
        writer?.writeln("}")
    }

    @Throws(IOException::class)
    private fun writeClassDef(node: ClassNode) {
        val parent = if (node.ident != null) {
            if (node.name.contains("MsgGC")) {
                "IGCSerializableMessage"
            } else {
                "ISteamSerializableMessage"
            }
        } else if (node.name.contains("Hdr")) {
            if (node.name.contains("MsgGC")) {
                "IGCSerializableHeader"
            } else {
                "ISteamSerializableHeader"
            }
        } else {
            "ISteamSerializable"
        }

        // TODO why null check
        @Suppress("SENSELESS_COMPARISON")
        if (parent != null) {
            writer?.writeln("public class ${node.name} implements $parent {")
        } else {
            writer?.writeln("public class ${node.name} {")
        }

        writer?.writeln()
    }

    @Throws(IOException::class)
    private fun writeClassIdentity(node: ClassNode) {
        if (node.ident != null) {
            val sIdent = node.ident as StrongSymbol?

            var suppressObsolete = false

            if (sIdent != null) {
                val propNode = sIdent.prop as PropNode?

                if (propNode?.obsolete != null) {
                    suppressObsolete = true
                }
            }

            @Suppress("ControlFlowWithEmptyBody")
            if (suppressObsolete) {
                // TODO: 2018-02-19
            }

            if (node.name.contains("MsgGC")) {
                writer?.writeln("@Override")
                writer?.writeln("public int getEMsg() {")
                writer?.writeln("    return ${getType(node.ident)};")
                writer?.writeln("}")
            } else {
                writer?.writeln("@Override")
                writer?.writeln("public EMsg getEMsg() {")
                writer?.writeln("    return ${getType(node.ident)};")
                writer?.writeln("}")
            }

            writer?.writeln()
        } else if (node.name.contains("Hdr")) {
            if (node.name.contains("MsgGC")) {
                if (node.childNodes.any { childNode -> "msg" == childNode.name }) {
                    writer?.writeln("@Override")
                    writer?.writeln("public void setEMsg(int msg) {")
                    writer?.writeln("    this.msg = msg;")
                    writer?.writeln("}")
                } else {
                    // this is required for a gc header which doesn't have an eMsg
                    writer?.writeln("@Override")
                    writer?.writeln("public void setEMsg(int msg) {}")
                }
            } else {
                writer?.writeln("@Override")
                writer?.writeln("public void setEMsg(EMsg msg) {")
                writer?.writeln("    this.msg = msg;")
                writer?.writeln("}")
            }

            writer?.writeln()
        }
    }

    @Throws(IOException::class)
    private fun writeClassProperties(node: ClassNode) {
        if (node.parent != null) {
            val parentType = getType(node.parent)

            writer?.writeln("private $parentType header;")
            writer?.writeln()
        }

        for (child in node.childNodes) {
            val prop = child as PropNode
            var typeStr = getType(prop.type)
            val propName = prop.name

            val defSym = if (prop.default.isEmpty()) null else prop.default[0]
            var ctor = getType(defSym)

            if (prop.flags == "proto") {
                ctor = "CMsgProtoBufHeader.newBuilder()"
                typeStr += ".Builder"
            } else if (defSym == null) {
                ctor = if (!prop.flagsOpt.isNullOrEmpty()) {
                    "new $typeStr[${getTypeSize(prop)}]"
                } else {
                    "0"
                }
            }

            if (flagEnums.contains(typeStr)) {
                typeStr = "EnumSet<$typeStr>"
            }

            if (NUMBER_PATTERN.matcher(ctor).matches()) {
                when (typeStr) {
                    "long" -> ctor += "L"
                    "byte" -> ctor = "(byte) $ctor"
                    "short" -> ctor = "(short) $ctor"
                }

                if (prop.type is StrongSymbol) {
                    val strongSymbol = prop.type as StrongSymbol

                    if (strongSymbol.clazz is EnumNode) {
                        ctor = "${strongSymbol.clazz.name}.from($ctor)"
                    }
                }
            }

            if ("const" == prop.flags) {
                writer?.writeln("public static final $typeStr $propName = ${getType(prop.default[0])};")
                writer?.writeln()

                continue
            }

            if ("steamidmarshal" == prop.flags && "long" == typeStr) {
                writer?.writeln("private long $propName = $ctor;")
            } else if ("boolmarshal" == prop.flags && "byte" == typeStr) {
                writer?.writeln("private boolean $propName = false;")
            } else if ("gameidmarshal" == prop.flags && "long" == typeStr) {
                writer?.writeln("private long $propName = $ctor;")
            } else {
                if (!prop.flagsOpt.isNullOrEmpty() &&
                    NUMBER_PATTERN.matcher(prop.flagsOpt!!).matches()
                ) {
                    typeStr += "[]"
                }

                writer?.writeln("private $typeStr $propName = $ctor;")
            }

            writer?.writeln()
        }
    }

    @Throws(IOException::class)
    private fun writeSetterGetter(node: ClassNode) {
        if (node.parent != null) {
            val parentType = getType(node.parent)

            writer?.writeln("public $parentType getHeader() {")
            writer?.writeln("    return this.header;")
            writer?.writeln("}")
            writer?.writeln()
            writer?.writeln("public void setHeader($parentType header) {")
            writer?.writeln("    this.header = header;")
            writer?.writeln("}")
        }

        for (child in node.childNodes) {
            val propNode = child as PropNode
            var typeStr = getType(propNode.type)
            val propName = propNode.name

            if (flagEnums.contains(typeStr)) {
                typeStr = "EnumSet<$typeStr>"
            }

            if ("const" == propNode.flags) {
                continue
            }

            if (propNode.flags == "proto") {
                typeStr += ".Builder"
            }

            if ("steamidmarshal" == propNode.flags && "long" == typeStr) {
                writer?.writeln("public SteamID get${capitalize(propName)}() {")
                writer?.writeln("    return new SteamID(this.$propName);")
                writer?.writeln("}")
                writer?.writeln()
                writer?.writeln("public void set${capitalize(propName)}(SteamID steamId) {")
                writer?.writeln("    this.$propName = steamId.convertToUInt64();")
                writer?.writeln("}")
            } else if ("boolmarshal" == propNode.flags && "byte" == typeStr) {
                writer?.writeln("public boolean get${capitalize(propName)}() {")
                writer?.writeln("    return this.$propName;")
                writer?.writeln("}")
                writer?.writeln()
                writer?.writeln("public void set${capitalize(propName)}(boolean $propName) {")
                writer?.writeln("    this.$propName = $propName;")
                writer?.writeln("}")
            } else if ("gameidmarshal" == propNode.flags && "long" == typeStr) {
                writer?.writeln("public GameID get${capitalize(propName)}() {")
                writer?.writeln("    return new GameID(this.$propName);")
                writer?.writeln("}")
                writer?.writeln()
                writer?.writeln("public void set${capitalize(propName)}(GameID gameId) {")
                writer?.writeln("    this.$propName = gameId.convertToUInt64();")
                writer?.writeln("}")
            } else {
                if (!propNode.flagsOpt.isNullOrEmpty() &&
                    NUMBER_PATTERN.matcher(propNode.flagsOpt!!).matches()
                ) {
                    typeStr += "[]"
                }

                writer?.writeln("public $typeStr get${capitalize(propName)}() {")
                writer?.writeln("    return this.$propName;")
                writer?.writeln("}")
                writer?.writeln()
                writer?.writeln("public void set${capitalize(propName)}($typeStr $propName) {")
                writer?.writeln("    this.$propName = $propName;")
                writer?.writeln("}")
            }

            writer?.writeln()
        }
    }

    @Throws(IOException::class)
    private fun writeClassConstructor(node: ClassNode) {
        if (node.parent != null) {
            writer?.writeln("public ${node.name}() {")
            writer?.writeln("    this.header = new ${getType(node.parent)}();")
            writer?.writeln("    header.setMsg(getEMsg());")
            writer?.writeln("}")
        }
    }

    @Throws(IOException::class)
    private fun writeSerializationMethods(node: ClassNode) {
        val skip = HashSet<String>()

        node.childNodes.forEach { child ->
            val prop = child as PropNode

            if (prop.flags == "proto") {
                skip.add(prop.flagsOpt!!)
            }
        }

        writer?.writeln("@Override")
        writer?.writeln("public void serialize(OutputStream stream) throws IOException {")
        writer?.indent()
        writer?.writeln("BinaryWriter bw = new BinaryWriter(stream);")

        if (node.childNodes.isNotEmpty()) {
            writer?.writeln()
        }

        for (child in node.childNodes) {
            val prop = child as PropNode
            val typeStr = getType(prop.type)
            val propName = prop.name

            if (skip.contains(propName)) {
                continue
            }

            if (prop.flags == "protomask") {
                writer?.writeln("bw.writeInt(MsgUtil.makeMsg($propName.code(), true));")

                continue
            }

            if (prop.flags == "proto") {
                writer?.writeln("byte[] ${propName}Buffer = $propName.build().toByteArray();")

                if (prop.flagsOpt != null) {
                    writer?.writeln("${prop.flagsOpt} = ${propName}Buffer.length;")
                    writer?.writeln("bw.writeInt(${prop.flagsOpt});")
                } else {
                    writer?.writeln("bw.writeInt(${propName}Buffer.length);")
                }

                writer?.writeln("bw.write(${propName}Buffer);")

                continue
            }

            if (prop.flags == "const") {
                continue
            }

            if (prop.type is StrongSymbol) {
                val strongSymbol = prop.type as StrongSymbol
                if (strongSymbol.clazz is EnumNode) {
                    val enumType = getType(strongSymbol.clazz.type)

                    if (flagEnums.contains(typeStr)) {
                        when (enumType) {
                            "long" -> writer?.writeln("bw.writeLong($typeStr.code($propName));")
                            "byte" -> writer?.writeln("bw.writeByte($typeStr.code($propName));")
                            "short" -> writer?.writeln("bw.writeShort($typeStr.code($propName));")
                            else -> writer?.writeln("bw.writeInt($typeStr.code($propName));")
                        }
                    } else {
                        when (enumType) {
                            "long" -> writer?.writeln("bw.writeLong($propName.code());")
                            "byte" -> writer?.writeln("bw.writeByte($propName.code());")
                            "short" -> writer?.writeln("bw.writeShort($propName.code());")
                            else -> writer?.writeln("bw.writeInt($propName.code());")
                        }
                    }

                    continue
                }
            }

            if ("steamidmarshal" == prop.flags && "long" == typeStr) {
                writer?.writeln("bw.writeLong($propName);")
            } else if ("boolmarshal" == prop.flags && "byte" == typeStr) {
                writer?.writeln("bw.writeBoolean($propName);")
            } else if ("gameidmarshal" == prop.flags && "long" == typeStr) {
                writer?.writeln("bw.writeLong($propName);")
            } else {
                var isArray = false

                if (!prop.flagsOpt.isNullOrEmpty() &&
                    NUMBER_PATTERN.matcher(prop.flagsOpt!!).matches()
                ) {
                    isArray = true
                }

                if (isArray) {
                    writer?.writeln("bw.writeInt($propName.length);")
                    writer?.writeln("bw.write($propName);")
                } else {
                    when (typeStr) {
                        "long" -> writer?.writeln("bw.writeLong($propName);")
                        "byte" -> writer?.writeln("bw.writeByte($propName);")
                        "short" -> writer?.writeln("bw.writeShort($propName);")
                        else -> writer?.writeln("bw.writeInt($propName);")
                    }
                }
            }
        }

        writer?.unindent()
        writer?.writeln("}")
        writer?.writeln()
        writer?.writeln("@Override")
        writer?.writeln("public void deserialize(InputStream stream) throws IOException {")
        writer?.indent()
        writer?.writeln("BinaryReader br = new BinaryReader(stream);")

        if (node.childNodes.isNotEmpty()) {
            writer?.writeln()
        }

        for (child in node.childNodes) {
            val prop = child as PropNode
            val typeStr = getType(prop.type)
            val propName = prop.name

            if (skip.contains(propName)) {
                continue
            }

            if (prop.flags != null) {
                if (prop.flags == "protomask") {
                    writer?.writeln("$propName = MsgUtil.getMsg(br.readInt());")

                    continue
                }

                if (prop.flags == "proto") {
                    if (prop.flagsOpt != null) {
                        writer?.writeln("${prop.flagsOpt} = br.readInt();")
                        writer?.writeln("byte[] ${propName}Buffer = br.readBytes(${prop.flagsOpt});")
                    } else {
                        writer?.writeln("byte[] ${propName}Buffer = br.readBytes(br.readInt());")
                    }

                    writer?.writeln("$propName = $typeStr.newBuilder().mergeFrom(${propName}Buffer);")

                    continue
                }

                if (prop.flags == "const") {
                    continue
                }
            }

            if (prop.type is StrongSymbol) {
                val strongSymbol = prop.type as StrongSymbol

                if (strongSymbol.clazz is EnumNode) {
                    val enumType = getType((strongSymbol.clazz).type)
                    val className = strongSymbol.clazz.name

                    when (enumType) {
                        "long" -> writer?.writeln("$propName = $className.from(br.readLong());")
                        "byte" -> writer?.writeln("$propName = $className.from(br.readByte());")
                        "short" -> writer?.writeln("$propName = $className.from(br.readShort());")
                        else -> writer?.writeln("$propName = $className.from(br.readInt());")
                    }

                    continue
                }
            }

            if ("steamidmarshal" == prop.flags && "long" == typeStr) {
                writer?.writeln("$propName = br.readLong();")
            } else if ("boolmarshal" == prop.flags && "byte" == typeStr) {
                writer?.writeln("$propName = br.readBoolean();")
            } else if ("gameidmarshal" == prop.flags && "long" == typeStr) {
                writer?.writeln("$propName = br.readLong();")
            } else {
                var isArray = false

                if (!prop.flagsOpt.isNullOrEmpty() &&
                    NUMBER_PATTERN.matcher(prop.flagsOpt!!).matches()
                ) {
                    isArray = true
                }

                if (isArray) {
                    writer?.writeln("$propName = br.readBytes(br.readInt());")
                } else {
                    when (typeStr) {
                        "long" -> writer?.writeln("$propName = br.readLong();")
                        "byte" -> writer?.writeln("$propName = br.readByte();")
                        "short" -> writer?.writeln("$propName = br.readShort();")
                        else -> writer?.writeln("$propName = br.readInt();")
                    }
                }
            }
        }

        writer?.unindent()
        writer?.writeln("}")
    }

    @Throws(IOException::class)
    private fun writeEnumClass(node: EnumNode) {
        val flags = ("flags" == node.flags)

        if (flags) {
            flagEnums.add(node.name)
        }

        writer?.writeln("public enum ${node.name} {")
        writer?.writeln()
        writer?.indent()

        val type: String = if (node.type == null) "int" else getType(node.type)

        writeEnumProperties(node, type, flags)
        writeEnumCode(type, flags)

        writer?.unindent()
        writer?.writeln("}")
    }

    @Throws(IOException::class)
    private fun writeEnumCode(type: String, flags: Boolean) {
        writer?.writeln("private final $type code;")
        writer?.writeln()
        writer?.writeln("${this.node.name}($type code) {")
        writer?.writeln("    this.code = code;")
        writer?.writeln("}")
        writer?.writeln()
        writer?.writeln("public $type code() {")
        writer?.writeln("    return this.code;")
        writer?.writeln("}")
        writer?.writeln()

        if (flags) {
            writer?.writeln("public static EnumSet<${this.node.name}> from($type code) {")
            writer?.writeln("    EnumSet<${this.node.name}> set = EnumSet.noneOf(${this.node.name}.class);")
            writer?.writeln("    for (${this.node.name} e : ${this.node.name}.values()) {")
            writer?.writeln("        if ((e.code & code) == e.code) {")
            writer?.writeln("            set.add(e);")
            writer?.writeln("        }")
            writer?.writeln("    }")
            writer?.writeln("    return set;")
            writer?.writeln("}")
            writer?.writeln()
            writer?.writeln("public static $type code(EnumSet<${this.node.name}> flags) {")
            writer?.writeln("    $type code = 0;")
            writer?.writeln("    for (${this.node.name} flag : flags) {")
            writer?.writeln("        code |= flag.code;")
            writer?.writeln("    }")
            writer?.writeln("    return code;")
            writer?.writeln("}")
        } else {
            writer?.writeln("public static ${this.node.name} from($type code) {")
            writer?.writeln("    for (${this.node.name} e : ${this.node.name}.values()) {")
            writer?.writeln("        if (e.code == code) {")
            writer?.writeln("            return e;")
            writer?.writeln("        }")
            writer?.writeln("    }")
            writer?.writeln("    return null;")
            writer?.writeln("}")
        }
    }

    @Throws(IOException::class)
    private fun writeEnumProperties(node: EnumNode, type: String, flags: Boolean) {
        val statics = mutableListOf<PropNode>()
        for (child in node.childNodes) {
            val prop = child as PropNode
            if (prop.emit) {
                if (prop.obsolete != null) {
                    // including obsolete items can introduce duplicates
                    continue
                    // writer?.writeln("/**")
                    // writer?.writeln(" * @deprecated $prop.obsolete")
                    // writer?.writeln(" */")
                    // writer?.writeln("@Deprecated")
                }

                if (flags && !NUMBER_PATTERN.matcher(getType(prop.default[0])).matches()) {
                    statics.add(prop)
                } else {
                    val types = prop.default.map { symbol ->
                        val temp = getType(symbol)

                        if (NUMBER_PATTERN.matcher(temp).matches()) {
                            when (type) {
                                "long" -> if (temp.startsWith("-")) "$temp L" else "${temp.toLong()} L"
                                "byte" -> "(byte) $temp"
                                "short" -> "(short) $temp"
                                else ->
                                    if (temp.startsWith('-') || temp.contains('x')) {
                                        temp
                                    } else {
                                        temp.toLongOrNull()?.takeIf {
                                            it in Int.MIN_VALUE..Int.MAX_VALUE
                                        }?.toInt() ?: -1
                                    }
                            }
                        } else {
                            "$temp.code"
                        }
                    }

                    val value: String = types.joinToString(" | ")

                    writer?.writeln("${prop.name}($value),\n")
                }
            }
        }

        writer?.writeln(";")
        writer?.writeln()

        statics.forEach { p ->
            val defaults = p.default.map(::getType)
            writer?.writeln(
                "public static final EnumSet<${this.node.name}> ${p.name} = " +
                    "EnumSet.of(${defaults.joinToString(", ")});"
            )
            writer?.writeln()
        }
    }

    private fun getType(symbol: Symbol?): String {
        if (symbol is WeakSymbol) {
            // TODO: 2018-02-21 eeeeeehhh
            if (symbol.identifier.contains("CMsgProtoBufHeader")) {
                return "CMsgProtoBufHeader"
            }

            return if (WEAK_TYPES.containsKey(symbol.identifier)) {
                WEAK_TYPES[symbol.identifier]!!.name
            } else {
                symbol.identifier
            }
        } else if (symbol is StrongSymbol) {
            return if (symbol.prop == null) {
                symbol.clazz.name
            } else {
                "${symbol.clazz.name}.${symbol.prop.name}"
            }
        }

        return "INVALID"
    }

    private fun getTypeSize(prop: PropNode): Int {
        if (prop.flags == "proto") {
            return 0
        }

        val sym: Symbol? = prop.type
        if (sym is WeakSymbol) {
            var key = sym.identifier

            if (!WEAK_TYPES.containsKey(key)) {
                key = DEFAULT_TYPE
            }

            if (!prop.flagsOpt.isNullOrEmpty()) {
                return Integer.parseInt(prop.flagsOpt)
            }

            return WEAK_TYPES[key]!!.size
        } else if (sym is StrongSymbol) {
            if (sym.clazz is EnumNode) {
                val eNode = sym.clazz

                return if (eNode.type is WeakSymbol) {
                    WEAK_TYPES[(eNode.type as WeakSymbol).identifier]!!.size
                } else {
                    WEAK_TYPES[DEFAULT_TYPE]!!.size
                }
            }
        }

        return 0
    }

    private fun capitalize(string: String): String = string.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }

    @Throws(IOException::class)
    override fun close() {
        writer?.close()
    }

    @Throws(IOException::class)
    override fun flush() {
        writer?.flush()
    }

    private data class TypeInfo(val size: Int, val name: String)
}
