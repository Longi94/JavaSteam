package in.dragonbra.steamlanguagegen.parser.token

class TokenSourceInfo {
    String fileName
    int startLineNumber
    int startColumnNumber
    int endLineNumber
    int endColumnNumber

    TokenSourceInfo(String fileName, int startLineNumber, int startColumnNumber, int endLineNumber, int endColumnNumber) {
        this.fileName = fileName;
        this.startLineNumber = startLineNumber;
        this.startColumnNumber = startColumnNumber;
        this.endLineNumber = endLineNumber;
        this.endColumnNumber = endColumnNumber;
    }
}
