package nzgo.toolkit.core.naming;

import nzgo.toolkit.core.community.OTU;
import nzgo.toolkit.core.community.OTUs;
import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.uparse.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * AssemblerUtil
 * @author Walter Xie
 */
public class AssemblerUtil {

    public static String appendItemsToLabel (String label, String... items) {
        String finalLabel = label;
        for (String item : items) {
            finalLabel += item;
        }
        return finalLabel;
    }

    public static String appendSizeToLabel(OTU otu, boolean removeSizeAnnotation) {
        String otuName = Parser.getLabel(otu.getName(), removeSizeAnnotation);
        return AssemblerUtil.appendItemsToLabel(otuName, "|" + otu.size());
    }

    /**
     * USEARCH map.uc bug: otu head sequence does not definitely appear in Target column in map.uc
     * @param label
     * @param otus
     * @return        null if above bug
     */
    public static String appendSizeToLabel(String label, OTUs otus) {
        String otuName = Parser.getLabel(label, otus.removeSizeAnnotation);
        OTU otu = otus.getOTU(otuName);
        if (otu == null)
            return null;
//            throw new IllegalArgumentException("Cannot OTU " + otuName + " in community: " + otus.getName());

        return AssemblerUtil.appendSizeToLabel(otu, otus.removeSizeAnnotation);
    }

    // TODO why slow?
    public static void removeAnnotationAppendSizeToLabel(Path otusFastaFile, OTUs otus) throws IOException {
        Module.validateFileName(otusFastaFile.getFileName().toString(), "OTUs", NameSpace.SUFFIX_FASTA);

        Path outFile = Paths.get(otusFastaFile.getParent().toString(), "sized-" + otusFastaFile.getFileName());
        PrintStream out = FileIO.getPrintStream(outFile, null);
        PrintStream outErr = null;

        MyLogger.info("\nRename sequences labels from " + otusFastaFile + " to " + outFile);

        BufferedReader reader = FileIO.getReader(otusFastaFile, "OTUs representative sequences to add size in");
        String line = reader.readLine();
        boolean hasErr = false;
        int err = 0;
        while (line != null) {
            if (line.startsWith(">")) {
                String label = AssemblerUtil.appendSizeToLabel(line.substring(1), otus);

                if (label != null) {
                    line = ">" + label;
                    hasErr = false;
                } else {
                    if (err < 1) {
                        Path errFile = Paths.get(otusFastaFile.getParent().toString(), "err-" + otusFastaFile.getFileName());
                        outErr = FileIO.getPrintStream(errFile, null);
                    }
                    outErr.println(line);
                    line = reader.readLine();
                    outErr.println(line);
                    line = reader.readLine();
                    hasErr = true;
                    err++;
                }
            }
            if (!hasErr) {
                out.println(line);
                line = reader.readLine();
            }
        }
        reader.close();

        out.flush();
        out.close();

        if (err > 0)
            MyLogger.warn("\nFind " + err + " OTUs are not mapped correctly in uc file.");
    }

}
