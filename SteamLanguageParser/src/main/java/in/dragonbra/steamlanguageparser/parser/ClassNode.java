package in.dragonbra.steamlanguageparser.parser;

/**
 * @author lngtr
 * @since 2018-02-15
 */
public class ClassNode extends Node {

    private Symbol ident;
    private Symbol parent;
    private boolean emit;

    public Symbol getIdent() {
        return ident;
    }

    public void setIdent(Symbol ident) {
        this.ident = ident;
    }

    public Symbol getParent() {
        return parent;
    }

    public void setParent(Symbol parent) {
        this.parent = parent;
    }

    public boolean isEmit() {
        return emit;
    }

    public void setEmit(boolean emit) {
        this.emit = emit;
    }
}
