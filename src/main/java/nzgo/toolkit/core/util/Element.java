package nzgo.toolkit.core.util;

/**
 * Element: basic class for naming, Comparable, Countable
 * @author Walter Xie
 */
public class Element implements Comparable, Countable{
    public static final String DEFAULT_NAME = "Unknown";

    protected String name;
    protected int count = 0;

    public Element() { }

    public Element(String name) {
        setName(name);
    }

    public String getName() {
        if (name == null ) setName(DEFAULT_NAME);
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    @Override
    public void incrementCount(int step) {
        this.count = count + step;
    }

    @Override
    public void setCount(int count) {
        this.count = count;
    }

    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(Object o) {
        return toString().compareTo(o.toString());
    }

    public int compareCountTo(Element element) {
        return Integer.compare(this.count, element.getCount());
    }

    @Override
    public boolean equals(Object obj) {
        return (toString().equalsIgnoreCase(obj.toString()));
    }
}
