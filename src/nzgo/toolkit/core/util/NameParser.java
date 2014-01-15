package nzgo.toolkit.core.util;

/**
 * Name Parser
 * @author Walter Xie
 */
public class NameParser {

    //create an object of SingleObject
    private static NameParser instance = new NameParser();

    //make the constructor private so that this class cannot be instantiated
    protected NameParser(){}

    //Get the only object available
    public static NameParser getInstance(){
        return instance;
    }

    public static final String COLUMN_SEPARATOR = "\t"; // TODO customize?

    // parse name by mutli-separators in different naming level
    // allow to customize separator according to index
    protected String[] separators = new String[]{"\\|", "-", "_"};

    public String getSeparator(int index) {
        return separators[index];
    }

    public void setSeparator(int index, String separator) {
        this.separators[index] = separator;
    }

}
