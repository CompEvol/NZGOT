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
import nzgo.toolkit.core.util.MapUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.DataFormatException;

/**
 * JEBL Sequence Util
 * @author Thomas Hummel
 * @author Walter Xie
 */
public class SequenceUtil {

    public static long getNumOfSequences(long numOfLines, String fileName) {
        return NameUtil.isFASTA(fileName) ? numOfLines / 2 : numOfLines / 4;
    }

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
     * @param sequences  Sequence list
     * @return amino acid sequence found from the list or null
     */
    public static String getSequenceStringFrom(String sequenceLabel, List<Sequence> sequences) {
        int i = indexOf(sequenceLabel, sequences);
        if (i > -1) {
            Sequence sequence = sequences.get(i);
            return sequence.getString();
        }
        return null;
    }

    /**
     *
     * @param sequenceLabel
     * @param sequences
     * @return       index of sequence in the list, -1 if cannot find
     */
    public static int indexOf(String sequenceLabel, List<Sequence> sequences) {
        String seqName;
        for (int i = 0; i < sequences.size(); i++) {
            Sequence sequence = sequences.get(i);
            seqName = sequence.getTaxon().toString();
            if (seqName.contentEquals(sequenceLabel))
                return i;
        }
        return -1;
    }

    /**
     * remove subset sequences from sequences
     * @param toRemove
     * @param sequences
     * @return
     */
    public static List<Sequence> removeAllFrom(List<Sequence> toRemove, List<Sequence> sequences) {
        List<Sequence> survivals = new ArrayList<>();
        String seqName;
        for (Sequence sequence : sequences) {
            seqName = sequence.getTaxon().toString();
            int i = indexOf(seqName, toRemove);
            // add if not in toRemove list
            if (i < 0)
                survivals.add(sequence);
        }
        return survivals;
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

        final int fileLimit = 100;
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
                item = NameUtil.getNameNoExtension(item); // SRR1706107.16107|8779

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
    @Deprecated
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

        long lineNum = 0;
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
    public static void renameIdFastAOrQ(String workPathString, String inFileName, String regex, String replacement,
                                        int removeLengthSmallerThan) throws IOException {
        Path inFastaFilePath = Module.validateInputFile(Paths.get(workPathString), inFileName,
                "original file", NameSpace.SUFFIX_FASTA, NameSpace.SUFFIX_FNA, NameSpace.SUFFIX_FASTQ);

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
            if ( (NameUtil.isFASTA(inFileName) && line.startsWith(">")) ||
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

    //fastqToSplit >= fastqToMath
    // make QIIME split_libraries_fastq.py work with FLASH
    public static void splitFastqGzByMatchedLabel(String workPathString, String fastqToSplit, String fastqToMath) throws IOException {
        Path file1 = Module.validateInputFile(Paths.get(workPathString), fastqToSplit,
                "original file", NameSpace.SUFFIX_GZ);
        Path file2 = Module.validateInputFile(Paths.get(workPathString), fastqToMath,
                "original file", NameSpace.SUFFIX_GZ);

        BufferedReader readerToSplit = FileIO.getReaderGZIP(file1, "fastq to split");

        List<String> matchedLabels = SequenceFileIO.importFastqGzLabelOnly(file2, "fastq to match");
        MyLogger.debug("sequences to match = " + matchedLabels.size());

        // NameUtil.getNameNoExtension(*.fastq.gz) = *.fastq
        Path outDiffPath = Paths.get(workPathString, "diff-" + NameUtil.getNameNoExtension(fastqToSplit) +
                NameSpace.SUFFIX_GZ);
        BufferedWriter writerDiff = FileIO.getWriterGZIP(outDiffPath, "sequences not matched in labels");
        Path outSamePath = Paths.get(workPathString, "same-" + NameUtil.getNameNoExtension(fastqToSplit) +
                NameSpace.SUFFIX_GZ);
        BufferedWriter writerSame = FileIO.getWriterGZIP(outSamePath, "sequences matched in labels");

        long lineNum = 0;
        long diff = 0;
        long same = 0;
        String line = readerToSplit.readLine();
        boolean isSame = false;
        while (line != null) {
            if (lineNum % 4 == 0) {
                String label = line.substring(1);
//                int index = matchedLabels.indexOf(label);
                if (matchedLabels.size() > 0 && label.contentEquals(matchedLabels.get(0))) {
//                    MyLogger.debug("index = " + index);
                    isSame = true;
                    same++;
                    matchedLabels.remove(0);
//                    MyLogger.debug("size = " + matchedLabels.size());
                } else {
                    isSame = false;
                    diff++;
                }
            }

            if (isSame) {
                writerSame.append(line);
                writerSame.newLine();
            } else {
                writerDiff.append(line);
                writerDiff.newLine();
            }
            line = readerToSplit.readLine();
            lineNum++;

            // flush every 100,000 sequences
            if (lineNum % 400000 == 0) {
                writerSame.flush();
                writerDiff.flush();
                MyLogger.debug("same = " + same + ", diff = " + diff);
            }
        }

        readerToSplit.close();
        writerSame.flush();
        writerDiff.flush();
        writerSame.close();
        writerDiff.close();

        MyLogger.info("explore same sequences = " + same + ", different sequences = " + diff +
                ", total = " + (diff+same));
    }

    public static void renameIdFastqGz(String workPathString, String inFileName, String regex, String replacement) throws IOException {
        Path inFilePath = Module.validateInputFile(Paths.get(workPathString), inFileName,
                "original file", NameSpace.SUFFIX_FASTQ_GZ);

        BufferedReader reader = FileIO.getReaderGZIP(inFilePath, "identifier to rename");

        // NameUtil.getNameNoExtension(*.fastq.gz) = *.fastq
        Path outFilePath = Paths.get(workPathString, NameUtil.getNameNoExtension(NameUtil.getNameNoExtension(inFileName)) +
                "-new" + NameSpace.SUFFIX_FASTQ_GZ);
        BufferedWriter writer = FileIO.getWriterGZIP(outFilePath, "identifier renamed");

        long lineNum = 0;
        String line = reader.readLine();
        while (line != null) {
            if (lineNum % 2 == 0) {
                line = line.replaceAll(regex, replacement);
            }
            writer.append(line);
            writer.newLine();

            if (lineNum % 100000 == 0) {
                writer.flush();
            }

            line = reader.readLine();
            lineNum++;
        }
        reader.close();
        writer.flush();
        writer.close();

        MyLogger.debug("total lines = " + lineNum + ", renamed sequences = " + (lineNum/4));
    }

    /**
     * summarize FastA Or Q By Label which contains sample information
     * @param workPathString
     * @param inFileName
     * @param regex         separator to delimit the label into String items[]
     * @param index         index of String items[] to retrieve sample
     * @throws IOException
     * @throws DataFormatException
     */
    public static void summarizeFastAOrQByLabel(String workPathString, String inFileName, String regex, int index)
            throws IOException, DataFormatException {
        Path inFilePath = Module.validateInputFile(Paths.get(workPathString), inFileName,
                "original file", NameSpace.SUFFIX_FASTA, NameSpace.SUFFIX_FNA, NameSpace.SUFFIX_FASTQ);

        BufferedReader reader = FileIO.getReader(inFilePath, "for summary");
        SortedMap<String, Integer> sampleSummary = new TreeMap<>();

        final int sampleLimit = 200;
        long lineNum = 0;
        String line = reader.readLine();
        while (line != null) {
            if ( (NameUtil.isFASTA(inFileName) && line.startsWith(">")) ||
                    (NameUtil.hasFileExtension(inFileName, NameSpace.SUFFIX_FASTQ) && lineNum % 4 == 0)) {

                String label = line.substring(1);
                String[] items = label.split(regex, -1);

                if (index >= items.length)
                    throw new DataFormatException("index " + index + " should < items.length " + items.length);

                String sample = items[index];

                if (sampleSummary.containsKey(sample)) {
                    Integer v = sampleSummary.get(sample);
                    v++;
                    sampleSummary.put(sample,v);
                } else {
                    sampleSummary.put(sample, 1);
                }

                if (sampleSummary.size() > sampleLimit)
                    throw new DataFormatException("sample size reaches the limit " + sampleLimit +
                            ", please check your regex or index, or reset sampleLimit in the code.");

            }

            line = reader.readLine();
            lineNum++;
        }
        reader.close();

        MyLogger.debug("lines = " + lineNum + ", sequences = " + getNumOfSequences(lineNum, inFileName));

        MyLogger.info(sampleSummary.size() + " samples :");
        int total = 0;
        // sorted by value in descending
        for (Map.Entry<String, Integer> entry : MapUtil.entriesSortedByValues(sampleSummary, false)) {
            MyLogger.info(entry.getKey() + "\t" + entry.getValue());
            total += entry.getValue();
        }
        MyLogger.info("total sequences = " + total);
    }

    // main
    public static void main(String[] args) throws IOException{
//        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        Path workDir = Paths.get(System.getProperty("user.home") + "/Projects/FishGutMicrobiomes/OTUs");
        MyLogger.info("\nWorking path = " + workDir);

        String file1 = "Undetermined_S0_L001_I1_001.fastq.gz";
        String file2 = "out.extendedFrags.fastq.gz";

        String file = "qc/postqc.fasta";

        //@806rcbc9_3 M00598:32:000000000-A5R9N:1:1101:17145:1723 1:N:0:0 orig_bc=GTATGCGCTGTA new_bc=GTATGCGCTGTA bc_diffs=0
        try {
            summarizeFastAOrQByLabel(workDir.toString(), file, "_", 0);
        } catch (DataFormatException e) {
            e.printStackTrace();
        }

//        renameIdFastqGz(workDir.toString(), file2, "_1:N:0:0", " 1:N:0:0");

//        splitFastqGzByMatchedLabel(workDir.toString(), file1, file2);

//        String inFile = "16S.fasta";//"sorted.fasta";
//        String regex = ".*\\|prep1.*";//".*\\|MID-.*";  //".*\\|28S.*";
//        String regex = ".*up=chimera.*";
//        splitFastAOrQTo2(workDir.toString(), inFastaFile, regex);
//        splitFastaByLabelItem(workDir.toString(), inFile, 0);

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

//        try(DirectoryStream<Path> stream = Files.newDirectoryStream(workDir)) {
//            for(Path file : stream) {
//                if (Files.exists(file)) {
//                    String fileName = file.getFileName().toString();
//                    if (fileName.toLowerCase().endsWith(NameSpace.SUFFIX_FASTQ) && !fileName.toLowerCase().contains("-new")) {
//                        renameIdentifierInFastAOrQ(workDir.toString(), fileName, "\\|", ":", 1);
//                    }
//                }
//            }
//        }
    }

}
