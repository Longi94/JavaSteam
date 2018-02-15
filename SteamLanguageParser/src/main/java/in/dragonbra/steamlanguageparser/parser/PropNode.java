package in.dragonbra.steamlanguageparser.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lngtr
 * @since 2018-02-15
 */
public class PropNode extends Node {
    private String flags;

    private String flagsOpt;

    private Symbol type;

    private List<Symbol> _default = new ArrayList<>();

    private String obsolete;

    private boolean emit = true;

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    public String getFlagsOpt() {
        return flagsOpt;
    }

    public void setFlagsOpt(String flagsOpt) {
        this.flagsOpt = flagsOpt;
    }

    public Symbol getType() {
        return type;
    }

    public void setType(Symbol type) {
        this.type = type;
    }

    public List<Symbol> getDefault() {
        return _default;
    }

    public void setDefault(List<Symbol> _default) {
        this._default = _default;
    }

    public String getObsolete() {
        return obsolete;
    }

    public void setObsolete(String obsolete) {
        this.obsolete = obsolete;
    }

    public boolean isEmit() {
        return emit;
    }

    public void setEmit(boolean emit) {
        this.emit = emit;
    }
}
