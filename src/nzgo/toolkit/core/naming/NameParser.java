package nzgo.toolkit.core.naming;

import nzgo.toolkit.core.io.ConfigFileIO;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Name Parser
 * parse names by mutli-regex in different naming level
 * or parse names by mutli-regex into groups
 *
 * tips:
 * 1) change <tt>regex</tt> to use <tt>getSeparator(index).setRegex(regex)</tt>,
 * and change <tt>splitIndex</tt> to <tt>use getSeparator(index).setSplitIndex(splitIndex)</tt>.
 * 2) <tt>index</tt> in <tt>NameParser</tt> is index for <tt>List<Separator> regex</tt>.
 * 3) <tt>splitIndex</tt> in <tt>Separator</tt> for String[] <tt>parse(label)</tt> splitting by <tt>regex</tt>.
 * 4) if <tt>isGroupMatcher</tt> true, then use to match names into groups (regexGroup),
 * if <tt>isGroupMatcher</tt> false, then use to parse names in different naming level (levelSeparator).
 *
 * @author Walter Xie
 */
public class NameParser {

    public static final String OTHER = "Other";

    protected List<Regex> regexList;
    public final RegexType regexType;

    // mostly use for tab-separated values (*.tsv)
    // default to have 2 regex
    public NameParser(){
        // primary separator default tab
        // secondary separator default |
        this("\t", "\\|");
    }

    public NameParser(String regex1, String regex2){
        regexList = new ArrayList<>();
        regexType = RegexType.SEPARATOR;
        regexList.add(new Separator(regex1));
        regexList.add(new Separator(regex2));
    }

    // load regexList from file, regexList.size >= 1
    // if isGroupMatcher true, then use to match names into groups (groupMatcher)
    // if isGroupMatcher false, then use to parse names in different naming level (levelSeparator)
    public NameParser(Path regexTSV, final RegexType regexType){
        this.regexType = regexType;
        try {
            regexList = ConfigFileIO.importRegex(regexTSV, regexType);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public String getFinalItem(String label) {
        String finalItem = null;

        if (regexType == RegexType.SEPARATOR) {
            String item = label;
            for (Regex regex : regexList) {
                finalItem = ((Separator) regex).getItem(item);
                item = finalItem;
            }

        } else if (regexType == RegexType.MATCHER) {
            for (Regex regex : regexList) {
                if (regex.isMatched(label)) {
                    if (finalItem != null)
                        throw new IllegalArgumentException("Multi-matches [" + finalItem + ", " +
                                ((Matcher) regex).getName() +  "] are invalid : " + label);
                    finalItem = ((Matcher) regex).getName();
                }
            }

        }

        if (finalItem == null || finalItem.equals(label))
            return OTHER;
        return finalItem;
    }

    public List<Regex> getRegexList() {
        return regexList;
    }

    public Regex getRegex(int index) {
        if (index >= regexList.size())
            throw new IllegalArgumentException("Regular expression index " + index + " cannot >= regexList size" + regexList.size());
        return regexList.get(index);
    }

    public Separator getSeparator(int index) {
        if (regexType != RegexType.SEPARATOR)
            throw new IllegalArgumentException("Wrong regex type : " + regexType + ", it should be " + RegexType.SEPARATOR);

        return (Separator) getRegex(index);
    }

    public Matcher getMatcher(int index) {
        if (regexType != RegexType.MATCHER)
            throw new IllegalArgumentException("Wrong regex type : " + regexType + ", it should be " + RegexType.MATCHER);

        return (Matcher) getRegex(index);
    }

    public void addRegex(Regex regex) {
        regexList.add(regex);
    }

    public void setRegex(int index, Regex regex) {
        regexList.set(index, regex);
    }

    public void printSeparators() {
        System.out.println("  All defined regular expressions are : ");
        for (Regex regex : regexList) {
            System.out.println("  Regex: " + regex.getRegex() + ", split index : " + ((Separator) regex).getSplitIndex());
        }
        System.out.println();
    }


}
