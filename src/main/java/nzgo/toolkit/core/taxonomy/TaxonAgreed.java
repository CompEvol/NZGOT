package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.logger.MyLogger;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Arrays;

/**
 * Taxon Agreed given a set of taxon
 * @author Walter Xie
 */
public class TaxonAgreed {

    /**
     * return agreed taxon given Taxa whose elements could be String or Taxon
     * String is faster than Taxon
     * @param taxidSet
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    public static Taxon getTaxonAgreed(Taxa taxidSet) throws IOException, XMLStreamException {
        Taxon taxonAgreed = null;

        for (Object taxid : taxidSet) {
//            Taxon taxon2 = EFetchStAXParser.getTaxonById(taxid.toString());
            Taxon taxon2 = TaxaUtil.getTaxonById(taxid.toString());

            if (taxon2 == null) {
                MyLogger.error("Error: cannot find taxid " + taxid + " from EFetch !");
            } else if (taxonAgreed == null) {
                taxonAgreed = taxon2;
            } else {
                taxonAgreed = taxonAgreed.getTaxonAgreed(taxon2);
            }
        }

        if (taxonAgreed == null) {
            MyLogger.error("Error: cannot find agreed taxon " + taxonAgreed +
                    " from taxid set: " + taxidSet.elementsToString());
        } else {
            MyLogger.debug("find agreed taxon " + taxonAgreed + ", rank " + taxonAgreed.getRank() +
                    ", from taxid set: "+ taxidSet.elementsToString());
        }

        return taxonAgreed;
    }

    public static Taxon getTaxonAgreed(Taxon taxon1, Taxon... taxonMore) {
        Taxon taxonAgreed = taxon1;

        if (taxonMore != null) {
            for (int i = 0; i < taxonMore.length; i++) {
                if (taxonMore[i] != null) {
                    taxonAgreed = taxonAgreed.getTaxonAgreed(taxonMore[i]);
                }
            }
        }

        return taxonAgreed;
    }

    //Main method
    public static void main(final String[] args) {

        Taxa taxidSet = new Taxa(Arrays.asList("104782", "104786", "104788", "317506", "563909", "563911"));

        try {
            Taxon taxonAgreed = TaxonAgreed.getTaxonAgreed(taxidSet);

            MyLogger.info("\nTaxonomy Agreed is " + taxonAgreed + ", taxid = " + taxonAgreed.getTaxId() +
                    ", rank = " + taxonAgreed.getRank());

        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }


    }

}
