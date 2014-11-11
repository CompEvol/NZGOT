package nzgo.toolkit.beast;

import java.io.IOException;

/**
 * take ids to generate beast 2 xml up-down op section.
 *
 * @author Walter Xie
 */
public class UpDownAppender {

    private static String[] ids = new String[]{
            "Pad_concate1", "Pad_concate2", "Pad_concate3", "Pad_concate4", "Pad_concate5", "Pad_concate6", "Pad_concate7", "Pad_concate8", "Pad_concate9", "Pad_concate10", "Pad_concate11", "Pad_concate12", "Pad_concate13", "Pad_concate14", "Pad_concate15", "Pad_concate16", "Pad_concate17", "Pad_concate18", "Pad_concate19", "Pad_concate20", "Pad_concate21", "Pad_concate22", "Pad_concate23", "Pad_concate24", "Pad_concate25", "Pad_concate26", "Pad_concate27", "Pad_concate28", "Pad_concate29", "Pad_concate30", "Pad_concate31", "Pad_concate32", "Pad_concate33", "Pad_concate34", "Pad_concate35", "Pad_concate36", "Pad_concate37", "Pad_concate38", "Pad_concate39", "Pad_concate40"
    };

    public UpDownAppender() {

    }

    public static void main(String[] args) throws IOException {
        System.out.println("Input " + ids.length + " ids ...\n\n");

        for (String id : ids) {
            System.out.print(getUpDownXML(id));
        }
        System.out.println();
        System.out.println(getBigUpDownXML());
    } // main


    private static String getUpDownXML(String id) {
        return "\t\t<operator id=\"strictClockUpDownOperator.c:" + id +
                "\" scaleFactor=\"0.9\" optimise=\"false\" spec=\"UpDownOperator\" weight=\"15.0\">\n" +
                "\t\t\t<parameter idref=\"clockRate.c:Pad_R000255_Scaffold10\" name=\"up\"/>\n" +
                "\t\t\t<tree idref=\"Tree.t:" + id + "\" name=\"down\"/>\n" +
                "\t\t</operator>\n";
    }

    private static String getBigUpDownXML() {
        StringBuilder stringBuilder = new StringBuilder(1000);
        stringBuilder.append("\t\t<operator id=\"strictClockUpDownOperator.all\" scaleFactor=\"0.95\" ");
        stringBuilder.append("optimise=\"false\" spec=\"UpDownOperator\" weight=\"15.0\">\n");
        stringBuilder.append("\t\t\t<parameter idref=\"clockRate.c:Pad_R000255_Scaffold10\" name=\"up\"/>\n");
        for (String id : ids) {
            stringBuilder.append("\t\t\t<tree idref=\"Tree.t:").append(id).append("\" name=\"down\"/>\n");
        }
        stringBuilder.append("\t\t</operator>\n");
        return stringBuilder.toString();
    }
}
