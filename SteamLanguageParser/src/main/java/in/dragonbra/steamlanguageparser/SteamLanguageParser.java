package in.dragonbra.steamlanguageparser;

import in.dragonbra.steamlanguageparser.generator.JavaGen;
import in.dragonbra.steamlanguageparser.parser.*;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * @author lngtr
 * @since 2018-02-15
 */
public class SteamLanguageParser {

    public static void main(String[] args) throws IOException {

        if (args.length < 3) {
            System.out.print("Usage: [input file] [package] [output folder]");
            return;
        }

        String fileName = args[0];
        String _package = args[1];
        String destination = args[2];

        new SteamLanguageParser(fileName, _package, destination).parseFile();
    }

    private String fileName;

    private String _package;

    private String destination;

    public SteamLanguageParser(String fileName, String _package, String destionation) {
        this.fileName = fileName;
        this._package = _package;
        this.destination = destionation;
    }

    public void parseFile() throws IOException {
        File file = new File(fileName);
        Queue<Token> tokens = LanguageParser.tokenizeString(IOUtils.toString(new FileInputStream(file), "utf-8"), fileName);
        Node root = TokenAnalyzer.analyze(tokens);

        List<Node> enums = root.getChildNodes().stream().filter(child -> child instanceof EnumNode).collect(Collectors.toList());
        List<Node> classes = root.getChildNodes().stream().filter(child -> child instanceof ClassNode).collect(Collectors.toList());

        for (Node _enum : enums) {
            JavaGen javaGen = new JavaGen(_enum, _package + ".enums", destination + "/enums");

            javaGen.emit();
            javaGen.flush();
            javaGen.close();
        }
    }
}
