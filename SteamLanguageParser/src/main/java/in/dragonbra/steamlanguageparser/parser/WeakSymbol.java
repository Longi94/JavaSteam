package in.dragonbra.steamlanguageparser.parser;

public class WeakSymbol implements Symbol {
    private String identifier;

    public WeakSymbol(String ident) {
        this.identifier = ident;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}