package nzgo.toolkit.core.naming;

/**
 * Sample Name Parser
 * @author Walter Xie
 */
public class SampleNameParser extends NameParser {

    public static final int LABEL_SAMPLE_INDEX = 2;

    // the sampling location parsed is determined by sampleType
    // e.g. 454 soil data: by subplot is 2-C and 2-N, by plot is 2
    public final String sampleType; //default by subplot

    public SampleNameParser () {
        this(NameSpace.BY_SUBPLOT);
    }

    public SampleNameParser (String sampleType) {
        this(sampleType, LABEL_SAMPLE_INDEX);
    }

    // mostly use for sequences annotation (*.fasta)
    // eg IDME8NK01ETVXF|DirectSoil|LB1-A
    public SampleNameParser (String sampleType, int labelSampleId) {
        super("\\|", "-");
        getSeparator(0).setSplitIndex(labelSampleId);
        this.sampleType = sampleType;
    }


//    public void setSampleType(String sampleType) {
//        this.sampleType = sampleType;
//        if (samples != null) {
//            //TODO update matrix and diversity
//        }
//    }

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


    public String getSample(String readName) {
        String sample = getSampleFromRead(readName);
        return sampleType == NameSpace.BY_PLOT ? getPlotFromSample(sample)[0] : sample;
    }
}
