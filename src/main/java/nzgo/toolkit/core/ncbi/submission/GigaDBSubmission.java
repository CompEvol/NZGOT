package nzgo.toolkit.core.ncbi.submission;

import nzgo.toolkit.core.io.ConfigFileIO;
import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.taxonomy.TaxonLCA;
import nzgo.toolkit.core.uparse.io.OTUsFileIO;
import nzgo.toolkit.core.util.ListUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Submission of GigaDB
 *
 * @author Walter Xie
 */
public class GigaDBSubmission {

    protected static List<String[]> samples;
    protected static List<String> images;

    public GigaDBSubmission() {
        samples = new ArrayList<>();
        images = new ArrayList<>();
    }

    public List<String> getImageFileNameList(Path workDir, String suffix) throws IOException {
        List<String> imageNameList = new ArrayList<>();
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(workDir)) {
            for(Path file : stream) {
                if (Files.exists(file)) {
                    String fileName = file.getFileName().toString();
                    if (fileName.toLowerCase().endsWith(suffix)) {
                        imageNameList.add(fileName);
                    }
                }
            }
        }

        MyLogger.info("\nFind " + imageNameList.size() + " images (" + suffix + ") in total from " + workDir);
        return imageNameList;
    }


    //Main method
    public static void main(final String[] args) {
        final String biome = "temperate broadleaf forest";
        final String feature = "island";
        final String store = "Landcare NZ";

        final String collection_date = "06-Dec-2010";
        final String[] lat_lon_array = new String[]{"36.22465404 S 175.0698348 E", "36.21832519 S 175.0703615 E", "36.21677479 S 175.0736421 E", "36.21286308 S 175.0745618 E", "36.21538985 S 175.0753478 E", "36.21351260 S 175.0759379 E", "36.21177994 S 175.0788458 E", "36.21004661 S 175.0789839 E", "36.20175079 S 175.0711887 E", "36.19910561 S 175.0757758 E"};
        final String[] elev_array = new String[]{"50 m", "90 m", "160 m", "260 m", "240 m", "320 m", "420 m", "460 m", "595 m", "640 m"};

        GigaDBSubmission gigaDBSubmission = new GigaDBSubmission();

        Path workDir2 = Paths.get(System.getProperty("user.home") + "/Documents/ModelEcoSystem/454/2010-pilot/GigaDB-NZGO/");
        try {
            images = gigaDBSubmission.getImageFileNameList(Paths.get(workDir2.toString(), "images"), ".jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Path workDir = Paths.get(System.getProperty("user.home") + "/Documents/ModelEcoSystem/454/2010-pilot/COITraditional/data/");
        Path inFilePath = Module.validateInputFile(workDir, "COI-LCA-order-SampleSourceModifiersTable.txt", "input"); // 1st row is head
        try {
            samples = ConfigFileIO.importTSV(inFilePath, "samples");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Path workDir3 = Paths.get(System.getProperty("user.home") + "/Documents/ModelEcoSystem/database/");
        Path allSamplesPath = Module.validateInputFile(workDir3, "Invert-size-lifestage.txt", "input");


        int[] count = new int[4];
        try {
            BufferedReader reader = OTUsFileIO.getReader(allSamplesPath, "all samples");

            String line = reader.readLine();
            while (line != null) {
                String[] items = FileIO.lineParser.getSeparator(0).parse(line); // default "\t"
                final String Sample_ID = items[items.length-1];

                String[] matchedSample = ListUtil.getFirstRowMatch(samples, 6, Sample_ID);

                if (matchedSample != null) {
                    final String Species = matchedSample[7];
                    final String taxId = matchedSample[10];
                    final String samp_collect_device = matchedSample[11];

                    final String geo_loc_name = matchedSample[8];

                    int plotIndex;
                    if (geo_loc_name.toUpperCase().startsWith("CM30C30")) {
                        plotIndex = 9;
                    } else if (geo_loc_name.toUpperCase().startsWith("LB1")) {
                        plotIndex = 10;
                    } else {
                        String p = geo_loc_name.substring(0, geo_loc_name.indexOf("-"));
                        plotIndex = Integer.parseInt(p);
                    }
                    if (plotIndex < 1 || plotIndex > 10)
                        throw new IllegalArgumentException("Invalid plotIndex " + plotIndex + " from plot name " + geo_loc_name);

                    final String lat_lon = lat_lon_array[plotIndex - 1];
                    final String elev = elev_array[plotIndex - 1];

                } else {
                    MyLogger.warn("Cannot find sample from list : " + Sample_ID);
                }

                line = reader.readLine();
            }

            reader.close();

            MyLogger.info("\nTaxonomy identified by BLAST is lower than morphology (but still take morphology?) = " + count[0] +
                    ", by " + TaxonLCA.MORPHOLOGY + " = " + count[1] + ", (contradicted) take LCA = " + count[2] +
                    ", no morphology = " + count[3]);

        } catch (IOException e) {
            e.printStackTrace();
        }

//        Path outputFilePath = Paths.get(workDir.toString(), outputFileNameStem + "-SampleSourceModifiersTable" + outputFileExtension);

//        try {
//            sourceModifiersTable.outputTable(outputFilePath);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



    }

}
