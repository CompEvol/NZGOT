package nzgo.toolkit.core.taxonomy.parser;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.taxonomy.*;
import nzgo.toolkit.core.util.XMLUtil;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.net.URL;
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
            Taxon taxon = TaxaUtil.getTaxonById(taxId);
            if (taxon != null) taxonList.add(taxon);
        }

        return taxonList;
    }

    /**
     * replaced by TaxaUtil.getTaxonById(taxId)
     * return Taxon given NCBI taxId
     * @param taxId
     * @return
     * @throws XMLStreamException
     * @throws IOException
     */
    @Deprecated
    public static Taxon getTaxonById(String taxId) throws XMLStreamException, IOException {
        URL url = NCBIeUtils.eFetch(taxId);
        XMLStreamReader xmlStreamReader = XMLUtil.parse(url);

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

    public static Taxon parseTaxon(XMLStreamReader xmlStreamReader) throws XMLStreamException {
        Taxon taxon = new Taxon();
        while(xmlStreamReader.hasNext()){
            xmlStreamReader.next();

            if(xmlStreamReader.getEventType() == XMLStreamReader.END_ELEMENT){
                String elementName = xmlStreamReader.getLocalName();
                if(NCBIeUtils.isTaxon(elementName)) return taxon;

            } else if(xmlStreamReader.getEventType() == XMLStreamReader.START_ELEMENT){
                String elementName = xmlStreamReader.getLocalName();
                if (NCBIeUtils.isTaxId(elementName)) {
                    taxon.setTaxId(xmlStreamReader.getElementText()); // required
                } else if (NCBIeUtils.isScientificName(elementName)) {
                    taxon.setScientificName(xmlStreamReader.getElementText());
                } else if (NCBIeUtils.isParentTaxId(elementName)) {
                    taxon.setParentTaxId(xmlStreamReader.getElementText()); //TODO
                } else if (NCBIeUtils.isRank(elementName)) {
                    Rank rank = Rank.fromString(xmlStreamReader.getElementText());
                    taxon.setRank(rank); // if text not included in Rank, then rank == null
                } else if (NCBIeUtils.isLineageEx(elementName)) {
                    List<Taxon> lineage = parseLineage(xmlStreamReader); //TODO
                    taxon.lineage.addAll(lineage);
                }
            }
        }
        return taxon;
    }

    public static Taxa parseTaxonAndLineage(XMLStreamReader xmlStreamReader) throws XMLStreamException {
        Taxa taxa = new Taxa();
        while(xmlStreamReader.hasNext()){
            xmlStreamReader.next();

//            if(xmlStreamReader.getEventType() == XMLStreamReader.END_ELEMENT){
//                String elementName = xmlStreamReader.getLocalName();
//                if(NCBIeUtils.isTaxon(elementName)) return taxon;
//
//            } else if(xmlStreamReader.getEventType() == XMLStreamReader.START_ELEMENT){
//                String elementName = xmlStreamReader.getLocalName();
//                if (NCBIeUtils.isTaxId(elementName)) {
//                    taxon.setTaxId(xmlStreamReader.getElementText()); // required
//                } else if (NCBIeUtils.isScientificName(elementName)) {
//                    taxon.setScientificName(xmlStreamReader.getElementText());
//                } else if (NCBIeUtils.isParentTaxId(elementName)) {
//                    taxon.setParentTaxId(xmlStreamReader.getElementText()); //TODO
//                } else if (NCBIeUtils.isRank(elementName)) {
//                    Rank rank = Rank.fromString(xmlStreamReader.getElementText());
//                    taxon.setRank(rank); // if text not included in Rank, then rank == null
//                } else if (NCBIeUtils.isLineageEx(elementName)) {
//                    List<Taxon> lineage = parseLineage(xmlStreamReader); //TODO
//                    taxon.lineage.addAll(lineage);
//                }
//            }
        }
        return taxa;
    }

    // recursive
    protected static void addTaxonFromLineage(XMLStreamReader xmlStreamReader, Taxa taxa) throws XMLStreamException {
        while(xmlStreamReader.hasNext()){
            xmlStreamReader.next();

//            if(xmlStreamReader.getEventType() == XMLStreamReader.END_ELEMENT){
//                if(NCBIeUtils.isLineageEx(xmlStreamReader.getLocalName())) return lineage;
//
//            } else if(xmlStreamReader.getEventType() == XMLStreamReader.START_ELEMENT){
//                if(NCBIeUtils.isTaxon(xmlStreamReader.getLocalName())) {
//                    Taxon taxon = parseTaxon(xmlStreamReader);
//                    taxa.add(taxon);
//                }
//            }
        }
    }

    protected static List<Taxon> parseLineage(XMLStreamReader xmlStreamReader) throws XMLStreamException {
        List<Taxon> lineage = new ArrayList<>();
        while(xmlStreamReader.hasNext()){
            xmlStreamReader.next();

            if(xmlStreamReader.getEventType() == XMLStreamReader.END_ELEMENT){
                if(NCBIeUtils.isLineageEx(xmlStreamReader.getLocalName())) return lineage;

            } else if(xmlStreamReader.getEventType() == XMLStreamReader.START_ELEMENT){
                if(NCBIeUtils.isTaxon(xmlStreamReader.getLocalName())) {
                    Taxon taxon = parseTaxon(xmlStreamReader);
                    lineage.add(taxon);
                }
            }
        }
        return lineage;
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
