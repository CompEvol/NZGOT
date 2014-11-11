package nzgo.toolkit.beast;

/**
 * generate beast 2 xml.
 *
 * @author Walter Xie
 */
public class XMLGenerator {


    public XMLGenerator() {

    }

    public static void main(String[] args) {

//        for (int i=1; i<41; i++) {
//            System.out.print(getXMLId(i));
//        }
//        System.out.println();
        for (int i=1; i<41; i++) {
            System.out.print(getXMLId2(i));
        }
    } // main

//    private static String getXMLId(int id) {
//        return "<operator id=\"treeScaler.t:Pad_concate" + id + "\" scaleFactor=\"0.8\" optimise=\"false\" spec=\"ScaleOperator\"\n" +
//                "\ttree=\"@Tree.t:Pad_concate" + id + "\" weight=\"3.0\"/>\n" +
//                "\n" +
//                "<operator id=\"treeRootScaler.t:Pad_concate" + id + "\" rootOnly=\"true\" scaleFactor=\"0.7\" optimise=\"false\" spec=\"ScaleOperator\"\n" +
//                "\t  tree=\"@Tree.t:Pad_concate" + id + "\" weight=\"3.0\"/>\n" +
//                "\n" +
//                "<operator id=\"UniformOperator.t:Pad_concate" + id + "\" spec=\"Uniform\" tree=\"@Tree.t:Pad_concate" + id + "\"\n" +
//                "\tweight=\"30.0\"/>\n" +
//                "\n" +
//                "<operator id=\"SubtreeSlide.t:Pad_concate" + id + "\" spec=\"SubtreeSlide\" tree=\"@Tree.t:Pad_concate" + id + "\"\n" +
//                "\tweight=\"15.0\"/>\n" +
//                "\n" +
//                "<operator id=\"narrow.t:Pad_concate" + id + "\" spec=\"Exchange\" tree=\"@Tree.t:Pad_concate" + id + "\"\n" +
//                "\tweight=\"15.0\"/>\n" +
//                "\n" +
//                "<operator id=\"wide.t:Pad_concate" + id + "\" isNarrow=\"false\" spec=\"Exchange\"\n" +
//                "\ttree=\"@Tree.t:Pad_concate" + id + "\" weight=\"3.0\"/>\n" +
//                "\n" +
//                "<operator id=\"WilsonBalding.t:Pad_concate" + id + "\" spec=\"WilsonBalding\" tree=\"@Tree.t:Pad_concate" + id + "\"\n" +
//                "\tweight=\"3.0\"/>\n";

//        return "<distribution id=\"CoalescentConstant.t:Pad_concate" + id + "\" spec=\"Coalescent\">\n" +
//                "\t<populationModel id=\"ConstantPopulation.t:Pad_concate" + id + "\"\n" +
//                "\t\tpopSize=\"@popSize.t:Pad_concate0\" spec=\"ConstantPopulation\"/>\n" +
//                "\t<treeIntervals id=\"TreeIntervals.t:Pad_concate" + id + "\" spec=\"TreeIntervals\" tree=\"@Tree.t:Pad_concate" + id + "\"/>\n" +
//                "</distribution>\n";
//        return "<tree id=\"Tree.t:Pad_concate" + id + "\" name=\"stateNode\">\n"+
//            "\t<trait idref=\"dateTrait.t:Pad_concate0\" spec=\"beast.evolution.tree.TraitSet\" traitname=\"date\"/>\n"+
//            "\t<taxonset idref=\"TaxonSet.Pad_concate0\"/>\n"+
//            "</tree>\n";
//
//        return "<distribution branchRateModel=\"@StrictClock.c:Pad_concate0\" data=\"@Pad_concate" + id + "\"\n" +
//               "\tid=\"treeLikelihood.Pad_concate" + id + "\" siteModel=\"@SiteModel.s:Pad_concate0\"\n" +
//               "\tspec=\"TreeLikelihood\" tree=\"@Tree.t:Pad_concate" + id + "\"/>\n";
//    }

    private static String getXMLId2(int id) {
//        return "<logger fileName=\"$(tree).trees\" id=\"treelog.t:Pad_concate" + id + "\" logEvery=\"100000\" mode=\"tree\">\n" +
//                "\t<log id=\"TreeWithMetaDataLogger.t:Pad_concate" + id + "\" spec=\"beast.evolution.tree.TreeWithMetaDataLogger\"\n" +
//                "\t\ttree=\"@Tree.t:Pad_concate" + id + "\"/>\n" +
//                "</logger>\n";
        return "<init estimate=\"false\" id=\"RandomTree.t:Pad_concate" + id + "\" initial=\"@Tree.t:Pad_concate" + id + "\"\n" +
                "\tspec=\"beast.evolution.tree.RandomTree\" taxa=\"@Pad_concate" + id + "\">\n" +
                "\t<populationModel id=\"ConstantPopulation0.t:Pad_concate" + id + "\" spec=\"ConstantPopulation\">\n" +
                "\t\t<parameter idref=\"randomPopSize.t:Pad_concate0\" name=\"popSize\"/>\n" +
                "\t</populationModel>\n" +
                "</init>\n";
    }

    private static String getXMLIdref(int id) {
        return "<log idref=\"treeLikelihood.Pad_concate" + id + "\"/>\n" +
                "<log id=\"TreeHeight.t:Pad_concate" + id + "\" spec=\"beast.evolution.tree.TreeHeightLogger\"\n" +
                "\ttree=\"@Tree.t:Pad_concate" + id + "\"/>\n";
    }
}
