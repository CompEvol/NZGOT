package nzgo.toolkit.beast;

import nzgo.toolkit.core.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * change models in beast 2 xml.
 *
 * @author Walter Xie
 */
public class XMLConverterCP2BDSS extends XMLConverter{

    private static final int NUM_GENES = 3;
    private static final String FILE_PREFIX = "sim" + NUM_GENES + "g10k6t";

    private static final int replicates = 100;

    public XMLConverterCP2BDSS() { }

    public static void main(String[] args) {
        XMLConverter xmlConverter = new XMLConverterCP2BDSS();

        String sourcePath = "C:\\Beast2\\Adelie\\simulation\\ConstantPopulation\\" + NUM_GENES + "g0";
        String targetPath = "C:\\Beast2\\Adelie\\simulation\\BDSS\\" + NUM_GENES + "g0";
        String filePrefix = FILE_PREFIX + "0-";
        xmlConverter.batchConvertXML(sourcePath, targetPath, filePrefix, filePrefix.replace("sim", "bdss"), replicates);

        sourcePath = "C:\\Beast2\\Adelie\\simulation\\ConstantPopulation\\" + NUM_GENES + "g716";
        targetPath = "C:\\Beast2\\Adelie\\simulation\\BDSS\\" + NUM_GENES + "g716";
        filePrefix = FILE_PREFIX + "716-";
        xmlConverter.batchConvertXML(sourcePath, targetPath, filePrefix, filePrefix.replace("sim", "bdss"), replicates);
    } // main

    protected String replaceFrom(final BufferedReader reader) throws IOException {
        final String line = reader.readLine();
        if (line == null) return null;

        String newLine = line;
        if (line.contains("id=\"popSize.t:")) {
//            "\t\t\t<parameter id=\"popSize.t:" + firstId + "\" name=\"stateNode\">300000.0</parameter>\n" :
//            <parameter id="samplingProportion.t:gene1" name="stateNode">0.0</parameter>
//            <parameter id="becomeUninfectiousRate.t:gene1" dimension="10" lower="0.0" name="stateNode" upper="Infinity">1.0</parameter>
//            <parameter id="origin.t:gene1" lower="0.0" name="stateNode" upper="Infinity">1000.0</parameter>
//            <parameter id="R0.t:gene1" dimension="10" lower="0.0" name="stateNode" upper="Infinity">2.0</parameter>
            String id = StringUtil.substringBetween(line, "id=\"popSize.t:", "\" name=\"stateNode\">");
            newLine = "\t\t\t<parameter id=\"samplingProportion.t:gene1\" name=\"stateNode\">0.0</parameter>\n" +
                  "\t\t\t<parameter id=\"becomeUninfectiousRate.t:gene1\" dimension=\"10\" lower=\"0.0\" name=\"stateNode\" upper=\"Infinity\">1.0</parameter>\n" +
                  "\t\t\t<parameter id=\"origin.t:gene1\" lower=\"0.0\" name=\"stateNode\" upper=\"Infinity\">1000000</parameter>\n" +
                  "\t\t\t<parameter id=\"R0.t:gene1\" dimension=\"10\" lower=\"0.0\" name=\"stateNode\" upper=\"Infinity\">2.0</parameter>\n";

        } else if (line.contains("id=\"CoalescentConstant.t:")) {
//            "\t\t\t\t<distribution id=\"CoalescentConstant.t:" + id + "\" spec=\"Coalescent\">\n" +
//                  "\t\t\t\t\t<populationModel id=\"ConstantPopulation.t:" + id + "\"\n" +
//                  "\t\t\t\t\t\tpopSize=\"@popSize.t:" + idref + "\" spec=\"ConstantPopulation\"/>\n" +
//                  "\t\t\t\t\t<treeIntervals id=\"TreeIntervals.t:" + id + "\" spec=\"TreeIntervals\"\n" +
//                  "\t\t\t\t\t\ttree=\"@Tree.t:" + id + "\"/>\n" +
//                  "\t\t\t\t</distribution>\n" :
//            <distribution id="BirthDeathSkySerial.t:gene2" spec="beast.evolution.speciation.BirthDeathSkylineModel"
//            becomeUninfectiousRate="@becomeUninfectiousRate.t:gene1" origin="@origin.t:gene1" samplingProportion="@samplingProportion.t:gene1" R0="@R0.t:gene1" tree="@Tree.t:gene1">
//            <parameter id="R0.t:gene1" dimension="10" estimate="false" lower="0.0" name="R0" upper="Infinity">2.0</parameter>
//            </distribution>

            String id = StringUtil.substringBetween(line, "id=\"CoalescentConstant.t:", "\" spec=\"Coalescent\">");
            String idref = null;
            String lineNext = reader.readLine();
            while (!lineNext.contains("</distribution>")) {
                if (lineNext.contains("@popSize.t:"))
                    idref = StringUtil.substringBetween(lineNext, "@popSize.t:", "\" spec=\"ConstantPopulation\"/>");
                lineNext = reader.readLine();
            }
            assert idref != null;
            newLine = "\t\t\t\t<distribution id=\"BirthDeathSkySerial.t:" + id + "\" spec=\"beast.evolution.speciation.BirthDeathSkylineModel\" \n" +
                  "\t\t\t\t\tbecomeUninfectiousRate=\"@becomeUninfectiousRate.t:" + idref + "\" origin=\"@origin.t:" + idref +
                  "\" samplingProportion=\"@samplingProportion.t:" + idref +  "\" R0=\"@R0.t:" + idref +  "\" tree=\"@Tree.t:" + idref + "\">\n" +
                  "\t\t\t\t</distribution>\n";

        } else if (line.contains("id=\"PopSizePrior.t:")) {
//            "\t\t\t\t<prior id=\"PopSizePrior.t:" + firstId + "\" name=\"distribution\"\n" +
//                  "\t\t\t\t\t\tx=\"@popSize.t:" + firstId + "\">\n" +
//                  "\t\t\t\t\t<OneOnX id=\"OneOnX.0\" name=\"distr\"/>\n" +
//                  "\t\t\t\t</prior>\n" :
//            <prior id="samplingProportionPrior.t:gene1" name="distribution" x="@samplingProportion.t:gene1">
//            <Beta id="Beta.0" name="distr">
//            <parameter id="RealParameter.03" lower="0.0" name="alpha" upper="0.0">1.0</parameter>
//            <parameter id="RealParameter.04" lower="0.0" name="beta" upper="0.0">1.0</parameter>
//            </Beta>
//            </prior>
//            <prior id="becomeUninfectiousRatePrior.t:gene1" name="distribution" x="@becomeUninfectiousRate.t:gene1">
//            <LogNormal id="LogNormalDistributionModel.01" name="distr">
//            <parameter id="RealParameter.09" lower="0.0" name="M" upper="0.0">0.0</parameter>
//            <parameter id="RealParameter.010" lower="0.0" name="S" upper="0.0">1.0</parameter>
//            </LogNormal>
//            </prior>
//            <prior id="originPrior.t:gene1" name="distribution" x="@origin.t:gene1">
//            <Uniform id="Uniform.02" name="distr" upper="Infinity"/>
//            </prior>
//            <prior id="RPrior.t:gene1" name="distribution" x="@R0.t:gene1">
//            <LogNormal id="LogNormalDistributionModel.02" name="distr">
//            <parameter id="RealParameter.011" lower="0.0" name="M" upper="0.0">0.0</parameter>
//            <parameter id="RealParameter.012" lower="0.0" name="S" upper="0.0">1.0</parameter>
//            </LogNormal>
//            </prior>

            String idref = StringUtil.substringBetween(line, "id=\"PopSizePrior.t:", "\" name=\"distribution\"");
            String lineNext = reader.readLine();
            while (!lineNext.contains("</prior>")) {
                lineNext = reader.readLine();
            }
            newLine = "\t\t\t\t<prior id=\"samplingProportionPrior.t:" + idref + "\" name=\"distribution\" x=\"@samplingProportion.t:" + idref + "\">\n" +
                  "\t\t\t\t\t<Beta id=\"Beta.0\" name=\"distr\">\n" +
                  "\t\t\t\t\t\t<parameter id=\"RealParameter.03\" lower=\"0.0\" name=\"alpha\" upper=\"0.0\">1.0</parameter>\n" +
                  "\t\t\t\t\t\t<parameter id=\"RealParameter.04\" lower=\"0.0\" name=\"beta\" upper=\"0.0\">1.0</parameter>\n" +
                  "\t\t\t\t\t</Beta>\n" +
                  "\t\t\t\t</prior>\n" +
//                  "\t\t\t\t<prior id=\"becomeUninfectiousRatePrior.t:" + idref + "\" name=\"distribution\" x=\"@becomeUninfectiousRate.t:" + idref + "\">\n" +
//                  "\t\t\t\t\t<LogNormal id=\"LogNormalDistributionModel.01\" name=\"distr\">\n" +
//                  "\t\t\t\t\t\t<parameter id=\"RealParameter.09\" lower=\"0.0\" name=\"M\" upper=\"0.0\">0.0</parameter>\n" +
//                  "\t\t\t\t\t\t<parameter id=\"RealParameter.010\" lower=\"0.0\" name=\"S\" upper=\"0.0\">1.0</parameter>\n" +
//                  "\t\t\t\t\t</LogNormal>\n" +
//                  "\t\t\t\t</prior>\n" +
                  "\t\t\t\t<prior id=\"originPrior.t:" + idref + "\" name=\"distribution\" x=\"@origin.t:" + idref + "\">\n" +
                  "\t\t\t\t\t\t<Uniform id=\"Uniform.02\" name=\"distr\" upper=\"Infinity\"/>\n" +
                  "\t\t\t\t</prior>\n" +
                  "\t\t\t\t<prior id=\"RPrior.t:" + idref + "\" name=\"distribution\" x=\"@R0.t:" + idref + "\">\n" +
                  "\t\t\t\t\t<LogNormal id=\"LogNormalDistributionModel.02\" name=\"distr\">\n" +
                  "\t\t\t\t\t\t<parameter id=\"RealParameter.011\" lower=\"0.0\" name=\"M\" upper=\"0.0\">0.0</parameter>\n" +
                  "\t\t\t\t\t\t<parameter id=\"RealParameter.012\" lower=\"0.0\" name=\"S\" upper=\"0.0\">1.0</parameter>\n" +
                  "\t\t\t\t\t</LogNormal>\n" +
                  "\t\t\t\t</prior>\n";

        } else if (line.contains("id=\"PopSizeScaler.t:")) {
//            "\t\t<operator id=\"PopSizeScaler.t:" + firstId + "\" parameter=\"@popSize.t:" + firstId + "\"\n" +
//                  "\t\t\t\tscaleFactor=\"0.75\" spec=\"ScaleOperator\" weight=\"3.0\"/>\n" :
//            <operator id="samplingScaler.t:gene1" spec="ScaleOperator" parameter="@samplingProportion.t:gene1" scaleFactor="0.75" weight="10.0"/>
//            <operator id="becomeUninfectiousRateScaler.t:gene1" spec="ScaleOperator" parameter="@becomeUninfectiousRate.t:gene1" scaleFactor="0.75" weight="10.0"/>
//            <operator id="RScaler.t:gene1" spec="ScaleOperator" parameter="@R0.t:gene1" scaleFactor="0.75" weight="10.0"/>
//            <operator id="origScaler.t:gene1" spec="ScaleOperator" parameter="@origin.t:gene1" scaleFactor="0.75" weight="1.0"/>

            String idref = StringUtil.substringBetween(line, "id=\"PopSizeScaler.t:", "\" parameter=\"@popSize.t:");
            String lineNext = reader.readLine();
            while (!lineNext.contains("weight=\"3.0\"/>")) {
                lineNext = reader.readLine();
            }
            newLine = "\t\t<operator id=\"samplingScaler.t:" + idref + "\" spec=\"ScaleOperator\" parameter=\"@samplingProportion.t:" + idref + "\" scaleFactor=\"0.75\" weight=\"10.0\"/>\n" +
                  "\t\t<operator id=\"becomeUninfectiousRateScaler.t:" + idref + "\" spec=\"ScaleOperator\" parameter=\"@becomeUninfectiousRate.t:" + idref + "\" scaleFactor=\"0.75\" weight=\"10.0\"/>\n" +
                  "\t\t<operator id=\"RScaler.t:" + idref + "\" spec=\"ScaleOperator\" parameter=\"@R0.t:" + idref + "\" scaleFactor=\"0.75\" weight=\"10.0\"/>\n" +
                  "\t\t<operator id=\"origScaler.t:" + idref + "\" spec=\"ScaleOperator\" parameter=\"@origin.t:" + idref + "\" scaleFactor=\"0.75\" weight=\"1.0\"/>\n";

        } else if (line.contains("<parameter idref=\"popSize.t:") && line.contains("log")) {
//            "\t\t\t<parameter idref=\"popSize.t:" + firstId + "\" name=\"log\"/>\n\n" :
//            <log idref="BirthDeathSkySerial.t:gene1"/>
//            <log idref="samplingProportion.t:gene1"/>
//            <log idref="becomeUninfectiousRate.t:gene1"/>
//            <log id="birth.t:gene1" spec="beast.math.statistic.RPNcalculator" expression="R0.t:gene1 becomeUninfectiousRate.t:gene1 *">
//            <parameter idref="becomeUninfectiousRate.t:gene1"/>
//            <parameter idref="R0.t:gene1"/>
//            </log>
//            <log id="death.t:gene1" spec="beast.math.statistic.RPNcalculator" expression="becomeUninfectiousRate.t:gene1 1 samplingProportion.t:gene1 - *">
//            <parameter idref="becomeUninfectiousRate.t:gene1"/>
//            <parameter idref="samplingProportion.t:gene1"/>
//            </log>
//            <log id="sampling.t:gene1" spec="beast.math.statistic.RPNcalculator" expression="becomeUninfectiousRate.t:gene1 samplingProportion.t:gene1 *">
//            <parameter idref="becomeUninfectiousRate.t:gene1"/>
//            <parameter idref="samplingProportion.t:gene1"/>
//            </log>
//            <log idref="origin.t:gene1"/>
//            <log idref="R0.t:gene1"/>

            String idref = StringUtil.substringBetween(line, "<parameter idref=\"popSize.t:", "\" name=\"log\"/>");
            newLine = "\t\t\t<log idref=\"samplingProportion.t:" + idref + "\"/>\n" +
                  "\t\t\t<log idref=\"becomeUninfectiousRate.t:" + idref + "\"/>\n" +
                  "\t\t\t<log id=\"birth.t:" + idref + "\" spec=\"beast.math.statistic.RPNcalculator\" expression=\"R0.t:" + idref + " becomeUninfectiousRate.t:" + idref + " *\">\n" +
                  "\t\t\t<parameter idref=\"becomeUninfectiousRate.t:" + idref + "\"/>\n" +
                  "\t\t\t<parameter idref=\"R0.t:" + idref + "\"/>\n" +
                  "\t\t\t</log>\n" +
                  "\t\t\t<log id=\"death.t:" + idref + "\" spec=\"beast.math.statistic.RPNcalculator\" expression=\"becomeUninfectiousRate.t:" + idref + " 1 samplingProportion.t:" + idref + " - *\">\n" +
                  "\t\t\t<parameter idref=\"becomeUninfectiousRate.t:" + idref + "\"/>\n" +
                  "\t\t\t<parameter idref=\"samplingProportion.t:" + idref + "\"/>\n" +
                  "\t\t\t</log>\n" +
                  "\t\t\t<log id=\"sampling.t:" + idref + "\" spec=\"beast.math.statistic.RPNcalculator\" expression=\"becomeUninfectiousRate.t:" + idref + " samplingProportion.t:" + idref + " *\">\n" +
                  "\t\t\t<parameter idref=\"becomeUninfectiousRate.t:" + idref + "\"/>\n" +
                  "\t\t\t<parameter idref=\"samplingProportion.t:" + idref + "\"/>\n" +
                  "\t\t\t</log>\n" +
                  "\t\t\t<log idref=\"origin.t:" + idref + "\"/>\n" +
                  "\t\t\t<log idref=\"R0.t:" + idref + "\"/>\n";

        } else if (line.contains("<log idref=\"CoalescentConstant.t:")) {
//            "\t\t\t<log idref=\"CoalescentConstant.t:" + id + "\"/>\n" :
//            "\t\t\t<log idref=\"BirthDeathSkySerial.t:" + id + "\"/>");
            newLine = line.replace("CoalescentConstant", "BirthDeathSkySerial");

        }

        return newLine;
    }
}
