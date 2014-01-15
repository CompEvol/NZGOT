package nzgo.toolkit.core.util;

import beast.evolution.tree.Node;
import beast.util.TreeParser;
import nzgo.toolkit.core.community.util.SampleNameParser;
import nzgo.toolkit.core.io.Importer;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.taxonomy.Rank;
import nzgo.toolkit.core.taxonomy.Taxa;
import nzgo.toolkit.core.taxonomy.TaxaBreak;
import nzgo.toolkit.core.taxonomy.Taxon;
import nzgo.toolkit.core.uc.MixedOTUs;

import java.io.*;
import java.util.List;

/**
 * standard newick tree
 * depend on BEAST 2
 * @author Walter Xie
 */
public class TreeUtil {

    public static SampleNameParser sampleNameParser = new SampleNameParser(); //"\\|", "-"

    /**
     * clean not standard format generated from FastTree
     *
     * @param newickTree    FastTree output
     * @return              standard newick tree
     * @throws IOException
     */
    public static String cleanFastTreeOutput(String newickTree) {
        // e.g. Coleoptera:0.13579)0.909:0.03425)0.963:0.04190,
        // replace )0.909: to ):
        return newickTree.replaceAll("\\)[0..1]\\.\\d+:", "):");
    }

    /**
     * change newick tree into nexus tree
     *
     * @param newickTree
     * @param nexusFilePath
     * @throws IOException
     */
    public static void writeNexusTree(String newickTree, String nexusFilePath) throws IOException {
        MyLogger.info("\nCreating Nexus tree ...");

        BufferedWriter out = new BufferedWriter(new FileWriter(nexusFilePath));
        out.write("#nexus\n" + "Begin trees;\n");
        out.write("tree = " + newickTree + "\n");
        out.write("End;\n");
        out.flush();
        out.close();
    }

    /**
     * get traits from the tree leave nodes' labels
     * TODO generalize to select any type of trait
     * @param newickTree
     * @return
     */
    public static BioSortedSet<Element> getTaxaFromTree(TreeParser newickTree) {
        BioSortedSet<Element> traits = new BioSortedSet<>("taxa");

        Element notIdentified = new Element("Not identified");

        for (int i = 0; i < newickTree.getLeafNodeCount(); i++) {
            Node leafNode = newickTree.getNode(i);

            // only work for taxon at moment
            String taxon = getTaxon(leafNode.getID());

            if (taxon == null || "null".equalsIgnoreCase(taxon)) {
//                MyLogger.warn("Find invalid taxon " + taxon + " from tip " + leafNode.getID());
                notIdentified.incrementCount(1);
            } else {
                if (!traits.containsUniqueElement(taxon)) {
                    Element countableTaxon = new Element(taxon);
                    countableTaxon.incrementCount(1); // default count 0
                    traits.add(countableTaxon);
                } else {
                    Element countableTaxon = traits.getUniqueElement(taxon);
                    countableTaxon.incrementCount(1);
                }
            }
        }
        if (notIdentified.getCount() > 0)
            traits.add(notIdentified);

        return traits;
    }

    /**
     * method to specify how to retrieve taxon from tip label
     * and add into traits list
     * overwrite for new traits
     * @param label
     * @return
     */
    protected static String getTaxon(String label) {
        char c = label.charAt(0);
        String[] fields = sampleNameParser.parse(label);
        if (Character.isDigit(c)) {
//            if (fields.length < 10) return fields[8];
//            if (fields[9] == null) return fields[8];
//            if (fields[9].contentEquals("null")) return fields[8];
//            return fields[9];
            return fields[3];
        } else {
            return fields[1];
        }
    }

    protected static void printTraits(List traits) {
        MyLogger.info("\n" + traits.size() + " Taxa extracted from tree tips labels : ");
        for (Object taxon : traits) {
            MyLogger.info(taxon.toString());
        }
    }

    /**
     * TODO: generalize hard code to simplify label
     * @param label
     * @return
     */
    protected static String simplifyLabel(String label) {
        char c = label.charAt(0);
        if (Character.isDigit(c)) {
            String[] fields = sampleNameParser.parse(label);
            String taxon = fields[8];
            if (fields.length > 9 && fields[9] != null && !fields[9].contentEquals("null"))
                taxon = fields[9];
            return fields[0]+"|"+fields[1]+"|"+fields[7]+"|"+taxon;
        } else {
            return label;
        }
    }

    protected static void simplifyLabelsOfTree(TreeParser newickTree) {
        for (int i = 0; i < newickTree.getLeafNodeCount(); i++) {
            Node leafNode = newickTree.getNode(i);

            String label = simplifyLabel(leafNode.getID());
            leafNode.setID(label);
        }
    }

    protected static String getMetaString(String label, List traits) {
        for (Object tr : traits) {
            if (label.contains(tr.toString()))
                return "trait=" + tr.toString();
        }
        return "trait=Not identified";
    }

    protected static List<String> getMixedOTUs(String ucFilePath) {
        File ucFile = new File(ucFilePath);
        MixedOTUs mixedOTUs = new MixedOTUs(ucFile);

        List<String> mixedOTUsList = mixedOTUs.getMixedOTUs();
        for (int i = 0; i < mixedOTUsList.size(); i++) {
            mixedOTUsList.set(i, simplifyLabel(mixedOTUsList.get(i)));
        }

//        MyLogger.info(mixedOTUsList.size() + " Mixed OTUs = " + mixedOTUsList);

        return mixedOTUsList;
    }

    protected static String getMetaStringByDB(String label, List<String> mixedOTUs) {
        if (mixedOTUs.contains(label)) {
            return "col=MIXED";
        } else {
            char c = label.charAt(0);
            if (Character.isDigit(c)) {
                return "col=GO";
            } else {
                return "col=BOLD";
            }
        }
    }

    protected static String complementTaxon(String label, String workPath) throws IOException {

        File sequences = new File(workPath + "all.fasta");

        BufferedReader reader = Importer.getReader(sequences, null);

        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith(">")) {
                line = line.substring(1);
                String[] fields = sampleNameParser.parse(line);

                if (fields.length < 2)
                    throw new IllegalArgumentException("Error: invalid sequence label in the line: " + line);

                if (line.startsWith(label)) {
                    char c = label.charAt(0);
                    if (Character.isDigit(c)) {
                        return fields[0]+"|"+fields[1]+"|"+fields[7]+(fields.length > 9 ? "|"+fields[9] : "");
                    } else {
                        return line;
                    }
                }

            }

            line = reader.readLine();
        }

        reader.close();

        return label;
    }

    protected static String getRawNewickTree(String workPath, String stem) throws Exception {
        File treeFile = new File(workPath + stem + NameSpace.POSTFIX_NEWICK);

        BufferedReader reader = Importer.getReader(treeFile, "tree");
        String rawNewickTree = reader.readLine();
        reader.close();

        return rawNewickTree;
    }

    protected static void annotateTree(TreeParser newickTree, List traits) {

        for (int i = 0; i < newickTree.getLeafNodeCount(); i++) {
            Node leafNode = newickTree.getNode(i);

            String metaDataString = getMetaString(leafNode.getID(), traits);
            leafNode.metaDataString = metaDataString;
        }

    }

    protected static void annotateTreeByOTUs(TreeParser newickTree, List<String> mixedOTUs) {

        for (int i = 0; i < newickTree.getLeafNodeCount(); i++) {
            Node leafNode = newickTree.getNode(i);

            String metaDataString = getMetaStringByDB(leafNode.getID(), mixedOTUs);
            leafNode.metaDataString = metaDataString;
        }

    }

    //Main method
    public static void main(final String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        MyLogger.info("\nWorking path = " + workPath);

        final String stem = "tree";

        String rawNewickTree = getRawNewickTree(workPath, stem);
        String cleanedNewickTree = cleanFastTreeOutput(rawNewickTree);
        TreeParser newickTree = new TreeParser(cleanedNewickTree, false, false, true, 1);
        simplifyLabelsOfTree(newickTree);

        // annotate tree by database
//        List<String> mixedOTUs = getMixedOTUs(workPath + "clusters.uc");
//        annotateTreeByOTUs(newickTree, mixedOTUs);
//        writeNexusTree(newickTree.getRoot().toNewick() + ";", workPath + "new-" + stem + NameSpace.POSTFIX_NEX);

        // taxa break
        BioSortedSet<Element> taxaFromTree = getTaxaFromTree(newickTree);
        Taxa taxa = new Taxa(taxaFromTree);
        TaxaBreak taxaBreak = new TaxaBreak(taxa, Rank.ORDER);
        Taxon bioClassification = new Taxon("Insecta", "50557");
        taxaBreak.setBioClassification(bioClassification);
        taxaBreak.writeTaxaBreakTable(workPath);

        // annotate tree by traits (taxa)
        List traits = taxaBreak.getTaxaOnGivenRank();
        annotateTree(newickTree, traits);
        writeNexusTree(newickTree.getRoot().toNewick() + ";", workPath + "taxa-" + stem + NameSpace.POSTFIX_NEX);

    }


}
