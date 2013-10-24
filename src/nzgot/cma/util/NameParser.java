package nzgot.cma.util;

/**
 * Name Space
 * @author Walter Xie
 */
public class NameParser {

    //create an object of SingleObject
    private static NameParser instance = new NameParser();

    //make the constructor private so that this class cannot be instantiated
    private NameParser(){}

    //Get the only object available
    public static NameParser getInstance(){
        return instance;
    }

    public static final String SEPARATOR_COLUMN = "\t"; // TODO customize?

    // parse name by mutli-separators in different naming level
    // allow to customize separator according to index
    private String[] separators = new String[]{"|", "-", "_", ","};

    public String getSeparator(int index) {
        return separators[index];
    }

    public void setSeparator(int index, String separator) {
        this.separators[index] = separator;
    }

    /**
     * parse read name into plot and subplot
     * @param readName
     * @return
     */
    public String[] getPlotFromRead(String readName) {
        // 3 fields in read name
        // eg IDME8NK01ETVXF|DirectSoil|LB1-A
        String[] fields = readName.split(separators[0], -1);
        if (fields.length < 3)
            throw new IllegalArgumentException("Error: invalid read name : " + readName);

        // plot, subplot
        String[] plot = fields[2].split(separators[1], -1);
        if (plot.length != 2)
            throw new IllegalArgumentException("Error: invalid plot name in the read name : " + readName);

        return plot;
    }

}
