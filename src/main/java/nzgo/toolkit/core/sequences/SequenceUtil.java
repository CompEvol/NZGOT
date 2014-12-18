package nzgo.toolkit.core.sequences;

import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.taxa.Taxon;
import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.io.SequenceFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.Assembler;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.NameUtil;
import nzgo.toolkit.core.naming.SiteNameParser;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.uparse.io.OTUsFileIO;
import nzgo.toolkit.core.util.ArrayUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
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
                "original file", NameSpace.SUFFIX_FASTA, NameSpace.SUFFIX_FASTQ);

        BufferedReader reader = OTUsFileIO.getReader(inFastaFilePath, "original file");

        String outputFileNameStem = NameUtil.getNameNoExtension(inFileName);
        String suffix = NameUtil.getSuffix(inFileName);
        Path outputFilePath = Paths.get(workPathString, outputFileNameStem + "-1" + suffix);
        PrintStream out1 = FileIO.getPrintStream(outputFilePath, "split");
        outputFilePath = Paths.get(workPathString, outputFileNameStem + "-2" + suffix);
        PrintStream out2 = FileIO.getPrintStream(outputFilePath, "split");

        int l = 0;
        String line = reader.readLine();
        PrintStream out = out1;
        while (line != null) {

            if ( (NameUtil.hasFileExtension(inFileName, NameSpace.SUFFIX_FASTA) && line.startsWith(">")) ||
                    (NameUtil.hasFileExtension(inFileName, NameSpace.SUFFIX_FASTQ) && l % 4 == 0)) {
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
     * split sequences into n fasta files by items parsed by itemIndex in the label, files are limited to 100
     *
     * @param workPathString
     * @param inFastaFileName
     * @param itemIndex
     * @throws IOException
     */
    public static void splitFastaByLabelItem(String workPathString, String inFastaFileName, int itemIndex) throws IOException {
        Path inFastaFilePath = Module.validateInputFile(Paths.get(workPathString), inFastaFileName, "input", NameSpace.SUFFIX_FASTA);

        String outputFileNameStem = NameUtil.getNameNoExtension(inFastaFilePath.toFile().getName());

        int fileLimit = 100;
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

        MyLogger.info("Split to " + outMap.size() + " files.");
        MyLogger.debug("original file total lines = " + originalTotal + ", write to new files total lines = " + total);
    }

    /**
     *
     * @param workPathString
     * @param inFastaFileName
     * @param itemIndex
     * @param matches          such as the barcode: NZAC03010806, NZAC03010894
     * @throws IOException
     */
    public static void splitFastaByLabelItem(String workPathString, String inFastaFileName, int itemIndex, String... matches) throws IOException {
        Path inFastaFilePath = Module.validateInputFile(Paths.get(workPathString), inFastaFileName, "input", NameSpace.SUFFIX_FASTA);

        BufferedReader reader = OTUsFileIO.getReader(inFastaFilePath, "original file");

        String outputFileNameStem = NameUtil.getNameNoExtension(inFastaFilePath.toFile().getName());

        Path outputFilePath = Paths.get(workPathString, outputFileNameStem + "-matches" + NameSpace.SUFFIX_FASTA);
        PrintStream out1 = FileIO.getPrintStream(outputFilePath, "matches");
        outputFilePath = Paths.get(workPathString, outputFileNameStem + "-remains" + NameSpace.SUFFIX_FASTA);
        PrintStream out2 = FileIO.getPrintStream(outputFilePath, "remains");

        int lMatch = 0;
        int lRemain = 0;
        String line = reader.readLine();
        PrintStream out = out1;
        while (line != null) {
            if (line.startsWith(">")) {
                String label = line.substring(1);
                String[] items = FileIO.lineParser.getSeparator(1).parse(label); // default "|"

                if (items.length <= itemIndex)
                    throw new IllegalArgumentException("Do not have enough items for itemIndex = " + itemIndex + " in the string " + label);

                int matchedId = ArrayUtil.indexOf(items[itemIndex], matches);

                if (matchedId >= 0) {
                    out = out1;
                    lMatch++;
                } else {
                    out = out2;
                    lRemain++;
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

        MyLogger.debug("Total " + (lMatch+lRemain) + " sequences, separate " + lMatch + " matched sequences, and " + lRemain + " remain.");
    }

    /**
     * export difference from file1 to file2 (exist in file1 but not file2), not include difference from file2 to file1 yet.
     * such as file1 > file2
     * @param workPathString
     * @param fileName1
     * @param fileName2
     * @throws IOException
     */
    public static void diffFastAFrom(String workPathString, String fileName1, String fileName2) throws IOException {
        Path file1 = Module.validateInputFile(Paths.get(workPathString), fileName1,
                "original file", NameSpace.SUFFIX_FASTA);
        Path file2 = Module.validateInputFile(Paths.get(workPathString), fileName2,
                "original file", NameSpace.SUFFIX_FASTA);

        BufferedReader reader1 = OTUsFileIO.getReader(file1, "file 1");
        List<String> labels = SequenceFileIO.importFastaLabelOnly(file2);

        Path outputFilePath = Paths.get(workPathString, "diff-" + NameUtil.getNameNoExtension(fileName1) +
                "-" + NameUtil.getNameNoExtension(fileName2) + NameSpace.SUFFIX_FASTA);
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

    /**
     *
     * @param workPathString
     * @param inFastqFileName
     * @param itemIndex
     * @throws IOException
     */
    public static void splitFastqByLabelItem(String workPathString, String inFastqFileName, int itemIndex) throws IOException {
        Path inFastqFilePath = Module.validateInputFile(Paths.get(workPathString), inFastqFileName, "input", NameSpace.SUFFIX_FASTQ);

        String outputFileNameStem = NameUtil.getNameNoExtension(inFastqFilePath.toFile().getName());

        int fileLimit = 100;
        SiteNameParser siteNameParser = new SiteNameParser(itemIndex);
        Map<String, PrintStream> outMap = new HashMap<>();

        BufferedReader reader = OTUsFileIO.getReader(inFastqFilePath, "original file");

        int lineNum = 0;
        int total = 0;
        String line = reader.readLine();
        PrintStream out = null;
        while (line != null) {
            if (lineNum % 4 == 0) {
                String label = line.substring(1);
                String item = siteNameParser.getSiteFullName(label);

                if (outMap.containsKey(item)) {
                    out = outMap.get(item);
                } else {
                    if (outMap.size() > fileLimit)
                        throw new IllegalStateException("Cannot split to more than " + fileLimit + " files !");

                    Path outputFilePath = Paths.get(workPathString, outputFileNameStem + "-" + item + NameSpace.SUFFIX_FASTQ);
                    out = FileIO.getPrintStream(outputFilePath, null);
                    outMap.put(item, out);
                }
            }

            if (out != null) {
                out.println(line);
                total++;
            }

            line = reader.readLine();
            lineNum++;
        }

        reader.close();

        for (Map.Entry<String, PrintStream> entry : outMap.entrySet()) {
            entry.getValue().flush();
            entry.getValue().close();
        }

        MyLogger.info("Split to " + outMap.size() + " files.");
        MyLogger.debug("original file total lines = " + lineNum + ", write to new files total lines = " + total);
    }

    /**
     * replace all regex to replacement in identifier (and  3rd line description) in given FastA or FastaQ
     *
     * @param workPathString
     * @param inFileName
     * @param regex
     * @param replacement
     * @param removeLengthSmallerThan    remove sequence length smaller than given number, 0 will keep all empty sequences
     * @throws IOException
     */
    public static void renameIdentifierInFastAOrQ(String workPathString, String inFileName, String regex, String replacement, int removeLengthSmallerThan) throws IOException {
        Path inFastaFilePath = Module.validateInputFile(Paths.get(workPathString), inFileName,
                "original file", NameSpace.SUFFIX_FASTA, NameSpace.SUFFIX_FASTQ);

        BufferedReader reader = OTUsFileIO.getReader(inFastaFilePath, "original file");

        String outputFileNameStem = NameUtil.getNameNoExtension(inFileName);
        String suffix = NameUtil.getSuffix(inFileName);
        Path outputFilePath = Paths.get(workPathString, outputFileNameStem + "-new" + suffix);
        PrintStream out = FileIO.getPrintStream(outputFilePath, "identifier renamed file");

        if (removeLengthSmallerThan > 0)
            MyLogger.info("Remove all sequences whose length < " + removeLengthSmallerThan);

        int l = 0;
        int removed = 0;
        String line = reader.readLine();
        while (line != null) {
            if ( (NameUtil.hasFileExtension(inFileName, NameSpace.SUFFIX_FASTA) && line.startsWith(">")) ||
                    (NameUtil.hasFileExtension(inFileName, NameSpace.SUFFIX_FASTQ) && l % 2 == 0)) {
                String newIdentifier = line.replaceAll(regex, replacement);
                // read line
                String sequence = reader.readLine();
                l++;

                if (sequence.trim().length() >= removeLengthSmallerThan) {
                    out.println(newIdentifier);
                    out.println(sequence);
                } else {
                    removed++;
                }
            }

            line = reader.readLine();
            l++;
        }

        reader.close();
        out.flush();
        out.close();

        if (NameUtil.hasFileExtension(inFileName, NameSpace.SUFFIX_FASTQ))
            removed = removed / 2;

        MyLogger.debug("total lines = " + l + ", removed sequences = " + removed);
    }

    // main
    public static void main(String[] args) throws IOException{
//        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        Path workDir = Paths.get(System.getProperty("user.home") + "/Documents/ModelEcoSystem/454/2010-pilot/GigaDB-NZGO/SRA/bak/COI-spun");
        MyLogger.info("\nWorking path = " + workDir);

//        String inFile = "otus.fasta";//"sorted.fasta";
//        String regex = ".*\\|prep1.*";//".*\\|MID-.*";  //".*\\|28S.*";
//        String regex = ".*up=chimera.*";
//        splitFastAOrQTo2(workDir.toString(), inFastaFile, regex);
//        splitFastaByLabelItem(workDir.toString(), inFile, SiteNameParser.LABEL_SAMPLE_INDEX);

//        splitFastaBySites(workDir.toString(), "otus.fasta");

//        diffFastAFrom(workDir.toString(), "reads.fasta", "map.fasta");

//        splitFastaByLabelItem(workDir.toString(), "COI.fasta", 1, "NZAC03010806", "NZAC03010894", "NZAC03011914", "NZAC03011905", "NZAC03011634", "NZAC03010302", "NZAC03010913", "NZAC03010897", "NZAC03010906", "NZAC03012413", "NZAC03010752", "NZAC03011443", "NZAC03013543", "NZAC03011474", "NZAC03009260", "NZAC03010909", "NZAC03010904", "NZAC03010711", "NZAC03013640", "NZAC03011787");

//        String[] experiments = new String[]{"COI","COI-spun","ITS","trnL","18S","16S"}; //"COI","COI-spun","ITS","trnL","18S","16S"
//        for (String experiment : experiments) {
//            // go into each gene folder
//            Path workPath = Paths.get(workDir.toString(), experiment);
//            MyLogger.info("\nWorking path = " + workPath);
//            splitFastqByLabelItem(workPath.toString(), experiment + NameSpace.SUFFIX_FASTQ, SiteNameParser.LABEL_SAMPLE_INDEX);
//        }

        try(DirectoryStream<Path> stream = Files.newDirectoryStream(workDir)) {
            for(Path file : stream) {
                if (Files.exists(file)) {
                    String fileName = file.getFileName().toString();
                    if (fileName.toLowerCase().endsWith(NameSpace.SUFFIX_FASTQ) && !fileName.toLowerCase().contains("-new")) {
                        renameIdentifierInFastAOrQ(workDir.toString(), fileName, "\\|", ":", 1);
                    }
                }
            }
        }
    }

}
