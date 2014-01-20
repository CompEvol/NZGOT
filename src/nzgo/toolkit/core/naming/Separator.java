package nzgo.toolkit.core.naming;

/**
 * Separator
 * splitIndex for String[] splitting by regex
 * @author Walter Xie
 */
public class Separator{

    private String regex;
    private int splitIndex;

    public Separator(String regex) {
        setRegex(regex);
    }

    public Separator(String regex, int splitIndex) {
        setRegex(regex);
        setSplitIndex(splitIndex);
    }

    public String getRegex() {
        if (regex == null)
            setRegex("\t");
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
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
        return getRegex();
    }

}
