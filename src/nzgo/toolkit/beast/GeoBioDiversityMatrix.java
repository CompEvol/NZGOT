package nzgo.toolkit.beast;

import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.TreeParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Walter Xie
 */
public class GeoBioDiversityMatrix {

    String[] plots = new String[]{"Plot_1", "Plot_2", "Plot_3", "Plot_4", "Plot_5", "Plot_6", "Plot_7", "Plot_8", "CM30c30", "LB1"};
    Double[] elevations = new Double[]{50.0,90.0,160.0,260.0,240.0,320.0,420.0,460.0,595.0,640.0};

//    static String path ="/Users/local/EC/dxie004/Documents/My Papers/New Zealand Genomic Observatory/by plots/";
//    static String[] files = new String[]{"Sequences_Inv_Pitfall_Trap_903_CO1 alignment last tree.newick",
//            "Sequences_Inv_Leaf_Litter_517_CO1 alignment last tree 2.newick"};

    // Note: collectionTypes length is controlling return plot or type in getLabelFrom
    String[] collectionTypes = new String[]{"Invertebrate_Pitfall_Trap", "Leaf_Litter_Collection"};

//    static String path ="/Users/local/EC/dxie004/Documents/My Papers/New Zealand Genomic Observatory/by collection types/";
//    static String[] files = new String[]{"CO1_Plot_4_alignment_last tree.newick", "CO1_Plot_5_alignment_last tree.newick",
//            "CO1_Plot_6_alignment_last tree.newick", "CO1_CM30_alignment_last tree.newick",
//            "CO1_LB1_alignment_last tree.newick"};

    static String path ="/Users/local/EC/dxie004/Documents/My Papers/New Zealand Genomic Observatory/all CO1/";
    static String[] files = new String[]{"All_CO1_alignment_last_tree.newick"};

    private boolean verbose = false;

    public String addTraitToTree(String tree, String[] traits) {
        String newTree = "";
        String[] names = tree.split("\'", -1);
        for (int i = 0; i < names.length; i++) {
            String tmp = names[i];
//            System.out.println(tmp);
            if (i % 2 != 0) {
                String taxonName = tmp;
                tmp = "\'" + names[i].split("\\|", -1)[0] + "|" + names[i].split("\\|", -1)[1] +
                        "|" + names[i].split("\\|", -1)[3] + "|" + names[i].split("\\|", -1)[4] +
                        "|" + names[i].split("\\|", -1)[5] + "\'";
                tmp += "[&state=\"" + getIndexFrom(taxonName, traits) + "\"]";
//                tmp += ":[&state=" + getIndexFrom(taxonName, plots) + "]";
                if (getIndexFrom(taxonName, traits) < 0)
                    throw new RuntimeException(names[i] + " cannot find plot index = " + getIndexFrom(taxonName, traits));
            }
//            else if (i > 0) {
//                if (names[i].indexOf(":") != 0)
//                    throw new RuntimeException(names[i] + " is not correct newick format ! ");
//
//                tmp = names[i].substring(1, names[i].length());
//            }
            newTree += tmp;
        }
        return newTree;
    }

    public void getGeoBioDiversityMatrix(String tree, boolean verbose) throws Exception {
        this.verbose = verbose;
        getGeoBioDiversityMatrix(tree);
    }

    public void getGeoBioDiversityMatrix(String tree) throws Exception {
        System.out.println(tree);
        TreeParser newickTree = new TreeParser(tree, false);
        String newTree = newickTree.getRoot().toNewick(new ArrayList<String>(Arrays.asList(newickTree.getTaxaNames())));
        if (!tree.equals(newTree)) System.err.println("Error: newickTree != inputted tree \n" + newTree);

        newTree = newickTree.getRoot().toNewick(new ArrayList<String>(Arrays.asList(parseNames(newickTree.getTaxaNames()))));
        System.out.println(newTree);
        print(newickTree);

        System.out.println("\n=================== By Locations ===================\n");

        System.out.println("\nAdd plot index into tree trait : ");
        System.out.println(addTraitToTree(tree, plots));
        System.out.println("\n");

        double[][] geoBioDiversities = new double[plots.length][plots.length];
        int[][] totalNodes = new int[2][plots.length];
        double[] totalLength = new double[plots.length];

        double totalLengthOverTree = traverse(newickTree, geoBioDiversities, plots, totalNodes, totalLength);

        if (verbose) print(geoBioDiversities, plots, "Geo Bio Diversity Matrix By Locations : ");

        calculatePercentage(geoBioDiversities);

        print(geoBioDiversities, plots, "Geo Bio Diversity Matrix By Locations : ");
        print(totalNodes, totalLength, totalLengthOverTree, 1);

        calculateElevationDiff(geoBioDiversities, 1);
        print(geoBioDiversities, plots, "Geo Bio Diversity vs Elevation Matrix By Locations : ");

        System.out.println("\n=================== By Collection Types ===================\n");

        System.out.println("\nAdd collection types index into tree trait : ");
        System.out.println(addTraitToTree(tree, collectionTypes));
        System.out.println("\n");

        geoBioDiversities = new double[collectionTypes.length][collectionTypes.length];
        totalNodes = new int[2][collectionTypes.length];
        totalLength = new double[collectionTypes.length];

        totalLengthOverTree = traverse(newickTree, geoBioDiversities, collectionTypes, totalNodes, totalLength);

        if (verbose) print(geoBioDiversities, collectionTypes, "Geo Bio Diversity Matrix By Collection Types : ");

        calculatePercentage(geoBioDiversities);

        print(geoBioDiversities, collectionTypes, "Geo Bio Diversity Matrix By Collection Types : ");
        print(totalNodes, totalLength, totalLengthOverTree, 1);

        System.out.println("\n=================== By Locations and Collection Types ===================\n");

        int len = plots.length * collectionTypes.length;
        geoBioDiversities = new double[len][len];
        totalNodes = new int[2][len];
        totalLength = new double[len];

        totalLengthOverTree = traverse(newickTree, geoBioDiversities, plots, collectionTypes, totalNodes, totalLength);

        if (verbose) print(geoBioDiversities, plots, collectionTypes, "Geo Bio Diversity Matrix By Locations and Collection Types : ");

        calculatePercentage(geoBioDiversities);

        print(geoBioDiversities, plots, collectionTypes, "Geo Bio Diversity Matrix By Locations and Collection Types : ");
        print(totalNodes, totalLength, totalLengthOverTree, 2);

        calculateElevationDiff(geoBioDiversities, 2);
        print(geoBioDiversities, plots, collectionTypes, "Geo Bio Diversity vs Elevation Matrix By Locations and Collection Types : ");

        System.out.println("\n=================== End ===================\n");
    }

    private void print(double[][] geoBioDiversities, String[] traitsLong, String[] traitsShort, String tittle) {
        System.out.println(tittle);

        System.out.print("");

        for (String p : traitsLong) {
            System.out.print("\t\t" + p);
        }
        System.out.print("\n");
        System.out.print("\t");
        for (String p : traitsLong) {
            for (String p2 : traitsShort) {
                System.out.print("\t" + p2);
            }
        }
        System.out.print("\n");

        int len =traitsLong.length * traitsShort.length;
        assert geoBioDiversities.length == len && geoBioDiversities[0].length == len;

        int i = 0;
        for (String p : traitsLong) {
            System.out.print(p);
            for (String p2 : traitsShort) {
                System.out.print("\t" + p2);
                for (int j = 0; j < len; j++) {
                    System.out.print("\t" + ((i > j) ? (geoBioDiversities[i][j] * 100 + "%") : geoBioDiversities[i][j]));
                }

                System.out.print("\n");
                i++;
            }
        }
    }

    private double traverse(TreeParser newickTree, double[][] geoBioDiversities, String[] traitsLong, String[] traitsShort, int[][] totalNodes, double[] totalLength) {
        if (traitsLong.length < traitsShort.length)
            throw new RuntimeException("Error: Trait 1 length = " + traitsLong.length + " < Trait 2 length = " + traitsShort.length);

        final int taxonCount = newickTree.getLeafNodeCount();
        final Node[] nodes = newickTree.getNodesAsArray();
        final String[] taxaNames = newickTree.getTaxaNames();
        double totalLengthOverTree = 0;

        for (int n = 0; n < taxonCount; n++) {
            assert (nodes[n].isLeaf());

            int matrixId = getIndexFrom(taxaNames[nodes[n].getNr()], traitsLong, traitsShort);

            if (matrixId < 0)
                throw new RuntimeException("Cannot find traits from " + taxaNames[nodes[n].getNr()]);
            if (matrixId >= traitsLong.length * traitsShort.length)
                throw new RuntimeException("Index = " + matrixId + " out of boundary at node " + n);

            if (verbose) {
                System.out.println(n + ", geoBioDiversities[" + matrixId + "][" + matrixId + "] = " +
                        geoBioDiversities[matrixId][matrixId] + " + " + nodes[n].getLength());
            }

            geoBioDiversities[matrixId][matrixId] += nodes[n].getLength();

            totalNodes[0][matrixId] ++;
            totalNodes[1][matrixId] ++;
            totalLength[matrixId] += nodes[n].getLength();
            totalLengthOverTree += nodes[n].getLength();
        }

        for (int n = taxonCount; n < nodes.length - 1; n++) { // exclude nodes[length - 1] which is root
            if (nodes[n].isRoot()) System.out.println(n + " is Root : " + nodes[n].getLength());

            List<Integer> traitsIndex = getAllTraitsIndexFromInternalNode(nodes[n], taxaNames, traitsLong, traitsShort);
            Collections.sort(traitsIndex);

            if (verbose) System.out.println(n + ", traitsIndex = " + traitsIndex);

            if (traitsIndex.size() < 1) {
                throw new RuntimeException("plotsIndex.size() < 1 when n = " + n);
            } else if (traitsIndex.size() == 1) {
                if (verbose) {
                    System.out.println(n + ", geoBioDiversities[" + traitsIndex.get(0) + "][" + traitsIndex.get(0) + "] = " +
                            geoBioDiversities[traitsIndex.get(0)][traitsIndex.get(0)] + " + " + nodes[n].getLength());
                }
                geoBioDiversities[traitsIndex.get(0)][traitsIndex.get(0)] += nodes[n].getLength();

                totalNodes[0][traitsIndex.get(0)] ++;
                totalNodes[1][traitsIndex.get(0)] ++;
                totalLength[traitsIndex.get(0)] += nodes[n].getLength();

            } else {
                for (int i = 0; i < traitsIndex.size(); i++) {
                    for (int j = i + 1; j < traitsIndex.size(); j++) {
                        if (verbose) {
                            System.out.println(n + ", geoBioDiversities[" + traitsIndex.get(i) + "][" + traitsIndex.get(j) + "] = " +
                                    geoBioDiversities[traitsIndex.get(i)][traitsIndex.get(j)] + " + " + nodes[n].getLength());
                        }

                        geoBioDiversities[traitsIndex.get(i)][traitsIndex.get(j)] += nodes[n].getLength();

                    }
                    totalNodes[1][traitsIndex.get(i)] ++;
                    totalLength[traitsIndex.get(i)] += nodes[n].getLength();
                }
            }

            totalLengthOverTree += nodes[n].getLength();
        }
        return totalLengthOverTree;

    }

    private List<Integer> getAllTraitsIndexFromInternalNode(Node node, String[] taxaNames, String[] traitsLong, String[] traitsShort) {
        assert (!node.isLeaf());

        List<Integer> traitsIndex = new ArrayList<Integer>();

        List<Node> leafNodes = node.getAllLeafNodes();

        // get all unique leaf nodes index under this internal node
        for (Node n : leafNodes) {
//            System.out.println(n.isLeaf() + ": " + n.getNr());

            int matrixId = getIndexFrom(taxaNames[n.getNr()], traitsLong, traitsShort);

            if (matrixId < 0)
                throw new RuntimeException("Cannot find traits from " + taxaNames[n.getNr()]);
            if (matrixId >= traitsLong.length * traitsShort.length)
                throw new RuntimeException("Index = " + matrixId + " out of boundary at node " + n);


            if (!traitsIndex.contains(matrixId)) traitsIndex.add(matrixId);
        }

        return traitsIndex;
    }

    private int getIndexFrom(String taxonName, String[] traitsLong, String[] traitsShort) {
        int indexLong = getIndexFrom(taxonName, traitsLong);
        int indexShort = getIndexFrom(taxonName, traitsShort);
        if (indexLong < 0 || indexShort< 0) return -1;
        return indexLong * traitsShort.length + indexShort;
    }

    //============= 1 trait ================

    private String[] parseNames(String[] taxaNames) {
        String[] newNames = new String[taxaNames.length];
        for (int i = 0; i < taxaNames.length; i++) {
            newNames[i] = taxaNames[i].split("\\|", -1)[0] + "|" + taxaNames[i].split("\\|", -1)[1] +
                    "|" + taxaNames[i].split("\\|", -1)[3] + "|" + taxaNames[i].split("\\|", -1)[4] +
                    "|" + taxaNames[i].split("\\|", -1)[5];// + taxaNames[i].charAt(0);
        }
        return newNames;
    }

    private void print(int[][] totalNodes, double[] totalLength, double totalLengthOverTree, int numOfTrait) {
        System.out.println("");
        System.out.print("nodes(i)");
        for (int j = 1; j < numOfTrait; j++) {
            System.out.print("\t"); // if numOfTrait >=2 add more \t
        }
        for (int j = 0; j < totalNodes[0].length; j++) {
            System.out.print("\t" + totalNodes[0][j]);
        }
        System.out.print("\n");

        System.out.print("nodes(i,*)");
        for (int j = 1; j < numOfTrait; j++) {
            System.out.print("\t"); // if numOfTrait >=2 add more \t
        }
        for (int j = 0; j < totalNodes[1].length; j++) {
            System.out.print("\t" + totalNodes[1][j]);
        }
        System.out.print("\n");

        System.out.print("length(i,*)");
        for (int j = 1; j < numOfTrait; j++) {
            System.out.print("\t"); // if numOfTrait >=2 add more \t
        }
        for (int j = 0; j < totalLength.length; j++) {
            System.out.print("\t" + totalLength[j]);
        }
        System.out.print("\n");

        System.out.println("\ntotal branch length = \t" + totalLengthOverTree);
        System.out.println("\n");
    }

    // keep % but overwrite branch length to elevation Ei and |Ei-Ej|
    private void calculateElevationDiff(double[][] geoBioDiversities, int numOfTrait) {
        assert plots.length == elevations.length;
        for (int i = 0; i < geoBioDiversities.length; i++) {
            geoBioDiversities[i][i] = elevations[i/numOfTrait];
            for (int j = i + 1; j < geoBioDiversities.length; j++) {
                geoBioDiversities[i][j] = Math.abs(elevations[i/numOfTrait] - elevations[j/numOfTrait]);
            }
        }
    }

    private void calculatePercentage(double[][] geoBioDiversities) {
        for (int i = 0; i < geoBioDiversities.length; i++) {
            for (int j = 0; j < i; j++) {
                if (geoBioDiversities[j][i] > 0)
                    geoBioDiversities[i][j] = geoBioDiversities[j][i] / (geoBioDiversities[i][i] + geoBioDiversities[j][j] + geoBioDiversities[j][i]);
            }
        }
    }

    private void print(Tree newickTree) {
        System.out.println("Newick tree : " + newickTree.getLeafNodeCount() +
                " leaf nodes, " + newickTree.getInternalNodeCount() + " internal nodes");
        System.out.println("taxa names : " + newickTree.getTaxaNames().length);
        for (String t : newickTree.getTaxaNames()) {
            System.out.print(t + "\t");
        }
        System.out.println("\n" + newickTree + "\n");

    }

    private void print(double[][] geoBioDiversities, String[] header, String tittle) {
        System.out.println(tittle);

        System.out.print("");

        for (String p : header) {
            System.out.print("\t" + p);
        }
        System.out.print("\n");

        assert geoBioDiversities.length == header.length && geoBioDiversities[0].length == header.length;

        for (int i = 0; i < header.length; i++) {
            System.out.print(header[i]);
            for (int j = 0; j < header.length; j++) {
                System.out.print("\t" + ((i > j) ? (geoBioDiversities[i][j] * 100 + "%") : geoBioDiversities[i][j]));
            }

            System.out.print("\n");
        }

    }

    private double traverse(TreeParser newickTree, double[][] geoBioDiversities, String[] traits, int[][] totalNodes, double[] totalLength) {
        final int taxonCount = newickTree.getLeafNodeCount();
        final Node[] nodes = newickTree.getNodesAsArray();
        final String[] taxaNames = newickTree.getTaxaNames();
        double totalLengthOverTree = 0;

        for (int n = 0; n < taxonCount; n++) {
            assert (nodes[n].isLeaf());

            int matrixId = getIndexFrom(taxaNames[nodes[n].getNr()], traits);

            if (matrixId < 0)
                throw new RuntimeException("Cannot find trait " + getLabelFrom(taxaNames[nodes[n].getNr()], traits) + " from " + taxaNames[nodes[n].getNr()]);
            if (matrixId >= traits.length)
                throw new RuntimeException("Index = " + matrixId + " out of boundary for trait " + getLabelFrom(taxaNames[nodes[n].getNr()], traits) + " at node " + n);

            if (verbose) {
                System.out.println(n + ", geoBioDiversities[" + matrixId + "][" + matrixId + "] = " +
                        geoBioDiversities[matrixId][matrixId] + " + " + nodes[n].getLength());
            }

            geoBioDiversities[matrixId][matrixId] += nodes[n].getLength();

            totalNodes[0][matrixId] ++;
            totalNodes[1][matrixId] ++;
            totalLength[matrixId] += nodes[n].getLength();
            totalLengthOverTree += nodes[n].getLength();
        }

        for (int n = taxonCount; n < nodes.length - 1; n++) { // exclude nodes[length - 1] which is root
            if (nodes[n].isRoot()) System.out.println(n + " is Root : " + nodes[n].getLength());

            List<Integer> traitsIndex = getAllTraitsIndexFromInternalNode(nodes[n], taxaNames, traits);
            Collections.sort(traitsIndex);

            if (verbose) System.out.println(n + ", traitsIndex = " + traitsIndex);

            if (traitsIndex.size() < 1) {
                throw new RuntimeException("plotsIndex.size() < 1 when n = " + n);
            } else if (traitsIndex.size() == 1) {
                if (verbose) {
                    System.out.println(n + ", geoBioDiversities[" + traitsIndex.get(0) + "][" + traitsIndex.get(0) + "] = " +
                            geoBioDiversities[traitsIndex.get(0)][traitsIndex.get(0)] + " + " + nodes[n].getLength());
                }
                geoBioDiversities[traitsIndex.get(0)][traitsIndex.get(0)] += nodes[n].getLength();

                totalNodes[0][traitsIndex.get(0)] ++;
                totalNodes[1][traitsIndex.get(0)] ++;
                totalLength[traitsIndex.get(0)] += nodes[n].getLength();

            } else {
                for (int i = 0; i < traitsIndex.size(); i++) {
                    for (int j = i + 1; j < traitsIndex.size(); j++) {
                        if (verbose) {
                            System.out.println(n + ", geoBioDiversities[" + traitsIndex.get(i) + "][" + traitsIndex.get(j) + "] = " +
                                    geoBioDiversities[traitsIndex.get(i)][traitsIndex.get(j)] + " + " + nodes[n].getLength());
                        }

                        geoBioDiversities[traitsIndex.get(i)][traitsIndex.get(j)] += nodes[n].getLength();

                    }
                    totalNodes[1][traitsIndex.get(i)] ++;
                    totalLength[traitsIndex.get(i)] += nodes[n].getLength();
                }
            }

            totalLengthOverTree += nodes[n].getLength();
        }
        return totalLengthOverTree;
    }

    private List<Integer> getAllTraitsIndexFromInternalNode(Node node, final String[] taxaNames, String[] traits) {
        assert (!node.isLeaf());

        List<Integer> traitsIndex = new ArrayList<Integer>();

        List<Node> leafNodes = node.getAllLeafNodes();

        // get all unique leaf nodes index under this internal node
        for (Node n : leafNodes) {
//            System.out.println(n.isLeaf() + ": " + n.getNr());

            int matrixId = getIndexFrom(taxaNames[n.getNr()], traits);

            if (matrixId < 0)
                throw new RuntimeException("Cannot find trait " + getLabelFrom(taxaNames[n.getNr()], traits) + " from " + taxaNames[n.getNr()]);
            if (matrixId >= traits.length)
                throw new RuntimeException("Index = " + matrixId + " out of boundary for trait " + getLabelFrom(taxaNames[n.getNr()], traits) + " at node " + n);

            if (!traitsIndex.contains(matrixId)) traitsIndex.add(matrixId);
        }

        return traitsIndex;
    }

    /**
     *
     * @param taxonName
     * @param traits
     * @return  if traits.length < 3 return type, >= 3 return plot
     */
    private String getLabelFrom(String taxonName, String[] traits) {
        String[] labels = taxonName.split("\\|", -1);
//        System.out.println(plot[0] + ", " + plot[1] + ", " + plot[3]);
        if (labels == null || labels.length < 4) throw new RuntimeException("Incorrect leaf node label " + taxonName);

        if (traits.length < 3) { // 2 collection types at moment
            return labels[3]; // collection type
        } else {
            return labels[1]; // plot
        }
    }

    private int getIndexFrom(String taxonName, String[] traits) {
        String label = getLabelFrom(taxonName, traits);
        for (int i = 0; i < traits.length; i++) {
            if (label.equalsIgnoreCase(traits[i])) return i;
        }
        return -1;
    }


    public static void main(String[] args) {
        GeoBioDiversityMatrix geoBioDiversityMatrix = new GeoBioDiversityMatrix();
        try {
            for (String file : files) {
                file = path + file;
                BufferedReader br = new BufferedReader(new FileReader(file));
                System.out.println("load tree from file : " + file);

                geoBioDiversityMatrix.getGeoBioDiversityMatrix(br.readLine(), false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
