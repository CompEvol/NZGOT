package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.util.SystemUtil;
import nzgo.toolkit.core.util.XMLUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
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

    public static final Path taxonLDBDir = SystemUtil.getAppDir();

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
        File f;
        try {
            f = new File(url.toURI());
        } catch(URISyntaxException e) {
            f = new File(url.getPath());
        }
        if (!taxIdXML.contentEquals(f.getName())) {
            Path target = Paths.get(taxonLDBDir.toString(), taxIdXML);
            Files.move(f.toPath(), target);
        }
        return XMLUtil.parse(url);
    }

    //Main method
    public static void main(final String[] args) {
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        MyLogger.info("\nWorking path = " + workPath);



    }

}
