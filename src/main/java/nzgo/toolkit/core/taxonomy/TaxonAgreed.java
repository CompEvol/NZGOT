package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.logger.MyLogger;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * Taxon Agreed given a set of taxon
 * @author Walter Xie
 */
public class TaxonAgreed {
    //TODO not working?
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
                    " from taxid set: "+ taxidSet.elementsToString());
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

}
