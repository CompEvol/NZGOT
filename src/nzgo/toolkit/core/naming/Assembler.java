package nzgo.toolkit.core.naming;


/**
 * Separator using <a href="../util/regex/Pattern.html#sum">regular expression</a>
 * <tt>splitIndex</tt> for String[] <tt>parse(label)</tt> splitting by <tt>regex</tt>
 * @author Walter Xie
 */
public class Assembler {

    private final Separator separator;
    private final Matcher matcher;
    private final int size;

    public Assembler(String regex, int size) {
        this("\\|", regex, size);
    }

    public Assembler(String regex1, String regex2, int size) {
        this.separator = new Separator(regex1);
        this.matcher = new Matcher(regex2);
        this.size = size;
    }

    public Assembler(Separator separator, Matcher matcher, int size) {
        this.separator = separator;
        this.matcher = matcher;
        this.size = size;
    }

    public Separator getSeparator() {
        return separator;
    }

    public Matcher getMatcher() {
        return matcher;
    }

    public int getSize() {
        return size;
    }

}
