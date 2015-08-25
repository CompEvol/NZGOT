package nzgo.toolkit.core.r;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.Separator;
import nzgo.toolkit.core.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static nzgo.toolkit.core.io.FileIO.getReader;

/**
 * resemble R utils
 *
 * @author Walter Xie
 */
public class Utils {
    public static final char comment = '#';

    /**
     *
     * @param file           file
     * @param sep            separator
     * @param hasColnames    whether file contains column names in the 1st row
     * @param hasRownames    whether file contains row names in the 1st column
     * @param quote          remove all quotes, such as \" or \' in each line
     * @return               DataFrame
     */
    public static DataFrame readTable(Path file, String sep, boolean hasColnames, boolean hasRownames, String quote) {
        DataFrame dataFrame = null;
        Separator separator = new Separator(sep);
        int firstCol = 0;
        if (hasRownames)
            firstCol = 1;
        int nrow = 0;

        try {
            BufferedReader reader = getReader(file, "");

            String line = reader.readLine();
            while (line != null) {
                if (!line.startsWith(String.valueOf(comment))) {
                    // remove all quotes, if given
                    line = line.replaceAll(quote, "");
                    String[] items = separator.parse(line);

                    // validation
                    if (items.length-firstCol < 1)
                        throw new IllegalArgumentException("Invalid file format or separator: ncol = " +
                                (items.length-firstCol) + "excluding row names, line = " + line);

                    // create data frame
                    if (dataFrame == null) {
                        List<String> colNames = new ArrayList<>();
                        if (hasColnames) {
                            colNames.addAll(Arrays.asList(items).subList(firstCol, items.length));
                        } else {
                            colNames = StringUtil.getNames("V", 0, items.length);
                        }

                        dataFrame = new DataFrame(colNames);

                        if (!hasColnames) {
                            dataFrame.appendRow(items);
                            nrow++;
                        }

                    } else {
                        dataFrame.appendRow(items);
                        nrow++;

                        // validation: dataFrame.ncol() here ignore hasRownames
                        if ( items.length != dataFrame.ncol() )
                            throw new IllegalArgumentException("Invalid items NOT fit in data frame: items = " +
                                    dataFrame.ncol() + ", line = " + line);
                    } // end if dataFrame == null

                } else {
                    MyLogger.warn("Ignore comment : " + line);
                } // end if !line.startsWith

                line = reader.readLine();
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // validation
        assert dataFrame != null;
        if ( nrow != dataFrame.nrow() )
            throw new IllegalArgumentException("Invalid data frame: nrow = " + dataFrame.nrow() + ", != " + nrow);

        // 1st col is row names
        if (hasRownames) {
            dataFrame.setRowNames(dataFrame.getData(0));
            dataFrame.removeCol(0);
        }

        MyLogger.debug("Create data frame : ncol = " + dataFrame.ncol() + ", nrow = " + dataFrame.nrow() +
                ", colnames size = " + dataFrame.getColNames().size() + ", rownames size = " + dataFrame.getRowNames().size());

        return dataFrame;
    }

    public static DataFrame readTable(Path file, boolean hasColnames, boolean hasRownames) {
        return readTable(file, "\t", hasColnames, hasRownames, "");
    }

    public static DataFrame readTable(Path file) {
        return readTable(file, false, false);
    }

    public static DataFrame readCSV(Path file, boolean hasColnames, boolean hasRownames) {
        return readTable(file, ",", hasColnames, hasRownames, "\"");
    }

}
