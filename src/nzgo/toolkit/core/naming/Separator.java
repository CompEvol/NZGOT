package nzgo.toolkit.core.naming;

import java.util.List;

/**
 * Separator using <a href="../util/regex/Pattern.html#sum">regular expression</a>
 * parse names in different naming level
 * @author Walter Xie
 */
public class Separator extends Regex{

    protected int splitIndex;

    public Separator(String regex) {
        super(regex);
    }

    public Separator(String regex, int splitIndex) {
        super(regex);
        setSplitIndex(splitIndex);
    }

    /**
     * apply primary separator to parse label
     * and get the item at splitIndex of String[] fields
     * return <tt>label</tt> if no item found
     * @param label
     * @return
     */
    public String getItem(String label) {
        String[] items = parse(label);
        if (items == null || getSplitIndex() >= items.length)
            return null;

        return items[getSplitIndex()];
    }

    /**
     * get the string of items separated by regex
     * @param items
     * @return
     */
    public String getLabel(List<String> items) {
        String label = items.get(0);
        for (int i = 1; i < items.size(); i++) {
            label += getRegex() + items.get(i);
        }
        return label;
    }

    public String getLabel(String... items) {
        String label = items[0];
        for (int i = 1; i < items.length; i++) {
            label += getRegex() + items[i];
        }
        return label;
    }

    public void print(String label, boolean printRegex) {
        if (printRegex)
            System.out.print("  Use regex : " + getRegex() + " to get item :");
        System.out.print(getItem(label) + "\n");
    }

    public int getSplitIndex() {
        if (splitIndex < 0)
            setSplitIndex(0);
        return splitIndex;
    }

    public void setSplitIndex(int splitIndex) {
        this.splitIndex = splitIndex;
    }

}
