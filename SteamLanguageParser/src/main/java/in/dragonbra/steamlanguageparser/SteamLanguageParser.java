package in.dragonbra.steamlanguageparser;

import in.dragonbra.steamlanguageparser.parser.LanguageParser;
import in.dragonbra.steamlanguageparser.parser.Token;
import in.dragonbra.steamlanguageparser.parser.TokenAnalyzer;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Queue;

/**
 * @author lngtr
 * @since 2018-02-15
 */
public class SteamLanguageParser {
    public static void main(String[] args) throws IOException {
        String fileName = args[0];
        parseFile(fileName);
    }

    public static void parseFile(String filePath) throws IOException {
        File file = new File(filePath);
        Queue<Token> tokens = LanguageParser.tokenizeString(IOUtils.toString(new FileInputStream(file), "utf-8"), filePath);
        Object root = TokenAnalyzer.analyze(tokens);
    }
}
