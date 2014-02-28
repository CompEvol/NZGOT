package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.taxonomy.parser.EFetchStAXParser;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * Taxon Agreed given a set of taxon
 * @author Walter Xie
 */
public class TaxonAgreed {

    public static Taxon getTaxonAgreed(Taxa taxidSet) throws IOException, XMLStreamException {
        Taxon taxonAgreed = null;

        for (Object taxid : taxidSet) {
            Taxon taxon1 = EFetchStAXParser.getTaxonById(taxid.toString());

            if (taxon1 == null) {
                MyLogger.error("Error: cannot find taxid " + taxid + " from EFetch !");
            } else if (taxonAgreed == null) {
                taxonAgreed = taxon1;
            } else {
                taxonAgreed = taxonAgreed.getTaxonAgreed(taxon1);
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
