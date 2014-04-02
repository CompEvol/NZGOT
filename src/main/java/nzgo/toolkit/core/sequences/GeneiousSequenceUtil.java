package nzgo.toolkit.core.sequences;

import nzgo.toolkit.core.io.ConfigFileIO;
import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.io.SequenceFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.util.ArrayUtil;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * Geneious Sequence Util
 * @author Thomas Hummel
 * @author Walter Xie
 */
public class GeneiousSequenceUtil {

    /**
     * batch to rename (append, replace, remove, ...) sequence labels regarding the file name
     * e.g. use to add gene and site to fastq after Geneious "Separate Reads By Barcode" and "Trim End"
     * @param workPath             working directory
     * @param fileSuffix
     * @param twoColumnTSVFiles     1st column is file name pattern, 2nd is label item, if multi-files, then will action (e.g. append) one by one
     * @param action         //TODO generalize actions append, replace, remove, ...
     * @param combinedFile         if not null, then combine all result into one file
     * @throws IOException
     */
    public static void renameLabelByFileNames(Path workPath, String fileSuffix, Path combinedFile, int action, Path... twoColumnTSVFiles) throws IOException {
        if (twoColumnTSVFiles == null)
            throw new IllegalArgumentException("At least one mapping file is required !");

        List<SortedMap<String, String>> patternItemMapList = new ArrayList<>();
        for (Path file : twoColumnTSVFiles) {
            SortedMap<String, String> patternItemMap = ConfigFileIO.importTwoColumnTSV(file,
                    "file name pattern and label item mapping");
            patternItemMapList.add(patternItemMap);
        }

        // if combinedFile is null, then out is null
        PrintStream out = FileIO.getPrintStream(combinedFile, "combined output");

        // better not > 500 files
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(workPath, "*" + fileSuffix)) {
            int size =0;
            for(Path filePath : stream) {
                String fileName = filePath.getFileName().toString();

                String combinedFileName = combinedFile == null ? null : combinedFile.getFileName().toString();
                if (!fileName.equals(combinedFileName)) {
                    String[] items = getItems(fileName, patternItemMapList);

                    if (action == 1) {
                        appendItemsToLabel(filePath, out, items);
                    }
                    size++;
                }
            }

            MyLogger.debug("Find " + size + " *" + fileSuffix + " files in " + workPath);
        }

        if (out != null) {
            out.flush();
            out.close();
        }
    }

    protected static String[] getItems(String fileName, List<SortedMap<String, String>> patternItemMapList) {
        String[] items = new String[patternItemMapList.size()];
        for (int i = 0; i < patternItemMapList.size(); i++) {
            SortedMap<String, String> patternItemMap = patternItemMapList.get(i);
            String item = getItem(fileName, patternItemMap);
            if (item == null)
                throw new IllegalArgumentException("Cannot match the file (" + fileName + ") from mapping list : " + patternItemMapList);
            items[i] = item;
        }

        MyLogger.debug("Get items " + ArrayUtil.toString(items) + ", given " + fileName);

        return items;
    }

    protected static String getItem(String fileName, SortedMap<String, String> patternItemMap){
        String item = null;
        for (Map.Entry<String, String> entry : patternItemMap.entrySet()) {
            if (fileName.contains(entry.getKey())) {
                if (item != null)
                    throw new IllegalArgumentException("Find file name " + fileName +
                            " has multi-matches: " + item + " and " + entry.getValue());
                item = entry.getValue();
            }
        }
        return item;
    }

    protected static void appendItemsToLabel(Path filePath, PrintStream out, String... items) throws IOException {
        MyLogger.info("Append " + ArrayUtil.toString(items) + " to sequences labels in " + filePath.getFileName());

        if (filePath.toString().endsWith(NameSpace.SUFFIX_FASTQ)) {
            SequenceFileIO.appendItemsToLabelsFastq(filePath, out, items);
        } else if (filePath.toString().endsWith(NameSpace.SUFFIX_FASTA)) {
            throw new UnsupportedOperationException("TODO !"); //TODO
        } else {
            throw new IllegalArgumentException("Invalid sequence file " + filePath);
        }
    }

    // main
    public static void main(String[] args) throws IOException{
//        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String[] experiments = new String[]{"CO1-soilkit"}; //"CO1-soilkit","CO1-indirect","ITS","trnL","16S","18S"

        for (String experiment : experiments) {
            Path workPath = Paths.get(System.getProperty("user.home") +
                    "/Documents/ModelEcoSystem/454/2010-pilot/WalterPipeline/", experiment, "deconvoluted");
            MyLogger.info("\nWorking path = " + workPath);

            Path midTSVFile = Paths.get(workPath.toString(), "MID.tsv");
            Path prepTSVFile = Paths.get(workPath.toString(), "Prep.tsv");
            Path combinedFile = Paths.get(workPath.toString(), experiment + NameSpace.SUFFIX_FASTQ);
            renameLabelByFileNames(workPath, NameSpace.SUFFIX_FASTQ, combinedFile, 1, midTSVFile, prepTSVFile);
        }
    }
}
