package nzgo.toolkit.core.taxonomy.parser;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.taxonomy.NCBIeUtils;
import nzgo.toolkit.core.taxonomy.Rank;
import nzgo.toolkit.core.taxonomy.Taxon;
import nzgo.toolkit.core.taxonomy.TaxonomyPool;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * parse eFetch XML result
 * @author Walter Xie
 */
public class EFetchStAXParser {

    /**
     * mostly 1 Taxon, there may be multi-result because of duplicate names but different id
     * @param scientificName
     * @return
     * @throws XMLStreamException
     * @throws IOException
     */
    public static List<Taxon> getTaxonByName(String scientificName) throws XMLStreamException, IOException {
        List<Taxon> taxonList = new ArrayList<>();
        List<String> idList = ESearchStAXParser.getIdList(scientificName);

        MyLogger.debug("\neFetch " + scientificName + " get : " + idList);

        for (String taxId : idList) {
            Taxon taxon = TaxonomyPool.getAndAddTaxIdByMemory(taxId);
            if (taxon != null) taxonList.add(taxon);
        }

        return taxonList;
    }

    /**
     * return Taxon given eFetch XMLStreamReader
     * @param xmlStreamReader
     * @return
     * @throws XMLStreamException
     */
    public static Taxon getTaxon(XMLStreamReader xmlStreamReader) throws XMLStreamException {
        try {
            while(xmlStreamReader.hasNext()){
                xmlStreamReader.next();

                if(xmlStreamReader.getEventType() == XMLStreamConstants.START_ELEMENT){
                    String elementName = xmlStreamReader.getLocalName();
                    if (NCBIeUtils.isTaxon(elementName)) {
                        return parseTaxon(xmlStreamReader);
                    }
                }
            }
        } finally {
            xmlStreamReader.close();
        }

        return null;
    }

    /**
     * only return queried Taxon, but the lineage is hidden from recursive parentTaxon
     * it causes duplication for Tree of Life
     * @param xmlStreamReader
     * @return
     * @throws XMLStreamException
     */
    public static Taxon parseTaxon(XMLStreamReader xmlStreamReader) throws XMLStreamException {
        Taxon taxon = new Taxon();

        while(xmlStreamReader.hasNext()){
            xmlStreamReader.next();

            if(xmlStreamReader.getEventType() == XMLStreamReader.END_ELEMENT){
                String elementName = xmlStreamReader.getLocalName();
                if(NCBIeUtils.isTaxon(elementName))
                    return taxon;

            } else if(xmlStreamReader.getEventType() == XMLStreamReader.START_ELEMENT){
                String elementName = xmlStreamReader.getLocalName();
                if (NCBIeUtils.isTaxId(elementName)) {
                    String taxId = xmlStreamReader.getElementText();
//                    // return "cellular organisms"
//                    if (TaxonomyUtil.getCellularOrganisms().getTaxId().contentEquals(taxId))
//                        // stop recursive here
//                        return TaxonomyUtil.getCellularOrganisms();
                    taxon.setTaxId(taxId); // required
                } else if (NCBIeUtils.isScientificName(elementName)) {
                    taxon.setScientificName(xmlStreamReader.getElementText());
                } else if (NCBIeUtils.isParentTaxId(elementName)) {
                    taxon.setParentTaxId(xmlStreamReader.getElementText());
                } else if (NCBIeUtils.isRank(elementName)) {
                    Rank rank = Rank.fromString(xmlStreamReader.getElementText());
                    taxon.setRank(rank); // if text not included in Rank, then rank == null
                } else if (NCBIeUtils.isLineageEx(elementName)) {
                    return taxon; // not parse lineage, use List<Taxon> getLineage () now

                    // start from "cellular organisms" in xml
//                    setParentFromLineage(xmlStreamReader, taxon);
//
//                    Taxon parentTaxon = taxon.getParentTaxon();
//                    if (parentTaxId == null || parentTaxon == null || !parentTaxId.contentEquals(parentTaxon.getTaxId()))
//                        throw new IllegalArgumentException(taxon + "'s parent TaxId not match : " + parentTaxId +
//                                " != " + (parentTaxon == null?null:parentTaxon.getTaxId()));
                }
            }
        }
        return taxon;
    }

    /**
     * set parentTaxon recursively from lineage
     * @param xmlStreamReader
     * @param taxon
     * @throws XMLStreamException
     */
    @Deprecated
    protected static void setParentFromLineage(XMLStreamReader xmlStreamReader, Taxon taxon) throws XMLStreamException {
        List<Taxon> lineage = new ArrayList<>();
        while(xmlStreamReader.hasNext()){
            xmlStreamReader.next();

            if(xmlStreamReader.getEventType() == XMLStreamReader.END_ELEMENT){
                if(NCBIeUtils.isLineageEx(xmlStreamReader.getLocalName()))
                    break;

            } else if(xmlStreamReader.getEventType() == XMLStreamReader.START_ELEMENT){
                if(NCBIeUtils.isTaxon(xmlStreamReader.getLocalName())) {
                    Taxon t = parseTaxon(xmlStreamReader);
                    lineage.add(t);
                }
            }
        }

        Taxon t = taxon;
        for(int i = lineage.size() - 1; i >= 0; i--){
//            t.setParentTaxon(lineage.get(i));
            t = lineage.get(i);
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        MyLogger.info("\nWorking path = " + workPath);

        try {

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
