package nzgo.toolkit.core.pipeline;

import beast.app.util.Arguments;
import beast.app.util.Version;
import nzgo.toolkit.NZGOTVersion;
import nzgo.toolkit.core.logger.MyLogger;
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
    protected String name = "NZGOToolkit";

    public Module() {  }

    public Module(String name) {
        setName(name);
    }

    protected String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public Arguments getArguments(Arguments.Option[] newOptions) {
        Arguments.Option[] commonOptions = new Arguments.Option[]{
                new Arguments.Option("working", "Change working directory (user.dir) to input file's directory"),
                new Arguments.Option("overwrite", "Allow overwriting of output files"),
//                        new Arguments.Option("options", "Display an options dialog"),
//                        new Arguments.Option("window", "Provide a console window"),
//                        new Arguments.Option("verbose", "Give verbose parsing messages"),

                new Arguments.Option("help", "Print this information and stop"),
        };

        Arguments.Option[] allOptions = ArrayUtil.combineArrays(commonOptions, newOptions);
        return new Arguments(allOptions);
    }

    public void printTitle() {
        System.out.println();
        centreLine(getName() + " " + version.getVersionString() + ", " + version.getDateString(), 60);
        centreLine("New Zealand Genomic Observatory Toolkit", 60);
        for (String creditLine : version.getCredits()) {
            centreLine(creditLine, 60);
        }
        System.out.println();
    }

    public void centreLine(String line, int pageWidth) {
        int n = pageWidth - line.length();
        int n1 = n / 2;
        for (int i = 0; i < n1; i++) {
            System.out.print(" ");
        }
        System.out.println(line);
    }

    public void printUsage(final Arguments arguments) {
        arguments.printUsage(getName(), "[<input-file-name>]");
        System.out.println();
        System.out.println("  Example: " + getName() + " -help");
        System.out.println();
    }

    /**
     * common method to get input file
     * @param args
     * @param arguments
     * @param inputFileNamePostfix
     * @return
     */
    public Path getInputFile(String[] args, final Arguments arguments, String inputFileNamePostfix) {

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

        String inputFileName = null;

        // check args[]
        final String[] args2 = arguments.getLeftoverArguments();
        if (args2.length > 1) {
            MyLogger.error("Unknown option: " + args2[1]);
            printUsage(arguments);
            System.exit(0);
        } else if (args2.length > 0) {
            inputFileName = args2[0];
        }

        Path inputFile = validateInputFile(inputFileName, inputFileNamePostfix);

        // set working directory
        if (inputFile.toFile().getParent() != null && arguments.hasOption("working")) {
            System.setProperty("user.dir", inputFile.toFile().getParentFile().getAbsolutePath());
        }

        return inputFile;
    }

    //++++++++++ Validate File ++++++++++

    public void validateFile(String fileName, String fileNamePostfix, String ioMessage) {
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
     * @param fileName
     * @param fileNamePostfix
     * @return
     */
    public Path validateInputFile(String fileName, String fileNamePostfix) {
        validateFile(fileName, fileNamePostfix, "input");

        // input
        Path file = Paths.get(fileName);
        if (file == null || Files.notExists(file)) {
            MyLogger.error("Cannot find input file : " + fileName);
            System.exit(0);
        }

        return file;
    }

    /**
     * validate output file
     * if fileNamePostfix is null, ignore checking postfix
     * @param fileName
     * @param fileNamePostfix
     * @return
     */
    public Path validateOutputFile(String fileName, String fileNamePostfix) {
        validateFile(fileName, fileNamePostfix, "output");

        return FileSystems.getDefault().getPath(".", fileName);
    }

}
