package nzgo.toolkit.core.naming;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Separator using <a href="../util/regex/Pattern.html#sum">regular expression</a>
 * <tt>splitIndex</tt> for String[] <tt>parse(label)</tt> splitting by <tt>regex</tt>
 * @author Walter Xie
 */
public class Separator{

    private Pattern regex;
    private int splitIndex;
    private String name;

    public Separator(String regex) {
        setRegex(regex);
    }

    public Separator(String regex, int splitIndex) {
        setRegex(regex);
        setSplitIndex(splitIndex);
    }


    public String getPrefix(String label) {
        return parse(label)[0];
    }

    public boolean isMatched(String label) {
        return isMatched(label, false);
    }

    public boolean isMatched(String label, boolean entireRegionMatched) {
        Matcher matcher = getRegex().matcher(label);
        return entireRegionMatched ? matcher.matches() : matcher.lookingAt();
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
     * apply regex to parse label and get String[] fields
     * return <tt>label</tt> if this pattern does not match any subsequence of the input
     * same as <blockquote><pre>java.util.regex.Pattern.split(input)</pre></blockquote>
     * @param label
     * @return
     */
    public String[] parse (String label) {
        if (label == null || label.equalsIgnoreCase("null"))
            throw new IllegalArgumentException("Cannot parse null !");
        return getRegex().split(label);
    }

    public void printItem(String label, boolean printRegex) {
        if (printRegex)
            System.out.print("  Use regex : " + getRegex() + " to get item :");
        System.out.print(getItem(label) + "\n");
    }

    public void printName(String label, boolean printRegex) {
        if (isMatched(label)){
            if (printRegex)
                System.out.print("  Regex : " + getRegex() + " is matched and get name :");
            System.out.print(getName() + "\n");
        }
    }

    public String getName() {
        setName(NameUtil.getWordCharacters(getRegex().toString()));
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Pattern getRegex() {
        if (regex == null)
            setRegex("\t");
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = Pattern.compile(regex);
    }

    public int getSplitIndex() {
        if (splitIndex < 0)
            setSplitIndex(0);
        return splitIndex;
    }

    public void setSplitIndex(int splitIndex) {
        this.splitIndex = splitIndex;
    }

    public String toString() {
        return getRegex().toString();
    }

}
