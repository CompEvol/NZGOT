package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.blast.*;
import nzgo.toolkit.core.blast.parser.BlastStAXParser;
import nzgo.toolkit.core.community.Community;
import nzgo.toolkit.core.community.OTU;
import nzgo.toolkit.core.community.OTUs;
import nzgo.toolkit.core.io.CommunityFileIO;
import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.io.GiTaxidIO;
import nzgo.toolkit.core.io.TaxonomyFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.SiteNameParser;
import nzgo.toolkit.core.taxonomy.parser.EFetchStAXParser;
import nzgo.toolkit.core.util.XMLUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedReader;
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
public class TaxonomyUtil {

    public static final String UNCLASSIFIED = "unclassified";
    public static SiteNameParser siteNameParser = new SiteNameParser();

    // gi|261497976|gb|GU013865.1|
    public static final int GI_INDEX = 1;

    // use xml parser code directly for large search, to make it faster.
    public static SortedMap<String, Taxon> getOTUTaxaMapByBLAST(File xmlBLASTOutputFile, File gi_taxid_raf_nucl) throws JAXBException, IOException, XMLStreamException {

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
                    iteration.reduceToTopHits(BlastStAXParser.TOP_HITS_LIMITS); // keep top hits by limit

                    fillOTUTaxaMap(iteration, otuTaxaMap, giTaxidIO);
                }
            }
        }

        return otuTaxaMap;
    }

    public static void fillOTUTaxaMap(Iteration iteration, SortedMap<String, Taxon> otuTaxaMap, GiTaxidIO giTaxidIO) throws JAXBException, IOException, XMLStreamException {
        long lStartTime = System.currentTimeMillis();

        // a set of taxid
        TaxonSet taxidSet = new TaxonSet();

        String otuName = iteration.getIterationQueryDef();

        MyLogger.debug("\niteration: " + otuName);

        IterationHits hits = iteration.getIterationHits();
        for(Hit hit : hits.getHit()) {
            String hitId = hit.getHitId();
            String[] fields = siteNameParser.getSeparator(0).parse(hitId);
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
    @Deprecated
    public static void getOTUTaxaMapByBLAST(File xmlBLASTOutputFile, File gi_taxid_raf_nucl, OTUs otus) throws JAXBException, IOException, XMLStreamException {

        SortedMap<String, Taxon> otuTaxaMap = getOTUTaxaMapByBLAST(xmlBLASTOutputFile, gi_taxid_raf_nucl);

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
     *
     * @param otuTaxidMappingFile   1st column otu, 2nd taxid
     * @return
     * @throws JAXBException
     * @throws IOException
     * @throws XMLStreamException
     */
    public static SortedMap<String, Taxon> getOTUTaxaMapByFile(File otuTaxidMappingFile) throws JAXBException, IOException, XMLStreamException {

        SortedMap<String, Taxon> otuTaxaMap = new TreeMap<>();

        BufferedReader reader = FileIO.getReader(otuTaxidMappingFile, "taxonomic mapping");

        String line = reader.readLine();
        while (line != null) {
            if (FileIO.hasContent(line)) { // not comments or empty
                String[] items = FileIO.lineParser.getSeparator(0).parse(line);
                if (items.length < 2)
                    throw new IllegalArgumentException("Invalid file format for taxonomic mapping, line : " + line);
                if (otuTaxaMap.containsKey(items[0]))
                    throw new IllegalArgumentException("Find duplicate name for OTU : " + items[0]);

                Taxon taxon = TaxonomyPool.getAndAddTaxIdByMemory(items[1]);
                if (taxon == null) {
                    MyLogger.error("Cannot find taxon from taxid " + items[1]);
                } else {
                    otuTaxaMap.put(items[0], taxon);
                }
            }

            line = reader.readLine();
        }
        reader.close();

        if (otuTaxaMap.size() < 1)
            throw new IllegalArgumentException("It needs at least one separator !");

        return otuTaxaMap;
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

    // customized taxId, use taxId.compareTo("0") > 0 to filter out
    public static Taxon getNoRankOn(Rank rank) {
        return new Taxon("no " + rank.toString(), "-1");
    }

    public static Taxon getUnclassified() {
        return new Taxon(UNCLASSIFIED, "12908", "1");
    }

    public static Taxon getCellularOrganisms() {
        return new Taxon("cellular organisms", "131567", "1");
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
            File otuMappingFile = new File(workPath + "map.uc");
            SiteNameParser siteNameParser = new SiteNameParser();
            Community community = new Community(siteNameParser, otuMappingFile);

            File otuTaxidMappingFile = new File(workPath + "otus1-Arthopoda.txt");
            SortedMap<String, Taxon> otuTaxaMap = getOTUTaxaMapByFile(otuTaxidMappingFile);
            community.setTaxonomy(otuTaxaMap);

            Community communityArthopoda = community.getClassifiedCommunity();
            Path outCMFilePath = Paths.get(workPath, CommunityFileIO.COMMUNITY_MATRIX + "-Arthopoda.csv");
            CommunityFileIO.writeCommunityMatrix(outCMFilePath, communityArthopoda);

            Path outFilePath = Paths.get(workPath, "otus_taxa-Arthopoda.tsv");
            TaxonomyFileIO.writeElementTaxonomyMap(outFilePath, otuTaxaMap, Rank.CLASS, Rank.ORDER);

//            File xmlBLASTOutputFile = new File(workPath + "blast" + File.separator + "otus1.xml");
//            File gi_taxid_raf_nucl = new File("/Users/dxie004/Documents/ModelEcoSystem/454/BLAST/gi_taxid_nucl.dmp");
//
//            SortedMap<String, Taxon> otuTaxaMap = getOTUTaxaMapByBLAST(xmlBLASTOutputFile, gi_taxid_raf_nucl);
//            Path outFilePath = Paths.get(workPath, "otus_taxa.tsv");
//            TaxonomyFileIO.writeElementTaxonomyMap(outFilePath, otuTaxaMap, Rank.PHYLUM, Rank.ORDER);

            TaxonomicAssignment taxonomicAssignment = new TaxonomicAssignment(communityArthopoda, Rank.CLASS, Rank.ORDER);

            taxonomicAssignment.writeTaxonomyAssignment(workPath);

//            TaxonSet<Taxon> taxonomySet = communityArthopoda.getTaxonomy();
//
//            TaxonomyFileIO.writeTaxonomyAssignment(workPath, taxonomySet, Rank.CLASS, Rank.ORDER);

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
