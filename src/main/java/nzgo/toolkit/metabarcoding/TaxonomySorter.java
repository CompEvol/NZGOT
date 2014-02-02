package nzgo.toolkit.metabarcoding;

import nzgo.toolkit.NZGOToolkit;
import nzgo.toolkit.core.io.Arguments;
import nzgo.toolkit.core.io.ConfigFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.taxonomy.Rank;
import nzgo.toolkit.core.taxonomy.Taxa;
import nzgo.toolkit.core.taxonomy.TaxaSort;
import nzgo.toolkit.core.taxonomy.Taxon;
import nzgo.toolkit.core.taxonomy.parser.EFetchStAXParser;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * TaxonomySorter
 * @author Walter Xie
 */
public class TaxonomySorter extends Module{

    public TaxonomySorter() {
        super("TaxonomySorter", NZGOToolkit.TOOLKIT[1]);
    }


    private TaxonomySorter(Path inputFile, Path outFile, Path errorOutFile, Rank rankToBreak, String regexPrefix, String taxIdNCBI) {
        super();

        Taxa taxa = null;
        try {
            taxa = ConfigFileIO.importTaxa(inputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Taxon bioClass = null;
        if (taxIdNCBI != null) {
            try {
                bioClass = EFetchStAXParser.getTaxonById(taxIdNCBI);

                MyLogger.info("Initializing biological classification : " +
                        bioClass.getScientificName() + ", " + bioClass.getTaxId());

            } catch (XMLStreamException | IOException e) {
                e.printStackTrace();
            }
        }

        TaxaSort taxaSort = new TaxaSort(taxa, rankToBreak, regexPrefix, bioClass);

        Map<String, String> taxaSortMap = null;
        try {
            taxaSortMap = taxaSort.getTaxaSortMap();
        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }

        try {
            if (outFile == null) {
                ConfigFileIO.writeTaxaMap(inputFile, taxaSortMap);
            } else {
                ConfigFileIO.writeTaxaMap(outFile, taxaSortMap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (errorOutFile != null) {
            try {
                ConfigFileIO.writeConfigMap(errorOutFile, taxaSort.getErrors(), "errors");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void printUsage(final Arguments arguments) {
        arguments.printUsage(getName(), "[<*.tsv>]");
        System.out.println();
        System.out.println("  Example: " + getName() + " " + NameSpace.TRAITS_MAPPING_FILE);
        System.out.println("  Example: " + getName() + " -help");
        System.out.println();
    }

    // main
    public static void main(String[] args) {
        Module module = new TaxonomySorter();

        Arguments.Option[] newOptions = new Arguments.Option[]{
                new Arguments.StringOption("out", "output-file-name", "Output file name (*.tsv), " +
                        "if this option is not selected, then overwrite the input file."),
                new Arguments.Option("out_error", "Output all unidentified taxa into file " + NameSpace.ERROR_MAPPING_FILE),
                new Arguments.StringOption("rank_to_break", Rank.mainRanksToString(), false,
                        "The the high level taxonomy of biological classification that all given taxa should belong to."),
                new Arguments.StringOption("bio_class", "NCBI-taxId", "The NCBI taxId to define a high level taxonomy of " +
                        "biological classification that all given taxa should belong to."),
                new Arguments.StringOption("regex_prefix", "regular-expression", "The regular expression to get name prefix."),

        };
        final Arguments arguments = module.getArguments(newOptions);

        Path working = module.init(arguments, args);
        // input
        String inputFileName = module.getFirstArg(arguments);
        Path inputFile = module.getInputFile(working, inputFileName, new String[]{NameSpace.SUFFIX_TSV});

        // output
        String outFileName = arguments.getStringOption("out");
        Path outFile;
        if (outFileName == null) {
            outFile = module.validateOutputFile(inputFileName, new String[]{NameSpace.SUFFIX_TSV}, "output", arguments.hasOption("overwrite"));
        } else {
            outFile = module.validateOutputFile(outFileName, new String[]{NameSpace.SUFFIX_TSV}, "output", arguments.hasOption("overwrite"));
        }

        // error list output
        String out_error = arguments.getStringOption("out_error");
        Path errorOutFile = null;
        if (out_error != null) {
            errorOutFile = module.validateOutputFile(out_error, new String[]{NameSpace.SUFFIX_TSV}, "output error", arguments.hasOption("overwrite"));
        }

        // program parameters
        String rank_to_break = arguments.getStringOption("rank_to_break");
        Rank rankToBreak = Rank.valueOf(rank_to_break);

        String taxIdNCBI = arguments.getStringOption("bio_class");
        String regex_prefix = arguments.getStringOption("regex_prefix");

        // traits Map
        new TaxonomySorter(inputFile, outFile, errorOutFile, rankToBreak, regex_prefix, taxIdNCBI);
    }
}
