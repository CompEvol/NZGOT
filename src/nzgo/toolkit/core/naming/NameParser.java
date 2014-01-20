package nzgo.toolkit.core.naming;

/**
 * Name Parser
 * @author Walter Xie
 */
public class NameParser {

    // parse name by mutli-separators in different naming level
    protected String separator = "\t"; // primary separator default tab
    protected String secondarySeparator = "\\|"; // secondary separator default |

    protected int index = 0; // start from 0
    protected int secondaryIndex = 0;

    public NameParser(){}

    public NameParser(String separator){
        setSeparator(separator);
    }

    public NameParser(String separator, int index){
        setSeparator(separator);
        setIndex(index);
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
     * use sampleNameParser to parse label and get element at index
     * default separator "\\|", "-", default index 0
     * @param label
     * @return
     */
    public String getTrait(String label) {
        String[] fields = parse(label);
        if (fields == null || index >= fields.length)
            return null;

        return fields[index];
    }

    /**
     * apply primary separator
     * @param line
     * @return
     */
    public String[] parse (String line) {
        if (line == null)
            throw new IllegalArgumentException("Cannot parse null !");
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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getSecondaryIndex() {
        return secondaryIndex;
    }

    public void setSecondaryIndex(int secondaryIndex) {
        this.secondaryIndex = secondaryIndex;
    }
}
