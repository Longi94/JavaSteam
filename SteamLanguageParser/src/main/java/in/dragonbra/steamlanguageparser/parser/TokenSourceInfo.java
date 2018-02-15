package in.dragonbra.steamlanguageparser.parser;

/**
 * @author lngtr
 * @since 2018-02-15
 */
public class TokenSourceInfo {
    private String fileName;
    private int startLineNumber;
    private int startColumnNumber;
    private int endLineNumber;
    private int endColumnNumber;

    public TokenSourceInfo(String fileName, int startLineNumber, int startColumnNumber, int endLineNumber, int endColumnNumber) {
        this.fileName = fileName;
        this.startLineNumber = startLineNumber;
        this.startColumnNumber = startColumnNumber;
        this.endLineNumber = endLineNumber;
        this.endColumnNumber = endColumnNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public int getStartLineNumber() {
        return startLineNumber;
    }

    public int getStartColumnNumber() {
        return startColumnNumber;
    }

    public int getEndLineNumber() {
        return endLineNumber;
    }

    public int getEndColumnNumber() {
        return endColumnNumber;
    }
}
