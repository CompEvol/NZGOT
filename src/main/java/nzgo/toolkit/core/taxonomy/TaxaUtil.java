package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.blast.*;
import nzgo.toolkit.core.blast.parser.BlastStAXParser;
import nzgo.toolkit.core.community.OTU;
import nzgo.toolkit.core.community.OTUs;
import nzgo.toolkit.core.io.GiTaxidIO;
import nzgo.toolkit.core.io.TaxonomyFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.SampleNameParser;
import nzgo.toolkit.core.taxonomy.parser.EFetchStAXParser;
import nzgo.toolkit.core.util.XMLUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Taxa Util
 * @author Walter Xie
 */
public class TaxaUtil {

    public static SampleNameParser sampleNameParser = new SampleNameParser();

    // gi|261497976|gb|GU013865.1|
    public static final int GI_INDEX = 1;

    // use xml parser code directly for large search, to make it faster.
    public static SortedMap<String, Taxon> mapTaxaToOTUsByBLAST(File xmlBLASTOutputFile, File gi_taxid_raf_nucl) throws JAXBException, IOException, XMLStreamException {

        SortedMap<String, Taxon> otuTaxaMap = new TreeMap<>();

        GiTaxidIO giTaxidIO = new GiTaxidIO(gi_taxid_raf_nucl);

        MyLogger.info("\nParsing BLAST xml output file : " + xmlBLASTOutputFile);

        XMLStreamReader xmlStreamReader = XMLUtil.parse(xmlBLASTOutputFile);

        JAXBContext jc = JAXBContext.newInstance(Iteration.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();

        while(xmlStreamReader.hasNext()){
            xmlStreamReader.next();

            if(xmlStreamReader.getEventType() == XMLStreamConstants.START_ELEMENT){
                String elementName = xmlStreamReader.getLocalName();
                if(Iteration.TAG.equals(elementName)){
                    Iteration iteration = (Iteration) unmarshaller.unmarshal(xmlStreamReader);
                    iteration.reduceToTopHits(BlastStAXParser.TOP_HITS_LIMITS);

                    fillOTUTaxaMap(iteration, otuTaxaMap, giTaxidIO);
                }
            }
        }

        return otuTaxaMap;
    }

    public static void fillOTUTaxaMap(Iteration iteration, SortedMap<String, Taxon> otuTaxaMap, GiTaxidIO giTaxidIO) throws JAXBException, IOException, XMLStreamException {
        long lStartTime = System.currentTimeMillis();

        // a set of taxid
        Taxa taxidSet = new Taxa();

        String otuName = iteration.getIterationQueryDef();

        MyLogger.debug("\niteration: " + otuName);

        IterationHits hits = iteration.getIterationHits();
        for(Hit hit : hits.getHit()) {
            String hitId = hit.getHitId();
            String[] fields = sampleNameParser.getSeparator(0).parse(hitId);
            String gi = fields[GI_INDEX];

            MyLogger.debug("hit id: " + hitId + ", get gi = " + gi);

            MyLogger.debug("hit length: " + hit.getHitLen());

            HitHsps hitHsps = hit.getHitHsps();
            for(Hsp hsp : hitHsps.getHsp()) {
                MyLogger.debug("hsp num: " + hsp.getHspNum());
                MyLogger.debug("hsp bit score: " + hsp.getHspBitScore());
                MyLogger.debug("hsp e-value: " + hsp.getHspEvalue());
                MyLogger.debug("identity/len: " + hsp.getHspIdentity() + " / " + hsp.getHspAlignLen() + " = " +
                        Double.parseDouble(hsp.getHspIdentity()) / Double.parseDouble(hsp.getHspAlignLen()));
            }

            String taxid = giTaxidIO.mapGIToTaxid(gi);

            if (taxid != null) taxidSet.add(taxid);
        }


        if (otuTaxaMap.containsKey(otuName)) {
            throw new IllegalArgumentException("BLAST result contains duplicate OTU name : " + otuName);
        } else if (taxidSet.size() < 1) {
            MyLogger.warn("BLAST has no result for OTU : " + otuName);
        } else {
            Taxon taxonLCA = TaxonLCA.getTaxonLCA(taxidSet);

            otuTaxaMap.put(otuName, taxonLCA);
        }

        long lEndTime = System.currentTimeMillis();

        MyLogger.debug("Elapsed milliseconds: " + (lEndTime - lStartTime) + "\n");
    }

    // slow?
    public static void setTaxaToOTUsByBLAST(File xmlBLASTOutputFile, File gi_taxid_raf_nucl, OTUs otus) throws JAXBException, IOException, XMLStreamException {

        SortedMap<String, Taxon> otuTaxaMap = mapTaxaToOTUsByBLAST(xmlBLASTOutputFile, gi_taxid_raf_nucl);

        for (Map.Entry<String, Taxon> entry : otuTaxaMap.entrySet()) {

            OTU otu = (OTU) otus.getOTUByName(entry.getKey());
            Taxon taxon = entry.getValue();

            if (otu == null) {
                MyLogger.error("Error: cannot find otu " + entry.getKey() + " from OTUs " + otus.getName());
            } else if (taxon == null) {
                MyLogger.error("Error: cannot find taxonomy " + taxon + " from OTU " + otu.getName());
            } else {
                otu.setTaxonLCA(taxon);
            }
        }

    }

    /**
     * return Taxon given NCBI taxId
     * @param taxId
     * @return
     * @throws javax.xml.stream.XMLStreamException
     * @throws java.io.IOException
     */
    public static Taxon getTaxonByeFetch(String taxId) throws XMLStreamException, IOException {
        MyLogger.debug("eFetch " + taxId + " ...");

        URL url = NCBIeUtils.eFetch(taxId);
        XMLStreamReader xmlStreamReader = XMLUtil.parse(url);

        return EFetchStAXParser.getTaxon(xmlStreamReader);
    }

    public static Taxon getCellularOrganisms() {
        return new Taxon("cellular organisms", "131567"); // no parentTaxon
    }

    public static Taxon getRoot() {
        return new Taxon("root", "1"); // no parentTaxon
    }

    public static boolean isRoot(String taxId) {
        return "1".contentEquals(taxId);
    }

    //Main method
    public static void main(final String[] args) {
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        MyLogger.info("\nWorking path = " + workPath);

        try {
//            File otusFile = new File(workPath + "otus1.fasta");
/*            File otuMappingFile = new File(workPath + "map.uc");
            Community community = new Community(otuMappingFile);

//            setTaxaToOTUsByBLAST(xmlBLASTOutputFile, gi_taxid_raf_nucl, community);

            String outFileAndPath = workPath + File.separator + "community_matrix.csv";
            CommunityFileIO.writeCommunityMatrix(outFileAndPath, community);  */

            File xmlBLASTOutputFile = new File(workPath + "blast" + File.separator + "otus1.xml");
            File gi_taxid_raf_nucl = new File("/Users/dxie004/Documents/ModelEcoSystem/454/BLAST/gi_taxid_nucl.dmp");

            SortedMap<String, Taxon> otuTaxaMap = mapTaxaToOTUsByBLAST(xmlBLASTOutputFile, gi_taxid_raf_nucl);
            Path outFilePath = Paths.get(workPath, "otus_taxa.tsv");
            TaxonomyFileIO.writeElementTaxonomyMap(outFilePath, otuTaxaMap, Rank.PHYLUM, Rank.ORDER);

//            Path inFilePath = Paths.get(workPath, "otus_taxa_id.tsv");
//            SortedMap<String, Taxon> otuTaxaMap = TaxonomyFileIO.importElementTaxonomyMap(inFilePath);
//            Path outFilePath = Paths.get(workPath, "otus_taxa.tsv");
//            TaxonomyFileIO.writeElementTaxonomyMap(outFilePath, otuTaxaMap, Rank.PHYLUM, Rank.ORDER);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
