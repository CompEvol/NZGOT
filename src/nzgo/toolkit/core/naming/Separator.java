package nzgo.toolkit.core.naming;

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
