package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.taxonomy.parser.EFetchStAXParser;
import nzgo.toolkit.core.util.SystemUtil;
import nzgo.toolkit.core.util.XMLUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Taxonomy Pool
 * @author Walter Xie
 */
public class TaxonomyPool {

    public static final String DB_DIR = "TaxID";
    public static final String taxonLDBDir = SystemUtil.APP_DIR + File.separator + DB_DIR;
    // the capacity of each pool, such as 1-10,000, 10,001-20,000, ...
    public static final int POOL_INDEX = 10000; // 10,000

    public static TaxonSet<Taxon> taxonPool = new TaxonSet<>();

    /**
     * return Taxon given a taxid
     * if not exist in taxonPool, then search local file system, if not again, then eFetch,
     * and then add in taxonPool
     * @param taxId
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    public static Taxon getAndAddTaxIdByMemory(String taxId) throws IOException, XMLStreamException {
//        MyLogger.debug("Search " + taxId + " in local taxonomy pool ...");
        if (taxId.compareTo("0") > 0 && !taxonPool.containsTaxon(taxId)) {
            // if not exist in taxonPool, then eFetch and add in taxonPool
//            Taxon taxon = TaxonomyUtil.getTaxonByeFetch(taxId);
            Taxon taxon = getAndAddTaxIdByFileSystem(taxId);
            taxonPool.addElement(taxon);
            return taxon;
        }
        return taxonPool.getTaxon(taxId);
    }

    /**
     * return eFetch result xml given a taxid
     * if not exist taxid.xml in taxonLDBDir, then eFetch and create xml in taxonLDBDir
     *
     * @param taxId
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    public static Taxon getAndAddTaxIdByFileSystem(String taxId) throws IOException, XMLStreamException {
        String taxIdXML = taxId + NameSpace.SUFFIX_TAX_ID_FILE;
        Path taxIdXMLDir = getTaxonLDBDirByTaxId(taxId);

        // better not > 500 files
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(taxIdXMLDir)) {
            for(Path xmlFile : stream) {
                if (Files.exists(xmlFile)) {
                    String fileName = xmlFile.getFileName().toString();
                    if (taxIdXML.contentEquals(fileName)) {
                        XMLStreamReader xmlStreamReader = XMLUtil.parse(xmlFile.toFile());
                        return EFetchStAXParser.getTaxon(xmlStreamReader);
                    }
                }
            }
        }

        // if not exist taxid.xml in taxonLDBDir, then eFetch and create xml in taxonLDBDir
        URL url = NCBIeUtils.eFetch(taxId);
        InputStream input = url.openStream();

        Path newXML = Paths.get(taxIdXMLDir.toString(), taxIdXML);
        BufferedWriter writer = FileIO.getWriter(newXML, "Create new taxId XML");

        int byteRead;
        // Read a raw byte, returns an int of 0 to 255.
        while ((byteRead = input.read()) != -1) {
            writer.write(byteRead);
        }

        input.close();
        writer.close();

        XMLStreamReader xmlStreamReader = XMLUtil.parse(url);
        return EFetchStAXParser.getTaxon(xmlStreamReader);
    }

    protected static Path getTaxonLDBDirByTaxId(String taxId) {
        int taxIdInt = Integer.parseInt(taxId);
        int dirInt = taxIdInt / POOL_INDEX; // 10,000
        String dir = Integer.toString(dirInt);
        return SystemUtil.getUserDir(taxonLDBDir + File.separator + dir);
    }

    protected static void organizeTaxIdXML() throws IOException {
        Path sourceDir = SystemUtil.getUserDir(taxonLDBDir);

        try(DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
            for(Path xmlFile : stream) {
                if (Files.exists(xmlFile)) {
                    String fileName = xmlFile.getFileName().toString();
                    String taxId = fileName.substring(0, fileName.lastIndexOf(".xml"));
                    Path targertDir = getTaxonLDBDirByTaxId(taxId);

                    if (!Files.exists(targertDir)) {
                        Files.createDirectory(targertDir);
                        MyLogger.debug("create dir " + targertDir);
                    }

                    Files.move(xmlFile, Paths.get(targertDir.toString(), fileName));
                    MyLogger.debug("move " + xmlFile + " to " + Paths.get(targertDir.toString(), fileName));
                }
            }
        }

    }

    //Main method
    public static void main(final String[] args) {
        MyLogger.info("\nTaxonomy local database path = " + taxonLDBDir);
//
//        try {
//            getAndAddTaxIdByMemory("1372409");
//        } catch (IOException | XMLStreamException e) {
//            e.printStackTrace();
//        }

        try {
            organizeTaxIdXML();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
