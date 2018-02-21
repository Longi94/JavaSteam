package in.dragonbra.steamlanguageparser.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author lngtr
 * @since 2018-02-16
 */
public class JavaFileWriter extends FileWriter {

    private static final String INDENTATION = "    ";

    private String indent = "";

    public JavaFileWriter(File file) throws IOException {
        super(file);
    }

    public void indent() {
        indent += INDENTATION;
    }

    public void unindent() {
        indent = indent.substring(INDENTATION.length());
    }

    public void writeln(String line) throws IOException {
        write(indent);
        write(line);
        writeln();
    }

    public void writeln() throws IOException {
        write('\n');
    }
}
