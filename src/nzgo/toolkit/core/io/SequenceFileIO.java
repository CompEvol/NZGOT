package nzgo.toolkit.core.io;

import jebl.evolution.io.FastaExporter;
import jebl.evolution.io.FastaImporter;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.taxa.Taxon;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.AssemblerUtil;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.NameUtil;
import nzgo.toolkit.core.uparse.DereplicatedSequence;
import nzgo.toolkit.core.uparse.Parser;
import nzgo.toolkit.core.uparse.io.OTUsFileIO;

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

    public static List<String> importFastqGzLabelOnly (Path sequenceFile, String desc) throws IOException {
        List<String> labels = new ArrayList<>();

        BufferedReader reader = FileIO.getReaderGZIP(sequenceFile, desc);

        long lineNum = 0;
        String line = reader.readLine();
        while (line != null) {
            if (lineNum % 4 == 0) {
                String label = line.substring(1);
                labels.add(label);
            }

            line = reader.readLine();
            lineNum++;
        }
        reader.close();
        MyLogger.debug("\nimport " + labels.size() + " sequences");
        return labels;
    }

    public static List<Sequence> importNucleotideSequences (Path sequenceFile) throws IOException, ImportException {
        boolean isFasta = sequenceFile.toString().endsWith(NameSpace.SUFFIX_FASTA);

        MyLogger.info("\nimport sequences from " + sequenceFile);

        if (isFasta) {
            FastaImporter sequenceImporter = new FastaImporter(sequenceFile.toFile(), SequenceType.NUCLEOTIDE);
            return sequenceImporter.importSequences();
        } else {
            List<Sequence> sequenceList = new ArrayList<>();

            BufferedReader reader = getReader(sequenceFile, null);
            String line = reader.readLine();
            Long nLine = 0L;

            while (line != null) {
                if (nLine % 4 == 0) { // cannot use "@" or "+" or ">" as keyword
                    String label = line.substring(1);
                    line = reader.readLine();
                    nLine++;
                    Sequence sequence = new BasicSequence(SequenceType.NUCLEOTIDE, Taxon.getTaxon(label), line);
                    sequenceList.add(sequence);
                }
                line = reader.readLine();
                nLine++;
            }
            reader.close();

            MyLogger.debug("There are " + nLine + " lines in fastq file.");
            return sequenceList;
        }
    }

    public static List<String> importFastaLabelOnly (Path sequenceFile) throws IOException {
        return importFastaLabelOnly(sequenceFile, true);
    }

    /**
     * get the list of label only from fasta
     * @param sequenceFile
     * @param removeSizeAnnotation       true, to remove size annotation from label
     * @return
     * @throws IOException
     */
    public static List<String> importFastaLabelOnly (Path sequenceFile, boolean removeSizeAnnotation) throws IOException {
        List<String> labels = new ArrayList<>();

        BufferedReader reader = OTUsFileIO.getReader(sequenceFile, "fasta file");

        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith(">")) {
                String label = line.substring(1);
                label = Parser.getLabel(label, removeSizeAnnotation);
                labels.add(label);
            }

            line = reader.readLine();
        }

        reader.close();
        MyLogger.info("\nimport " + labels.size() + " sequences");
        return labels;
    }

    public static List<DereplicatedSequence> importDereplicatedSequences (Path sequenceFile) throws IOException {
        return importDereplicatedSequences(sequenceFile, false, true);
    }

    /**
     * get the list of DereplicatedSequence from fasta
     * DereplicatedSequence has the count of annotated size
     * @param sequenceFile
     * @param importSequence              true, to import the DNA sequence
     * @param removeSizeAnnotation        true, to remove size annotation from label
     * @return
     * @throws IOException
     */
    public static List<DereplicatedSequence> importDereplicatedSequences (Path sequenceFile, boolean importSequence,
                                                                          boolean removeSizeAnnotation) throws IOException {

        List<DereplicatedSequence> dereplicatedSequences = new ArrayList<>();

        BufferedReader reader = OTUsFileIO.getReader(sequenceFile, "fasta file");

        DereplicatedSequence ds = null;
        String seq = "";

        int size = 0;
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith(">")) {
                // set sequence when find >
                if (importSequence && ds != null && seq.length() > 0)
                    ds.setSequence(seq);

                String label = line.substring(1);
                // get annotated size before remove it from label
                int annotatedSize = Parser.getAnnotatedSizeInt(label);
                label = Parser.getLabel(label, removeSizeAnnotation);

                ds = new DereplicatedSequence(label);
                ds.setAnnotatedSize(annotatedSize);
                size += annotatedSize;

                dereplicatedSequences.add(ds);
                seq = ""; // init seq

            } else if (importSequence) {
                if (ds == null || ds.getSequence() != null)
                    throw new IllegalArgumentException("Cannot find the sequence in file : " + ds + ",\n at line : " + line);
                // cannot set sequence here, because it could be multi-line in fasta
                seq += line.trim();
            }

            line = reader.readLine();
        }
        // set last sequence
        if (importSequence && ds != null && seq.length() > 0) ds.setSequence(seq);
        reader.close();

        MyLogger.debug("\nimport " + dereplicatedSequences.size() + " dereplicated sequences, where total annotated size = " + size);
        return dereplicatedSequences;
    }

    public static void writeToFasta(Path sequenceFile, List<Sequence> sequences) throws IOException {
        BufferedWriter writer = getWriter(sequenceFile, "fasta");

        FastaExporter sequenceExporter = new FastaExporter(writer);

        sequenceExporter.exportSequences(sequences);
        writer.flush();
        writer.close();

        MyLogger.debug("\nexport " + sequences.size() + " sequences");
    }

    public static void writeDereplicatedSequenceToFasta(Path sequenceFile, List<DereplicatedSequence> sequences) throws IOException {
        BufferedWriter writer = getWriter(sequenceFile, "fasta");

        for (DereplicatedSequence dereplicatedSequence : sequences) {
            writer.write(">");
            writer.write(dereplicatedSequence.getName());
            writer.write("\n");
            writer.write(dereplicatedSequence.getSequence());
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

        if (!NameUtil.hasFileExtension(inFilePath.toString(), NameSpace.SUFFIX_FASTQ, NameSpace.SUFFIX_FASTA))
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
