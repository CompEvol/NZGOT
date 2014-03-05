package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.logger.MyLogger;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Arrays;

/**
 * Finding the lowest common ancestor (LCA)
 * @author Walter Xie
 */
public class TaxonLCA {

    /**
     * return LCA given Taxa whose elements could be String or Taxon
     * String is faster than Taxon
     * @param taxidSet
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    public static Taxon getTaxonLCA(Taxa taxidSet) throws IOException, XMLStreamException {
        Taxon taxonLCA = null;

        for (Object taxid : taxidSet) {
//            Taxon taxon2 = TaxaUtil.getTaxonByeFetch(taxid.toString());
            Taxon taxon2 = TaxonomyPool.getAndAddTaxIdByMemory(taxid.toString());

            if (taxon2 == null) {
                MyLogger.error("Error: cannot find taxid " + taxid + " getAndAddTaxIdByMemory !");
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

    //Main method
    public static void main(final String[] args) {

        Taxa taxidSet = new Taxa(Arrays.asList("104782", "104786", "104788", "317506", "563909", "563911"));

        try {
            Taxon taxonLCA = TaxonLCA.getTaxonLCA(taxidSet);

            MyLogger.info("\nTaxonomy LCA is " + taxonLCA.getScientificName() + ", taxid = " + taxonLCA.getTaxId() +
                    ", rank = " + taxonLCA.getRank());

        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }


    }

}
