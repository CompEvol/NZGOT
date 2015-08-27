package nzgo.toolkit.uparse;


import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.Sequence;
import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.io.SequenceFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.math.Arithmetic;
import nzgo.toolkit.core.r.DataFrame;
import nzgo.toolkit.core.r.Matrix;
import nzgo.toolkit.core.r.Utils;
import nzgo.toolkit.core.sequences.SequenceUtil;
import nzgo.toolkit.core.uparse.Parser;
import nzgo.toolkit.core.uparse.UCParser;
import nzgo.toolkit.core.uparse.UPParser;
import nzgo.toolkit.core.util.ArrayUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Walter Xie
 */
public class CommunityMatrix {

    public static void removeChimeras(Path otusPath, Path chimerasPath, Path finalOTUsPath) throws IOException, ImportException {
        List<Sequence> otus = SequenceFileIO.importNucleotideSequences(otusPath);
        List<Sequence> chimeras = SequenceFileIO.importNucleotideSequences(chimerasPath);

        List<Sequence> finalOTUs = SequenceUtil.removeAllFrom(chimeras, otus);

        SequenceFileIO.writeToFasta(finalOTUsPath, finalOTUs);
    }

    public static Matrix createCommunityMatrix(Path finalOTUsPath, Path outUpPath, Path derepUcPath) throws IOException {
        // sort samples
        final boolean sort = true;
        final String sampleRegx = "_.*";

        List<String> finalOTUs = SequenceFileIO.importFastaLabelOnly(finalOTUsPath, true); // remove size annotation
        Set<String> finalOTUsSet = new HashSet<>(finalOTUs);

        if (finalOTUs.size() != finalOTUsSet.size())
            throw new IllegalArgumentException("Find duplicate ids from final OTUs file : " + finalOTUsPath);

        DataFrame<String> derep_uc = Utils.readTable(derepUcPath);
        DataFrame<String> out_up = Utils.readTable(outUpPath);

        // only derep_uc has all sample names
        List<String> labels = derep_uc.getColData(UCParser.Query_Sequence_COLUMN_ID);
        // sample name is the 1st element separated by _, such as 806rcbc67_3069;size=19037;
        Set<String> samples = Parser.getSamples(labels, sampleRegx, sort);

        int ncol = samples.size();
        int nrow = finalOTUs.size();
        Matrix communityMatrix = new Matrix(nrow, ncol);
        communityMatrix.setColNames(samples.toArray(new String[ncol]));
        communityMatrix.setRowNames(finalOTUs.toArray(new String[nrow]));

        MyLogger.info("Fill in community matrix to " + ncol + " columns " + nrow + " rows ...");

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

            MyLogger.debug("Row " + r + " : " + rowName + ", members = " + memberNames.size() + ", sum = " + rowsum);
        }

        String[] summary = communityMatrix.summary();
        for (String s : summary)
            MyLogger.info(s);

        return communityMatrix;
    }

    /**
     * count 1 row community matrix by matching labels to col names using given sampleRegx
     * @param colNames     sample names in the same order of community matrix
     * @param labels       the list of sequence labels containing sample names
     * @param sampleRegx   regx to extract sample name from label
     * @return
     */
    public static double[] getOneRowCM(String[] colNames, List<String> labels, String sampleRegx) {
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

    public static void writeCommunityMatrix(Path cmPath, Matrix communityMatrix, String sep) throws IOException {
        BufferedWriter writer = FileIO.getWriter(cmPath, "community matrix");

        String[] colNames = communityMatrix.getColNames();
        String[] rowNames = communityMatrix.getRowNames();
        assert colNames.length == communityMatrix.ncol();
        assert rowNames.length == communityMatrix.nrow();

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
    }


    //Main method
    public static void main(final String[] args) {
//        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        Path workDir = Paths.get(System.getProperty("user.home") + "/Projects/FishGutMicrobiomes/OTUs");
        MyLogger.info("\nWorking path = " + workDir);

//        Path otusPath = Paths.get(workDir.toString(), "otus97", "otus.fasta");
//        Path chimerasPath = Paths.get(workDir.toString(), "otus97", "chimeras.fasta");
        Path finalOTUsPath = Paths.get(workDir.toString(), "otus97", "16s.fasta");
//        try {
//            removeChimeras(otusPath, chimerasPath, finalOTUsPath);
//        } catch (IOException | ImportException e) {
//            e.printStackTrace();
//        }

        Path derepUcPath = Paths.get(workDir.toString(), "qc", "derep.uc");

        Path outUpPath = Paths.get(workDir.toString(), "otus97", "out.up");

        Path cmPath = Paths.get(workDir.toString(), "otus97", "16s-97.csv");

        Matrix communityMatrix = null;
        try {
            communityMatrix = createCommunityMatrix(finalOTUsPath, outUpPath, derepUcPath);

            writeCommunityMatrix(cmPath, communityMatrix, ",");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
