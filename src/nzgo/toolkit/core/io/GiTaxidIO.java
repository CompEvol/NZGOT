package nzgo.toolkit.core.io;

import nzgo.toolkit.core.logger.MyLogger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Large File IO
 * @author Walter Xie
 */
public class GiTaxidIO extends FileIO {

    public static int SEARCH_LIMIT = 60;
    public static RandomAccessFile gi_taxid_raf;
    public static long filePointer;

    public GiTaxidIO(File file) throws IOException {
        gi_taxid_raf = new RandomAccessFile(file, "r");

        MyLogger.info("\nLoad gi taxid mapping file " + file);

        long rafLength = gi_taxid_raf.length();
        filePointer = rafLength / 2;
    }


    public static String mapGIToTaxid(int sourceGi, long prePointer) throws IOException {
        String taxid = null;
        int count = 0;

        int preGi = sourceGi;
        long step = -1; // if step < 1 then disable smart jump
        do {
            gi_taxid_raf.seek(filePointer);

            String incompleteLine = gi_taxid_raf.readLine();

            if (incompleteLine == null) {
                break;
            }

            String line = gi_taxid_raf.readLine();
//            MyLogger.debug("line = " + line + ", filePointer = " + filePointer);

            String[] map = lineParser.getSeparator(0).parse(line);

            int curGi = Integer.parseInt(map[0]);

            if (sourceGi == curGi) {
                taxid = map[1];
                break;
            }

            long pointer;
            if (curGi == preGi) {
                MyLogger.error("\ncurGi == preGi, sourceGi = " + sourceGi + " !");
                taxid = null;
                break;
            } else if (curGi < sourceGi && sourceGi-curGi <= 10) {
                taxid = readLines(10, gi_taxid_raf, sourceGi);
                break;
            } else {
                if (step > 0) // if step < 1 then keep using binary search
                    step = Math.abs( (filePointer - prePointer) / (long)((curGi - preGi) * 0.9));

                pointer = estimateNextFilePointer(prePointer, sourceGi, curGi, step);

//                MyLogger.debug("curGi = " + curGi + ", step = " + step + ", pointer = " + pointer + ", filePointer = " + filePointer + ", prePointer = " + prePointer);
            }

            preGi = curGi;
            prePointer = filePointer;
            filePointer = pointer;

            count++;

        } while (filePointer != -1 && count < SEARCH_LIMIT);

        if (taxid == null) {
            MyLogger.error("\nCannot find gi " + sourceGi + " after " + count + " searches.");
        } else {
            MyLogger.debug("\nFind gi " + sourceGi + " taxid = " + taxid + " by " + count + " searches.\n");
        }

        return taxid;
    }

    /**
     * smart jump, if step < 1 then use binary search
     * adjust weight to make smaller/bigger step in the next point
     */
    protected static long estimateNextFilePointer(long prePointer, int sourceGi, int curGi, long step) throws IOException {

        // smart jump, but may exit bound
        long pointer = filePointer + (sourceGi - curGi) * step;

        if (pointer < 1 || pointer >= gi_taxid_raf.length() || step < 1) {
            if (sourceGi > curGi) {
                pointer = filePointer + Math.abs(filePointer - prePointer) / 2;
            } else {
                pointer = filePointer - Math.abs(filePointer - prePointer) / 2;
            }
        }
        return pointer;
    }

    protected static String readLines(int numOfLines, RandomAccessFile gi_taxid_raf, int sourceGi) throws IOException {
        for (int i=0; i<numOfLines; i++) {
            String line = gi_taxid_raf.readLine();
            filePointer = gi_taxid_raf.getFilePointer();

//            MyLogger.debug("line = " + line + ", filePointer = " + filePointer);

            String[] map = lineParser.getSeparator(0).parse(line);

            int curGi = Integer.parseInt(map[0]);

            if (sourceGi == curGi) {
                return map[1];
            }
        }
        return null; // error
    }

    /**
     * single seach only
     * @param gi
     * @return
     * @throws IOException
     */
    public String mapGIToTaxid(String gi) throws IOException {
        long rafLength = gi_taxid_raf.length();
        filePointer = rafLength / 2;
        long prePointer = 1;
        int sourceGi = Integer.parseInt(gi);
        int preGi = sourceGi;

        String taxid = mapGIToTaxid(sourceGi, prePointer);

        return taxid;
    }

    //Main test method
    public static void main(final String[] args) throws Exception {
        Path gi_taxid_raf_nucl = Paths.get(System.getProperty("user.home") + "/Projects/BLAST/gi_taxid_nucl_diff.dmp");
        GiTaxidIO giTaxidIO = new GiTaxidIO(gi_taxid_raf_nucl.toFile());

        long lStartTime = System.currentTimeMillis();

        String taxid = giTaxidIO.mapGIToTaxid("77133");

        MyLogger.info("taxid = " + taxid);

        long lEndTime = System.currentTimeMillis();

        MyLogger.info("Elapsed milliseconds: " + (lEndTime - lStartTime));
    }

}
