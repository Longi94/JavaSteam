package in.dragonbra.steamlanguageparser.parser;

public class StrongSymbol implements Symbol {
    public Node clazz;

    public Node prop;

    public StrongSymbol(Node classNode) {
        clazz = classNode;
    }

    public StrongSymbol(Node classNode, Node prop) {
        clazz = classNode;
        this.prop = prop;
    }

    public Node getClazz() {
        return clazz;
    }

    public Node getProp() {
        return prop;
    }
}