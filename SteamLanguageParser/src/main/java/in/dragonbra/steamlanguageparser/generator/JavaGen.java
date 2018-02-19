package in.dragonbra.steamlanguageparser.generator;

import in.dragonbra.steamlanguageparser.parser.*;

import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author lngtr
 * @since 2018-02-15
 */
public class JavaGen implements Closeable, Flushable {

    private static final String INDENTATION = "    ";

    private static final Pattern NUMBER_PATTERN = Pattern.compile("^-?[0-9].*?");

    private static final Map<String, String> WEAK_TYPES;

    static {
        Map<String, String> weakTypes = new HashMap<>();

        weakTypes.put("byte", "byte");
        weakTypes.put("short", "short");
        weakTypes.put("ushort", "int");
        weakTypes.put("int", "int");
        weakTypes.put("uint", "long");
        weakTypes.put("long", "long");
        weakTypes.put("ulong", "long");

        WEAK_TYPES = Collections.unmodifiableMap(weakTypes);
    }

    private final JavaFileWriter writer;

    private final Node node;

    private String _package;

    public JavaGen(Node node, String _package, String destination) throws IOException {
        File folder = new File(destination);

        if (!folder.exists() && !folder.isDirectory() && !folder.mkdirs()) {
            throw new IllegalStateException("Couldn't create folders");
        }

        File file = new File(folder, node.getName() + ".java");

        this.writer = new JavaFileWriter(file);
        this.node = node;
        this._package = _package;
    }

    public void emit() throws IOException {
        writePackage(_package);
        writer.writeLine();
        writeImports();
        writer.writeLine();
        writeClass(node, "");
    }

    private void writeImports() throws IOException {
        if (node instanceof ClassNode) {
        } else if (node instanceof EnumNode) {
            writer.writeLine("import java.util.Arrays;");
        }
    }

    private void writePackage(String _package) throws IOException {
        writer.writeLine("package " + _package + ";");
    }

    private void writeClass(Node node, String indent) throws IOException {
        if (node instanceof ClassNode) {
            writeMessageClass((ClassNode) node, indent);
        } else if (node instanceof EnumNode) {
            writeEnumClass((EnumNode) node, indent);
        }
    }

    private void writeMessageClass(ClassNode node, String indent) throws IOException {
        writer.writeLine(indent + "public class " + node.getName() + " {");


        writer.writeLine(indent + "}");
    }

    private void writeEnumClass(EnumNode node, String indent) throws IOException {
        writer.writeLine(indent + "public enum " + node.getName() + " {");
        writer.writeLine();

        String newIndent = indent + INDENTATION;

        String type = node.getType() == null ? "int" : getType(node.getType());

        for (Node child : node.getChildNodes()) {
            PropNode prop = (PropNode) child;
            writeProperty(prop, newIndent, type);
        }

        writer.writeLine();
        writer.writeLine(newIndent + ";");
        writer.writeLine();

        writeEnumCode(newIndent, type);

        writer.writeLine(indent + "}");
    }

    private void writeEnumCode(String indent, String type) throws IOException {
        writer.writeLine(indent + "private final " + type + " code;");
        writer.writeLine();
        writer.writeLine(indent + this.node.getName() + "(" + type + " code) {");
        writer.writeLine(indent + "    this.code = code;");
        writer.writeLine(indent + "}");
        writer.writeLine();
        writer.writeLine(indent + "public " + type + " getCode() {");
        writer.writeLine(indent + "    return this.code;");
        writer.writeLine(indent + "}");
        writer.writeLine();
        writer.writeLine(indent + "public " + this.node.getName() + " from(" + type + " code) {");
        writer.writeLine(indent + "    return Arrays.stream(" + this.node.getName() + ".values()).filter(x -> x.code == code).findFirst().orElse(null);");
        writer.writeLine(indent + "}");
    }

    private void writeProperty(PropNode node, String indent, String type) throws IOException {
        if (node.isEmit()) {
            if (node.getObsolete() != null) {
                writer.writeLine(indent + "@Deprecated");
            }

            List<String> types = node.getDefault().stream().map(symbol -> {
                String temp = getType(symbol);

                if (NUMBER_PATTERN.matcher(temp).matches()) {
                    switch (type) {
                        case "long":
                            return temp + "L";
                        case "byte":
                            return "(byte) " + temp;
                        case "short":
                            return "(short)" + temp;
                        default:
                            return temp;
                    }
                }

                return temp + ".code";
            }).collect(Collectors.toList());

            String val = String.join(" | ", types);

            writer.writeLine(indent + node.getName() + "(" + val + "),");
        }
    }

    private String getType(Symbol symbol) {
        if (symbol instanceof WeakSymbol) {
            WeakSymbol ws = (WeakSymbol) symbol;

            return WEAK_TYPES.getOrDefault(ws.getIdentifier(), ws.getIdentifier());
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

    @Override
    public void close() throws IOException {
        writer.close();
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }
}
