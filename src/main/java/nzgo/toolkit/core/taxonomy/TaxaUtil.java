package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.blast.*;
import nzgo.toolkit.core.blast.parser.BlastStAXParser;
import nzgo.toolkit.core.community.OTU;
import nzgo.toolkit.core.community.OTUs;
import nzgo.toolkit.core.io.GiTaxidIO;
import nzgo.toolkit.core.io.OTUsFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.SampleNameParser;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Taxa Util
 * @author Walter Xie
 */
public class TaxaUtil {

    public static SampleNameParser sampleNameParser = new SampleNameParser();

    // gi|261497976|gb|GU013865.1|
    public static final int GI_INDEX = 1;

    public static void setTaxaToOTUsByBLAST(File xmlBLASTOutputFile, File gi_taxid_raf_nucl, OTUs otus) throws JAXBException, IOException, XMLStreamException {
        GiTaxidIO giTaxidIO = new GiTaxidIO(gi_taxid_raf_nucl);
        // a set of taxid
        Taxa taxidSet = new Taxa();

        MyLogger.info("\nParsing BLAST xml output file : " + xmlBLASTOutputFile);

        List<Iteration> iterationList = BlastStAXParser.parse(xmlBLASTOutputFile);

        for(Iteration iteration : iterationList) {
            String otuName = iteration.getIterationQueryDef();

            MyLogger.debug("iteration:" + otuName);

            IterationHits hits = iteration.getIterationHits();
            for(Hit hit : hits.getHit()) {
                String hitId = hit.getHitId();
                String[] fields = sampleNameParser.getSeparator(0).parse(hitId);
                String gi = fields[GI_INDEX];

                String taxid = giTaxidIO.mapGIToTaxid(gi);

                taxidSet.addUniqueElement(taxid);

                MyLogger.debug("hit id:" + hitId + ", get gi = " + gi);

                MyLogger.debug("hit length:" + hit.getHitLen());

                HitHsps hitHsps = hit.getHitHsps();
                for(Hsp hsp : hitHsps.getHsp()) {
                    MyLogger.debug("hsp num:" + hsp.getHspNum());
                    MyLogger.debug("hsp bit score:" + hsp.getHspBitScore());
                    MyLogger.debug("hsp e-value:" + hsp.getHspEvalue());
                    MyLogger.debug("identity / len:" + hsp.getHspIdentity() + " / " + hsp.getHspAlignLen() + " = " +
                            Double.parseDouble(hsp.getHspIdentity()) / Double.parseDouble(hsp.getHspAlignLen()));
                }

                MyLogger.debug("\n");
            }

            OTU otu = (OTU) otus.getOTUOfSeq(otuName);

            if (otu == null) {
                MyLogger.error("Error: cannot find otu " + otuName + " from OTUs " + otu.getName());
            } else {
                Taxon taxonAgreed = TaxonAgreed.getTaxonAgreed(taxidSet);

                if (taxonAgreed != null)
                    otu.setTaxonAgreed(taxonAgreed);
            }
        }

    }


    //Main method
    public static void main(final String[] args) {
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPath = args[0];
        MyLogger.info("\nWorking path = " + workPath);

        try {
            File otusFile = new File(workPath + "otus1.fasta");
            OTUs otus = new OTUs(otusFile.getName());

            OTUsFileIO.importOTUs(otusFile, otus);

            File xmlBLASTOutputFile = new File(workPath + "blast/otus1.xml");
            File gi_taxid_raf_nucl = new File("/Users/dxie004/Documents/ModelEcoSystem/454/BLAST/gi_taxid_nucl.dmp");

            setTaxaToOTUsByBLAST(xmlBLASTOutputFile, gi_taxid_raf_nucl, otus);



        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

}
