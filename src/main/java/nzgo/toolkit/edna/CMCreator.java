package nzgo.toolkit.edna;

import nzgo.toolkit.core.community.Community;
import nzgo.toolkit.core.io.Arguments;
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
        super("CMCreator", "Create Community Matrix from UPARSE output file");
    }

    public void printUsage(final Arguments arguments) {
        arguments.printUsage(getName(), "[<input-file-name>]");
        System.out.println("  <input-file-name> is either uc or up mapping file from UPARSE.");
        System.out.println();
        System.out.println("  Example: " + getName() + " out.up");
        System.out.println("  Example: " + getName() + " -help");
        System.out.println();
    }

    //Main method
    public static void main(final String[] args) {
        Module module = new CMCreator();

        Arguments.Option[] newOptions = new Arguments.Option[]{
                new Arguments.StringOption("out", "output-file-name", "Output community matrix in csv file."),
        };
        final Arguments arguments = module.getArguments(newOptions);

        Path working = module.init(arguments, args);
        // input
        String inputFileName = module.getFirstArg(arguments);
        Path inputFile = module.getInputFile(working, inputFileName, NameSpace.SUFFIX_UC, NameSpace.SUFFIX_UP);

        // output
        String outFileName = "cm" + NameSpace.SUFFIX_CSV;
        if (arguments.hasOption("out")) {
            outFileName = arguments.getStringOption("out");
        }
        Path outFile = module.validateOutputFile(outFileName, "output", arguments.hasOption("overwrite"), NameSpace.SUFFIX_CSV);

        boolean countSizeAnnotation = true;
        boolean removeElements = false;

        SiteNameParser siteNameParser = new SiteNameParser();
        Community community = new Community(inputFile, siteNameParser, countSizeAnnotation, removeElements);

        try {
            int[] row = CommunityFileIO.writeCommunityMatrix(outFile, community);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
