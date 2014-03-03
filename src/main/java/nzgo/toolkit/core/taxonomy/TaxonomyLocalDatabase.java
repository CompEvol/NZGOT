package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
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
 * Taxonomy Local Database
 * @author Walter Xie
 */
public class TaxonomyLocalDatabase {

    public static final String DB_DIR = "TaxID";
    public static final Path taxonLDBDir = SystemUtil.getUserDir(SystemUtil.APP_DIR + File.separator + DB_DIR);

    /**
     * return eFetch result xml given a taxid
     * if not exist taxid.xml in taxonLDBDir, then eFetch and create xml in taxonLDBDir
     * @param taxId
     * @return
     * @throws IOException
     * @throws XMLStreamException
     */
    public static XMLStreamReader getAndAddTaxId(String taxId) throws IOException, XMLStreamException {
        String taxIdXML = taxId + NameSpace.SUFFIX_TAX_ID_FILE;

        try(DirectoryStream<Path> stream = Files.newDirectoryStream(taxonLDBDir)) {
            for(Path xmlFile : stream) {
                if (Files.exists(xmlFile)) {
                    String fileName = xmlFile.getFileName().toString();
                    if (taxIdXML.contentEquals(fileName)) {
                        return XMLUtil.parse(xmlFile.toFile());
                    }
                }
            }
        }

        // if not exist taxid.xml in taxonLDBDir, then eFetch and create xml in taxonLDBDir
        URL url = NCBIeUtils.eFetch(taxId);
        InputStream input = url.openStream();

        Path newXML = Paths.get(taxonLDBDir.toString(), taxIdXML);
        BufferedWriter writer = FileIO.getWriter(newXML, "Create new taxId XML");

        int byteRead;
        // Read a raw byte, returns an int of 0 to 255.
        while ((byteRead = input.read()) != -1) {
            writer.write(byteRead);
        }

        input.close();
        writer.close();

        return XMLUtil.parse(url);
    }

    //Main method
    public static void main(final String[] args) {
        MyLogger.info("\nTaxonomy local database path = " + taxonLDBDir);

        try {
            TaxonomyLocalDatabase.getAndAddTaxId("1372409");
        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }

    }

}
