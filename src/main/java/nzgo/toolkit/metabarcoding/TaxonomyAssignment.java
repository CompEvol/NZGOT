package nzgo.toolkit.metabarcoding;

import nzgo.toolkit.NZGOToolkit;
import nzgo.toolkit.core.io.Arguments;
import nzgo.toolkit.core.io.ConfigFileIO;
import nzgo.toolkit.core.io.TaxonomyFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.taxonomy.*;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Taxonomy Assignment
 * @author Walter Xie
 */
public class TaxonomyAssignment extends Module{

    public TaxonomyAssignment() {
        super("TaxonomyAssignment", NZGOToolkit.TOOLKIT[1]);
    }


    private TaxonomyAssignment(Path inputFile, Path outFile, Path errorOutFile, Rank rankToBreak, String regexPrefix, String taxIdNCBI) {
        super();

        TaxonSet taxonSet = null;
        try {
            taxonSet = TaxonomyFileIO.importTaxa(inputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Taxon bioClass = null;
        if (taxIdNCBI != null) {
            try {
                bioClass = TaxonomyPool.getAndAddTaxIdByMemory(taxIdNCBI);

                MyLogger.info("Initializing biological classification : " +
                        bioClass.getScientificName() + ", " + bioClass.getTaxId());

            } catch (XMLStreamException | IOException e) {
                e.printStackTrace();
            }
        }

        TaxonomicAssignment taxonomicAssignment = new TaxonomicAssignment(taxonSet, rankToBreak, regexPrefix, bioClass);

        Map<String, String> taxaSortMap = null;
        try {
            taxaSortMap = taxonomicAssignment.getTaxaAssignementMap();
        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }

        try {
            if (outFile == null) {
                TaxonomyFileIO.writeTaxaMap(inputFile, taxaSortMap);
            } else {
                TaxonomyFileIO.writeTaxaMap(outFile, taxaSortMap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (errorOutFile != null) {
            try {
                ConfigFileIO.writeTSVFileFromMap(errorOutFile, taxonomicAssignment.getErrors(), "errors");
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
        Module module = new TaxonomyAssignment();

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
        new TaxonomyAssignment(inputFile, outFile, errorOutFile, rankToBreak, regex_prefix, taxIdNCBI);
    }
}
