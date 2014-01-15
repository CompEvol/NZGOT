package nzgo.toolkit.core.community.util;

import nzgo.toolkit.core.util.NameParser;
import nzgo.toolkit.core.util.NameSpace;

/**
 * Sample Name Parser
 * @author Walter Xie
 */
public class SampleNameParser extends NameParser {
    //create an object of SingleObject
    private static SampleNameParser instance = new SampleNameParser();

    //Get the only object available
    public static SampleNameParser getInstance(){
        return instance;
    }

    // eg IDME8NK01ETVXF|DirectSoil|LB1-A
    public static final int READ_INDEX_SAMPLE = 2;

    /**
     * parse read name into sample location, e.g. 2-C
     * only suit for NZ GO database
     * @param readName
     * @return
     */
    public String getSampleFromRead(String readName) {
        // 3 fields in read name
        String[] fields = readName.split(separators[0], -1);
        if (fields.length < 3)
            throw new IllegalArgumentException("Error: invalid read name : " + readName);

        return fields[READ_INDEX_SAMPLE];
    }

    /**
     * parse sample location in the read name into plot and subplot
     * @param sample
     * @return
     */
    public String[] getPlotFromSample(String sample) {
        // plot_subplot, subplot
        String[] plot_subplot = sample.split(separators[1], -1);
        if (plot_subplot.length != 2)
            throw new IllegalArgumentException("Error: invalid sample location in the read name : " + sample);

        return plot_subplot;
    }


    public String getSampleBy (String samplesBy, String readName) {
        String sample = getSampleFromRead(readName);
        return samplesBy == NameSpace.BY_PLOT ? getPlotFromSample(sample)[0] : sample;
    }
}
