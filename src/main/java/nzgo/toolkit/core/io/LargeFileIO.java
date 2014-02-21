package nzgo.toolkit.core.io;

import nzgo.toolkit.core.logger.MyLogger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Large File IO
 * @author Walter Xie
 */
public class LargeFileIO extends FileIO {

//    public static void mapGIToTaxid(File file, int buffCapacity, SortedMap<Integer, String> giToTaxid, String msg) throws IOException {
//
//        RandomAccessFile raFile = new RandomAccessFile(file, "r");
//        ByteBuffer buffer = ByteBuffer.allocate(buffCapacity);
//
//        FileChannel fc = raFile.getChannel();
//            // Read the first buffCapacity bytes of the file.
//            int nread;
//            do {
//                nread = fc.read(buffer);
//
//
//
//            } while (nread != -1 && buffer.hasRemaining());
//
//            buffer.clear(); // do something with the data and clear/compact it.
//            raFile.close();
//
//
//
//
//            if (msg != null)
//            MyLogger.info("\nImport " + msg + " file: " + file);
//
//    }

    public static String mapGIToTaxid(File file, String gi, String msg) throws IOException {
        String taxid = null;
        RandomAccessFile raFile = new RandomAccessFile(file, "r");

        long rafLength = raFile.length();
        long filePointer = rafLength / 2;

        do {
            raFile.seek(filePointer);

            String incompleteLine = raFile.readLine();

            if (incompleteLine == null) {

            }

            String line = raFile.readLine();
            MyLogger.debug("\nline = " + line + ", filePointer =" + filePointer);

            String[] map = lineParser.getSeparator(0).parse(line);

//            MyLogger.debug("\nFound: gi = " + map[0] + ", taxid = " + map[1]);

            if (gi.contentEquals(map[0])) {
                taxid = map[1];
                break;
            } else if (gi.compareTo(map[0]) < 0) {
                filePointer = filePointer / 2;
            } else {
                filePointer += filePointer / 2;
            }

        } while (filePointer != -1);

        raFile.close();

        if (msg != null)
            MyLogger.info("\nSearch gi " + gi + " from " + file.getName() + ", taxid = " + taxid);

        return taxid;
    }

    //Main test method
    public static void main(final String[] args) throws Exception {
        File gi_taxid_nucl = new File("/Users/dxie004/Documents/ModelEcoSystem/454/BLAST/gi_taxid_nucl.dmp");

        long start = System.currentTimeMillis();

        String taxid = mapGIToTaxid(gi_taxid_nucl, "261498544", "");

        long elapsedTimeMillis = System.currentTimeMillis()-start;

        MyLogger.info("\nIt takes " + elapsedTimeMillis + " milliseconds");
    }

}
