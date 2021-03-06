package nzgo.toolkit.core.taxonomy.parser;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.taxonomy.NCBIeUtils;
import nzgo.toolkit.core.util.XMLUtil;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * parse eSearch XML result
 * @author Walter Xie
 */
public class ESearchStAXParser {

    /**
     * get NCBI tax id list given a scientific name, list could 0, 1, or multi ids (same name but different id)
     *
     * @param scientificName
     * @return
     * @throws XMLStreamException
     * @throws IOException
     */
    public static List<String> getIdList(String scientificName) throws XMLStreamException, IOException {
        URL url = NCBIeUtils.eSearch(scientificName);
        XMLStreamReader xmlStreamReader = XMLUtil.parse(url);

        int count = 0;
        List<String> idList = new ArrayList<>();
        try {
            while(xmlStreamReader.hasNext()){
                xmlStreamReader.next();

                if(xmlStreamReader.getEventType() == XMLStreamConstants.START_ELEMENT){
                    String elementName = xmlStreamReader.getLocalName();
                    if (NCBIeUtils.isCount(elementName)) {
                        String elementText = xmlStreamReader.getElementText();
                        count = Integer.parseInt(elementText);

                        if (count < 1) break; // quick exist from loop, if no result

                    } else if (count > 0 && NCBIeUtils.isIdList(elementName)) {
                        idList = parseIdList(xmlStreamReader);

                        if (count != idList.size())
                            throw new IllegalArgumentException("Inconsistent count in the eSearch result !");
                    }
                }
            }
        } finally {
            xmlStreamReader.close();
        }

        return idList;
    }

    protected static List<String> parseIdList(XMLStreamReader xmlStreamReader) throws XMLStreamException {
        List<String> idList = new ArrayList<>();
        while(xmlStreamReader.hasNext()){
            xmlStreamReader.next();

            if(xmlStreamReader.getEventType() == XMLStreamReader.END_ELEMENT){
                if(NCBIeUtils.isIdList(xmlStreamReader.getLocalName())) return idList;

            } else if(xmlStreamReader.getEventType() == XMLStreamReader.START_ELEMENT){
                if(NCBIeUtils.isId(xmlStreamReader.getLocalName())) {
                    String elementText = xmlStreamReader.getElementText();
                    idList.add(elementText);
                }
            }
        }
        return idList;
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
