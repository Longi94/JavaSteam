package in.dragonbra.steamlanguagegen.parser.node

import in.dragonbra.steamlanguagegen.parser.symbol.Symbol

class ClassNode extends Node {
    Symbol ident
    Symbol parent
    boolean emit
}
