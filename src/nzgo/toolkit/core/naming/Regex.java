package nzgo.toolkit.core.naming;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regex using <a href="../util/regex/Pattern.html#sum">regular expression</a>
 * @author Walter Xie
 */
public class Regex {

    protected Pattern regex;

    public Regex(String regex) {
        setRegex(regex);
    }

    public boolean isMatched(String label) {
        return isMatched(label, false);
    }

    public boolean isMatched(String label, boolean entireRegionMatched) {
        Matcher matcher = getRegex().matcher(label);
        return entireRegionMatched ? matcher.matches() : matcher.lookingAt();
    }

    public String getPrefix(String label) {
        return parse(label)[0];
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

    public void print(String label, boolean printRegex) {
        if (printRegex)
            System.out.print("  Use regex : ");
        System.out.print(getRegex() + "\n");
    }

    public Pattern getRegex() {
        if (regex == null)
            setRegex("\t");
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = Pattern.compile(regex);
    }

    public String toString() {
        return getRegex().toString();
    }

}
