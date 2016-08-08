package nzgo.toolkit.uparse;


import jebl.evolution.io.ImportException;
import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.io.SequenceFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.math.Arithmetic;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.Separator;
import nzgo.toolkit.core.r.DataFrame;
import nzgo.toolkit.core.r.Matrix;
import nzgo.toolkit.core.r.Utils;
import nzgo.toolkit.core.uparse.Parser;
import nzgo.toolkit.core.uparse.UCParser;
import nzgo.toolkit.core.uparse.UPParser;
import nzgo.toolkit.core.util.ArrayUtil;
import nzgo.toolkit.core.util.StringUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static nzgo.toolkit.core.io.FileIO.getReader;

/**
 * @author Walter Xie
 */
public class CommunityMatrix {

    final Path finalOTUsPath; final Path outUpPath; final Path derepUcPath;


    public CommunityMatrix(Path finalOTUsPath, Path outUpPath, Path derepUcPath) {
        this.finalOTUsPath = finalOTUsPath;
        this.outUpPath = outUpPath;
        this.derepUcPath = derepUcPath;
    }

    public void createCommunityMatrix(Path cmPath, String sep, final String sampleRegx) throws IOException {
        DataFrame<Double> communityMatrix = getCommunityMatrix(sampleRegx, true);

        writeCommunityMatrix(cmPath, sep, communityMatrix);
    }

    protected DataFrame<Double> getCommunityMatrix(final String sampleRegx, final boolean sort) throws IOException {
        // remove size annotation
        List<String> finalOTUs = SequenceFileIO.importFastaLabelOnly(finalOTUsPath, true);
        validateID(finalOTUs);

//        DataFrame<String> derep_uc = Utils.readTable(derepUcPath); // too slow
//        DataFrame<String> out_up = Utils.readTable(outUpPath);
        UPParser upParser = UPParser.getInstance();
        HashMap<String, String> otus_map = upParser.createOTUsMap(finalOTUs, outUpPath);

        DataFrame<Double> communityMatrix = computeCommunityMatrix(finalOTUs, otus_map, null, sampleRegx, sort);

        int nrow1 = finalOTUs.size();
        int nrow2 = communityMatrix.nrow();

        return communityMatrix;
    }


    // single thread
    protected DataFrame<Double> computeCommunityMatrix(List<String> finalOTUs, HashMap<String, String> otus_map,
                                                       HashMap<String, String> derep_uc,
                                                       String sampleRegx, boolean sort) {
        // only derep_uc has all sample names, may add new samples when loop through derep.uc
        // sample name is the 1st element separated by sampleRegx, sort samples
        Set<String> samples = Parser.getSamples(otus_map.keySet(), sampleRegx, sort);

        DataFrame<Double> communityMatrix = new DataFrame<>(samples);
        int ncol = communityMatrix.ncol();

        MyLogger.info("Find " + ncol + " samples initially");

        if (derep_uc == null) {
            if (!derepUcPath.endsWith(NameSpace.SUFFIX_UC))
                throw new IllegalArgumentException("The UC mapping file is required ! " + derepUcPath.getFileName());

            Separator separator = new Separator("\t");
            int nrow = 0;
            try {
                BufferedReader reader = getReader(derepUcPath, "De-replication UC mapping file");

                String line = reader.readLine();
                while (line != null) {
                    String[] items = separator.parse(line);
                    nrow++;

                    // validation
                    if (items.length < UCParser.Target_Sequence_COLUMN_ID + 1)
                        throw new IllegalArgumentException("Invalid file format or separator to get " +
                                items.length + " columns, but expecting at least " +
                                (UCParser.Target_Sequence_COLUMN_ID + 1) + " columns in row " + nrow);

                    // rm size annotation
                    String duplSeqID = items[UCParser.Query_Sequence_COLUMN_ID];
                    String uniqSeqID = items[UCParser.Target_Sequence_COLUMN_ID];
                    if (finalOTUs.contains(uniqSeqID)) {
                        int rowId = communityMatrix.getRowId(uniqSeqID);
                        if (rowId < 0)
                            communityMatrix.appendRow(uniqSeqID, 0.0);


                        

                    } else if (otus_map.containsKey(uniqSeqID)){
                        String sampleName = Parser.getSample(uniqSeqID, sampleRegx);
                        int colId = communityMatrix.getColId(sampleName);
                        if (colId < 0) {
                            communityMatrix.appendCol(sampleName, 0.0);
                            ncol = communityMatrix.ncol();
                        }
                        double count = communityMatrix.getData(rowId, colId);
                        communityMatrix.setData(rowId, colId, count + 1);

                    }

                    line = reader.readLine();
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            MyLogger.info("Match " + dupl_seq_to_uniq.size() + " unique sequences to " +
                    otus.size() + " OTUs from " + nrow + " lines in mapping file.");

            Set<String> uniq_v = new HashSet<>(dupl_seq_to_uniq.values());
            if (otus.size() != uniq_v.size())
                throw new RuntimeException("OTUs (" + otus.size() + ") in UP mapping file" +
                        " does not match given OTUs (" + uniq_v.size() + ") !");
        } else {

        }



        for (int r = 0; r < nrow; r++) {
            // OTU representative sequence label
            String rowName = communityMatrix.getRowName(r);
            List<String> duplicateSequences = UCParser.getDuplicateSequences(rowName, derep_uc);
            duplicateSequences.add(0, rowName); // count OTU representative sequence
            double[] otuDupSeqCount = getOneRowCM(communityMatrix.getColNames(), duplicateSequences, sampleRegx);
            communityMatrix.addRowData(r, otuDupSeqCount);

            double rowsum = Arithmetic.sum(otuDupSeqCount);

            // unique reads belonging to this OTU
            List<String> memberNames = UPParser.getMembers(rowName, out_up);
            for (String member : memberNames) {
                duplicateSequences = UCParser.getDuplicateSequences(member, derep_uc);
                duplicateSequences.add(0, member); // count member sequence
                otuDupSeqCount = getOneRowCM(communityMatrix.getColNames(), duplicateSequences, sampleRegx);
                communityMatrix.addRowData(r, otuDupSeqCount);

                rowsum += Arithmetic.sum(otuDupSeqCount);
            }

            if (rowsum < 1)
                MyLogger.warn("Row " + r + " " + rowName + " is empty !");
            MyLogger.debug("Row " + r + " " + rowName + ", members = " + memberNames.size() + ", sum = " + rowsum);
        }

        return communityMatrix;
    }

    protected void validateID(List<String> finalOTUs) {
        for (int i = 0; i < finalOTUs.size(); i++) {
            String label = finalOTUs.get(i);
            finalOTUs.set(i, label.replaceAll("\\|\\d+", ""));
        }

        Set<String> finalOTUsSet = new HashSet<>(finalOTUs);

        if (finalOTUs.size() != finalOTUsSet.size())
            throw new IllegalArgumentException("Find duplicate ids from final OTUs !");
    }

    /**
     * count 1 row community matrix by matching labels to col names using given sampleRegx
     * @param colNames     sample names in the same order of community matrix
     * @param labels       the list of sequence labels containing sample names
     * @param sampleRegx   regx to extract sample name from label
     * @return
     */
    protected double[] getOneRowCM(String[] colNames, List<String> labels, String sampleRegx) {
        final double step = 1;
        double[] oneRowCM = new double[colNames.length];
        Arrays.fill(oneRowCM, (double) 0);
        for (String label : labels) {
            String sampleName = Parser.getSample(label, sampleRegx);
            int colId = ArrayUtil.indexOf(sampleName, colNames);
            if (colId < 0 || colId >= colNames.length)
                throw new IllegalArgumentException("Sample " + sampleName + " from label " + label + " does NOT match column names ! ");

            oneRowCM[colId] += step;
        }
        return oneRowCM;
    }

    protected void writeCommunityMatrix(Path cmPath, String sep, DataFrame<Double> communityMatrix) throws IOException {
        BufferedWriter writer = FileIO.getWriter(cmPath, "community matrix");

        String[] colNames = communityMatrix.getColNames();
        String[] rowNames = communityMatrix.getRowNames();

        double[][] data = communityMatrix.getData();
        // col names
        writer.write(""); // the column of row name
        for (int c = 0; c < communityMatrix.ncol(); c++) {
            writer.write(sep + colNames[c]);
        }
        writer.write("\n");
        // main
        for (int r = 0; r < communityMatrix.nrow(); r++) {
            writer.write(rowNames[r]); // the column of row name
            for (int c = 0; c < communityMatrix.ncol(); c++) {
                writer.write(sep + data[r][c]);
            }
            writer.write("\n");
        }

        writer.flush();
        writer.close();

        String[] summary = communityMatrix.summary();
        for (String s : summary)
            MyLogger.info(s);

        Path logPath = Paths.get(cmPath.getParent().toString(), cmPath.getFileName() + ".log");
        PrintStream log_out = FileIO.getPrintStream(logPath, "log");
        for (String s : communityMatrix.summary())
            log_out.println(s);
        log_out.close();
    }


    //Main method
    public static void main(final String[] args) {
//        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        Path workDir = Paths.get(System.getProperty("user.home") + "/WorkSpace/NGBA2/OTUs");
        MyLogger.info("\nWorking path = " + workDir);

        Path otusPath = Paths.get(workDir.toString(), "otus97", "otus.fasta");
        Path chimerasPath = Paths.get(workDir.toString(), "otus97", "chimeras.fasta");
        Path finalOTUsPath = Paths.get(workDir.toString(), "otus97", "16s.fasta");

        FinalOTUs finalOTUs = new FinalOTUs(otusPath, chimerasPath);
        try {
            finalOTUs.rmChimeraOTUs(finalOTUsPath);
        } catch (IOException | ImportException e) {
            e.printStackTrace();
        }

        Path derepUcPath = Paths.get(workDir.toString(), "qc", "derep.uc");

        Path outUpPath = Paths.get(workDir.toString(), "otus97", "out.up");

        Path cmPath = Paths.get(workDir.toString(), "otus97", "16s.csv");

        CommunityMatrix communityMatrix = new CommunityMatrix(finalOTUsPath, outUpPath, derepUcPath);
        try {
            // sample name is the 1st element separated by ., such as AB144_Leaf.6
            communityMatrix.createCommunityMatrix(cmPath, ",", "\\..*");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
