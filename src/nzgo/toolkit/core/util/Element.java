package nzgo.toolkit.core.util;

/**
 * Element: basic class for naming
 * @author Walter Xie
 */
public class Element {

    protected String name;

    public Element(String name) {
        setName(name);
    }

    public String getName() {
        if (name == null ) setName("Unknown");
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
