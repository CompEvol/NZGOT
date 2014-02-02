package nzgo.toolkit.core.pipeline;

import beast.app.util.Version;
import nzgo.toolkit.NZGOTVersion;
import nzgo.toolkit.core.io.Arguments;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.NameUtil;
import nzgo.toolkit.core.util.ArrayUtil;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Module of each program
 * @author Walter Xie
 */
public class Module {

    private final static Version version = new NZGOTVersion();
    protected final String[] names = new String[2];

    public Module() {  }

    public Module(String name, String fullName) {
        setName(name, fullName);
    }

    protected String getName() {
        return names[0];
    }

    protected String getFullName() {
        return names[1];
    }

    protected void setName(String name, String fullName) {
        this.names[0] = name;
        this.names[1] = fullName;
    }

    public Path init(Arguments arguments, String[] args) {
        try {
            arguments.parseArguments(args);
        } catch (Arguments.ArgumentException ae) {
            System.out.println();
            System.out.println(ae.getMessage());
            System.out.println();
            printUsage(arguments);
            System.exit(1);
        }

        if (arguments.hasOption("help")) {
            printUsage(arguments);
            System.exit(0);
        }

        printTitle();

        // set working directory
        Path working = null;
        if (arguments.hasOption("working")) {
            working = Paths.get(arguments.getStringOption("working"));
            if (Files.notExists(working)) {
                MyLogger.error("Cannot find working path : " + working);
                System.exit(0);
            }
        }
        return working;
    }

    // args[0]
    public String getFirstArg(Arguments arguments) {
        String firstArg = null;

        // check args[]
        final String[] args2 = arguments.getLeftoverArguments();
        if (args2.length > 1) {
            MyLogger.error("Unknown option: " + args2[1]);
            printUsage(arguments);
            System.exit(0);
        } else if (args2.length > 0) {
            firstArg = args2[0];

            if (NameUtil.isEmptyNull(firstArg))
                throw new IllegalArgumentException("Argument cannot be null or empty");
        }
        return firstArg;
    }

    /**
     * add new options to Arguments
     * @param newOptions
     * @return
     */
    public Arguments getArguments(Arguments.Option[] newOptions) {
        Arguments.Option[] commonOptions = new Arguments.Option[]{
                new Arguments.StringOption("working", "working-path", "Change working directory (" + NameSpace.HOME_DIR +
                        ") to given path, otherwise change to input file's directory by default if this option is not used."),
                new Arguments.Option("overwrite", "Allow overwriting of output files."),
//                        new Arguments.Option("options", "Display an options dialog."),
//                        new Arguments.Option("window", "Provide a console window."),
//                        new Arguments.Option("verbose", "Give verbose parsing messages."),

                new Arguments.Option("help", "Print this information and stop."),
        };

        Arguments.Option[] allOptions = ArrayUtil.combineArrays(commonOptions, newOptions);
        return new Arguments(allOptions);
    }

    protected void printTitle() {
        System.out.println();
        centreLine(getName() + " " + version.getVersionString() + ", " + version.getDateString(), 60);
        centreLine(getFullName(), 60);
        for (String creditLine : version.getCredits()) {
            centreLine(creditLine, 60);
        }
        System.out.println();
    }

    protected void centreLine(String line, int pageWidth) {
        int n = pageWidth - line.length();
        int n1 = n / 2;
        for (int i = 0; i < n1; i++) {
            System.out.print(" ");
        }
        System.out.println(line);
    }

    protected void printUsage(final Arguments arguments) {
        arguments.printUsage(getName(), "[<input-file-name>]");
        System.out.println();
        System.out.println("  Example: " + getName() + " -help");
        System.out.println();
    }


    //++++++++++ Validate/Input/Output File ++++++++++

    /**
     * common method to get input file
     * @param working
     * @param inputFileName
     * @param inputFileNameSuffixes
     * @return
     */
    public Path getInputFile(Path working, String inputFileName, String[] inputFileNameSuffixes) {

        Path inputFile = validateInputFile(working, inputFileName, inputFileNameSuffixes, "input");

        if (working != null) {
            System.setProperty(NameSpace.HOME_DIR, working.toAbsolutePath().toString());
            MyLogger.info("\nSet working path to " + working.toAbsolutePath());
        } else if (inputFile.getParent() != null) {
            // set working directory to the input directory as default
            System.setProperty(NameSpace.HOME_DIR, inputFile.getParent().toAbsolutePath().toString());
            MyLogger.info("\nSet working path to " + inputFile.getParent().toAbsolutePath());
        } else {
            MyLogger.error("Cannot find working path : " + inputFile.getParent());
            System.exit(0);
        }

        return inputFile;
    }

    /**
     * validate file name
     * @param fileName
     * @param fileNameSuffixes
     * @param ioMessage
     */
    public void validateFileName(String fileName, String[] fileNameSuffixes, String ioMessage) {
        if (fileName == null) {
            MyLogger.error("Invalid " + ioMessage + " file name : " + fileName);
            System.exit(0);
        } else if (fileNameSuffixes != null && !NameUtil.endsWith(fileName, fileNameSuffixes)) {
            MyLogger.error("Invalid " + ioMessage + " file format : " + fileName +
                    ", where " + fileNameSuffixes + " is required");
            System.exit(0);
        }
    }

    /**
     * validate input file
     * if fileNameSuffix is null, ignore checking suffix
     * @param working
     * @param fileName
     * @param fileNameSuffixes
     * @param ioMessage
     * @return
     */
    public Path validateInputFile(Path working, String fileName, String[] fileNameSuffixes, String ioMessage) {
        validateFileName(fileName, fileNameSuffixes, ioMessage);

        if (working == null)
            working = Paths.get("");
        // input
        Path file = Paths.get(working.toString(), fileName);
        if (file == null || Files.notExists(file)) {
            MyLogger.error("Cannot find " + ioMessage + " file : " + fileName);
            System.exit(0);
        }

        return file;
    }

    /**
     * validate output file
     * if fileNameSuffix is null, ignore checking suffix
     * if overwrite is false, check if outFile exists
     *
     * @param fileName
     * @param fileNameSuffixes
     * @param ioMessage
     * @param overwrite
     * @return
     */
    public Path validateOutputFile(String fileName, String[] fileNameSuffixes, String ioMessage, boolean overwrite) {
        validateFileName(fileName, fileNameSuffixes, ioMessage);

        Path outFile = FileSystems.getDefault().getPath("", fileName);
        if (!overwrite && Files.exists(outFile)) {
            MyLogger.error("Output file exists, please use \"-overwrite\" to allow overwrite output");
            System.exit(0);
        }

        return outFile;
    }

}
