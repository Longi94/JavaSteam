package in.dragonbra.steamlanguagegen.generator

import in.dragonbra.steamlanguagegen.parser.node.ClassNode
import in.dragonbra.steamlanguagegen.parser.node.EnumNode
import in.dragonbra.steamlanguagegen.parser.node.Node
import in.dragonbra.steamlanguagegen.parser.node.PropNode
import in.dragonbra.steamlanguagegen.parser.symbol.StrongSymbol
import in.dragonbra.steamlanguagegen.parser.symbol.Symbol
import in.dragonbra.steamlanguagegen.parser.symbol.WeakSymbol

import java.util.regex.Pattern
import java.util.stream.Collectors

class JavaGen implements Closeable, Flushable {

    private static final Pattern NUMBER_PATTERN = ~/^-?[0-9].*?L?/

    private static final String DEFAULT_TYPE = "uint"

    private static final Map<String, TypeInfo> WEAK_TYPES = [
            byte  : new TypeInfo(1, 'byte'),
            short : new TypeInfo(2, 'short'),
            ushort: new TypeInfo(2, 'short'),
            int   : new TypeInfo(4, 'int'),
            uint  : new TypeInfo(4, 'int'),
            long  : new TypeInfo(8, 'long'),
            ulong : new TypeInfo(8, 'long')
    ]

    private JavaFileWriter writer

    private final Node node

    private String _package

    private File destination

    private Set<String> flagEnums

    JavaGen(Node node, String _package, File destination, Set<String> flagEnums) {
        this.node = node
        this._package = _package
        this.destination = destination
        this.flagEnums = flagEnums
    }

    void emit() throws IOException {
        if (node instanceof ClassNode && !((ClassNode) node).emit) {
            return
        }

        if (!destination.exists() && !destination.isDirectory() && !destination.mkdirs()) {
            throw new IllegalStateException('Couldn\'t create folders')
        }

        def file = new File(destination, "${node.name}.java")

        this.writer = new JavaFileWriter(file)
        writePackage(_package)
        writer.writeln()
        writeImports()
        writer.writeln()
        writeClass(node)
    }

    private void writeImports() throws IOException {
        if (node instanceof ClassNode) {
            def classNode = (ClassNode) node
            def imports = new HashSet<String>()

            imports << 'java.io.IOException' << 'java.io.InputStream' << 'java.io.OutputStream'
            imports << 'in.dragonbra.javasteam.util.stream.BinaryReader' << 'in.dragonbra.javasteam.util.stream.BinaryWriter'
            if (classNode.ident != null) {
                if (node.name.contains('MsgGC')) {
                    imports << 'in.dragonbra.javasteam.base.IGCSerializableMessage'
                } else {
                    imports << 'in.dragonbra.javasteam.base.ISteamSerializableMessage' << 'in.dragonbra.javasteam.enums.EMsg'
                }
            } else if (node.name.contains('Hdr')) {
                if (node.name.contains('MsgGC')) {
                    imports << 'in.dragonbra.javasteam.base.IGCSerializableHeader'
                } else {
                    imports << 'in.dragonbra.javasteam.base.ISteamSerializableHeader' << 'in.dragonbra.javasteam.enums.EMsg'
                }
            } else {
                imports << 'in.dragonbra.javasteam.base.ISteamSerializable'
            }

            for (Node child : (classNode.childNodes)) {
                def prop = (PropNode) child
                def typeStr = getType(prop.type)

                if (flagEnums.contains(typeStr)) {
                    imports << 'java.util.EnumSet'
                }

                if ('steamidmarshal' == prop.flags && 'long' == typeStr) {
                    imports << 'in.dragonbra.javasteam.types.SteamID'
                } else if ('gameidmarshal' == prop.flags && 'long' == typeStr) {
                    imports << 'in.dragonbra.javasteam.types.GameID'
                } else if (prop.flags == 'proto') {
                    imports << 'in.dragonbra.javasteam.protobufs.steamclient.SteammessagesBase.CMsgProtoBufHeader'
                } else if (prop.flags == 'protomask') {
                    imports << 'in.dragonbra.javasteam.enums.EMsg'
                    imports << 'in.dragonbra.javasteam.util.MsgUtil'
                }

                if (prop.type instanceof StrongSymbol) {
                    def strongSymbol = (StrongSymbol) prop.type
                    if (strongSymbol.clazz instanceof EnumNode) {
                        imports << "in.dragonbra.javasteam.enums.$strongSymbol.clazz.name"
                    }
                }

            }

            def sortedImports = imports.stream().map({ s -> s.toString() }).collect(Collectors.toList())
            Collections.sort(sortedImports)
            String currentGroup = null
            for (String imp : sortedImports) {
                def group = imp.substring(0, imp.indexOf('.'))

                if (group != currentGroup) {
                    if (currentGroup != null) {
                        writer.writeln()
                    }
                    currentGroup = group
                }

                writer.writeln "import $imp;"
            }

        } else if (node instanceof EnumNode) {
            if ('flags' == ((EnumNode) node).flags) {
                writer.writeln 'import java.util.EnumSet;'
            }
        }
    }

    private void writePackage(String _package) throws IOException {
        writer.writeln "package $_package;"
    }

    private void writeClass(Node node) throws IOException {
        if (node instanceof ClassNode && ((ClassNode) node).emit) {
            writeMessageClass((ClassNode) node)
        } else if (node instanceof EnumNode) {
            writeEnumClass((EnumNode) node)
        }
    }

    private void writeMessageClass(ClassNode node) throws IOException {
        writeClassDef(node)

        writer.indent()

        writeClassConstructor(node)
        writeClassProperties(node)
        writeClassIdentity(node)
        writeSetterGetter(node)
        writeSerializationMethods(node)

        writer.unindent()

        writer.writeln '}'
    }

    private void writeClassDef(ClassNode node) throws IOException {
        String parent = null

        if (node.ident != null) {
            if (node.name.contains('MsgGC')) {
                parent = 'IGCSerializableMessage'
            } else {
                parent = 'ISteamSerializableMessage'
            }
        } else if (node.name.contains('Hdr')) {
            if (node.name.contains('MsgGC')) {
                parent = 'IGCSerializableHeader'
            } else {
                parent = 'ISteamSerializableHeader'
            }
        } else {
            parent = 'ISteamSerializable'
        }

        if (parent != null) {
            writer.writeln "public class $node.name implements $parent {"
        } else {
            writer.writeln "public class $node.name {"
        }

        writer.writeln()
    }

    private void writeClassIdentity(ClassNode node) throws IOException {
        if (node.ident != null) {
            def sIdent = (StrongSymbol) node.ident
            def suppressObsolete = false

            if (sIdent != null) {
                def propNode = (PropNode) sIdent.prop

                if (propNode != null && propNode.obsolete != null) {
                    suppressObsolete = true
                }
            }

            if (suppressObsolete) {
                // TODO: 2018-02-19
            }

            if (node.name.contains('MsgGC')) {
                writer.writeln '@Override'
                writer.writeln 'public int getEMsg() {'
                writer.writeln "    return ${getType(node.ident)};"
                writer.writeln '}'
            } else {
                writer.writeln '@Override'
                writer.writeln 'public EMsg getEMsg() {'
                writer.writeln "    return ${getType(node.ident)};"
                writer.writeln '}'
            }

            writer.writeln()
        } else if (node.name.contains('Hdr')) {
            if (node.name.contains('MsgGC')) {
                if (node.childNodes.stream().anyMatch({ childNode -> 'msg' == childNode.name })) {
                    writer.writeln '@Override'
                    writer.writeln 'public void setEMsg(int msg) {'
                    writer.writeln '    this.msg = msg;'
                    writer.writeln '}'
                } else {
                    // this is required for a gc header which doesn't have an emsg
                    writer.writeln '@Override'
                    writer.writeln 'public void setEMsg(int msg) {}'
                }
            } else {
                writer.writeln '@Override'
                writer.writeln 'public void setEMsg(EMsg msg) {'
                writer.writeln '    this.msg = msg;'
                writer.writeln '}'
            }
            writer.writeln()
        }
    }

    private void writeClassProperties(ClassNode node) throws IOException {

        if (node.parent != null) {
            def parentType = getType(node.parent)
            writer.writeln "private $parentType header;"
            writer.writeln()
        }

        for (Node child : (node.childNodes)) {
            def prop = (PropNode) child
            def typeStr = getType(prop.type)
            def propName = prop.name

            def defSym = prop._default.isEmpty() ? null : prop._default[0]
            def ctor = getType(defSym)

            if (prop.flags == 'proto') {
                ctor = 'CMsgProtoBufHeader.newBuilder()'
                typeStr += '.Builder'
            } else if (defSym == null) {
                if (prop.flagsOpt != null && !prop.flagsOpt.isEmpty()) {
                    ctor = "new $typeStr[${getTypeSize(prop)}]"
                } else {
                    ctor = '0'
                }
            }

            if (flagEnums.contains(typeStr)) {
                typeStr = "EnumSet<$typeStr>"
            }

            if (NUMBER_PATTERN.matcher(ctor).matches()) {
                if ('long' == typeStr) {
                    ctor += 'L'
                } else if ('byte' == typeStr) {
                    ctor = "(byte) $ctor"
                } else if ('short' == typeStr) {
                    ctor = "(short) $ctor"
                }

                if (prop.type instanceof StrongSymbol) {
                    def strongSymbol = (StrongSymbol) prop.type
                    if (strongSymbol.clazz instanceof EnumNode) {
                        ctor = "${strongSymbol.clazz.name}.from($ctor)"
                    }
                }
            }

            if ('const' == prop.flags) {
                writer.writeln "public static final $typeStr $propName = ${getType(prop._default[0])};"
                writer.writeln()
                continue
            }

            if ('steamidmarshal' == prop.flags && 'long' == typeStr) {
                writer.writeln "private long $propName = $ctor;"
            } else if ('boolmarshal' == prop.flags && 'byte' == typeStr) {
                writer.writeln "private boolean $propName = false;"
            } else if ('gameidmarshal' == prop.flags && 'long' == typeStr) {
                writer.writeln "private long $propName = $ctor;"
            } else {
                if (!(prop.flagsOpt == null || prop.flagsOpt.isEmpty()) &&
                        NUMBER_PATTERN.matcher(prop.flagsOpt).matches()) {
                    typeStr += '[]'
                }

                writer.writeln "private $typeStr $propName = $ctor;"
            }
            writer.writeln()
        }
    }

    private void writeSetterGetter(ClassNode node) throws IOException {

        if (node.parent != null) {
            def parentType = getType(node.parent)
            writer.writeln "public $parentType getHeader() {"
            writer.writeln '    return this.header;'
            writer.writeln '}'
            writer.writeln()
            writer.writeln "public void getHeader($parentType header) {"
            writer.writeln '    this.header = header;'
            writer.writeln('}')
        }

        for (Node child : (node.childNodes)) {
            def propNode = (PropNode) child
            def typeStr = getType(propNode.type)
            def propName = propNode.name

            if (flagEnums.contains(typeStr)) {
                typeStr = "EnumSet<$typeStr>"
            }

            if ('const' == propNode.flags) {
                continue
            }

            if (propNode.flags == 'proto') {
                typeStr += ".Builder"
            }

            if ('steamidmarshal' == propNode.flags && "long" == typeStr) {
                writer.writeln("public SteamID get${capitalize(propName)}() {")
                writer.writeln("    return new SteamID(this.$propName);")
                writer.writeln("}")
                writer.writeln()
                writer.writeln("public void set${capitalize(propName)}(SteamID steamId) {")
                writer.writeln("    this.$propName = steamId.convertToUInt64();")
                writer.writeln("}")
            } else if ('boolmarshal' == propNode.flags && 'byte' == typeStr) {
                writer.writeln("public boolean get${capitalize(propName)}() {")
                writer.writeln("    return this.$propName;")
                writer.writeln('}')
                writer.writeln()
                writer.writeln("public void set${capitalize(propName)}(boolean $propName) {")
                writer.writeln("    this.$propName = $propName;")
                writer.writeln("}")
            } else if ('gameidmarshal' == propNode.flags && 'long' == typeStr) {
                writer.writeln("public GameID get${capitalize(propName)}() {")
                writer.writeln("    return new GameID(this.$propName);")
                writer.writeln('}')
                writer.writeln()
                writer.writeln("public void set${capitalize(propName)}(GameID gameId) {")
                writer.writeln("    this.$propName = gameId.convertToUInt64();")
                writer.writeln('}')
            } else {
                if (!(propNode.flagsOpt == null || propNode.flagsOpt.isEmpty()) &&
                        NUMBER_PATTERN.matcher(propNode.flagsOpt).matches()) {
                    typeStr += "[]"
                }

                writer.writeln("public $typeStr get${capitalize(propName)}() {")
                writer.writeln("    return this.$propName;")
                writer.writeln("}")
                writer.writeln()
                writer.writeln("public void set${capitalize(propName)}($typeStr $propName) {")
                writer.writeln("    this.$propName = $propName;")
                writer.writeln("}")
            }
            writer.writeln()
        }
    }

    private void writeClassConstructor(ClassNode node) throws IOException {
        if (node.parent != null) {
            writer.writeln("public $node.name() {")
            writer.writeln("    this.header = new ${getType(node.parent)}();")
            writer.writeln('    header.setMsg(getEMsg());')
            writer.writeln('}')
        }
    }

    private void writeSerializationMethods(ClassNode node) throws IOException {
        Set<String> skip = new HashSet<>()

        for (Node child : (node.childNodes)) {
            def prop = (PropNode) child
            if (prop.flags == "proto") {
                skip << prop.flagsOpt
            }
        }

        writer.writeln('@Override')
        writer.writeln('public void serialize(OutputStream stream) throws IOException {')
        writer.indent()

        writer.writeln('BinaryWriter bw = new BinaryWriter(stream);')
        writer.writeln()

        for (Node child : (node.childNodes)) {
            def prop = (PropNode) child
            def typeStr = getType(prop.type)
            def propName = prop.name

            if (skip.contains(propName)) {
                continue
            }

            if (prop.flags == 'protomask') {
                writer.writeln("bw.writeInt(MsgUtil.makeMsg(${propName}.code(), true));")
                continue
            }

            if (prop.flags == "proto") {
                writer.writeln("byte[] ${propName}Buffer = ${propName}.build().toByteArray();")
                if (prop.flagsOpt != null) {
                    writer.writeln("$prop.flagsOpt = ${propName}Buffer.length;")
                    writer.writeln("bw.writeInt($prop.flagsOpt);")
                } else {
                    writer.writeln("bw.writeInt(${propName}Buffer.length);")
                }
                writer.writeln("bw.write(${propName}Buffer);")
                continue
            }

            if (prop.flags == "const") {
                continue
            }

            if (prop.type instanceof StrongSymbol) {
                def strongSymbol = (StrongSymbol) prop.type
                if (strongSymbol.clazz instanceof EnumNode) {
                    def enumType = getType(((EnumNode) strongSymbol.clazz).type)

                    if (flagEnums.contains(typeStr)) {
                        switch (enumType) {
                            case 'long':
                                writer.writeln "bw.writeLong(${typeStr}.code($propName));"
                                break
                            case 'byte':
                                writer.writeln "bw.writeByte(${typeStr}.code($propName));"
                                break
                            case 'short':
                                writer.writeln "bw.writeShort(${typeStr}.code($propName));"
                                break
                            default:
                                writer.writeln "bw.writeInt(${typeStr}.code($propName));"
                                break
                        }
                    } else {
                        switch (enumType) {
                            case 'long':
                                writer.writeln "bw.writeLong(${propName}.code());"
                                break
                            case 'byte':
                                writer.writeln "bw.writeByte(${propName}.code());"
                                break
                            case 'short':
                                writer.writeln "bw.writeShort(${propName}.code());"
                                break
                            default:
                                writer.writeln "bw.writeInt(${propName}.code());"
                                break
                        }
                    }

                    continue
                }
            }

            if ('steamidmarshal' == prop.flags && 'long' == typeStr) {
                writer.writeln "bw.writeLong($propName);"
            } else if ('boolmarshal' == prop.flags && 'byte' == typeStr) {
                writer.writeln "bw.writeBoolean($propName);"
            } else if ('gameidmarshal' == prop.flags && 'long' == typeStr) {
                writer.writeln "bw.writeLong($propName);"
            } else {
                def isArray = false
                if (!(prop.flagsOpt == null || prop.flagsOpt.isEmpty()) &&
                        NUMBER_PATTERN.matcher(prop.flagsOpt).matches()) {
                    isArray = true
                }

                if (isArray) {
                    writer.writeln "bw.writeInt(${propName}.length);"
                    writer.writeln "bw.write($propName);"
                } else {
                    switch (typeStr) {
                        case 'long':
                            writer.writeln "bw.writeLong($propName);"
                            break
                        case 'byte':
                            writer.writeln "bw.writeByte($propName);"
                            break
                        case 'short':
                            writer.writeln "bw.writeShort($propName);"
                            break
                        default:
                            writer.writeln "bw.writeInt($propName);"
                            break
                    }
                }
            }
        }

        writer.unindent()
        writer.writeln "}"
        writer.writeln()
        writer.writeln "@Override"
        writer.writeln "public void deserialize(InputStream stream) throws IOException {"
        writer.indent()

        writer.writeln "BinaryReader br = new BinaryReader(stream);"
        writer.writeln()

        for (Node child : (node.childNodes)) {
            def prop = (PropNode) child
            def typeStr = getType(prop.type)
            def propName = prop.name

            if (skip.contains(propName)) {
                continue
            }

            if (prop.flags != null) {
                if (prop.flags == 'protomask') {
                    writer.writeln "$propName = MsgUtil.getMsg(br.readInt());"
                    continue
                }

                if (prop.flags == 'proto') {
                    if (prop.flagsOpt != null) {
                        writer.writeln "$prop.flagsOpt = br.readInt();"
                        writer.writeln "byte[] ${propName}Buffer = br.readBytes($prop.flagsOpt);"
                    } else {
                        writer.writeln "byte[] ${propName}Buffer = br.readBytes(br.readInt());"
                    }
                    writer.writeln "$propName = ${typeStr}.newBuilder().mergeFrom(${propName}Buffer);"
                    continue
                }

                if (prop.flags == 'const') {
                    continue
                }
            }

            if (prop.type instanceof StrongSymbol) {
                def strongSymbol = (StrongSymbol) prop.type
                if (strongSymbol.clazz instanceof EnumNode) {
                    String enumType = getType(((EnumNode) strongSymbol.clazz).type)
                    String className = strongSymbol.clazz.name

                    switch (enumType) {
                        case 'long':
                            writer.writeln "$propName = ${className}.from(br.readLong());"
                            break
                        case 'byte':
                            writer.writeln "$propName = ${className}.from(br.readByte());"
                            break
                        case 'short':
                            writer.writeln "$propName = ${className}.from(br.readShort());"
                            break
                        default:
                            writer.writeln "$propName = ${className}.from(br.readInt());"
                            break
                    }
                    continue
                }
            }

            if ('steamidmarshal' == prop.flags && 'long' == typeStr) {
                writer.writeln "$propName = br.readLong();"
            } else if ('boolmarshal' == prop.flags && 'byte' == typeStr) {
                writer.writeln "$propName = br.readBoolean();"
            } else if ('gameidmarshal' == prop.flags && 'long' == typeStr) {
                writer.writeln "$propName = br.readLong();"
            } else {
                def isArray = false
                if (!(prop.flagsOpt == null || prop.flagsOpt.isEmpty()) &&
                        NUMBER_PATTERN.matcher(prop.flagsOpt).matches()) {
                    isArray = true
                }

                if (isArray) {
                    writer.writeln "$propName = br.readBytes(br.readInt());"
                } else {
                    switch (typeStr) {
                        case 'long':
                            writer.writeln "$propName = br.readLong();"
                            break
                        case 'byte':
                            writer.writeln "$propName = br.readByte();"
                            break
                        case 'short':
                            writer.writeln "$propName = br.readShort();"
                            break
                        default:
                            writer.writeln "$propName = br.readInt();"
                            break
                    }
                }
            }
        }

        writer.unindent()
        writer.writeln '}'
    }

    private void writeEnumClass(EnumNode node) throws IOException {
        boolean flags = 'flags' == node.flags

        if (flags) {
            flagEnums << node.name
        }

        writer.writeln "public enum $node.name {"
        writer.writeln()

        writer.indent()

        String type = node.type == null ? 'int' : getType(node.type)

        writeEnumProperties(node, type, flags)

        writeEnumCode(type, flags)

        writer.unindent()

        writer.writeln '}'
    }

    private void writeEnumCode(String type, boolean flags) throws IOException {
        writer.writeln "private final $type code;"
        writer.writeln()
        writer.writeln "${this.node.name}($type code) {"
        writer.writeln '    this.code = code;'
        writer.writeln '}'
        writer.writeln()
        writer.writeln "public $type code() {"
        writer.writeln '    return this.code;'
        writer.writeln '}'
        writer.writeln()
        if (flags) {
            writer.writeln "public static EnumSet<${this.node.name}> from($type code) {"
            writer.writeln "    EnumSet<${this.node.name}> set = EnumSet.noneOf(${this.node.name}.class);"
            writer.writeln "    for (${this.node.name} e : ${this.node.name}.values()) {"
            writer.writeln '        if ((e.code & code) == e.code) {'
            writer.writeln '            set.add(e);'
            writer.writeln '        }'
            writer.writeln '    }'
            writer.writeln '    return set;'
            writer.writeln '}'
            writer.writeln()
            writer.writeln "public static $type code(EnumSet<${this.node.name}> flags) {"
            writer.writeln "    $type code = 0;"
            writer.writeln "    for (${this.node.name} flag : flags) {"
            writer.writeln '        code |= flag.code;'
            writer.writeln '    }'
            writer.writeln '    return code;'
            writer.writeln '}'
        } else {
            writer.writeln "public static ${this.node.name} from($type code) {"
            writer.writeln "    for (${this.node.name} e : ${this.node.name}.values()) {"
            writer.writeln '        if (e.code == code) {'
            writer.writeln '            return e;'
            writer.writeln '        }'
            writer.writeln '    }'
            writer.writeln '    return null;'
            writer.writeln '}'
        }
    }

    private void writeEnumProperties(EnumNode node, String type, boolean flags) throws IOException {

        List<PropNode> statics = new ArrayList<>()
        for (Node child : (node.childNodes)) {
            def prop = (PropNode) child

            if (prop.emit) {
                if (prop.obsolete != null) {
                    writer.writeln('@Deprecated')
                }

                if (flags && !NUMBER_PATTERN.matcher(getType(prop._default[0])).matches()) {
                    statics << prop
                } else {
                    List<String> types = prop._default.stream().map({ symbol ->
                        def temp = getType(symbol)

                        if (NUMBER_PATTERN.matcher(temp).matches()) {
                            switch (type) {
                                case 'long':
                                    if (temp.startsWith("-")) {
                                        return "${temp}L"
                                    }
                                    return "${Long.parseUnsignedLong(temp)}L"
                                case 'byte':
                                    return "(byte) $temp"
                                case 'short':
                                    return "(short) $temp"
                                default:
                                    if (temp.startsWith('-') || temp.contains('x')) {
                                        return temp
                                    }
                                    return String.valueOf(Integer.parseUnsignedInt(temp))
                            }
                        }

                        return "${temp}.code"
                    }).collect(Collectors.toList())

                    String val = String.join(" | ", types)

                    writer.writeln "$prop.name($val),"
                }
            }
        }

        writer.writeln()
        writer.writeln ';'
        writer.writeln()

        for (PropNode p : statics) {
            List<String> defaults = p._default.stream().map({ defa -> return getType(defa) }).collect(Collectors.toList())
            writer.writeln "public static final EnumSet<${this.node.name}> $p.name = EnumSet.of(${String.join(", ", defaults)});"
            writer.writeln()
        }
    }

    private static String getType(Symbol symbol) {
        if (symbol instanceof WeakSymbol) {
            def ws = (WeakSymbol) symbol

            // TODO: 2018-02-21 eeeeeehhh
            if (ws.identifier.contains('CMsgProtoBufHeader')) {
                return 'CMsgProtoBufHeader'
            }

            return WEAK_TYPES.containsKey(ws.identifier) ? WEAK_TYPES[ws.identifier].name : ws.identifier
        } else if (symbol instanceof StrongSymbol) {
            def ss = (StrongSymbol) symbol

            if (ss.prop == null) {
                return ss.clazz.name
            } else {
                return ss.clazz.name + "." + ss.prop.name
            }
        }

        return "INVALID"
    }

    private static int getTypeSize(PropNode prop) {
        if (prop.flags == 'proto') {
            return 0
        }

        Symbol sym = prop.type

        if (sym instanceof WeakSymbol) {
            def wsym = (WeakSymbol) sym

            def key = wsym.identifier

            if (!WEAK_TYPES.containsKey(key)) {
                key = DEFAULT_TYPE
            }

            if (prop.flagsOpt != null && !prop.flagsOpt.isEmpty()) {
                return Integer.parseInt(prop.flagsOpt)
            }

            return WEAK_TYPES[key].size
        } else if (sym instanceof StrongSymbol) {
            def ssym = (StrongSymbol) sym

            if (ssym.clazz instanceof EnumNode) {
                def enode = (EnumNode) ssym.clazz

                if (enode.type instanceof WeakSymbol) {
                    return WEAK_TYPES[((WeakSymbol) enode.type).identifier].size
                } else {
                    return WEAK_TYPES[DEFAULT_TYPE].size
                }
            }
        }

        return 0
    }

    private static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1)
    }

    @Override
    void close() throws IOException {
        if (writer != null) {
            writer.close()
        }
    }

    @Override
    void flush() throws IOException {
        if (writer != null) {
            writer.flush()
        }
    }

    private static class TypeInfo {
        final int size

        final String name

        TypeInfo(int size, String name) {
            this.size = size
            this.name = name
        }
    }
}
