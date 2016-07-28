package nzgo.toolkit.core.util;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.net.URL;

/**
 * XMLUtil
 * @author Walter Xie
 */
public class XMLUtil {

    /**
     * does the tag equal the given name ignoring case
     * @param tag
     * @param name
     * @return
     */
    public static boolean isTag(String tag, String name) {
        if (tag.equalsIgnoreCase(name)) return true;
        return false;
    }

    /**
     * StAX parse XML resource into XMLStreamReader
     * @return XMLStreamReader
     * @throws IOException
     * @throws XMLStreamException
     */
    public static XMLStreamReader parse(URL url) throws IOException, XMLStreamException {
        InputStream input = url.openStream();
        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader = xif.createXMLStreamReader(input);

        return xmlStreamReader;
    }

    public static XMLStreamReader parse(File xmlFile) throws IOException, XMLStreamException {
        FileReader input = new FileReader(xmlFile);
        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader = xif.createXMLStreamReader(input);

        return xmlStreamReader;
    }

    public static XMLStreamReader parse(String fileName) throws IOException, XMLStreamException {
        FileInputStream input = new FileInputStream(fileName);
        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader = xif.createXMLStreamReader(input);

        return xmlStreamReader;
    }


}
