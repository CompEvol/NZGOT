package nzgo.toolkit.core.io;

import jebl.evolution.io.FastaExporter;
import jebl.evolution.io.FastaImporter;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.AssemblerUtil;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.sequences.SimpleSequence;
import nzgo.toolkit.core.uc.DereplicatedSequence;
import nzgo.toolkit.core.uc.UCParser;

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

    public static List<String> importFastaLabelOnly (Path sequenceFile, boolean removeAnnotationSize) throws IOException {
        List<String> labels = new ArrayList<>();

        BufferedReader reader = OTUsFileIO.getReader(sequenceFile, "fasta file");

        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith(">")) {
                String label = line.substring(1);
                if (removeAnnotationSize)
                    label = UCParser.getLabelNoSizeAnnotation(label);
                labels.add(label);
            }

            line = reader.readLine();
        }

        reader.close();
        MyLogger.debug("\nimport " + labels.size() + " sequences");
        return labels;
    }

    public static List<SimpleSequence> importSimpleSequences (Path sequenceFile, boolean isDereplicatedSequence) throws IOException {
        List<SimpleSequence> simpleSequences = new ArrayList<>();

        BufferedReader reader = OTUsFileIO.getReader(sequenceFile, "fasta file");

        SimpleSequence ss = null;
        String seq = "";

        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith(">")) {
                // set sequence when find >
                if (ss != null && seq.length() > 0) ss.setSequence(seq);

                String label = line.substring(1);
                if (isDereplicatedSequence) {
                    ss = new DereplicatedSequence(label);
                } else {
                    ss = new DereplicatedSequence(label);
                }

                simpleSequences.add(ss);
                seq = ""; // init seq
            } else {
                if (ss == null || ss.getSequence() != null)
                    throw new IllegalArgumentException("Cannot find the sequence in file : " + ss + ",\n at line : " + line);
                seq += line.trim(); // cannot set sequence here, because it could be multi-line
            }

            line = reader.readLine();
        }
        // set last sequence
        if (ss != null && seq.length() > 0) ss.setSequence(seq);
        reader.close();

        MyLogger.debug("\nimport " + simpleSequences.size() + " sequences");
        return simpleSequences;
    }

    public static void writeToFasta(Path sequenceFile, List<Sequence> sequences) throws IOException {
        MyLogger.info("\nCreating fasta ..." + sequenceFile);

        BufferedWriter writer = getWriter(sequenceFile, "fasta");

        FastaExporter sequenceExporter = new FastaExporter(writer);

        sequenceExporter.exportSequences(sequences);

        MyLogger.debug("\nexport " + sequences.size() + " sequences");
    }

    public static void writeSimpleSequenceToFasta(Path sequenceFile, List<SimpleSequence> sequences) throws IOException {
        MyLogger.info("\nCreating fasta ..." + sequenceFile);

        BufferedWriter writer = getWriter(sequenceFile, "fasta");

        for (SimpleSequence ss : sequences) {
            writer.write(">");
            writer.write(ss.getName());
            writer.write("\n");
            writer.write(ss.getSequence());
            writer.write("\n");
        }

        writer.flush();
        writer.close();

        MyLogger.debug("\nexport " + sequences.size() + " sequences");
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
