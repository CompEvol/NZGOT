package nzgo.toolkit.core.taxonomy.parser;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.taxonomy.NCBIEUtils;
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

    public static List<String> getIdList(String scientificName) throws XMLStreamException, IOException {
        URL url = NCBIEUtils.eFetch(scientificName);
        XMLStreamReader xmlStreamReader = XMLUtil.parse(url);

        int count = 0;
        List<String> idList = new ArrayList<>();
        try {
            while(xmlStreamReader.hasNext()){
//                xmlStreamReader.next();
                xmlStreamReader.nextTag();

                if(xmlStreamReader.getEventType() == XMLStreamConstants.START_ELEMENT){
                    String elementName = xmlStreamReader.getLocalName();
                    if (NCBIEUtils.isCount(elementName)) {
                        String elementText = xmlStreamReader.getElementText();
                        count = Integer.parseInt(elementText);

                        if (count < 1) break; // quick exist from loop, if no result

                    } else if (count > 0 && NCBIEUtils.isIdList(elementName)) {
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
            xmlStreamReader.nextTag();

            String elementName = xmlStreamReader.getLocalName();
            if(xmlStreamReader.getEventType() == XMLStreamReader.END_ELEMENT){
                if(NCBIEUtils.isIdList(elementName)){
                    return idList;
                }
            } else if(xmlStreamReader.getEventType() == XMLStreamReader.START_ELEMENT && NCBIEUtils.isId(elementName)){
                String elementText = xmlStreamReader.getElementText();
                idList.add(elementText);
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
