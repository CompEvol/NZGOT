package nzgo.toolkit.core.ncbi.submission;

import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.NameUtil;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.taxonomy.TaxonLCA;
import nzgo.toolkit.core.uparse.io.OTUsFileIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Source Modifiers Table for Barcode Submission Tool
 * http://www.ncbi.nlm.nih.gov/WebSub/html/help/source-table.html
 *
 * @author Walter Xie
 */
public class SourceModifiersTable {

    protected List<String> Sequence_ID;
    protected List<String> Collected_by;
    protected List<String> Collection_date;
    protected List<String> Country;
    protected List<String> Identified_by;
    //    protected List<String> Isolate;
    protected List<String> Lat_Lon;
    protected List<String> Specimen_voucher;

    protected List[] extraColumns;

    protected List<String> labels;

    public SourceModifiersTable() {
        Sequence_ID = new ArrayList<>();
        Collected_by = new ArrayList<>();
        Collection_date = new ArrayList<>();
        Country = new ArrayList<>();
        Identified_by = new ArrayList<>();
        //    Isolate = new ArrayList<>();
        Lat_Lon = new ArrayList<>();
        Specimen_voucher = new ArrayList<>();

        extraColumns = new List[0]; // make for loop valid

        labels = new ArrayList<>();
    }

    public void addValue(String sequence_ID, String specimen_voucher) {
        Sequence_ID.add(sequence_ID);
        Specimen_voucher.add(specimen_voucher);
    }

    public void addValue(String sequence_ID, String specimen_voucher, String collected_by, String collection_date, //String country,
                         String identified_by, String lat_lon, String... extraColumns) {
        // init
        if (extraColumns != null && Sequence_ID.size() < 1) {
            this.extraColumns = new List[extraColumns.length];
            for (int l = 0; l < extraColumns.length; l++) {
                this.extraColumns[l] = new ArrayList<String>();
            }
        }

        addValue(sequence_ID, specimen_voucher);
        Collected_by.add(collected_by);
        Collection_date.add(collection_date);
        Country.add("New Zealand");
        Identified_by.add(identified_by);
//        Isolate.add(isolate);
        Lat_Lon.add(lat_lon);

        if (extraColumns != null) {
            for (int l = 0; l < extraColumns.length; l++) {
                this.extraColumns[l].add(extraColumns[l]);
            }
        }
    }

    public void outputTwoColumnTable(Path outPath) throws IOException {
        if (Sequence_ID.size() != Specimen_voucher.size())
            throw new RuntimeException("Sequence_ID column number of elements does not equal to Specimen_voucher column !");

        PrintStream out = FileIO.getPrintStream(outPath, "Sample Two-Column Source Modifiers Table");

        //head
        out.println("Sequence_ID\tSpecimen_voucher");

        for (int i = 0; i < Sequence_ID.size(); i++) {
            out.println(Sequence_ID.get(i) + "\t" + Specimen_voucher.get(i));
        }

        out.flush();
        out.close();
    }

    public void outputTable(Path outPath) throws IOException {
        if (Sequence_ID.size() != Specimen_voucher.size()) //TODO more columns
            throw new RuntimeException("Sequence_ID column number of elements does not equal to Specimen_voucher column !");

        PrintStream out = FileIO.getPrintStream(outPath, "Sample Source Modifiers Table");

        //head
        out.println("Sequence_ID\tCollected_by\tCollection_date\tCountry\tIdentified_by\tLat_Lon\tSpecimen_voucher");

        for (int i = 0; i < Sequence_ID.size(); i++) {
            out.print(Sequence_ID.get(i) + "\t" + Collected_by.get(i) + "\t" + Collection_date.get(i) + "\t" +
                    Country.get(i) + "\t" + Identified_by.get(i) + "\t" + //Isolate.get(i) + "\t" +
                    Lat_Lon.get(i) + "\t" + Specimen_voucher.get(i));

            // extra columns
            for (int l = 0; l < extraColumns.length; l++) {
                out.print("\t" + extraColumns[l].get(i));
            }
            out.print("\n");
        }

        out.flush();
        out.close();
    }

    private String[] getTaxonIdentified(String[] items, int[] count) {
        String taxonIdentified = items[3];
        String identified_by = items[5];

        if (items[2].equalsIgnoreCase("null")) {
            taxonIdentified = items[1];
            identified_by = TaxonLCA.BLAST;
            count[3]++;
        } else {
            if (identified_by.equalsIgnoreCase("LCA")) {
                // contradicted, take LCA
                identified_by = TaxonLCA.MORPHOLOGY;
                count[2]++;
            } else if (identified_by.equalsIgnoreCase(TaxonLCA.MORPHOLOGY)) {
                count[1]++;
            } else {
                // BLAST is lower than morph, but still take morph?
                taxonIdentified = items[2];
                identified_by = TaxonLCA.MORPHOLOGY;
                count[0]++;
            }
        }

        return new String[]{taxonIdentified, identified_by};
    }

    private void addLabel(String label) {
        labels.add(label);
    }

    private String getLabel(String seqId) {
        for (String l : labels) {
            String id = l;
            if (id.contains("["))
                id = id.substring(0, id.indexOf("[")).trim();

            if (id.contentEquals(seqId))
                return ">" + l;
        }
        return null;
    }

    //Main method
    public static void main(final String[] args) {
        final String collected_by = "";
        final String collection_date = "06-Dec-2010";
        final String[] lat_lon_array = new String[]{"36.22465404 S 175.0698348 E", "36.21832519 S 175.0703615 E", "36.21677479 S 175.0736421 E", "36.21286308 S 175.0745618 E", "36.21538985 S 175.0753478 E", "36.21351260 S 175.0759379 E", "36.21177994 S 175.0788458 E", "36.21004661 S 175.0789839 E", "36.20175079 S 175.0711887 E", "36.19910561 S 175.0757758 E"};

        SourceModifiersTable sourceModifiersTable = new SourceModifiersTable();

        Path workDir = Paths.get(System.getProperty("user.home") + "/Documents/ModelEcoSystem/454/2010-pilot/COITraditional/data/");
        MyLogger.info("\nWorking path = " + workDir);

//        Path workDir2 = Paths.get(System.getProperty("user.home") + "/Documents/ModelEcoSystem/454/2010-pilot/COITraditional/data/");
//        Path inFilePath2 = Module.validateInputFile(workDir2, "COI-fixed.fasta", "old identifiers", NameSpace.SUFFIX_FASTA);
//        List<String> longIdentifier = new ArrayList<>();
//        try {
//            longIdentifier = SequenceFileIO.importFastaLabelOnly(inFilePath2, false);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        Path inFilePath = Module.validateInputFile(workDir, "COI-LCA-order.txt", "input");

        String outputFileNameStem = NameUtil.getNameNoExtension(inFilePath.toFile().getName());
        String outputFileExtension = NameUtil.getSuffix(inFilePath.toFile().getName());

        int[] count = new int[4];
        try {
            BufferedReader reader = OTUsFileIO.getReader(inFilePath, "LCA file");

            String line = reader.readLine();
            while (line != null) {
                String[] items = FileIO.lineParser.getSeparator(0).parse(line); // default "\t"
                String[] ids = FileIO.lineParser.getSeparator(1).parse(items[0]); // default "|"

                final String sequence_ID = ids[0];
                final String specimen_voucher = ids[1];
                final String plot = ids[2];

                int plotIndex;
                if (plot.toUpperCase().startsWith("CM30C30")) {
                    plotIndex = 9;
                } else if (plot.toUpperCase().startsWith("LB1")) {
                    plotIndex = 10;
                } else {
                    String p = plot.substring(0, plot.indexOf("-"));
                    plotIndex = Integer.parseInt(p);
                }
                if (plotIndex < 1 || plotIndex > 10)
                    throw new IllegalArgumentException("Invalid plotIndex " + plotIndex + " from plot name " + plot);

                final String lat_lon = lat_lon_array[plotIndex - 1];

                String[] ti = sourceModifiersTable.getTaxonIdentified(items, count);
                String taxonIdentified = ti[0];
                String identified_by = ti[1];

                final String label = sequence_ID + (taxonIdentified.length() > 1 ? " [organism=" + taxonIdentified + "]" : "");
                sourceModifiersTable.addLabel(label);

                String note = items[items.length-1];
//                for (String longL : longIdentifier) {
//                    String[] ids2 = FileIO.lineParser.getSeparator(1).parse(longL);
//                     if (ids2[0].contentEquals(sequence_ID)) {
//                         note = ids2[4];
//                         break;
//                     }
//                }
                sourceModifiersTable.addValue(sequence_ID, specimen_voucher, collected_by, collection_date,
                        identified_by, lat_lon, taxonIdentified, plot, label, note);

                line = reader.readLine();
            }

            reader.close();

            MyLogger.info("\nTaxonomy identified by BLAST is lower than morphology (but still take morphology?) = " + count[0] +
                    ", by " + TaxonLCA.MORPHOLOGY + " = " + count[1] + ", (contradicted) take LCA = " + count[2] +
                    ", no morphology = " + count[3]);

        } catch (IOException e) {
            e.printStackTrace();
        }

        Path outputFilePath = Paths.get(workDir.toString(), outputFileNameStem + "-SampleSourceModifiersTable" + outputFileExtension);

        try {
            sourceModifiersTable.outputTable(outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // fasta file
        inFilePath = Module.validateInputFile(workDir, "COI-fixed.fasta", "input", NameSpace.SUFFIX_FASTA);

        outputFileNameStem = NameUtil.getNameNoExtension(inFilePath.toFile().getName());
        outputFileExtension = NameUtil.getSuffix(inFilePath.toFile().getName());
        outputFilePath = Paths.get(workDir.toString(), outputFileNameStem + "-BankIt" + outputFileExtension);

        try {
            BufferedReader reader = OTUsFileIO.getReader(inFilePath, "old fasta file");
            PrintStream out = FileIO.getPrintStream(outputFilePath, "BankIt submission FASTA file");

            int i = 0;
            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith(">")) {
                    String label = line.substring(1);
//                    String[] ids = FileIO.lineParser.getSeparator(1).parse(label); // default "|"
                    String seqId = label.substring(0, label.indexOf("[")).trim();
                    String newLabel = sourceModifiersTable.getLabel(seqId);

                    if (newLabel == null) {
                        MyLogger.warn("Cannot find sequence id " + seqId + " from new labels !");
                    } else {
                        out.println(newLabel);
                        i++;
                        // sequences
                        line = reader.readLine();
                        out.println(line);
                    }
                }
                line = reader.readLine();
            }

            reader.close();
            out.flush();
            out.close();

            MyLogger.info("\nReplace " + i + " sequences labels.");

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
