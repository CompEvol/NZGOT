package nzgo.toolkit.core.io;

import jebl.evolution.io.FastaExporter;
import jebl.evolution.io.FastaImporter;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.AssemblerUtil;
import nzgo.toolkit.core.naming.NameSpace;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static List<String> importFastaLabelOnly (Path sequenceFile) throws IOException {
        List<String> labels = new ArrayList<>();

        BufferedReader reader = OTUsFileIO.getReader(sequenceFile, "fasta file");

        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith(">")) {
                String label = line.substring(1);

                labels.add(label);
            }

            line = reader.readLine();
        }

        reader.close();
        return labels;
    }

    public static void writeFasta (Path sequenceFile, List<Sequence> sequences) throws IOException {
        MyLogger.info("\nCreating fasta ..." + sequenceFile);

        BufferedWriter writer = getWriter(sequenceFile, "fasta");

        FastaExporter sequenceExporter = new FastaExporter(writer);

        sequenceExporter.exportSequences(sequences);
    }

    /**
     *
     * @param inFilePath         Fastq or Fasta format
     * @param out
     * @param items
     * @throws IOException
     */
    public static void appendItemsToLabelsFastQA(Path inFilePath, PrintStream out, String... items) throws IOException {
        MyLogger.info("Append " + Arrays.toString(items) + " to sequences labels in " + inFilePath.getFileName());

        if (!(inFilePath.toString().endsWith(NameSpace.SUFFIX_FASTQ) || inFilePath.toString().endsWith(NameSpace.SUFFIX_FASTA)))
            throw new IllegalArgumentException("Invalid sequence file " + inFilePath);

        PrintStream outThis;
        if (out == null) {
            Path outFile = Paths.get(inFilePath.getParent().toString(), "New-" + inFilePath.getFileName());
            outThis = getPrintStream(outFile.toString(), null);
        } else {
            outThis = out;
        }
        MyLogger.info("\nRename sequences labels in " + inFilePath);

        BufferedReader reader = getReader(inFilePath, null);
        String line = reader.readLine();
        Long nLine = 0L;
        while (line != null) {
            if (nLine%2==0) { // cannot use "@" or "+" or ">" as keyword
                String label = AssemblerUtil.appendItemsToLabel(line.substring(1), items);
                String firstCharacter = line.substring(0, 1); //"@" or "+" or ">"
                line = firstCharacter + label;
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
