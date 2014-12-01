package nzgo.toolkit.core.util;

import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameUtil;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.uparse.io.OTUsFileIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * IO Util
 * @author Walter Xie
 */
public class IOUtil {

    /**
     *
     * @param workPathString
     * @param inFastaFileName
     * @param matches          such as the barcode: NZAC03010806, NZAC03010894
     * @throws java.io.IOException
     */
    public static void splitFileByLabelItem(String workPathString, String inFastaFileName, String... matches) throws IOException {
        Path inFastaFilePath = Module.validateInputFile(Paths.get(workPathString), inFastaFileName, "input", null);

        BufferedReader reader = OTUsFileIO.getReader(inFastaFilePath, "original file");

        String outputFileNameStem = NameUtil.getNameNoExtension(inFastaFilePath.toFile().getName());
        String outputFileExtension = NameUtil.getSuffix(inFastaFilePath.toFile().getName());

        Path outputFilePath = Paths.get(workPathString, outputFileNameStem + "-matches" + outputFileExtension);
        PrintStream out1 = FileIO.getPrintStream(outputFilePath, "matches");
        outputFilePath = Paths.get(workPathString, outputFileNameStem + "-remains" + outputFileExtension);
        PrintStream out2 = FileIO.getPrintStream(outputFilePath, "remains");

        int lMatch = 0;
        int lRemain = 0;
        String line = reader.readLine();
        PrintStream out = out1;
        while (line != null) {
            if (StringUtil.contains(line, matches)) {
                out = out1;
                lMatch++;
            } else {
                out = out2;
                lRemain++;
            }

            out.println(line);
            line = reader.readLine();
        }

        reader.close();
        out1.flush();
        out1.close();
        out2.flush();
        out2.close();

        MyLogger.debug("Total " + (lMatch+lRemain) + " lines, separate " + lMatch + " matched lines, and " + lRemain + " remain.");
    }



    // main
    public static void main(String[] args) throws IOException{

        Path workDir = Paths.get(System.getProperty("user.home") + "/Documents/ModelEcoSystem/454/2010-pilot/COITraditional/data/");
        MyLogger.info("\nWorking path = " + workDir);

        splitFileByLabelItem(workDir.toString(), "COI-all-taxa.txt", "NZAC03010806", "NZAC03010894", "NZAC03011914", "NZAC03011905", "NZAC03011634", "NZAC03010302", "NZAC03010913", "NZAC03010897", "NZAC03010906", "NZAC03012413", "NZAC03010752", "NZAC03011443", "NZAC03013543", "NZAC03011474", "NZAC03009260", "NZAC03010909", "NZAC03010904", "NZAC03010711", "NZAC03013640", "NZAC03011787");

    }

}
