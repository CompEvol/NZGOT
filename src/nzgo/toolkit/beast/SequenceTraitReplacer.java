package nzgo.toolkit.beast;

import beast.util.NexusParser;
import beast.util.XMLProducer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * use all models and parameters in a source xml,
 * but replace its alignment given nex file(s),
 * and *BEAST trait mapping given in replaceSpecTaxonMapping(PrintStream out)
 *
 * use java 1.7 File.copy
 * @author Walter Xie
 */
public class SequenceTraitReplacer {

    private static int[] treeSet = new int[]{2,4,8,16,32,64,128}; // 2,4,8,16,32,64,128,256

    public static void main(String[] args) throws IOException {

        if (args.length != 2)
            throw new IllegalArgumentException("XML input file and folder containing nex files are missing !");

        // provide a template in xml
        // this will replace alignment and trait mapping in the xml
        String xmlFilePath = args[0];
        String nexFilePath = args[1];

        String stem_old = "tree_0_";
        String stem_end = "_new.xml";

        for (int treeTotal : treeSet) {
            String xmlFileName = xmlFilePath + File.separator + "tree_" + treeTotal + "_0" + stem_end;
            Path source = Paths.get(xmlFileName);
            if (!Files.exists(source)) throw new IllegalArgumentException("Cannot find input xml " + xmlFileName);

            // copy sample XML to target folder
//        Path target = Paths.get(nexFilePath + File.separator + "xml" + File.separator + "tree_" + treeTotal + "_0.xml");
//        Files.copy(source, target);
//        System.out.println("\nCopy sample XML to " + target + " ...");

            for (int i = 0; i < 100; i++) { // 100
                String stem = "tree_" + Integer.toString(i) + "_";

                System.out.println("\nReading all nex files from " + nexFilePath + ", stem = " + stem);
                Map<String, String> parserMap = readAllNexus(nexFilePath, stem, treeTotal);

                try {
                    // read XML
                    BufferedReader reader = new BufferedReader(new FileReader(xmlFileName));
                    System.out.println("\nReading XML " + xmlFileName + " ...");

                    // write new XML
                    String outFile = xmlFilePath + File.separator + treeTotal + File.separator +
                            "tree_" + treeTotal + "_" + Integer.toString(i) + stem_end;
                    PrintStream out = new PrintStream(new FileOutputStream(outFile));
                    System.out.println("\nWriting new XML " + outFile + " ...");

                    String line = reader.readLine();
                    while (line != null) {
//                    if (line.contains("<?xml")) {
//                        out.println(line);
//                        line = reader.readLine();
//                        // skip some empty lines
//                        while (line.trim().equals("")) line = reader.readLine();
//                    }
                        if (line.contains("<data id=") || line.contains("<sequence id=") || line.contains("</data>")
                                || line.contains("<taxon id=") || line.contains("</taxon>")) {
                            // skip sequence or species - individuals mapping
                        } else if (line.contains("beast.math.distributions.Beta")) {
                            // trigger to print new sequence
                            for (String xml : parserMap.values()) {
                                out.println(xml);
                            }
                            out.println(line);
                        } else if (line.contains("<taxonset id=\"taxonsuperset\"")) {
                            // trigger to print new species - individuals mapping
                            out.println(line);
                            replaceSpecTaxonMapping(out);
                        } else {
                            String newLine = line.replaceAll(stem_old, stem);
                            out.println(newLine);
                        }

                        line = reader.readLine();
                    }

                    reader.close();
                    out.flush();
                    out.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    } // main

    private static Map<String, String> readAllNexus(String nexFilePath, String stem, int treeTotal) {
        File folder = new File(nexFilePath);
        File[] listOfFiles = folder.listFiles();

        Map<String, String> parserMap = new HashMap<String, String>();
        for (File file : listOfFiles) {
            String fileName = file.getName();
            if (file.isFile() && fileName.endsWith("nex") && fileName.startsWith(stem)) {
                try {
                    String index = fileName.substring(fileName.lastIndexOf("_")+1, fileName.lastIndexOf(".nex"));

                    if (parserMap.containsKey(index)) throw new IllegalArgumentException("parser map already had index = " + index);

                    if (Integer.parseInt(index) < treeTotal) {
                        NexusParser parser = new NexusParser();
                        parser.parseFile(file);

                        System.out.println("\nReading nex " + file + ", index = " + index);

//                    if (parser.m_taxa != null) {
//                        System.out.println(parser.m_taxa.size() + " taxa");
//                        System.out.println(Arrays.toString(parser.m_taxa.toArray(new String[0])));
//                    } else {
//                        throw new IllegalArgumentException("No taxa in nexus file " + fileName);
//                    }
//                    if (parser.m_trees != null) {
//                        System.out.println(parser.m_trees.size() + " trees");
//                    }
                        if (parser.m_alignment != null) {
                            String sXML = new XMLProducer().toRawXML(parser.m_alignment, "alignment");
//                            System.out.println(sXML);
                            parserMap.put(index, sXML);
                        } else {
                            throw new IllegalArgumentException("No alignment in nexus file " + fileName);
                        }
//                    if (parser.m_traitSet != null) {
//                        String sXML = new XMLProducer().toXML(parser.m_traitSet);
//                        System.out.println(sXML);
//                    }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        System.out.println("\nRead " + parserMap.size() + " nex files in total");
        return parserMap;
    }

    private static void replaceSpecTaxonMapping(PrintStream out) {
        StringBuilder xml = new StringBuilder(
                "                <taxon id=\"s07\" spec=\"TaxonSet\">\n" +
                        "                    <taxon id=\"s07_tip0\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s07_tip01\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s07_tip1\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s07_tip11\" spec=\"Taxon\"/>\n" +
                        "                </taxon>\n" +
                        "                <taxon id=\"s08\" spec=\"TaxonSet\">\n" +
                        "                    <taxon id=\"s08_tip0\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s08_tip01\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s08_tip1\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s08_tip11\" spec=\"Taxon\"/>\n" +
                        "                </taxon>\n" +
                        "                <taxon id=\"s05\" spec=\"TaxonSet\">\n" +
                        "                    <taxon id=\"s05_tip1\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s05_tip11\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s05_tip0\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s05_tip01\" spec=\"Taxon\"/>\n" +
                        "                </taxon>\n" +
                        "                <taxon id=\"s06\" spec=\"TaxonSet\">\n" +
                        "                    <taxon id=\"s06_tip0\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s06_tip01\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s06_tip1\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s06_tip11\" spec=\"Taxon\"/>\n" +
                        "                </taxon>\n" +
                        "                <taxon id=\"s09\" spec=\"TaxonSet\">\n" +
                        "                    <taxon id=\"s09_tip0\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s09_tip01\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s09_tip1\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s09_tip11\" spec=\"Taxon\"/>\n" +
                        "                </taxon>\n" +
                        "                <taxon id=\"s12\" spec=\"TaxonSet\">\n" +
                        "                    <taxon id=\"s12_tip1\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s12_tip11\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s12_tip0\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s12_tip01\" spec=\"Taxon\"/>\n" +
                        "                </taxon>\n" +
                        "                <taxon id=\"s00\" spec=\"TaxonSet\">\n" +
                        "                    <taxon id=\"s00_tip1\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s00_tip11\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s00_tip0\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s00_tip01\" spec=\"Taxon\"/>\n" +
                        "                </taxon>\n" +
                        "                <taxon id=\"s03\" spec=\"TaxonSet\">\n" +
                        "                    <taxon id=\"s03_tip1\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s03_tip11\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s03_tip0\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s03_tip01\" spec=\"Taxon\"/>\n" +
                        "                </taxon>\n" +
                        "                <taxon id=\"s04\" spec=\"TaxonSet\">\n" +
                        "                    <taxon id=\"s04_tip0\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s04_tip01\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s04_tip1\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s04_tip11\" spec=\"Taxon\"/>\n" +
                        "                </taxon>\n" +
                        "                <taxon id=\"s10\" spec=\"TaxonSet\">\n" +
                        "                    <taxon id=\"s10_tip0\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s10_tip01\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s10_tip1\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s10_tip11\" spec=\"Taxon\"/>\n" +
                        "                </taxon>\n" +
                        "                <taxon id=\"s01\" spec=\"TaxonSet\">\n" +
                        "                    <taxon id=\"s01_tip0\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s01_tip01\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s01_tip1\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s01_tip11\" spec=\"Taxon\"/>\n" +
                        "                </taxon>\n" +
                        "                <taxon id=\"s02\" spec=\"TaxonSet\">\n" +
                        "                    <taxon id=\"s02_tip0\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s02_tip01\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s02_tip1\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s02_tip11\" spec=\"Taxon\"/>\n" +
                        "                </taxon>\n" +
                        "                <taxon id=\"s11\" spec=\"TaxonSet\">\n" +
                        "                    <taxon id=\"s11_tip0\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s11_tip01\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s11_tip1\" spec=\"Taxon\"/>\n" +
                        "                    <taxon id=\"s11_tip11\" spec=\"Taxon\"/>\n" +
                        "                </taxon>\n");

        out.print(xml);
    }
}
