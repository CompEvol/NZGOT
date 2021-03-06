package nzgo.toolkit.core.naming;

/**
 * Name Space
 * @author Walter Xie
 */
// TODO use XML to customize?
public class NameSpace {

    public static final String HOME_DIR = "user.dir";

    public static final String PREFIX_OTUS_RELABELED = "otus_relabeled_";
    public static final String PREFIX_OTU_MAPPING = "otu_map_";
    public static final String PREFIX_OTU_REFERENCE = "reference_";

    public static final String SUFFIX_FASTQ = ".fastq";
    public static final String SUFFIX_GZ = ".gz";
    public static final String SUFFIX_FASTQ_GZ = SUFFIX_FASTQ + SUFFIX_GZ;
    public static final String SUFFIX_FASTA = ".fasta";
    public static final String SUFFIX_FNA = ".fna";
    public static final String SUFFIX_OTUS = SUFFIX_FASTA;
    public static final String SUFFIX_MAPPING = ".m8";
    public static final String SUFFIX_UC = ".uc";
    public static final String SUFFIX_UP = ".up";

    public static final String BY_PLOT = "by plot";
    public static final String BY_SUBPLOT = "by subplot";

    public static final String SUFFIX_NEWICK = ".newick";
    public static final String SUFFIX_NEX = ".nex";

    public static final String SUFFIX_CSV = ".csv";
    public static final String SUFFIX_TSV = ".tsv";
    public static final String SUFFIX_TXT = ".txt";

    public static final String SUFFIX_TAX_ID_FILE = ".xml";

    public static final String MATCHERS_FILE = "matchers" + SUFFIX_TSV;
    public static final String SEPARATORS_FILE = "separators" + SUFFIX_TSV;
    public static final String TRAITS_MAPPING_FILE = "traits_map" + SUFFIX_TSV;
    public static final String ERROR_MAPPING_FILE = "error" + SUFFIX_TSV;
}
