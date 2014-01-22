package nzgo.toolkit.core.naming;

import nzgo.toolkit.core.io.ConfigFileIO;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Name Parser
 * parse names by mutli-separators in different naming level
 * or parse names by mutli-separators into groups
 *
 * tips:
 * 1) change <tt>regex</tt> to use <tt>getSeparator(index).setRegex(regex)</tt>,
 * and change <tt>splitIndex</tt> to <tt>use getSeparator(index).setSplitIndex(splitIndex)</tt>.
 * 2) <tt>index</tt> in <tt>NameParser</tt> is index for <tt>List<Separator> separators</tt>.
 * 3) <tt>splitIndex</tt> in <tt>Separator</tt> for String[] <tt>parse(label)</tt> splitting by <tt>regex</tt>.
 * 4) if <tt>isRegexGroup</tt> true, then use to parse names into groups (regexGroup),
 * if <tt>isRegexGroup</tt> false, then use to parse names in different naming level (levelSeparator).
 *
 * @author Walter Xie
 */
public class NameParser {

    public static final String OTHER = "Other";

    protected List<Separator> separators;
    protected final boolean isRegexGroup;

    // mostly use for tab-separated values (*.tsv)
    // default to have 2 separators
    public NameParser(){
        // primary separator default tab
        // secondary separator default |
        this("\t", "\\|");
    }

    public NameParser(String separator, String secondarySeparator){
        separators = new ArrayList<>();
        separators.add(new Separator(separator));
        separators.add(new Separator(secondarySeparator));
        isRegexGroup = false;
    }

    // load separators from file, separators.size >= 1
    // if isRegexGroup true, then use to parse names into groups (regexGroup)
    // if isRegexGroup false, then use to parse names in different naming level (levelSeparator)
    public NameParser(Path separatorsTSV, final boolean isRegexGroup){
        try {
            separators = ConfigFileIO.importSeparators(separatorsTSV);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.isRegexGroup = isRegexGroup;
    }

    public String getFinalItem(String label) {
        String finalItem = null;

        if (isRegexGroup) {
            for (Separator separator : separators) {
                if (separator.isMatched(label)) {
                    if (finalItem != null)
                        throw new IllegalArgumentException("Multi-matches [" + finalItem + ", " +
                                separator.getName() +  "] are invalid : " + label);
                    finalItem = separator.getName();
                }
            }

        } else {
            String item = label;
            for (Separator separator : separators) {
                finalItem = separator.getItem(item);
                item = finalItem;
            }
        }

        if (finalItem == null || finalItem.equals(label))
            return OTHER;
        return finalItem;
    }




    public List<Separator> getSeparators() {
        return separators;
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

    public void printSeparators() {
        System.out.println("  All defined separators are : ");
        for (Separator separator : separators) {
            System.out.println("  Regex: " + separator.getRegex() + ", split index : " + separator.getSplitIndex());
        }
        System.out.println();
    }


}
