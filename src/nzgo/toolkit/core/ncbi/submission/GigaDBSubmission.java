package nzgo.toolkit.core.ncbi.submission;

import nzgo.toolkit.core.io.ConfigFileIO;
import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameUtil;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.uparse.io.OTUsFileIO;
import nzgo.toolkit.core.util.FlexibleSourceTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
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

    protected static List<String[]> properties;
    protected List<String> images;

    public GigaDBSubmission() {
    }

    public void setPropertiesList(List<String[]> properties) {
        this.properties = new ArrayList<>(properties);
    }

    public String[] getProperties(String sampleId) {
        String[] ps = new String[]{"","",""};
        for (String[] p : properties) {
            if (p[p.length-1].equalsIgnoreCase(sampleId)) {
                if (p[1].contentEquals("1")) {
                    ps[0] = p[6]; // life_stage
                } else if (p[1].contentEquals("2")) {
                    ps[1] = p[2] + " mm"; // height_or_length
                } else if (p[1].contentEquals("3")) {
                    ps[2] = p[2] + " mm"; // max_width
                }
            }
        }
        return ps;
    }

    public void setImageFileNameList(Path workDir, String suffix) throws IOException {
        this.images = new ArrayList<>();
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(workDir)) {
            for(Path file : stream) {
                if (Files.exists(file)) {
                    String fileName = file.getFileName().toString();
                    if (fileName.toLowerCase().endsWith(suffix)) {
                        images.add(fileName);
                    }
                }
            }
        }

        MyLogger.info("\nFind " + images.size() + " images (" + suffix + ") in total from " + workDir);
    }

    public void outputImageFileMapping(Path outPath) throws IOException {
        PrintStream out = FileIO.getPrintStream(outPath, "files mapping");

        for (String fn : images) {
            out.println(fn + "\t" + NameUtil.getExtension(fn) + "\t" + NameUtil.getNameNoExtension(fn));
        }

        out.flush();
        out.close();
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

        Path workDir3 = Paths.get(System.getProperty("user.home") + "/Documents/ModelEcoSystem/database/");
        Path allPropertiesPath = Module.validateInputFile(workDir3, "Invert-size-lifestage.txt", "input");
        try {
            gigaDBSubmission.setPropertiesList(ConfigFileIO.importTSV(allPropertiesPath, " all properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        FlexibleSourceTable flexibleSourceTable = new FlexibleSourceTable("Sample_ID", "Species", "NCBI_taxId", "lat_lon",
                "geo_loc_name", "collection_date", "samp_collect_device", "biome", "feature", "elev", "samp_store_loc",
                "life_stage", "height_or_length", "max_width");


        Path workDir = Paths.get(System.getProperty("user.home") + "/Documents/ModelEcoSystem/454/2010-pilot/COITraditional/data/");
        Path inFilePath = Module.validateInputFile(workDir, "COI-LCA-order-SampleSourceModifiersTable.txt", "input"); // 1st row is head

        try {
            BufferedReader reader = OTUsFileIO.getReader(inFilePath, "samples");

            String line = reader.readLine();
            // 1st row is heads
            line = reader.readLine();
            while (line != null) {
                String[] items = FileIO.lineParser.getSeparator(0).parse(line); // default "\t"
                final String Sample_ID = items[6];

                final String Species = items[7];
                final String taxId = items[10];
                final String samp_collect_device = items[11];

                final String geo_loc_name = items[8];

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

                String[] properties = gigaDBSubmission.getProperties(Sample_ID);
                final String life_stage = properties[0];
                final String length = properties[1];
                final String max_width = properties[2];

                flexibleSourceTable.addValue(Sample_ID, Species, taxId, lat_lon, geo_loc_name, collection_date,
                        samp_collect_device, biome, feature, elev, store, life_stage, length, max_width);

                line = reader.readLine();
            }

            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Path workDir2 = Paths.get(System.getProperty("user.home") + "/Documents/ModelEcoSystem/454/2010-pilot/GigaDB-NZGO/");

        Path outputFilePath = Paths.get(workDir2.toString(), "Samples.txt");

        try {
            flexibleSourceTable.outputTableTranspose(outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }


// -------------- images
        outputFilePath = Paths.get(workDir2.toString(), "Images.txt");
        try {
            gigaDBSubmission.setImageFileNameList(Paths.get(workDir2.toString(), "images"), ".jpg");

            gigaDBSubmission.outputImageFileMapping(outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
