package nzgo.toolkit.core.sequences;

import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.taxa.Taxon;
import nzgo.toolkit.core.io.OTUsFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.Assembler;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.NameUtil;
import nzgo.toolkit.core.naming.SiteNameParser;
import nzgo.toolkit.core.pipeline.Module;

import java.io.*;
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
     * @param sequenceLabel Label of query sequence
     * @param sequenceList Sequence list
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
     * @param workPathString
     * @param inFastaFileName
     * @param regex
     * @throws IOException
     */
    public static void splitFastaTo2(String workPathString, String inFastaFileName, String regex) throws IOException {
        Path workPath = Paths.get(workPathString);
        Path inFastaFilePath = Module.validateInputFile(workPath, inFastaFileName, new String[]{NameSpace.SUFFIX_SEQUENCES}, "original file");

        String outputFileNameStem = NameUtil.getNameWithoutExtension(inFastaFilePath.toFile().getName());

        File outFastaFile1 = new File(workPath + outputFileNameStem + "-1.fasta");
        PrintStream out1 = new PrintStream(new FileOutputStream(outFastaFile1));
        File outFastaFile2 = new File(workPath + outputFileNameStem + "-2.fasta");
        PrintStream out2 = new PrintStream(new FileOutputStream(outFastaFile2));

        BufferedReader reader = OTUsFileIO.getReader(inFastaFilePath, "original file");

        String line = reader.readLine();
        PrintStream out = out1;
        while (line != null) {

            if (line.startsWith(">")) {
                String label = line.substring(1);

                if (label.matches(regex)) {
                    out = out2;
                } else {
                    out = out1;
                }
            }

            out.println(line);
            line = reader.readLine();
        }

        reader.close();
        out1.flush();
        out1.close();
        out2.flush();
        out2.close();
    }

    public static void splitFastaByLabelItem(String workPathString, String inFastaFileName) throws IOException {
        splitFastaByLabelItem(workPathString, inFastaFileName, SiteNameParser.LABEL_SAMPLE_INDEX);
    }

    /**
     * split sequences into n fasta files by items parsed by itemId in the label
     * @param workPathString
     * @param inFastaFileName
     * @param itemId
     * @throws IOException
     */
    public static void splitFastaByLabelItem(String workPathString, String inFastaFileName, int itemId) throws IOException {
        Path workPath = Paths.get(workPathString);
        Path inFastaFilePath = Module.validateInputFile(workPath, inFastaFileName, new String[]{NameSpace.SUFFIX_SEQUENCES}, "original file");

        String outputFileNameStem = NameUtil.getNameWithoutExtension(inFastaFilePath.toFile().getName());

        int fileLimit = 50;
        SiteNameParser siteNameParser = new SiteNameParser(itemId);
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

                    File outFastaFile = new File(workPath + outputFileNameStem + "-" + item + ".fasta");
                    out = new PrintStream(new FileOutputStream(outFastaFile));
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

    // main
    public static void main(String[] args) throws IOException{
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        MyLogger.info("\nWorking path = " + workPath);

//        File inFastaFile = new File(workPath + "otus-prep2.fasta");
//        String regex = ".*\\|28S.*";
//        String regex = ".*prep1.*";
//        splitFastaTo2(inFastaFile, "otus", regex);

        splitFastaByLabelItem(workPath, "otus-prep2.fasta");
    }
}
