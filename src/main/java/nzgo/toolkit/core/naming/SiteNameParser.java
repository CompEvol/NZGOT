package nzgo.toolkit.core.naming;

/**
 * Site Name Parser
 * @author Walter Xie
 */
public class SiteNameParser extends NameParser {
    // such as >IDME8NK01BSVYW|CO1-soilkit|8-I|prep1;size=60863;
    public static final int LABEL_SAMPLE_INDEX = 2;

    // the sampling location parsed is determined by siteType
    // e.g. 454 soil data: by subplot is 2-C and 2-N, by plot is 2
    public final String siteType; //default by subplot

    public SiteNameParser() {
        this(NameSpace.BY_SUBPLOT);
    }

    public SiteNameParser(int labelSampleId) {
        this(NameSpace.BY_SUBPLOT, labelSampleId);
    }

    public SiteNameParser(String siteType) {
        this(siteType, LABEL_SAMPLE_INDEX);
    }

    // mostly use for sequences annotation (*.fasta)
    // eg IDME8NK01ETVXF|DirectSoil|LB1-A
    public SiteNameParser(String siteType, int labelSampleId) {
        super("\\|", "-");
        getSeparator(0).setSplitIndex(labelSampleId);
        this.siteType = siteType;
    }


//    public void setSiteType(String siteType) {
//        this.siteType = siteType;
//        if (sites != null) {
//            //TODO update matrix and diversity
//        }
//    }

    /**
     * parse read name into sample location, e.g. 2-C
     * only suit for NZ GO database
     * @param readName
     * @return
     */
    public String getSiteFullName(String readName) {
        // 3 fields in read name
        String site = getSeparator(0).getItem(readName);
        if (site == null)
            throw new IllegalArgumentException("Error: invalid read name : " + readName);

        return site;
    }

    /**
     * parse site location in the read name into plot and subplot
     * @param site
     * @return
     */
    public String[] getSiteHierarchy(String site) {
        // plot_subplot, subplot
        String[] plot_subplot = getSeparator(1).parse(site);
        if (plot_subplot.length != 2)
            throw new IllegalArgumentException("Error: invalid site location in the read name : " + site);

        return plot_subplot;
    }


    public String getSite(String readName) {
        String site = getSiteFullName(readName);
        return (siteType.equals(NameSpace.BY_PLOT)) ? getSiteHierarchy(site)[0] : site;
    }
}
