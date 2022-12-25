package in.dragonbra.versiongen.generator

import in.dragonbra.steamlanguagegen.generator.JavaFileWriter

class JavaGen implements Closeable, Flushable {

    private JavaFileWriter writer

    private String _package

    private File destination

    JavaGen(String _package, File destination) {
        this._package = _package
        this.destination = destination
    }

    void emit(String classname, String version) throws IOException {
        if (!destination.exists() && !destination.isDirectory() && !destination.mkdirs()) {
            throw new IllegalStateException('Couldn\'t create folders')
        }

        def file = new File(destination, "${classname}.java")

        this.writer = new JavaFileWriter(file)
        writer.writeln "package $_package;"
        writer.writeln()
        writer.writeln "public class $classname {"
        writer.indent()
        writer.writeln "public static final String VERSION = \"$version\";"
        writer.unindent()
        writer.writeln '}'
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
}
