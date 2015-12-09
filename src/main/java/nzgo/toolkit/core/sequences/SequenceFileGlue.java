package nzgo.toolkit.core.sequences;

import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameUtil;
import nzgo.toolkit.core.uparse.io.OTUsFileIO;
import nzgo.toolkit.core.util.IOUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * concatenate all fasta files by adding their
 * sample name (in the part of file name) into labels
 *
 * @author Walter Xie
 */
public class SequenceFileGlue {

    public static void concatAllFasta(List<Path> files, Path outPath, String sampleSep, int sampleIndex, String labelSep) throws IOException {

        PrintStream out = FileIO.getPrintStream(outPath, "concatenated file");
        int total = 0;
        for (Path inPath : files) {
            String fileName = inPath.getFileName().toString();
            if (!NameUtil.isFASTA(fileName))
                throw new IllegalArgumentException("Incorrect fasta file format !");

            String sampleName = fileName.split(sampleSep)[sampleIndex];

            BufferedReader reader = OTUsFileIO.getReader(inPath, "original file");

            int s = 0;
            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith(">")) {
                    line = ">" + sampleName + labelSep + line.substring(1);
                    s++;
                }
                out.println(line);

                line = reader.readLine();
            }
            total+=s;

            MyLogger.debug("sequences = " + s);

            reader.close();
            out.flush();
        }
        out.close();

        MyLogger.debug("total sequences = " + total);
    }

    //Main method
    public static void main(final String[] args) {
//        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        Path workDir = Paths.get(System.getProperty("user.home") + "/Projects/FishGutMicrobiomes/Miyake et al Reef Fish/");
        MyLogger.info("\nWorking path = " + workDir);

        Path dir = Paths.get(workDir.toString(), "raw");
        Path outPath = Paths.get(workDir.toString(), "16s.fasta");

        try {
            List<Path> files = IOUtil.listFiles(dir, "*.{fasta}");
            //Agah01.Miyake454.fasta
            concatAllFasta(files, outPath, "\\.", 0, "_");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
