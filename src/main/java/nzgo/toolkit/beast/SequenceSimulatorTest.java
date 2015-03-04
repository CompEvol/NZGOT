package nzgo.toolkit.beast;

import beast.app.seqgen.SequenceSimulator;
import beast.evolution.alignment.Alignment;
import beast.evolution.alignment.Sequence;
import beast.evolution.branchratemodel.BranchRateModel;
import beast.evolution.branchratemodel.StrictClockModel;
import beast.evolution.sitemodel.SiteModel;
import beast.evolution.substitutionmodel.Frequencies;
import beast.evolution.substitutionmodel.HKY;
import beast.evolution.substitutionmodel.JukesCantor;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Incomplete //TODO pairwise distance between sequences
 *
 * @author Walter Xie
 */
public class SequenceSimulatorTest extends TestCase {
    String[] taxa = new String[]{"A","B","C"};
    final String tree = "((A:2.0,B:1.0):2.0,C:2.0);";
    final int sequenceLength = 10000;
    final double clockRate = 0.03;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testSequenceSimulator() throws Exception {
        Alignment dummyAlg = getDummyAlignment(taxa);
        SiteModel siteModel = getSiteModel(dummyAlg);
        BranchRateModel branchRateModel = getBranchModel(clockRate);

        SequenceSimulator sequenceSimulator = new SequenceSimulator(); // sequence length default 1000
        sequenceSimulator.initByName("data", dummyAlg, "tree", tree, "siteModel", siteModel,
                "branchRateModel", branchRateModel, "sequencelength", sequenceLength);

        Alignment simAlg = sequenceSimulator.simulate();

        for (Sequence seq : simAlg.sequenceInput.get()) {
            System.out.println(">" + seq.taxonInput.get());
            System.out.println(seq.dataInput.get());
        }

//        ClusterTree upgma = new ClusterTree();
//        upgma.initByName("clusterType", "upgma", "taxa", simAlg,
////                "distance", , // default Jukes Cantor
//                "clock.rate", Double.toString(clockRate));
//        System.out.println(upgma.getRoot().toNewick(false));
    }


    protected Alignment getDummyAlignment(String[] taxa) throws Exception {
        List<Sequence> seqList = new ArrayList<>();

        for (int i=0; i<taxa.length; i++) {
            seqList.add(new Sequence(taxa[i], "?"));
        }

        return new Alignment(seqList, 4, "nucleotide");
    }

    protected SiteModel getSiteModel(Alignment dummyAlg) throws Exception {
        JukesCantor jc = new JukesCantor();

        Frequencies freqs = new Frequencies();
        freqs.initByName("data", dummyAlg);

        double kappa = 5;
        double gammaShape = 1;

        HKY hky = new HKY();
        hky.initByName("kappa", Double.toString(kappa), "frequencies", freqs);

        SiteModel siteModel = new SiteModel();
        siteModel.initByName(
//                "mutationRate", Double.toString(mutationRate),
                "gammaCategoryCount", 4,
                "shape", Double.toString(gammaShape),
//                "proportionInvariant", "0.0",
                "substModel", hky);

        return siteModel;
    }

    // hard code
    protected BranchRateModel getBranchModel(double clockRate) throws Exception {
        StrictClockModel strictClockModel = new StrictClockModel();
        strictClockModel.initByName("clock.rate", Double.toString(clockRate));

        return strictClockModel;
    }


}
