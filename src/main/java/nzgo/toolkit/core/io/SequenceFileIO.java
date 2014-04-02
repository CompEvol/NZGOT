package nzgo.toolkit.core.io;

import jebl.evolution.io.FastaExporter;
import jebl.evolution.io.FastaImporter;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Sequence FileIO: adapter of FastaImporter
 *
 * @author Walter Xie
 */
public class SequenceFileIO extends FileIO {

    public static List<Sequence> importNucleotideSequences (Path sequenceFile) throws IOException, ImportException {
        FastaImporter sequenceImporter = new FastaImporter(sequenceFile.toFile() , SequenceType.NUCLEOTIDE);

        return sequenceImporter.importSequences();
    }


    public static void writeFasta (Path sequenceFile, List<Sequence> sequences) throws IOException {
        MyLogger.info("\nCreating fasta ..." + sequenceFile);

        BufferedWriter writer = getWriter(sequenceFile, "fasta");

        FastaExporter sequenceExporter = new FastaExporter(writer);

        sequenceExporter.exportSequences(sequences);
    }

    public static void appendItemsToLabelsFastq (Path inFilePath, PrintStream out, String... items) throws IOException {
        PrintStream outThis;
        if (out == null) {
            Path outFile = Paths.get(inFilePath.getParent().toString(), "New-" + inFilePath.getFileName());
            outThis = getPrintStream(outFile.toString(), null);
        } else {
            outThis = out;
        }
        MyLogger.info("\nRename sequences labels in fastq " + inFilePath);

        BufferedReader reader = getReader(inFilePath, null);
        String line = reader.readLine();
        Long nLine = 0L;
        while (line != null) {
            if (nLine%4==0) { // cannot use "@" as keyword
                String label = NameUtil.appendItemsToLabel(line.substring(1), items);
                line = "@" + label;
            }

            outThis.println(line);
            line = reader.readLine();
            nLine++;
        }
        reader.close();

        outThis.flush();
        if (out == null) outThis.close();
    }
}
