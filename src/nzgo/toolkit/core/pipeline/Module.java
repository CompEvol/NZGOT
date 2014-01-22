package nzgo.toolkit.core.pipeline;

import beast.app.util.Arguments;
import beast.app.util.Version;
import nzgo.toolkit.NZGOTVersion;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
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
     *
     * @param inputFileNamePostfix
     * @return
     */
    public Path getInputFile(Path working, String inputFileName, String inputFileNamePostfix) {

        Path inputFile = validateInputFile(working, inputFileName, inputFileNamePostfix, "input");

        if (working != null) {
            System.setProperty(NameSpace.HOME_DIR, working.toAbsolutePath().toString());
        } else if (inputFile.getParent() != null) {
            // set working directory to the input directory as default
            System.setProperty(NameSpace.HOME_DIR, inputFile.getParent().toAbsolutePath().toString());
        } else {
            MyLogger.error("Cannot find working path : " + inputFile.getParent());
            System.exit(0);
        }

        return inputFile;
    }

    public void validateFileName(String fileName, String fileNamePostfix, String ioMessage) {
        if (fileName == null) {
            MyLogger.error("Invalid " + ioMessage + " file name : " + fileName);
            System.exit(0);
        } else if (fileNamePostfix != null && !fileName.endsWith(fileNamePostfix)) {
            MyLogger.error("Invalid " + ioMessage + " file format : " + fileName +
                    ", where " + fileNamePostfix + " is required");
            System.exit(0);
        }
    }

    /**
     * validate input file
     * if fileNamePostfix is null, ignore checking postfix
     * @param working
     * @param fileName
     * @param fileNamePostfix
     * @param ioMessage
     * @return
     */
    public Path validateInputFile(Path working, String fileName, String fileNamePostfix, String ioMessage) {
        validateFileName(fileName, fileNamePostfix, ioMessage);

        if (working == null)
            working = Paths.get(".");
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
     * if fileNamePostfix is null, ignore checking postfix
     * if overwrite is false, check if outFile exists
     *
     * @param fileName
     * @param fileNamePostfix
     * @param ioMessage
     * @param overwrite
     * @return
     */
    public Path validateOutputFile(String fileName, String fileNamePostfix, String ioMessage, boolean overwrite) {
        validateFileName(fileName, fileNamePostfix, ioMessage);

        Path outFile = FileSystems.getDefault().getPath(".", fileName);
        if (!overwrite && Files.exists(outFile)) {
            MyLogger.error("Output file exists, please use \"-overwrite\" to allow overwrite output");
            System.exit(0);
        }

        return outFile;
    }

}
