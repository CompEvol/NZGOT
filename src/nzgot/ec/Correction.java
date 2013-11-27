package nzgot.ec;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import nzgot.core.logger.Logger;
import jebl.evolution.taxa.Taxon;

/**
 * Correction to reads
 * @author Walter Xie
 * @author Thomas Hummel
 */
public class Correction {

    public static final String[] correctionHeader = new String[]{"-A","-C","-G","-T","+A","+C","+G","+T"};
        
    /**
     *
     * @param correctionCounts
     * @param correctNuc        1 letter code
     * @param fType
     */
    public static void count(int[] correctionCounts, String correctNuc, FNode.FType fType) {
        // correctNuc is 1 letter code
        if (correctNuc != null && correctNuc.length() == 1) {
            int index = indexOf(correctNuc, fType);
            if (index > -1) {
                correctionCounts[index]++;
            }
        }
    }

    public static String toString (int[] correctionCounts) {
        StringBuilder c = new StringBuilder();

        c.append("\n");
        for (int i=0; i<correctionHeader.length; i++) {
            if (i>0) c.append("\t");
            c.append(correctionHeader[i]);
        }
        c.append("\n");
        int total = 0;
        for (int i=0; i<correctionCounts.length; i++) {
            if (i>0) c.append("\t");
            c.append(correctionCounts[i]);
            total += correctionCounts[i];
        }
        c.append("\n");
        c.append("total = ");
        c.append(total);
        c.append("\n");

        return c.toString();
    }

    protected static int indexOf(String correctNuc, FNode.FType fType) {
        switch (fType) {
            case match_delete:
            case ins_read_delete:
                return getNucIndex(correctNuc, 0);
            case match_duplicate:
            case ins_read_duplicate:
                return getNucIndex(correctNuc, 4);
        }
        return -1;
    }

    protected static int getNucIndex(String correctNuc, int offset) {
        switch (correctNuc.toUpperCase()) {
            case "A":
                return offset;
            case "C":
                return 1 + offset;
            case "G":
                return 2 + offset;
            case "T":
                return 3 + offset;
        }
        return -1;
    }
    
    /**
     * writes correctionMatrix from linked HashMap to table file
     * @param correctionCountMatrix
     * @param filePath
     * @throws IOException
     */
    public void writeCorrectionMatrix(LinkedHashMap<Taxon, int[]> correctionCountMatrix, String filePath) throws IOException {
        PrintStream out = new PrintStream(new FileOutputStream(filePath));

        Logger.getLogger().info("\nSave correction count matrix in file: " + filePath);
        
        String header = "";
        for (int i=0; i<correctionHeader.length; i++) {
        	header = header + "\t" + correctionHeader[i];
        }
        out.println(header);
             
        for(Map.Entry<Taxon, int[]> entry : correctionCountMatrix.entrySet()) {
        	String value = "";
        	for (int i=0; i<entry.getValue().length; i++) {
        		value = value + "\t" + entry.getValue()[i];
        	}
            out.println(entry.getKey() + value);
        }
        
        out.flush();
        out.close();
    }
}
