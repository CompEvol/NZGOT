package nzgo.toolkit.edna;

import nzgo.toolkit.core.community.Community;
import nzgo.toolkit.core.io.Arguments;
import nzgo.toolkit.core.naming.NameParser;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.SiteNameParser;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.uparse.io.CommunityFileIO;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Create Community Matrix from UPARSE output file
 * @author Walter Xie
 */
public class CMCreator extends Module {

    public CMCreator() {
        super("CMCreator", "Create community matrix from UPARSE output UP file");
    }

    public void printUsage(final Arguments arguments) {
        arguments.printUsage(getName(), "[<input-file-name>]");
        System.out.println("  <input-file-name> is up mapping file from UPARSE.");
        System.out.println();
//        System.out.println("  Example: " + getName() + " clusters.uc");
        System.out.println("  Example: " + getName() + " out.up");
        System.out.println("  Example: " + getName() + " -working /mypath out.up");
        System.out.println("  Example: " + getName() + " -help");
        System.out.println();
    }

    //Main method
    public static void main(final String[] args) {
        Module module = new CMCreator();

        Arguments.Option[] newOptions = new Arguments.Option[]{
                new Arguments.StringOption("out", "output-file-name", "Output community matrix in csv file."),
                new Arguments.StringOption("sra", "SRR-mapping-file", "The mapping file to map SRA runs to " +
                        "subplots and genes, for example, SRR1706032 <tab> 16S <tab> Plot1-B. If mapping file is given, " +
                        "then all sequences labels will be replaced to a new format id|gene|plot-subplot, " +
                        "such as SRR1706032.1|16S|Plot1-B."),
                new Arguments.StringOption("chi", "output-file-name", "Output community matrix in csv file.")
//                new Arguments.Option("ca", "Count annotated size in sequences identifier, " +
//                        "for example, HA5K40001BTFNL|IndirectSoil|3-H;size=177;")
        };
        final Arguments arguments = module.getArguments(newOptions);

        Path working = module.init(arguments, args);
        // input
        String inputFileName = module.getFirstArg(arguments);
        Path inputFile = module.getInputFile(working, inputFileName, NameSpace.SUFFIX_UP);

        // output
        String outFileName = "cm" + NameSpace.SUFFIX_CSV;
        if (arguments.hasOption("out")) {
            outFileName = arguments.getStringOption("out");
        }
        Path outFile = module.validateOutputFile(outFileName, "output", arguments.hasOption("overwrite"), NameSpace.SUFFIX_CSV);

        String sraFileName = arguments.getStringOption("sra");
        Path sraFile;
        if (sraFileName == null) {
            sraFile = null;
        } else {
            sraFile = module.validateInputFile(null, sraFileName, "SRA runs table", null);
        }

        String chimerasFileName = arguments.getStringOption("chi");
        Path chimerasFile;
        if (chimerasFileName == null) {
            chimerasFile = null;
        } else {
            chimerasFile = module.validateInputFile(working, chimerasFileName, "chimeras file", null);
        }

//        boolean countSizeAnnotation = arguments.hasOption("ca");
        boolean removeElements = false;

        SiteNameParser siteNameParser = new SiteNameParser();
        if (sraFile != null) {
            // e.g. SRR1720793.280;size=1749;
            siteNameParser = new SiteNameParser("\\.", "-", NameSpace.BY_SUBPLOT, SiteNameParser.SRR_LABEL_SAMPLE_INDEX);
        }

//        Community community = new Community(inputFile, siteNameParser, countSizeAnnotation, removeElements);
        Community community = new Community(inputFile, chimerasFile, siteNameParser, removeElements);

        if (sraFile != null) {
            NameParser lineParser = new NameParser();
            try {
                community.replaceSiteNames(sraFile, lineParser);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            int[] row = CommunityFileIO.writeCommunityMatrix(outFile, community);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
