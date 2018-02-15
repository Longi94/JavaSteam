package in.dragonbra.steamlanguageparser.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lngtr
 * @since 2018-02-15
 */
public class Node {

    private List<Node> childNodes = new ArrayList<>();

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Node> getChildNodes() {
        return childNodes;
    }
}
