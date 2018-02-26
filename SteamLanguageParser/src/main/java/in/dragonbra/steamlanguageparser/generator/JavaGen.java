package in.dragonbra.steamlanguageparser.generator;

import in.dragonbra.steamlanguageparser.parser.*;

import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author lngtr
 * @since 2018-02-15
 */
public class JavaGen implements Closeable, Flushable {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("^-?[0-9].*?L?");

    private static final String DEFAULT_TYPE = "uint";

    private static final Map<String, TypeInfo> WEAK_TYPES;

    static {
        Map<String, TypeInfo> weakTypes = new HashMap<>();

        weakTypes.put("byte", new TypeInfo(1, "byte"));
        weakTypes.put("short", new TypeInfo(2, "short"));
        weakTypes.put("ushort", new TypeInfo(4, "short"));
        weakTypes.put("int", new TypeInfo(4, "int"));
        weakTypes.put("uint", new TypeInfo(8, "int"));
        weakTypes.put("long", new TypeInfo(8, "long"));
        weakTypes.put("ulong", new TypeInfo(8, "long"));

        WEAK_TYPES = Collections.unmodifiableMap(weakTypes);
    }

    private JavaFileWriter writer;

    private final Node node;

    private String _package;

    private String destination;

    private Set<String> flagEnums;

    public JavaGen(Node node, String _package, String destination, Set<String> flagEnums) {
        this.node = node;
        this._package = _package;
        this.destination = destination;
        this.flagEnums = flagEnums;
    }

    public void emit() throws IOException {
        if (node instanceof ClassNode && !((ClassNode) node).isEmit()) {
            return;
        }

        File folder = new File(destination);

        if (!folder.exists() && !folder.isDirectory() && !folder.mkdirs()) {
            throw new IllegalStateException("Couldn't create folders");
        }

        File file = new File(folder, node.getName() + ".java");

        this.writer = new JavaFileWriter(file);
        writePackage(_package);
        writer.writeln();
        writeImports();
        writer.writeln();
        writeClass(node);
    }

    private void writeImports() throws IOException {
        if (node instanceof ClassNode) {
            ClassNode classNode = (ClassNode) node;
            Set<String> imports = new HashSet<>();

            if (classNode.getIdent() != null) {
                if (node.getName().contains("MsgGC")) {
                    imports.add("in.dragonbra.javasteam.base.IGCSerializableMessage");
                } else {
                    imports.add("in.dragonbra.javasteam.base.ISteamSerializableMessage");
                    imports.add("in.dragonbra.javasteam.enums.EMsg");
                }
                imports.add("java.io.IOException");
                imports.add("java.io.InputStream");
                imports.add("java.io.OutputStream");
                imports.add("in.dragonbra.javasteam.util.stream.BinaryReader");
                imports.add("in.dragonbra.javasteam.util.stream.BinaryWriter");
            } else if (node.getName().contains("Hdr")) {
                if (node.getName().contains("MsgGC")) {
                    imports.add("in.dragonbra.javasteam.base.IGCSerializableHeader");
                } else {
                    imports.add("in.dragonbra.javasteam.base.ISteamSerializableHeader");
                    imports.add("in.dragonbra.javasteam.enums.EMsg");
                }
                imports.add("java.io.IOException");
                imports.add("java.io.InputStream");
                imports.add("java.io.OutputStream");
                imports.add("in.dragonbra.javasteam.util.stream.BinaryReader");
                imports.add("in.dragonbra.javasteam.util.stream.BinaryWriter");
            }

            for (Node child : classNode.getChildNodes()) {
                PropNode prop = (PropNode) child;
                String typeStr = getType(prop.getType());

                if (flagEnums.contains(typeStr)) {
                    imports.add("java.util.EnumSet");
                }

                if (prop.getFlags() != null) {
                    if ("steamidmarshal".equals(prop.getFlags()) && "long".equals(typeStr)) {
                        imports.add("in.dragonbra.javasteam.types.SteamID");
                    } else if ("gameidmarshal".equals(prop.getFlags()) && "long".equals(typeStr)) {
                        imports.add("in.dragonbra.javasteam.types.GameID");
                    } else if (prop.getFlags().equals("proto")) {
                        imports.add("in.dragonbra.javasteam.protobufs.steamclient.SteammessagesBase.CMsgProtoBufHeader");
                    } else if (prop.getFlags().equals("protomask")) {
                        imports.add("in.dragonbra.javasteam.enums.EMsg");
                        imports.add("in.dragonbra.javasteam.util.MsgUtil");
                    }
                }

                if (prop.getType() instanceof StrongSymbol) {
                    StrongSymbol strongSymbol = (StrongSymbol) prop.getType();
                    if (strongSymbol.getClazz() instanceof EnumNode) {
                        imports.add("in.dragonbra.javasteam.enums." + strongSymbol.getClazz().getName());
                    }
                }

            }

            List<String> sortedImports = new ArrayList<>(imports);
            Collections.sort(sortedImports);
            String currentGroup = null;
            for (String imp : sortedImports) {
                String group = imp.substring(0, imp.indexOf('.'));

                if (!group.equals(currentGroup)) {
                    if (currentGroup != null) {
                        writer.writeln();
                    }
                    currentGroup = group;
                }

                writer.writeln("import " + imp + ";");
            }

        } else if (node instanceof EnumNode) {
            writer.writeln("import java.util.Arrays;");
            if ("flags".equals(((EnumNode) node).getFlags())) {
                writer.writeln("import java.util.EnumSet;");
                writer.writeln("import java.util.stream.Collectors;");
            }
        }
    }

    private void writePackage(String _package) throws IOException {
        writer.writeln("package " + _package + ";");
    }

    private void writeClass(Node node) throws IOException {
        if (node instanceof ClassNode && ((ClassNode) node).isEmit()) {
            writeMessageClass((ClassNode) node);
        } else if (node instanceof EnumNode) {
            writeEnumClass((EnumNode) node);
        }
    }

    private void writeMessageClass(ClassNode node) throws IOException {
        writeClassDef(node);

        writer.indent();

        writeClassConstructor(node);
        writeClassProperties(node);
        writeClassIdentity(node);
        writeSetterGetter(node);
        writeSerializationMethods(node);

        writer.unindent();

        writer.writeln("}");
    }

    private void writeClassDef(ClassNode node) throws IOException {
        String parent = null;

        if (node.getIdent() != null) {
            if (node.getName().contains("MsgGC")) {
                parent = "IGCSerializableMessage";
            } else {
                parent = "ISteamSerializableMessage";
            }
        } else if (node.getName().contains("Hdr")) {
            if (node.getName().contains("MsgGC")) {
                parent = "IGCSerializableHeader";
            } else {
                parent = "ISteamSerializableHeader";
            }
        }

        if (parent != null) {
            writer.writeln("public class " + node.getName() + " implements " + parent + " {");
        } else {
            writer.writeln("public class " + node.getName() + " {");
        }

        writer.writeln();
    }

    private void writeClassIdentity(ClassNode node) throws IOException {
        if (node.getIdent() != null) {
            StrongSymbol sIdent = (StrongSymbol) node.getIdent();
            boolean suppressObselete = false;

            if (sIdent != null) {
                PropNode propNode = (PropNode) sIdent.getProp();

                if (propNode != null && propNode.getObsolete() != null) {
                    suppressObselete = true;
                }
            }

            if (suppressObselete) {
                // TODO: 2018-02-19
            }

            if (node.getName().contains("MsgGC")) {
                writer.writeln("@Override");
                writer.writeln("public int getEMsg() {");
                writer.writeln("    return " + getType(node.getIdent()) + ";");
                writer.writeln("}");
            } else {
                writer.writeln("@Override");
                writer.writeln("public EMsg getEMsg() {");
                writer.writeln("    return " + getType(node.getIdent()) + ";");
                writer.writeln("}");
            }

            writer.writeln();
        } else if (node.getName().contains("Hdr")) {
            if (node.getName().contains("MsgGC")) {
                if (node.getChildNodes().stream().anyMatch(childNode -> "msg".equals(childNode.getName()))) {
                    writer.writeln("@Override");
                    writer.writeln("public void setEMsg(int msg) {");
                    writer.writeln("    this.msg = msg;");
                    writer.writeln("}");
                } else {
                    // this is required for a gc header which doesn't have an emsg
                    writer.writeln("@Override");
                    writer.writeln("public void setEMsg(int msg) {}");
                }
            } else {
                writer.writeln("@Override");
                writer.writeln("public void setEMsg(EMsg msg) {");
                writer.writeln("    this.msg = msg;");
                writer.writeln("}");
            }
            writer.writeln();
        }
    }

    private void writeClassProperties(ClassNode node) throws IOException {

        if (node.getParent() != null) {
            String parentType = getType(node.getParent());
            writer.writeln("private " + parentType + " header;");
            writer.writeln();
        }

        for (Node child : node.getChildNodes()) {
            PropNode prop = (PropNode) child;
            String typeStr = getType(prop.getType());
            String propName = prop.getName();

            Symbol defSym = prop.getDefault().isEmpty() ? null : prop.getDefault().get(0);
            String ctor = getType(defSym);

            if (prop.getFlags() != null && prop.getFlags().equals("proto")) {
                ctor = "CMsgProtoBufHeader.newBuilder()";
                typeStr += ".Builder";
            } else if (defSym == null) {
                if (prop.getFlagsOpt() != null && !prop.getFlagsOpt().isEmpty()) {
                    ctor = "new " + typeStr + "[" + getTypeSize(prop) + "]";
                } else {
                    ctor = "0";
                }
            }

            if (flagEnums.contains(typeStr)) {
                typeStr = "EnumSet<" + typeStr + ">";
            }

            if (NUMBER_PATTERN.matcher(ctor).matches()) {
                if ("long".equals(typeStr)) {
                    ctor += "L";
                } else if ("byte".equals(typeStr)) {
                    ctor = "(byte) " + ctor;
                } else if ("short".equals(typeStr)) {
                    ctor = "(short) " + ctor;
                }

                if (prop.getType() instanceof StrongSymbol) {
                    StrongSymbol strongSymbol = (StrongSymbol) prop.getType();
                    if (strongSymbol.getClazz() instanceof EnumNode) {
                        ctor = strongSymbol.getClazz().getName() + ".from(" + ctor + ")";
                    }
                }
            }

            if (prop.getFlags() != null && "const".equals(prop.getFlags())) {
                writer.writeln("public static final " + typeStr + " " + propName + " = " + getType(prop.getDefault().get(0)) + ";");
                writer.writeln();
                continue;
            }

            if (prop.getFlags() != null && "steamidmarshal".equals(prop.getFlags()) && "long".equals(typeStr)) {
                writer.writeln("private long " + propName + " = " + ctor + ";");
            } else if (prop.getFlags() != null && "boolmarshal".equals(prop.getFlags()) && "byte".equals(typeStr)) {
                writer.writeln("private boolean " + propName + " = false;");
            } else if (prop.getFlags() != null && "gameidmarshal".equals(prop.getFlags()) && "long".equals(typeStr)) {
                writer.writeln("private long " + propName + " = " + ctor + ";");
            } else {
                if (!(prop.getFlagsOpt() == null || prop.getFlagsOpt().isEmpty()) &&
                        NUMBER_PATTERN.matcher(prop.getFlagsOpt()).matches()) {
                    typeStr += "[]";
                }

                writer.writeln("private " + typeStr + " " + propName + " = " + ctor + ";");
            }
            writer.writeln();
        }
    }

    private void writeSetterGetter(ClassNode node) throws IOException {

        if (node.getParent() != null) {
            String parentType = getType(node.getParent());
            writer.writeln("public " + parentType + " getHeader() {");
            writer.writeln("    return this.header;");
            writer.writeln("}");
            writer.writeln();
            writer.writeln("public void getHeader(" + parentType + " header) {");
            writer.writeln("    this.header = header;");
            writer.writeln("}");
        }

        for (Node child : node.getChildNodes()) {
            PropNode propNode = (PropNode) child;
            String typeStr = getType(propNode.getType());
            String propName = propNode.getName();

            if (flagEnums.contains(typeStr)) {
                typeStr = "EnumSet<" + typeStr + ">";
            }

            if (propNode.getFlags() != null && "const".equals(propNode.getFlags())) {
                continue;
            }

            if (propNode.getFlags() != null && propNode.getFlags().equals("proto")) {
                typeStr += ".Builder";
            }

            if (propNode.getFlags() != null && "steamidmarshal".equals(propNode.getFlags()) && "long".equals(typeStr)) {
                writer.writeln("public SteamID get" + capitalize(propName) + "() {");
                writer.writeln("    return new SteamID(this." + propName + ");");
                writer.writeln("}");
                writer.writeln();
                writer.writeln("public void set" + capitalize(propName) + "(SteamID steamId) {");
                writer.writeln("    this." + propName + " = steamId.convertToUInt64();");
                writer.writeln("}");
            } else if (propNode.getFlags() != null && "boolmarshal".equals(propNode.getFlags()) && "byte".equals(typeStr)) {
                writer.writeln("public boolean get" + capitalize(propName) + "() {");
                writer.writeln("    return this." + propName + ";");
                writer.writeln("}");
                writer.writeln();
                writer.writeln("public void set" + capitalize(propName) + "(boolean " + propName + ") {");
                writer.writeln("    this." + propName + " = " + propName + ";");
                writer.writeln("}");
            } else if (propNode.getFlags() != null && "gameidmarshal".equals(propNode.getFlags()) && "long".equals(typeStr)) {
                writer.writeln("public GameID get" + capitalize(propName) + "() {");
                writer.writeln("    return new GameID(this." + propName + ");");
                writer.writeln("}");
                writer.writeln();
                writer.writeln("public void set" + capitalize(propName) + "(GameID gameId) {");
                writer.writeln("    this." + propName + " = gameId.convertToUInt64();");
                writer.writeln("}");
            } else {
                if (!(propNode.getFlagsOpt() == null || propNode.getFlagsOpt().isEmpty()) &&
                        NUMBER_PATTERN.matcher(propNode.getFlagsOpt()).matches()) {
                    typeStr += "[]";
                }

                writer.writeln("public " + typeStr + " get" + capitalize(propName) + "() {");
                writer.writeln("    return this." + propName + ";");
                writer.writeln("}");
                writer.writeln();
                writer.writeln("public void set" + capitalize(propName) + "(" + typeStr + " " + propName + ") {");
                writer.writeln("    this." + propName + " = " + propName + ";");
                writer.writeln("}");
            }
            writer.writeln();
        }
    }

    private void writeClassConstructor(ClassNode node) throws IOException {
        if (node.getParent() != null) {
            writer.writeln("public " + node.getName() + "() {");
            writer.writeln("    this.header = new " + getType(node.getParent()) + "();");
            writer.writeln("    header.setMsg(getEMsg());");
            writer.writeln("}");
        }
    }

    private void writeSerializationMethods(ClassNode node) throws IOException {
        Set<String> skip = new HashSet<>();

        for (Node child : node.getChildNodes()) {
            PropNode prop = (PropNode) child;
            if (prop.getFlags() != null && prop.getFlags().equals("proto") && prop.getFlagsOpt() != null) {
                skip.add(prop.getFlagsOpt());
            }
        }

        if (node.getIdent() != null || node.getName().contains("Hdr")) {
            writer.writeln("@Override");
            writer.writeln("public void serialize(OutputStream stream) throws IOException {");
            writer.indent();

            writer.writeln("BinaryWriter bw = new BinaryWriter(stream);");
            writer.writeln();

            for (Node child : node.getChildNodes()) {
                PropNode prop = (PropNode) child;
                String typeStr = getType(prop.getType());
                String propName = prop.getName();

                if (skip.contains(propName)) {
                    continue;
                }

                if (prop.getFlags() != null) {
                    if (prop.getFlags().equals("protomask")) {
                        writer.writeln("bw.writeInt(MsgUtil.makeMsg(" + propName + ".code(), true));");
                        continue;
                    }

                    if (prop.getFlags().equals("proto")) {
                        writer.writeln("byte[] " + propName + "Buffer = " + propName + ".build().toByteArray();");
                        if (prop.getFlagsOpt() != null) {
                            writer.writeln(prop.getFlagsOpt() + " = " + propName + "Buffer.length;");
                            writer.writeln("bw.writeInt(" + prop.getFlagsOpt() + ");");
                        } else {
                            writer.writeln("bw.writeInt(" + propName + "Buffer.length);");
                        }
                        writer.writeln("bw.write(" + propName + "Buffer);");
                        continue;
                    }

                    if (prop.getFlags().equals("const")) {
                        continue;
                    }
                }

                if (prop.getType() instanceof StrongSymbol) {
                    StrongSymbol strongSymbol = (StrongSymbol) prop.getType();
                    if (strongSymbol.getClazz() instanceof EnumNode) {
                        String enumType = getType(((EnumNode) strongSymbol.getClazz()).getType());

                        if (flagEnums.contains(typeStr)) {
                            switch (enumType) {
                                case "long":
                                    writer.writeln("bw.writeLong(" + typeStr + ".code(" + propName + "));");
                                    break;
                                case "byte":
                                    writer.writeln("bw.writeByte(" + typeStr + ".code(" + propName + "));");
                                    break;
                                case "short":
                                    writer.writeln("bw.writeShort(" + typeStr + ".code(" + propName + "));");
                                    break;
                                default:
                                    writer.writeln("bw.writeInt(" + typeStr + ".code(" + propName + "));");
                                    break;
                            }
                        } else {
                            switch (enumType) {
                                case "long":
                                    writer.writeln("bw.writeLong(" + propName + ".code());");
                                    break;
                                case "byte":
                                    writer.writeln("bw.writeByte(" + propName + ".code());");
                                    break;
                                case "short":
                                    writer.writeln("bw.writeShort(" + propName + ".code());");
                                    break;
                                default:
                                    writer.writeln("bw.writeInt(" + propName + ".code());");
                                    break;
                            }
                        }

                        continue;
                    }
                }

                if (prop.getFlags() != null && "steamidmarshal".equals(prop.getFlags()) && "long".equals(typeStr)) {
                    writer.writeln("bw.writeLong(" + propName + ");");
                } else if (prop.getFlags() != null && "boolmarshal".equals(prop.getFlags()) && "byte".equals(typeStr)) {
                    writer.writeln("bw.writeBoolean(" + propName + ");");
                } else if (prop.getFlags() != null && "gameidmarshal".equals(prop.getFlags()) && "long".equals(typeStr)) {
                    writer.writeln("bw.writeLong(" + propName + ");");
                } else {
                    boolean isArray = false;
                    if (!(prop.getFlagsOpt() == null || prop.getFlagsOpt().isEmpty()) &&
                            NUMBER_PATTERN.matcher(prop.getFlagsOpt()).matches()) {
                        isArray = true;
                    }

                    if (isArray) {
                        writer.writeln("bw.writeInt(" + propName + ".length);");
                        writer.writeln("bw.write(" + propName + ");");
                    } else {
                        switch (typeStr) {
                            case "long":
                                writer.writeln("bw.writeLong(" + propName + ");");
                                break;
                            case "byte":
                                writer.writeln("bw.writeByte(" + propName + ");");
                                break;
                            case "short":
                                writer.writeln("bw.writeShort(" + propName + ");");
                                break;
                            default:
                                writer.writeln("bw.writeInt(" + propName + ");");
                                break;
                        }
                    }
                }
            }

            writer.unindent();
            writer.writeln("}");
            writer.writeln();
            writer.writeln("@Override");
            writer.writeln("public void deserialize(InputStream stream) throws IOException {");
            writer.indent();

            writer.writeln("BinaryReader br = new BinaryReader(stream);");
            writer.writeln();

            for (Node child : node.getChildNodes()) {
                PropNode prop = (PropNode) child;
                String typeStr = getType(prop.getType());
                String propName = prop.getName();

                if (skip.contains(propName)) {
                    continue;
                }

                if (prop.getFlags() != null) {
                    if (prop.getFlags().equals("protomask")) {
                        writer.writeln(propName + " = MsgUtil.getMsg(br.readInt());");
                        continue;
                    }

                    if (prop.getFlags().equals("proto")) {
                        if (prop.getFlagsOpt() != null) {
                            writer.writeln(prop.getFlagsOpt() + " = br.readInt();");
                            writer.writeln("byte[] " + propName + "Buffer = br.readBytes(" + prop.getFlagsOpt() + ");");
                        } else {
                            writer.writeln("byte[] " + propName + "Buffer = br.readBytes(br.readInt());");
                        }
                        writer.writeln(propName + " = " + typeStr + ".newBuilder().mergeFrom(" + propName + "Buffer);");
                        continue;
                    }

                    if (prop.getFlags().equals("const")) {
                        continue;
                    }
                }

                if (prop.getType() instanceof StrongSymbol) {
                    StrongSymbol strongSymbol = (StrongSymbol) prop.getType();
                    if (strongSymbol.getClazz() instanceof EnumNode) {
                        String enumType = getType(((EnumNode) strongSymbol.getClazz()).getType());
                        String className = strongSymbol.getClazz().getName();

                        switch (enumType) {
                            case "long":
                                writer.writeln(propName + " = " + className + ".from(br.readLong());");
                                break;
                            case "byte":
                                writer.writeln(propName + " = " + className + ".from(br.readByte());");
                                break;
                            case "short":
                                writer.writeln(propName + " = " + className + ".from(br.readShort());");
                                break;
                            default:
                                writer.writeln(propName + " = " + className + ".from(br.readInt());");
                                break;
                        }
                        continue;
                    }
                }

                if (prop.getFlags() != null && "steamidmarshal".equals(prop.getFlags()) && "long".equals(typeStr)) {
                    writer.writeln(propName + " = br.readLong();");
                } else if (prop.getFlags() != null && "boolmarshal".equals(prop.getFlags()) && "byte".equals(typeStr)) {
                    writer.writeln(propName + " = br.readBoolean();");
                } else if (prop.getFlags() != null && "gameidmarshal".equals(prop.getFlags()) && "long".equals(typeStr)) {
                    writer.writeln(propName + " = br.readLong();");
                } else {
                    boolean isArray = false;
                    if (!(prop.getFlagsOpt() == null || prop.getFlagsOpt().isEmpty()) &&
                            NUMBER_PATTERN.matcher(prop.getFlagsOpt()).matches()) {
                        isArray = true;
                    }

                    if (isArray) {
                        writer.writeln(propName + " = br.readBytes(br.readInt());");
                    } else {
                        switch (typeStr) {
                            case "long":
                                writer.writeln(propName + " = br.readLong();");
                                break;
                            case "byte":
                                writer.writeln(propName + " = br.readByte();");
                                break;
                            case "short":
                                writer.writeln(propName + " = br.readShort();");
                                break;
                            default:
                                writer.writeln(propName + " = br.readInt();");
                                break;
                        }
                    }
                }
            }

            writer.unindent();
            writer.writeln("}");
        }
    }

    private void writeEnumClass(EnumNode node) throws IOException {
        boolean flags = "flags".equals(node.getFlags());

        if (flags) {
            flagEnums.add(node.getName());
        }

        writer.writeln("public enum " + node.getName() + " {");
        writer.writeln();

        writer.indent();

        String type = node.getType() == null ? "int" : getType(node.getType());

        for (Node child : node.getChildNodes()) {
            PropNode prop = (PropNode) child;
            writeProperty(prop, type);
        }

        writer.writeln();
        writer.writeln(";");
        writer.writeln();

        writeEnumCode(type, flags);

        writer.unindent();

        writer.writeln("}");
    }

    private void writeEnumCode(String type, boolean flags) throws IOException {
        writer.writeln("private final " + type + " code;");
        writer.writeln();
        writer.writeln(this.node.getName() + "(" + type + " code) {");
        writer.writeln("    this.code = code;");
        writer.writeln("}");
        writer.writeln();
        writer.writeln("public " + type + " code() {");
        writer.writeln("    return this.code;");
        writer.writeln("}");
        writer.writeln();
        if (flags) {
            writer.writeln("public static EnumSet<" + this.node.getName() + "> from(" + type + " code) {");
            writer.writeln("    return Arrays.stream(" + this.node.getName() + ".values()).filter(x -> (x.code & code) == x.code)");
            writer.writeln("            .collect(Collectors.toCollection(() -> EnumSet.noneOf(" + this.node.getName() + ".class)));");
            writer.writeln("}");
            writer.writeln();
            writer.writeln("public static " + type + " code(EnumSet<" + this.node.getName() + "> flags) {");
            writer.writeln("    return flags.stream().map(flag -> flag.code).reduce(0, (a, b) -> a | b);");
            writer.writeln("}");
        } else {
            writer.writeln("public static " + this.node.getName() + " from(" + type + " code) {");
            writer.writeln("    return Arrays.stream(" + this.node.getName() + ".values()).filter(x -> x.code == code).findFirst().orElse(null);");
            writer.writeln("}");
        }
    }

    private void writeProperty(PropNode node, String type) throws IOException {
        if (node.isEmit()) {
            if (node.getObsolete() != null) {
                writer.writeln("@Deprecated");
            }

            List<String> types = node.getDefault().stream().map(symbol -> {
                String temp = getType(symbol);

                if (NUMBER_PATTERN.matcher(temp).matches()) {
                    switch (type) {
                        case "long":
                            if (temp.startsWith("-")) {
                                return temp + "L";
                            }
                            return Long.parseUnsignedLong(temp) + "L";
                        case "byte":
                            return "(byte) " + temp;
                        case "short":
                            return "(short)" + temp;
                        default:
                            if (temp.startsWith("-") || temp.contains("x")) {
                                return temp;
                            }
                            return String.valueOf(Integer.parseUnsignedInt(temp));
                    }
                }

                return temp + ".code";
            }).collect(Collectors.toList());

            String val = String.join(" | ", types);

            writer.writeln(node.getName() + "(" + val + "),");
        }
    }

    private static String getType(Symbol symbol) {
        if (symbol instanceof WeakSymbol) {
            WeakSymbol ws = (WeakSymbol) symbol;

            // TODO: 2018-02-21 eeeeeehhh
            if (ws.getIdentifier().contains("CMsgProtoBufHeader")) {
                return "CMsgProtoBufHeader";
            }

            return WEAK_TYPES.containsKey(ws.getIdentifier()) ? WEAK_TYPES.get(ws.getIdentifier()).getName() : ws.getIdentifier();
        } else if (symbol instanceof StrongSymbol) {
            StrongSymbol ss = (StrongSymbol) symbol;

            if (ss.getProp() == null) {
                return ss.getClazz().getName();
            } else {
                return ss.getClazz().getName() + "." + ss.getProp().getName();
            }
        }

        return "INVALID";
    }

    private static int getTypeSize(PropNode prop) {
        if (prop.getFlags() != null && prop.getFlags().equals("proto")) {
            return 0;
        }

        Symbol sym = prop.getType();

        if (sym instanceof WeakSymbol) {
            WeakSymbol wsym = (WeakSymbol) sym;

            String key = wsym.getIdentifier();

            if (!WEAK_TYPES.containsKey(key)) {
                key = DEFAULT_TYPE;
            }

            if (prop.getFlagsOpt() != null && !prop.getFlagsOpt().isEmpty()) {
                return Integer.parseInt(prop.getFlagsOpt());
            }

            return WEAK_TYPES.get(key).getSize();
        } else if (sym instanceof StrongSymbol) {
            StrongSymbol ssym = (StrongSymbol) sym;

            if (ssym.getClazz() instanceof EnumNode) {
                EnumNode enode = (EnumNode) ssym.getClazz();

                if (enode.getType() instanceof WeakSymbol) {
                    return WEAK_TYPES.get(((WeakSymbol) enode.getType()).getIdentifier()).getSize();
                } else {
                    return WEAK_TYPES.get(DEFAULT_TYPE).getSize();
                }
            }
        }

        return 0;
    }

    private static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }

    @Override
    public void flush() throws IOException {
        if (writer != null) {
            writer.flush();
        }
    }

    private static class TypeInfo {
        private final int size;

        private final String name;

        public TypeInfo(int size, String name) {
            this.size = size;
            this.name = name;
        }

        public int getSize() {
            return size;
        }

        public String getName() {
            return name;
        }
    }
}
