package nzgo.tookit.core.blast.parser;

import nzgo.tookit.core.blast.Hit;
import nzgo.tookit.core.blast.Iteration;
import nzgo.tookit.core.blast.IterationHits;
import nzgo.tookit.core.logger.MyLogger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * parse Blast XML output by StAX (XMLStreamReader)
 * Java SAX vs. StAX, http://tutorials.jenkov.com/java-xml/sax-vs-stax.html
 * @author Walter Xie
 */
public class BlastStAXParser {

    /**
     * only return the first top hit for each iteration
     * @param xmlFile
     * @return
     * @throws JAXBException
     * @throws FileNotFoundException
     * @throws XMLStreamException
     */
    public static List<Iteration> parse(File xmlFile) throws JAXBException, FileNotFoundException, XMLStreamException {

        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader = xif.createXMLStreamReader(new FileReader(xmlFile));

        JAXBContext jc = JAXBContext.newInstance(Iteration.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();

//TODO        String iterationTag = Iteration.class.getClass().getAnnotation(XmlRootElement.class).name();
        String iterationTag = "Iteration";

        List<Iteration> iterationList = new ArrayList<>();
        while(xmlStreamReader.hasNext()){
            xmlStreamReader.next();

            if(xmlStreamReader.getEventType() == XMLStreamReader.START_ELEMENT){
                String elementName = xmlStreamReader.getLocalName();
                if(iterationTag.equals(elementName)){
                    Iteration iteration = (Iteration) unmarshaller.unmarshal(xmlStreamReader);
                    iteration.reduceToFirstTopHit();
                    iterationList.add(iteration);
                }
            }
        }

        return iterationList;
    }

    public static void main(String[] args) {
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        MyLogger.info("\nWorking path = " + workPath);

        try {
            File xmlFile = new File(workPath + "IndirectSoil_otus1.xml");
            List<Iteration> iterationList = parse(xmlFile);

            for(Iteration iteration: iterationList) {
                MyLogger.info("iteration:" + iteration.getIterationQueryDef());

                IterationHits hits = iteration.getIterationHits();
                for(Hit hit:hits.getHit()) {
                    MyLogger.info("def:" + hit.getHitDef());
                    MyLogger.info("len:" + hit.getHitLen());
                    MyLogger.info("\n");
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
