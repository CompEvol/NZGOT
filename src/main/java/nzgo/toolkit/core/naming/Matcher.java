package nzgo.toolkit.core.naming;


/**
 * Matcher using <a href="../util/regex/Pattern.html#sum">regular expression</a>
 * match names into groups
 * @author Walter Xie
 */
public class Matcher extends Regex{

    protected String name; // the group name for matching regex

    public Matcher(String regex) {
        super(regex);
    }

    public void print(String label, boolean printRegex) {
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

}
