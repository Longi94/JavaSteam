package in.dragonbra.steamlanguagegen.generator

class JavaFileWriter extends FileWriter {

    private static final String INDENTATION = '    ';

    private String indent = '';

    JavaFileWriter(File file) throws IOException {
        super(file);
    }

    void indent() {
        indent += INDENTATION;
    }

    void unindent() {
        indent = indent.substring(INDENTATION.length());
    }

    def writeln = { String line ->
        write(indent);
        write(line);
        writeln();
    }

    void writeln() throws IOException {
        write('\n');
    }
}
