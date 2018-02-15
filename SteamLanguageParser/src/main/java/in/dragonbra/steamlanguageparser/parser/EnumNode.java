package in.dragonbra.steamlanguageparser.parser;

/**
 * @author lngtr
 * @since 2018-02-15
 */
public class EnumNode extends Node {
    private String flags;

    private Symbol type;

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    public Symbol getType() {
        return type;
    }

    public void setType(Symbol type) {
        this.type = type;
    }
}
