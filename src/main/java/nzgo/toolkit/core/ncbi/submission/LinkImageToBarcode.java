package nzgo.toolkit.core.ncbi.submission;

import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameUtil;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.uparse.io.OTUsFileIO;
import nzgo.toolkit.core.util.IOUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Link Image To Barcode
 *
 * @author Walter Xie
 */
public class LinkImageToBarcode {

    protected List<String> Sequence_ID;
    protected List<String> Barcode;

    public LinkImageToBarcode() {
        Sequence_ID = new ArrayList<>();
        Barcode = new ArrayList<>();
    }

    public void addValue(String sequence_ID, String specimen_voucher) {
        Sequence_ID.add(sequence_ID);
        Barcode.add(specimen_voucher);
    }

    public void importBarcode(Path inPath) throws IOException {
        BufferedReader reader = OTUsFileIO.getReader(inPath, "Barcode list");

        int l = 0;
        String line = reader.readLine();
        while (line != null) {
            String[] items = FileIO.lineParser.getSeparator(0).parse(line); // default "\t"
            String[] ids = FileIO.lineParser.getSeparator(1).parse(items[0]); // default "|"

            final String sequence_ID = ids[0];
            final String specimen_voucher = ids[1];

            this.addValue(sequence_ID, specimen_voucher);
            l++;

            line = reader.readLine();
        }

        reader.close();

        if (Sequence_ID.size() != Barcode.size())
            throw new RuntimeException("Sequence_ID column number of elements does not equal to Barcode column !");

        MyLogger.info("\nImport Barcode = " + l);

    }

    public void removeImageNotInBarcodeList(Path workDir, Path notInDir, String suffix) throws IOException {
        int total = 0;
        int notIn = 0;
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(workDir)) {
            for(Path file : stream) {
                if (Files.exists(file)) {
                    String fileName = file.getFileName().toString();
                    if (fileName.toLowerCase().endsWith(suffix)) {
                        String code = NameUtil.getNameNoExtension(fileName);
                        if (!Barcode.contains(code)) {
                            MyLogger.warn("Image " + fileName + "'s barcode not in list, move to " + notInDir);
                            Files.move(file, Paths.get(notInDir.toString(), fileName));
                            notIn++;
                        }

                        total++;
                    }
                }
            }
        }

        MyLogger.info("\nTotal image " + total + ", where " + notIn + " not in Barcode list.");
    }

    //Main method
    public static void main(final String[] args) {
        String notInDirName = "notIn";

        Path workDir = Paths.get(System.getProperty("user.home") + "/Documents/ModelEcoSystem/454/2010-pilot/GigaDB-NZGO/images/");
        MyLogger.info("\nWorking path = " + workDir);

        Path workDir2 = Paths.get(System.getProperty("user.home") + "/Documents/ModelEcoSystem/454/2010-pilot/COITraditional/data/");
        Path inFilePath = Module.validateInputFile(workDir2, "COI-LCA.txt", "input");

        Path notInDir = Paths.get(workDir.toString(), notInDirName);

        LinkImageToBarcode sourceModifiersTable = new LinkImageToBarcode();
        try {
            sourceModifiersTable.importBarcode(inFilePath);

            if (Files.notExists(notInDir)) {
                Files.createDirectory(notInDir);
            } else if (IOUtil.isDirEmpty(notInDir)) {
                throw new IllegalArgumentException("Folder " + notInDirName + " not empty !");
            }

            sourceModifiersTable.removeImageNotInBarcodeList(workDir, notInDir, ".jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
