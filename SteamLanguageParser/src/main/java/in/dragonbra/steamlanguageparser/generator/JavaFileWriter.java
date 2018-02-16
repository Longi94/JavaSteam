package in.dragonbra.steamlanguageparser.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author lngtr
 * @since 2018-02-16
 */
public class JavaFileWriter extends FileWriter {
    public JavaFileWriter(File file) throws IOException {
        super(file);
    }

    public void writeLine(String line) throws IOException {
        write(line);
        write('\n');
    }

    public void writeLine() throws IOException {
        write('\n');
    }
}
