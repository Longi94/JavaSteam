package in.dragonbra.steamlanguagegen.parser.symbol

import in.dragonbra.steamlanguagegen.parser.node.Node

class StrongSymbol implements Symbol {
    Node clazz
    Node prop

    StrongSymbol(Node classNode) {
        clazz = classNode;
    }

    StrongSymbol(Node classNode, Node prop) {
        clazz = classNode;
        this.prop = prop;
    }
}
