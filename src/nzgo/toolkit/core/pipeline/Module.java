package nzgo.toolkit.core.pipeline;

import beast.app.util.Version;
import nzgo.toolkit.NZGOTVersion;
import nzgo.toolkit.core.io.Arguments;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.NameUtil;
import nzgo.toolkit.core.util.ArrayUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Module of each program
 *
 * @author Walter Xie
 */
public class Module {

    private final static Version version = new NZGOTVersion();
    // name, description
    protected final String[] names = new String[2];

    public Module() {
    }

    public Module(String name, String description) {
        setName(name, description);
    }

    protected String getName() {
        return names[0];
    }

    protected String getDescription() {
        return names[1];
    }

    protected void setName(String name, String description) {
        this.names[0] = name;
        this.names[1] = description;
    }

    /**
     * initiate program, return working directory or null if not set
     *
     * @param arguments
     * @param args
     * @return
     */
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
        Path working = Paths.get(System.getProperty(NameSpace.HOME_DIR));
        if (arguments.hasOption("working")) {
            working = Paths.get(arguments.getStringOption("working"));
            if (Files.notExists(working)) {
                MyLogger.error("Cannot find working path : " + working);
                System.exit(0);
            }
            System.setProperty(NameSpace.HOME_DIR, working.toString());
        }
        return working;
    }

    // args[0]
    public String getFirstArg(Arguments arguments, boolean compulsory) {
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
                throw new IllegalArgumentException("Argument value cannot be null or empty");
        }

        if (compulsory && firstArg == null) {
            MyLogger.error("Input is compulsory :");
            printUsage(arguments);
            System.exit(0);
        }
        return firstArg;
    }

    public String getFirstArg(Arguments arguments) {
        return getFirstArg(arguments, false);
    }

    //+++++++++++++++ Arguments +++++++++++++++++

    Arguments.Option[] defaultOptions = new Arguments.Option[]{
            new Arguments.StringOption("working", "working-path", "Change working directory (" + NameSpace.HOME_DIR +
                    ") to given path, otherwise change to input file's directory by default if this option is not used."),
            new Arguments.Option("overwrite", "Allow overwriting of output files."),
//                        new Arguments.Option("options", "Display an options dialog."),
//                        new Arguments.Option("window", "Provide a console window."),
//                        new Arguments.Option("verbose", "Give verbose parsing messages."),

            new Arguments.Option("help", "Print this information and stop.")
    };

    public Arguments getArguments() {
        return new Arguments(defaultOptions);
    }

    /**
     * add new options to Arguments
     *
     * @param newOptions
     * @return
     */
    public Arguments getArguments(Arguments.Option[] newOptions) {
        Arguments.Option[] allOptions = ArrayUtil.combineArrays(defaultOptions, newOptions, Arguments.Option.class);
        return new Arguments(allOptions);
    }

    protected void printTitle() {
        System.out.println();
        centreLine(getName() + " " + version.getVersionString() + ", " + version.getDateString(), 60);
        centreLine(getDescription(), 60);
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
     *
     * @param workPath
     * @param inputFileName
     * @param inputfileNameExtension
     * @return
     */
    public static Path getInputFile(Path workPath, String inputFileName, String... inputfileNameExtension) {

        Path inputFile = validateInputFile(workPath, inputFileName, "input", inputfileNameExtension);

        if (workPath != null) {
            System.setProperty(NameSpace.HOME_DIR, workPath.toAbsolutePath().toString());
            MyLogger.info("\nSet workPath path to " + workPath.toAbsolutePath());
        } else if (inputFile.getParent() != null) {
            // set workPath directory to the input directory as default
            System.setProperty(NameSpace.HOME_DIR, inputFile.getParent().toAbsolutePath().toString());
            MyLogger.info("\nSet workPath path to " + inputFile.getParent().toAbsolutePath());
        } else {
            MyLogger.error("Cannot find workPath path : " + inputFile.getParent());
            System.exit(0);
        }

        return inputFile;
    }

    /**
     * validate file name
     *
     * @param fileName
     * @param ioMessage
     * @param fileNameExtension
     */
    public static void validateFileName(String fileName, String ioMessage, String... fileNameExtension) {
        if (fileName == null) {
            MyLogger.error("Invalid " + ioMessage + " file name : " + fileName);
            System.exit(0);
        } else if (fileNameExtension != null && fileNameExtension.length > 0 && !NameUtil.hasFileExtension(fileName, fileNameExtension)) {
            MyLogger.error("Invalid " + ioMessage + " file format : " + fileName +
                    ", where " + ArrayUtil.toString(fileNameExtension) + " is required");
            System.exit(0);
        }
    }

    /**
     * validate input file
     * if fileNameSuffix is null, ignore checking suffix
     *
     * @param workPath
     * @param fileName
     * @param fileNameExtension
     * @param ioMessage
     * @return
     */
    public static Path validateInputFile(Path workPath, String fileName, String ioMessage, String... fileNameExtension) {
        validateFileName(fileName, ioMessage, fileNameExtension);

        if (workPath == null)
            workPath = Paths.get("");
        // input
        Path file = Paths.get(workPath.toString(), fileName);
        if (file == null || Files.notExists(file)) {
            MyLogger.error("Cannot find " + ioMessage + " file " + fileName + " from directory " + workPath);
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
     * @param fileNameExtension
     * @param ioMessage
     * @param overwrite
     * @return
     */
    public Path validateOutputFile(String fileName, String ioMessage, boolean overwrite, String... fileNameExtension) {
        validateFileName(fileName, ioMessage, fileNameExtension);

        Path working = Paths.get(System.getProperty(NameSpace.HOME_DIR));
        Path outFile = Paths.get(working.toString(), fileName);
        if (!overwrite && Files.exists(outFile)) {
            MyLogger.error("Output file exists, please use \"-overwrite\" to allow overwrite output");
            System.exit(0);
        }

        return outFile;
    }

}
