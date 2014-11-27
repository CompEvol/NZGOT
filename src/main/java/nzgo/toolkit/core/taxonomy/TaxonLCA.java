package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.util.ArrayUtil;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Arrays;

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
                throw new RuntimeException("Cannot get NCBI Id for " + taxon1);
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
                    throw new RuntimeException("Cannot get NCBI Id for " + taxon1);

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

        TaxonSet taxidSet = new TaxonSet(Arrays.asList("104782", "104786", "104788", "317506", "563909", "563911"));

        try {
            Taxon taxonLCA = TaxonLCA.getTaxonLCA(taxidSet);

            MyLogger.info("\nTaxonomy LCA is " + taxonLCA.getScientificName() + ", taxid = " + taxonLCA.getTaxId() +
                    ", rank = " + taxonLCA.getRank());

        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }


    }

}
