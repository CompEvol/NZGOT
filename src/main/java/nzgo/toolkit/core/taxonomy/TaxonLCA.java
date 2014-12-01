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

    public static Taxon getTaxonLCA(String taxon1, String... otherTaxa) {
        Taxon taxonLCA = null;
        try {
            String taxId = TaxonomyUtil.getTaxIdFromName(taxon1);
            if (taxId == null)
                return null;
//                throw new RuntimeException("Cannot get NCBI Id for " + taxon1);
            taxonLCA = TaxonomyPool.getAndAddTaxIdByMemory(taxId);
            if (taxonLCA == null)
                throw new RuntimeException("Cannot get taxid " + taxId + " from local taxonomy pool !");

        } catch (XMLStreamException | IOException e) {
            e.printStackTrace();
        }

        for (String oTaxon : otherTaxa) {
            Taxon taxon2 = null;
            try {
                String taxId = TaxonomyUtil.getTaxIdFromName(oTaxon);
                if (taxId == null)
                    return null;
//                    throw new RuntimeException("Cannot get NCBI Id for " + taxon1);

                taxon2 = TaxonomyPool.getAndAddTaxIdByMemory(taxId);

            } catch (XMLStreamException | IOException e) {
                e.printStackTrace();
            }

            taxonLCA = taxonLCA.getTaxonLCA(taxon2);
        }

        MyLogger.debug("Find LCA taxon " + taxonLCA.getScientificName() + ", rank " + taxonLCA.getRank() +
                ", between "+ taxon1 + " and " + ArrayUtil.toString(otherTaxa));

        return taxonLCA;
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

            String line = reader.readLine();
            while (line != null) {
                String[] items = FileIO.lineParser.getSeparator(0).parse(line); // default "\t"

                String blast = items[1];
                if (items.length > 3 && items[3].trim().length() > 0) {
                    blast = items[3];
                }

                String lca;
                if (items[2].equalsIgnoreCase("null")) {
                    lca = blast;
                } else {
                    Taxon t = getTaxonLCA(blast, items[2]);
                    lca = t == null ? "" : t.getScientificName();
                }

                out.println(items[0] + "\t" + blast + "\t" + items[2] + "\t" + lca);
                line = reader.readLine();
            }

            reader.close();
            out.flush();
            out.close();

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
