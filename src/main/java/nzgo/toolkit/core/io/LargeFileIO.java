package nzgo.toolkit.core.io;

import nzgo.toolkit.core.logger.MyLogger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.SortedMap;

/**
 * Large File IO
 * @author Walter Xie
 */
public class LargeFileIO extends FileIO {

    public static int COUNT_LIMIT = 100;
    public static RandomAccessFile gi_taxid_raf;
    public static long filePointer;

    public LargeFileIO(File file) throws IOException {
        gi_taxid_raf = new RandomAccessFile(file, "r");

        MyLogger.info("\nLoad gi taxid mapping file " + file);

        long rafLength = gi_taxid_raf.length();
        filePointer = rafLength / 2;
    }

    public static void mapGIToTaxid(SortedMap<String, String> giTaxidMap) throws IOException {
        long rafLength = gi_taxid_raf.length();
        filePointer = rafLength / 2;
        long prePointer = 1;

        for (String gi : giTaxidMap.keySet()) {
            int sourceGi = Integer.parseInt(gi);

            String taxid = mapGIToTaxid(sourceGi, prePointer);
            if (taxid != null)
                giTaxidMap.put(gi, taxid);
        }
    }


    public static String mapGIToTaxid(int sourceGi, long prePointer) throws IOException {
        String taxid = null;
        int count = 0;

        int preGi = sourceGi;

        do {
            gi_taxid_raf.seek(filePointer);

            String incompleteLine = gi_taxid_raf.readLine();

            if (incompleteLine == null) {
                break;
            }

            String line = gi_taxid_raf.readLine();
            MyLogger.debug("\nline = " + line + ", filePointer =" + filePointer);

            String[] map = lineParser.getSeparator(0).parse(line);

//            MyLogger.debug("\nFound: gi = " + map[0] + ", taxid = " + map[1]);

            int curGi = Integer.parseInt(map[0]);

            if (sourceGi == curGi) {
                taxid = map[1];
                break;
            }

            if (curGi == preGi) {
                MyLogger.error("\ncurGi == preGi !"); //TODO
            } else {
                double weight = 0.9;
                long step = Math.abs( (filePointer - prePointer) / (long)((curGi - preGi) * weight));

                // smart jump, but may exit bound
                long pointer = filePointer + (sourceGi - curGi) * step;

                if (pointer < 1 || pointer >= gi_taxid_raf.length()) {
                    if (sourceGi > curGi) {
                        pointer = filePointer + Math.abs(filePointer - prePointer) / 2;
                    } else {
                        pointer = filePointer - Math.abs(filePointer - prePointer) / 2;
                    }
                }
                preGi = curGi;
                prePointer = filePointer;
                filePointer = pointer;
            }

            count++;

        } while (filePointer != -1 || count > COUNT_LIMIT);

        if (taxid == null) {
            MyLogger.warn("\nCannot find gi " + sourceGi + " after " + count + " searches.");
        } else {
            MyLogger.info("\nFind gi " + sourceGi + " taxid = " + taxid + " by " + count + " searches.");
        }

        return taxid;
    }

    protected String readLines(int numOfLines, RandomAccessFile gi_taxid_raf) throws IOException {
        String line = null;
        for (int i=0; i<numOfLines; i++) {
            line = gi_taxid_raf.readLine();
            long filePointer = gi_taxid_raf.getFilePointer();

            MyLogger.debug("\nline = " + line + ", filePointer =" + filePointer);
        }
        return line;
    }

    /**
     * single seach only
     * @param gi
     * @return
     * @throws IOException
     */
    public static String mapGIToTaxid(String gi) throws IOException {
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
        File gi_taxid_raf_nucl = new File("/Users/dxie004/Documents/ModelEcoSystem/454/BLAST/gi_taxid_nucl.dmp");
        LargeFileIO largeFileIO = new LargeFileIO(gi_taxid_raf_nucl);

        long start = System.currentTimeMillis();

        String taxid = mapGIToTaxid("261498544");

        long elapsedTimeMillis = System.currentTimeMillis()-start;

        MyLogger.info("\nIt takes " + elapsedTimeMillis + " milliseconds");
    }

}
