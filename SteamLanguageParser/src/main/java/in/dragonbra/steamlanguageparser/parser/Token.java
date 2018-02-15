package in.dragonbra.steamlanguageparser.parser;

/**
 * @author lngtr
 * @since 2018-02-15
 */
public class Token {

    private String name;

    private String value;

    private TokenSourceInfo source;

    public Token(String name, String value) {
        this(name, value, null);
    }

    public Token(String name, String value, TokenSourceInfo source) {
        this.name = name;
        this.value = value;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public TokenSourceInfo getSource() {
        return source;
    }
}
