package nzgo.toolkit.core.naming;

import java.util.ArrayList;
import java.util.List;

/**
 * Name Parser
 * parse name by mutli-separators in different naming level
 *
 * tips:
 * 1) change separator to use setSeparatorRegex(String separatorRegex),
 * and change splitIndex to use setSplitIndex(int splitIndex).
 * 2) index in NameParser is index for List<Separator> separators.
 * 3) splitIndex in Separator is for String[] parse (String line), splitting by regex.
 *
 * @author Walter Xie
 */
public class NameParser {

    // default to have 2 separators
    public List<Separator> separators;

    // mostly use for tab-separated values (*.tsv)
    // where labels contains | as secondary separator
    public NameParser(){
        separators = new ArrayList<>();
        separators.add(new Separator("\t")); // primary separator default tab
        separators.add(new Separator("\\|")); // secondary separator default |
    }

    public NameParser(String separator, String secondarySeparator){
        separators = new ArrayList<>();
        separators.add(new Separator(separator));
        separators.add(new Separator(secondarySeparator));
    }

    public Separator getSeparator(int index) {
        if (index >= separators.size())
            throw new IllegalArgumentException("Separator index " + index + " cannot >= separators size" + separators.size());
        return separators.get(index);
    }

    public void addSeparator(Separator separator) {
        separators.add(separator);
    }

    public void setSeparator(int index, Separator separator) {
        separators.set(index, separator);
    }


    public static String getPrefix(String label, String separator) {
        int index = label.indexOf(separator);
        if (index > 0)
            return label.substring(0, index);
        return label;
    }

    public String getPrefix(String name) {
        return getPrefix(name, getSeparator(0).getRegex());
    }

    /**
     * apply primary separator to parse label
     * and get trait at splitIndex of String[] fields
     * @param label
     * @return
     */
    public String getTrait(String label) {
        String[] fields = parse(label);
        if (fields == null || getSplitIndex() >= fields.length)
            return null;

        return fields[getSplitIndex()];
    }
    /**
     * apply primary separator to parse label
     * and get String[] fields
     * @param label
     * @return
     */
    public String[] parse (String label) {
        if (label == null)
            throw new IllegalArgumentException("Cannot parse null !");
        return label.split(getSeparatorRegex(), -1);
    }

    /**
     * apply secondary separator to parse substring parsed by primary separator
     * @param substring
     * @return
     */
    public String[] secondaryParse (String substring) {
        if (substring == null)
            throw new IllegalArgumentException("Cannot parse null substring !");
        return substring.split(getSecondarySeparatorRegex(), -1);
    }


    //+++++++++ getter setter by string +++++++++++

    public String getSeparatorRegex() {
        return getSeparator(0).getRegex();
    }

    public void setSeparatorRegex(String separatorRegex) {
        getSeparator(0).setRegex(separatorRegex);
    }

    public String getSecondarySeparatorRegex() {
        return getSeparator(1).getRegex();
    }

    public void setSecondarySeparator(String secondarySeparatorRegex) {
        getSeparator(1).setRegex(secondarySeparatorRegex);
    }

    public int getSplitIndex() {
        return getSeparator(0).getSplitIndex();
    }

    public void setSplitIndex(int splitIndex) {
        getSeparator(0).setSplitIndex(splitIndex);
    }

    public int getSecondarySplitIndex() {
        return getSeparator(1).getSplitIndex();
    }

    public void setSecondarySplitIndex(int secondarySplitIndex) {
        getSeparator(1).setSplitIndex(secondarySplitIndex);
    }
}
