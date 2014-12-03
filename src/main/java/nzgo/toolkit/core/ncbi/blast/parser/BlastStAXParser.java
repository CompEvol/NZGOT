package nzgo.toolkit.core.ncbi.blast.parser;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.ncbi.blast.*;
import nzgo.toolkit.core.util.XMLUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * parse Blast XML output by StAX (XMLStreamReader)
 * Java SAX vs. StAX, http://tutorials.jenkov.com/java-xml/sax-vs-stax.html
 * @author Walter Xie
 */
public class BlastStAXParser {
    public static final int TOP_HITS_LIMITS = 1;
    /**
     * only return the first top hit for each iteration
     * it seems to be faster to use the code directly than to get List<Iteration>
     * @param xmlFile
     * @return
     * @throws JAXBException
     * @throws IOException
     * @throws XMLStreamException
     */
    public static List<Iteration> parse(File xmlFile) throws JAXBException, IOException, XMLStreamException {

        XMLStreamReader xmlStreamReader = XMLUtil.parse(xmlFile);

        JAXBContext jc = JAXBContext.newInstance(Iteration.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();

//TODO        String iterationTag = Iteration.class.getClass().getAnnotation(XmlRootElement.class).name();

        List<Iteration> iterationList = new ArrayList<>();
        while(xmlStreamReader.hasNext()){
            xmlStreamReader.next();

            if(xmlStreamReader.getEventType() == XMLStreamConstants.START_ELEMENT){
                String elementName = xmlStreamReader.getLocalName();
                if(Iteration.TAG.equals(elementName)){
                    Iteration iteration = (Iteration) unmarshaller.unmarshal(xmlStreamReader);
                    iteration.reduceToTopHits(TOP_HITS_LIMITS); // keep top hits by limit
                    iterationList.add(iteration);
                }
            }
        }

        return iterationList;
    }


    public static void printBLASTOutput(File xmlBLASTOutputFile) throws JAXBException, IOException, XMLStreamException {
        MyLogger.info("\nParsing BLAST xml output file : " + xmlBLASTOutputFile + "\n");

        List<Iteration> iterationList = BlastStAXParser.parse(xmlBLASTOutputFile);

        for(Iteration iteration : iterationList) {
            MyLogger.info("iteration:" + iteration.getIterationQueryDef());

            IterationHits hits = iteration.getIterationHits();
            for(Hit hit : hits.getHit()) {
                MyLogger.info("hit id:" + hit.getHitId());
                MyLogger.info("hit length:" + hit.getHitLen());

                HitHsps hitHsps = hit.getHitHsps();
                for(Hsp hsp : hitHsps.getHsp()) {
                    MyLogger.info("hsp num:" + hsp.getHspNum());
                    MyLogger.info("hsp bit score:" + hsp.getHspBitScore());
                    MyLogger.info("hsp e-value:" + hsp.getHspEvalue());
                    MyLogger.info("identity / len:" + hsp.getHspIdentity() + " / " + hsp.getHspAlignLen() + " = " +
                            Double.parseDouble(hsp.getHspIdentity()) / Double.parseDouble(hsp.getHspAlignLen()));
                }

                MyLogger.info("\n");
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        MyLogger.info("\nWorking path = " + workPath);

        try {
            File xmlFile = new File(workPath + "otus1.xml");
            printBLASTOutput(xmlFile);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
