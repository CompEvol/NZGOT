package nzgo.toolkit.core.sequences;

import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.taxa.Taxon;
import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.io.OTUsFileIO;
import nzgo.toolkit.core.io.SequenceFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.Assembler;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.NameUtil;
import nzgo.toolkit.core.naming.SiteNameParser;
import nzgo.toolkit.core.pipeline.Module;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JEBL Sequence Util
 * @author Thomas Hummel
 * @author Walter Xie
 */
public class SequenceUtil {

    public static void assembleSequenceLabels(List<Sequence> sequences, Assembler assembler) {
        for (int i = 0; i < sequences.size(); i++) {
            Sequence sequence = sequences.get(i);
            String taxon = assembler.getAssembledLabel(sequence.getTaxon().getName());

            if (!NameUtil.isEmptyNull(taxon)) {
                Sequence newSeq = new BasicSequence(SequenceType.NUCLEOTIDE, Taxon.getTaxon(taxon), sequence.getStates());
                sequences.set(i, newSeq);
            }
        }
    }

    /**
     * get sequence string from a list of sequences given the sequence label
     *
     * @param sequenceLabel Label of query sequence
     * @param sequenceList  Sequence list
     * @return amino acid sequence found from the list or null
     */
    public static String getSequenceStringFrom(String sequenceLabel, List<Sequence> sequenceList) {

        String seqName;

        for (Sequence sequence : sequenceList) {
            seqName = sequence.getTaxon().toString();
            if (seqName.contentEquals(sequenceLabel))
                return sequence.getString();
        }
        return null;

    }

    /**
     * split sequences into 2 fasta file by matching regex of labels
     *
     * @param workPathString
     * @param inFileName
     * @param regex
     * @throws IOException
     */
    public static void splitFastAOrQTo2(String workPathString, String inFileName, String regex) throws IOException {
        Path inFastaFilePath = Module.validateInputFile(Paths.get(workPathString), inFileName,
                new String[]{NameSpace.SUFFIX_FASTA, NameSpace.SUFFIX_FASTQ}, "original file");

        BufferedReader reader = OTUsFileIO.getReader(inFastaFilePath, "original file");

        String outputFileNameStem = NameUtil.getNameWithoutExtension(inFileName);
        String suffix = NameUtil.getSuffix(inFileName);
        Path outputFilePath = Paths.get(workPathString, outputFileNameStem + "-1" + suffix);
        PrintStream out1 = FileIO.getPrintStream(outputFilePath, "split");
        outputFilePath = Paths.get(workPathString, outputFileNameStem + "-2" + suffix);
        PrintStream out2 = FileIO.getPrintStream(outputFilePath, "split");

        int l = 0;
        String line = reader.readLine();
        PrintStream out = out1;
        while (line != null) {

            if ((suffix.equalsIgnoreCase(NameSpace.SUFFIX_FASTA) && line.startsWith(">")) ||
                    (suffix.equalsIgnoreCase(NameSpace.SUFFIX_FASTQ) && l % 4 == 0)) {
                String label = line.substring(1);

                if (label.matches(regex)) {
                    out = out2;
                } else {
                    out = out1;
                }
            }

            out.println(line);
            line = reader.readLine();
            l++;
        }

        reader.close();
        out1.flush();
        out1.close();
        out2.flush();
        out2.close();
    }

    public static void splitFastaBySites(String workPathString, String inFastaFileName) throws IOException {
        splitFastaByLabelItem(workPathString, inFastaFileName, SiteNameParser.LABEL_SAMPLE_INDEX);
    }

    /**
     * split sequences into n fasta files by items parsed by itemIndex in the label
     *
     * @param workPathString
     * @param inFastaFileName
     * @param itemIndex
     * @throws IOException
     */
    public static void splitFastaByLabelItem(String workPathString, String inFastaFileName, int itemIndex) throws IOException {
        Path inFastaFilePath = Module.validateInputFile(Paths.get(workPathString), inFastaFileName, new String[]{NameSpace.SUFFIX_FASTA}, "original file");

        String outputFileNameStem = NameUtil.getNameWithoutExtension(inFastaFilePath.toFile().getName());

        int fileLimit = 50;
        SiteNameParser siteNameParser = new SiteNameParser(itemIndex);
        Map<String, PrintStream> outMap = new HashMap<>();

        BufferedReader reader = OTUsFileIO.getReader(inFastaFilePath, "original file");

        int originalTotal = 0;
        int total = 0;
        String line = reader.readLine();
        PrintStream out = null;
        while (line != null) {
            if (line.startsWith(">")) {
                String label = line.substring(1);
                String item = siteNameParser.getSiteFullName(label);

                if (outMap.containsKey(item)) {
                    out = outMap.get(item);
                } else {
                    if (outMap.size() > fileLimit)
                        throw new IllegalStateException("Cannot split to more than " + fileLimit + " files !");

                    Path outputFilePath = Paths.get(workPathString, outputFileNameStem + "-" + item + NameSpace.SUFFIX_FASTA);
                    out = FileIO.getPrintStream(outputFilePath, null);
                    outMap.put(item, out);
                }
            }

            if (out != null) {
                out.println(line);
                total++;
            }

            line = reader.readLine();
            originalTotal++;
        }

        reader.close();

        for (Map.Entry<String, PrintStream> entry : outMap.entrySet()) {
            entry.getValue().flush();
            entry.getValue().close();
        }

        MyLogger.debug("original file total lines = " + originalTotal + ", write to new files total lines = " + total);
    }

    /**
     * export difference from file1 to file2, not include difference from file2 to file1 yet.
     * @param workPathString
     * @param fileName1
     * @param fileName2
     * @throws IOException
     */
    public static void diffFastAFrom(String workPathString, String fileName1, String fileName2) throws IOException {
        Path file1 = Module.validateInputFile(Paths.get(workPathString), fileName1,
                new String[]{NameSpace.SUFFIX_FASTA}, "original file");
        Path file2 = Module.validateInputFile(Paths.get(workPathString), fileName2,
                new String[]{NameSpace.SUFFIX_FASTA}, "original file");

        BufferedReader reader1 = OTUsFileIO.getReader(file1, "file 1");
        List<String> labels = SequenceFileIO.importFastaLabelOnly(file2);

        Path outputFilePath = Paths.get(workPathString, "diff" + NameSpace.SUFFIX_FASTA);
        PrintStream out = FileIO.getPrintStream(outputFilePath, "difference");

        int l = 0;
        boolean hasDiff = false;
        String line = reader1.readLine();
        while (line != null) {
            if (line.startsWith(">")) {
                hasDiff = false;
                String label = line.substring(1);
                if (!labels.contains(label)) {
                    hasDiff = true;
                    out.println(line);
                    l++;
                }
            } else if (hasDiff) {
                out.println(line);
                l++;
            }

            line = reader1.readLine();
        }

        reader1.close();
        out.flush();
        out.close();

        MyLogger.debug("export different lines = " + l);

    }

        // main
    public static void main(String[] args) throws IOException{
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        MyLogger.info("\nWorking path = " + workPath);

        String inFastaFile = "otus2.fasta";//"sorted.fasta";
//        String regex = ".*\\|MID-.*";  //".*\\|28S.*";
        String regex = ".*up=chimera.*";
        splitFastAOrQTo2(workPath, inFastaFile, regex);
//        splitFastaByLabelItem(workPath, inFastaFile, 3);

//        splitFastaBySites(workPath, "otus.fasta");

//        diffFastAFrom(workPath, "otus1.fasta", "otus.fasta");
    }

}
