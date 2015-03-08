package nzgo.toolkit.beast;

import nzgo.toolkit.beast.XMLGenerator.TREE_PRIOR;
import nzgo.toolkit.core.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * change models in beast 2 xml.
 *
 * @author Walter Xie
 */
public class XMLConverterCP2Yule extends XMLConverter{
    private static final TREE_PRIOR treePrior = TREE_PRIOR.Yule;

    private static final int NUM_GENES = 10;
    private static final String FILE_PREFIX = "sim" + NUM_GENES + "g10k6t";

    private static final int replicates = 100;

    public XMLConverterCP2Yule() { }

    public static void main(String[] args) {
        XMLConverter xmlConverter = new XMLConverterCP2Yule();

        String sourcePath = "C:\\Beast2\\Adelie\\simulation\\ConstantPopulation\\" + NUM_GENES + "g0";
        String targetPath = "C:\\Beast2\\Adelie\\simulation\\Yule\\" + NUM_GENES + "g0";
        String filePrefix = FILE_PREFIX + "0-";
        xmlConverter.batchConvertXML(sourcePath, targetPath, filePrefix, replicates);

        sourcePath = "C:\\Beast2\\Adelie\\simulation\\ConstantPopulation\\" + NUM_GENES + "g716";
        targetPath = "C:\\Beast2\\Adelie\\simulation\\Yule\\" + NUM_GENES + "g716";
        filePrefix = FILE_PREFIX + "716-";
        xmlConverter.batchConvertXML(sourcePath, targetPath, filePrefix, replicates);
    } // main

    protected String replaceFrom(final BufferedReader reader) throws IOException {
        final String line = reader.readLine();
        if (line == null) return null;

        String newLine = line;
        if (line.contains("id=\"popSize.t:")) {
//            "\t\t\t<parameter id=\"popSize.t:" + firstId + "\" name=\"stateNode\">300000.0</parameter>\n" :
//            "\t\t\t<parameter id=\"birthRate.t:" + firstId + "\" name=\"stateNode\">1.0</parameter>\n") +
            newLine = line.replace("popSize", "birthRate");
            newLine = newLine.replace("300000", "1");

        } else if (line.contains("id=\"CoalescentConstant.t:")) {
//            "\t\t\t\t<distribution id=\"CoalescentConstant.t:" + id + "\" spec=\"Coalescent\">\n" +
//                  "\t\t\t\t\t<populationModel id=\"ConstantPopulation.t:" + id + "\"\n" +
//                  "\t\t\t\t\t\tpopSize=\"@popSize.t:" + idref + "\" spec=\"ConstantPopulation\"/>\n" +
//                  "\t\t\t\t\t<treeIntervals id=\"TreeIntervals.t:" + id + "\" spec=\"TreeIntervals\"\n" +
//                  "\t\t\t\t\t\ttree=\"@Tree.t:" + id + "\"/>\n" +
//                  "\t\t\t\t</distribution>\n" :
//            "\t\t\t\t<distribution id=\"YuleModel.t:" + id + "\" spec=\"beast.evolution.speciation.YuleModel\" " +
//                  "birthDiffRate=\"@birthRate.t:" + idref + "\" tree=\"@Tree.t:" + id + "\"/>");
            String id = StringUtil.substringBetween(line, "id=\"CoalescentConstant.t:", "\" spec=\"Coalescent\">");
            String idref = null;
            String lineNext = reader.readLine();
            while (!lineNext.contains("</distribution>")) {
                if (lineNext.contains("@popSize.t:"))
                    idref = StringUtil.substringBetween(lineNext, "@popSize.t:", "\" spec=\"ConstantPopulation\"/>");
                lineNext = reader.readLine();
            }
            assert idref != null;
            newLine = "\t\t\t\t<distribution id=\"YuleModel.t:" + id + "\" spec=\"beast.evolution.speciation.YuleModel\" " +
                  "birthDiffRate=\"@birthRate.t:" + idref + "\" tree=\"@Tree.t:" + id + "\"/>";

        } else if (line.contains("id=\"PopSizePrior.t:")) {
//            "\t\t\t\t<prior id=\"PopSizePrior.t:" + firstId + "\" name=\"distribution\"\n" +
//                  "\t\t\t\t\t\tx=\"@popSize.t:" + firstId + "\">\n" +
//                  "\t\t\t\t\t<OneOnX id=\"OneOnX.0\" name=\"distr\"/>\n" +
//                  "\t\t\t\t</prior>\n" :
//            "\t\t\t\t<prior id=\"YuleBirthRatePrior.t:" + firstId + "\" name=\"distribution\" x=\"@birthRate.t:" + firstId + "\">\n" +
//                  "\t\t\t\t\t<Uniform id=\"Uniform.1\" name=\"distr\" upper=\"Infinity\"/>\n" +
//                  "\t\t\t\t</prior>\n") +
            String idref = StringUtil.substringBetween(line, "id=\"PopSizePrior.t:", "\" name=\"distribution\"");
            String lineNext = reader.readLine();
            while (!lineNext.contains("</prior>")) {
                lineNext = reader.readLine();
            }
            newLine = "\t\t\t\t<prior id=\"YuleBirthRatePrior.t:" + idref + "\" name=\"distribution\" x=\"@birthRate.t:" + idref + "\">\n" +
                  "\t\t\t\t\t<Uniform id=\"Uniform.1\" name=\"distr\" upper=\"Infinity\"/>\n" +
                  "\t\t\t\t</prior>\n";

        } else if (line.contains("id=\"PopSizeScaler.t:")) {
//            "\t\t<operator id=\"PopSizeScaler.t:" + firstId + "\" parameter=\"@popSize.t:" + firstId + "\"\n" +
//                  "\t\t\t\tscaleFactor=\"0.75\" spec=\"ScaleOperator\" weight=\"3.0\"/>\n" :
//            "\t\t<operator id=\"YuleBirthRateScaler.t:" + firstId + "\" spec=\"ScaleOperator\" parameter=\"@birthRate.t:" + firstId + "\"\n" +
//                  "\t\t\t\tscaleFactor=\"0.75\" weight=\"3.0\"/>\n");
            newLine = line.replace("PopSizeScaler", "YuleBirthRateScaler");
            newLine = newLine.replace("popSize", "birthRate");

        } else if (line.contains("<parameter idref=\"popSize.t:") && line.contains("log")) {
//            "\t\t\t<parameter idref=\"popSize.t:" + firstId + "\" name=\"log\"/>\n\n" :
//            "\t\t\t<log idref=\"birthRate.t:" + firstId + "\"/>\n\n");
            newLine = line.replace("popSize", "birthRate");

        } else if (line.contains("<log idref=\"CoalescentConstant.t:")) {
//            "\t\t\t<log idref=\"CoalescentConstant.t:" + id + "\"/>\n" :
//            "\t\t\t<log idref=\"YuleModel.t:" + id + "\"/>");
            newLine = line.replace("CoalescentConstant", "YuleModel");

        }

        return newLine;
    }
}
