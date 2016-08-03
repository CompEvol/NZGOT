//package nzgo.toolkit.uparse;
//
//import nzgo.toolkit.core.logger.MyLogger;
//import nzgo.toolkit.core.math.Arithmetic;
//import nzgo.toolkit.core.r.DataFrame;
//import nzgo.toolkit.core.r.Matrix;
//import nzgo.toolkit.core.uparse.UCParser;
//import nzgo.toolkit.core.uparse.UPParser;
//
//import java.nio.file.Path;
//import java.util.List;
//import java.util.Set;
//import java.util.concurrent.ForkJoinPool;
//
///**
// * @author Walter Xie
// */
//public class CommunityMatrixMT extends CommunityMatrix {
//
//    private ForkJoinPool forkJoinPool;
//
//    public CommunityMatrixMT(Path finalOTUsPath, Path outUpPath, Path derepUcPath, int nthread) {
//        super(finalOTUsPath, outUpPath, derepUcPath);
//        if (nthread < 2)
//            throw new IllegalArgumentException("Invalid number of threads " + nthread + " !");
//
//        forkJoinPool = new ForkJoinPool(nthread);
//    }
//
//    @Override
//    protected Matrix computeCommunityMatrix(List<String> finalOTUs, Set<String> samples, String sampleRegx,
//                                          DataFrame<String> derep_uc, DataFrame<String> out_up) {
//        int ncol = samples.size();
//        int nrow = finalOTUs.size();
//        Matrix communityMatrix = new Matrix(nrow, ncol);
//        communityMatrix.setColNames(samples.toArray(new String[ncol]));
//        communityMatrix.setRowNames(finalOTUs.toArray(new String[nrow]));
//
//        MyLogger.info("Fill in community matrix to " + ncol + " columns " + nrow + " rows ...");
//
//        for (int r = 0; r < nrow; r++) {
//
//        }
//
//        return communityMatrix;
//    }
//}
