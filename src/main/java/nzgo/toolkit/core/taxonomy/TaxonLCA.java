package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameUtil;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.uparse.io.OTUsFileIO;
import nzgo.toolkit.core.util.ArrayUtil;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Finding the lowest common ancestor (LCA)
 * @author Walter Xie
 */
public class TaxonLCA {
    public static enum Agreement {
        LCA_ONLY ("Always take the lowest common ancestor."),
        LOW_LIN_LCA ("Take the lowest taxon if in the same lineage, otherwise take LCA if contradict.");

        private String type;
        private Agreement(String type) {
            this.type = type;
        }
        @Override
        public String toString() {
            return type;
        }

    }

    public static final String BLAST = "BLAST";
    public static final String MORPHOLOGY = "Barbara Agabiti";

    /**
     * return LCA given Taxa whose elements could be taxid String or Taxon
     *
     * @param taxidSet
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    public static Taxon getTaxonLCA(TaxonSet taxidSet) throws IOException, XMLStreamException {
        Taxon taxonLCA = null;

        for (Object taxid : taxidSet) {
//            Taxon taxon2 = TaxonomyUtil.getTaxonByeFetch(taxid.toString());
            Taxon taxon2 = TaxonomyPool.getAndAddTaxIdByMemory(taxid.toString());

            if (taxon2 == null) {
                MyLogger.error("Error: cannot find taxid " + taxid + " from local taxonomy pool !");
            } else if (taxonLCA == null) {
                taxonLCA = taxon2;
            } else {
                taxonLCA = taxonLCA.getTaxonLCA(taxon2);
            }
        }

        if (taxonLCA == null) {
            MyLogger.error("Error: cannot find LCA taxon " + taxonLCA +
                    " from taxid set: " + taxidSet.elementsToString());
        } else {
            MyLogger.debug("find LCA taxon " + taxonLCA.getScientificName() + ", rank " + taxonLCA.getRank() +
                    ", from taxid set: "+ taxidSet.elementsToString());
        }

        return taxonLCA;
    }

    public static Taxon getAgreedTaxon(Agreement agreement, String taxonName1, String... otherTaxaNames) throws IOException, XMLStreamException {
        MyLogger.info("\n Apply agreement: " + agreement + "\n");

        String taxId = TaxonomyUtil.getTaxIdFromName(taxonName1);
        if (taxId == null)
            return null;
//           throw new RuntimeException("Cannot get NCBI Id for " + taxon1);
        Taxon taxonLCA = TaxonomyPool.getAndAddTaxIdByMemory(taxId);
        if (taxonLCA == null)
            throw new RuntimeException("Cannot get taxid " + taxId + " from local taxonomy pool !");
        // assume taxonName1 is identified by BLAST
        taxonLCA.identifiedBy = BLAST;

        for (String oTaxon : otherTaxaNames) {
            taxId = TaxonomyUtil.getTaxIdFromName(oTaxon);
            if (taxId == null)
                return null;
//               throw new RuntimeException("Cannot get NCBI Id for " + taxon1);

            Taxon taxon2 = TaxonomyPool.getAndAddTaxIdByMemory(taxId);
            // assume taxon2 is morphology
            taxon2.identifiedBy = MORPHOLOGY;
//            taxonLCA = taxonLCA.getTaxonLCA(taxon2);
//            taxonLCA = taxonLCA.getTaxonLowLinLCA(taxon2);
            taxonLCA = getAgreedTaxon(agreement, taxonLCA, taxon2);
        }

        MyLogger.debug("Find LCA taxon " + taxonLCA.getScientificName() + ", rank " + taxonLCA.getRank() +
                ", between "+ taxonName1 + " and " + ArrayUtil.toString(otherTaxaNames));

        return taxonLCA;
    }


    public static Taxon getAgreedTaxon(Agreement agreement, Taxon taxon1, Taxon taxon2) {
        if (agreement == Agreement.LOW_LIN_LCA) {
            return taxon1.getTaxonLowLinLCA(taxon2);
        } else { // default LCA only
            return taxon1.getTaxonLCA(taxon2);
        }
    }


    //Main method
    public static void main(final String[] args) {
        Path workDir = Paths.get(System.getProperty("user.home") + "/Documents/ModelEcoSystem/454/2010-pilot/COITraditional/data/");
        MyLogger.info("\nWorking path = " + workDir);

        Path inFilePath = Module.validateInputFile(workDir, "COI.txt", "input", null);

        String outputFileNameStem = NameUtil.getNameNoExtension(inFilePath.toFile().getName());
        String outputFileExtension = NameUtil.getSuffix(inFilePath.toFile().getName());

        Path outputFilePath = Paths.get(workDir.toString(), outputFileNameStem + "-LCA" + outputFileExtension);

        try {
            BufferedReader reader = OTUsFileIO.getReader(inFilePath, "original file");
            PrintStream out = FileIO.getPrintStream(outputFilePath, "LCA file");

            int[] count = new int[3];

            String line = reader.readLine();
            while (line != null) {
                String[] items = FileIO.lineParser.getSeparator(0).parse(line); // default "\t"

                String blast = items[1];
                if (items.length > 3 && items[3].trim().length() > 0) {
                    blast = items[3];
                }

                String lca = null;
                if (items[2].equalsIgnoreCase("null")) {
                    try {
                        Taxon t = TaxonomyUtil.getTaxonFromName(blast);
                        t.identifiedBy = BLAST;
                        lca = t.getScientificName() + "\t" + t.getTaxId() + "\t" + t.identifiedBy +
                                "\t" + t.getLineageString();
                        count[0]++;
                    } catch (XMLStreamException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Taxon t = getAgreedTaxon(Agreement.LOW_LIN_LCA, blast, items[2]);
                        lca = t == null ? "\t" : t.getScientificName() + "\t" + t.getTaxId() + "\t" + t.identifiedBy +
                                "\t" + t.getLineageString();
                        if (t.identifiedBy.equalsIgnoreCase(BLAST)) {
                            count[0]++;
                        } else if (t.identifiedBy.equalsIgnoreCase(MORPHOLOGY)) {
                            count[1]++;
                        } else {
                            count[2]++;
                        }
                    } catch (XMLStreamException e) {
                        e.printStackTrace();
                    }
                }

                out.println(items[0] + "\t" + blast + "\t" + items[2] + "\t" + lca);
                line = reader.readLine();
            }

            reader.close();
            out.flush();
            out.close();

            MyLogger.info("\nTaxonomy identified by BLAST = " + count[0] + ", by " + MORPHOLOGY + " = " + count[1] +
                    ", (contradicted) take LCA = " + count[2]);

        } catch (IOException e) {
            e.printStackTrace();
        }



//        TaxonSet taxidSet = new TaxonSet(Arrays.asList("104782", "104786", "104788", "317506", "563909", "563911"));
//
//        try {
//            Taxon taxonLCA = TaxonLCA.getTaxonLCA(taxidSet);
//
//            MyLogger.info("\nTaxonomy LCA is " + taxonLCA.getScientificName() + ", taxid = " + taxonLCA.getTaxId() +
//                    ", rank = " + taxonLCA.getRank());
//
//        } catch (IOException | XMLStreamException e) {
//            e.printStackTrace();
//        }


    }

}
