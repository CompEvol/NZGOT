package nzgo.toolkit.core.util;

/**
 * Name Parser
 * @author Walter Xie
 */
public class NameParser {

    // parse name by mutli-separators in different naming level
    protected String separator = "\t"; // primary separator default tab
    protected String secondarySeparator = "\\|"; // secondary separator default |

    public NameParser(){}

    public NameParser(String separator){
        setSeparator(separator);
    }

    public NameParser(String separator, String secondarySeparator){
        this(separator);
        setSecondarySeparator(secondarySeparator);
    }

    public static String getPrefix(String name, String separator) {
        int index = name.indexOf(separator);
        if (index > 0)
            return name.substring(0, index);
        return name;
    }

    public String getPrefix(String name) {
        int index = name.indexOf(separator);
        if (index > 0)
            return name.substring(0, index);
        return name;
    }

    /**
     * apply primary separator
     * @param line
     * @return
     */
    public String[] parse (String line) {
        if (line == null)
            throw new IllegalArgumentException("Cannot parse null string !");
        return line.split(getSeparator(), -1);
    }

    /**
     * apply primary separator to substring parsed by primary separator
     * @param substring
     * @return
     */
    public String[] secondaryParse (String substring) {
        if (substring == null)
            throw new IllegalArgumentException("Cannot parse null substring !");
        return substring.split(getSecondarySeparator(), -1);
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getSecondarySeparator() {
        return secondarySeparator;
    }

    public void setSecondarySeparator(String secondarySeparator) {
        this.secondarySeparator = secondarySeparator;
    }
}
