package nzgo.toolkit.core.naming;

/**
 * Sample Name Parser
 * @author Walter Xie
 */
public class SampleNameParser extends NameParser {

    // mostly use for sequences annotation (*.fasta)
    // eg IDME8NK01ETVXF|DirectSoil|LB1-A
    public SampleNameParser () {
        this("\\|", "-");
        getSeparator(0).setSplitIndex(2); // TODO
    }

    public SampleNameParser(String separator, String secondarySeparator){
        super(separator, secondarySeparator);
    }

    /**
     * parse read name into sample location, e.g. 2-C
     * only suit for NZ GO database
     * @param readName
     * @return
     */
    public String getSampleFromRead(String readName) {
        // 3 fields in read name
        String sample = getSeparator(0).getItem(readName);
        if (sample == null)
            throw new IllegalArgumentException("Error: invalid read name : " + readName);

        return sample;
    }

    /**
     * parse sample location in the read name into plot and subplot
     * @param sample
     * @return
     */
    public String[] getPlotFromSample(String sample) {
        // plot_subplot, subplot
        String[] plot_subplot = getSeparator(1).parse(sample);
        if (plot_subplot.length != 2)
            throw new IllegalArgumentException("Error: invalid sample location in the read name : " + sample);

        return plot_subplot;
    }


    public String getSampleBy (String samplesBy, String readName) {
        String sample = getSampleFromRead(readName);
        return samplesBy == NameSpace.BY_PLOT ? getPlotFromSample(sample)[0] : sample;
    }
}
