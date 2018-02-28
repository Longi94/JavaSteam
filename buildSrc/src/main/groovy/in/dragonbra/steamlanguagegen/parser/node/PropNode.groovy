package in.dragonbra.steamlanguagegen.parser.node

import in.dragonbra.steamlanguagegen.parser.symbol.Symbol

class PropNode extends Node {

    String flags

    String flagsOpt

    Symbol type

    List<Symbol> _default = []

    String obsolete

    boolean emit = true
}
