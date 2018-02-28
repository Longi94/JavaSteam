package in.dragonbra.steamlanguagegen.parser.token

class Token {
    String name
    String value
    TokenSourceInfo source

    Token(String name, String value) {
        this(name, value, null);
    }

    Token(String name, String value, TokenSourceInfo source) {
        this.name = name;
        this.value = value;
        this.source = source;
    }
}
