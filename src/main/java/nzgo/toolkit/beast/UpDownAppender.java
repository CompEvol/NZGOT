package nzgo.toolkit.beast;

import java.io.IOException;

/**
 * take ids to generate beast 2 xml up-down op section.
 *
 * @author Walter Xie
 */
public class UpDownAppender {

    private static String[] ids = new String[]{
            "Pad_R000255_Scaffold10","Pad_R000272_Scaffold10","Pad_R000333_Scaffold10","Pad_R000362_Scaffold100","Pad_R000433_Scaffold101","Pad_R000435_Scaffold101","Pad_R000501_Scaffold101","Pad_R000589_Scaffold105","Pad_R000921_Scaffold111","Pad_R000968_Scaffold113","Pad_R000982_Scaffold113","Pad_R001070_Scaffold113","Pad_R001268_Scaffold116","Pad_R001358_Scaffold117","Pad_R001436_Scaffold120","Pad_R001518_Scaffold120","Pad_R001556_Scaffold120","Pad_R001650_Scaffold121","Pad_R001701_Scaffold121","Pad_R001809_Scaffold123","Pad_R001830_Scaffold124","Pad_R001835_Scaffold124","Pad_R001844_Scaffold124","Pad_R001936_Scaffold126","Pad_R002184_Scaffold130","Pad_R002193_Scaffold130","Pad_R002321_Scaffold135","Pad_R002432_Scaffold14","Pad_R002437_Scaffold14","Pad_R002466_Scaffold14","Pad_R002648_Scaffold141","Pad_R002661_Scaffold142","Pad_R002689_Scaffold142","Pad_R002840_Scaffold149","Pad_R003054_Scaffold155","Pad_R003068_Scaffold155","Pad_R003087_Scaffold155","Pad_R003119_Scaffold156","Pad_R003213_Scaffold157","Pad_R003309_Scaffold160","Pad_R003496_Scaffold166","Pad_R003515_Scaffold166","Pad_R003684_Scaffold17","Pad_R003692_Scaffold170","Pad_R003815_Scaffold172","Pad_R003879_Scaffold172","Pad_R004040_Scaffold175","Pad_R004401_Scaffold187","Pad_R004775_Scaffold202","Pad_R004910_Scaffold208","Pad_R004973_Scaffold21","Pad_R005006_Scaffold21","Pad_R005264_Scaffold22","Pad_R005314_Scaffold22","Pad_R005397_Scaffold222","Pad_R005440_Scaffold223","Pad_R005530_Scaffold226","Pad_R005704_Scaffold234","Pad_R005753_Scaffold24","Pad_R005815_Scaffold241","Pad_R006128_Scaffold25","Pad_R006399_Scaffold262","Pad_R006420_Scaffold262","Pad_R006511_Scaffold267","Pad_R006690_Scaffold27","Pad_R006703_Scaffold27","Pad_R006949_Scaffold284","Pad_R006975_Scaffold286","Pad_R006991_Scaffold286","Pad_R007049_Scaffold286","Pad_R007056_Scaffold286","Pad_R007084_Scaffold286","Pad_R007143_Scaffold289","Pad_R007466_Scaffold3","Pad_R007561_Scaffold303","Pad_R007588_Scaffold304","Pad_R007642_Scaffold306","Pad_R007698_Scaffold308","Pad_R007787_Scaffold314","Pad_R007990_Scaffold32","Pad_R008005_Scaffold32","Pad_R008242_Scaffold34","Pad_R008771_Scaffold36","Pad_R009304_Scaffold383","Pad_R009392_Scaffold387","Pad_R009434_Scaffold39","Pad_R009560_Scaffold399","Pad_R009639_Scaffold4","Pad_R009654_Scaffold4","Pad_R009712_Scaffold4","Pad_R009883_Scaffold40","Pad_R010212_Scaffold411","Pad_R010264_Scaffold413","Pad_R010409_Scaffold42","Pad_R010811_Scaffold442","Pad_R011174_Scaffold458","Pad_R011504_Scaffold485","Pad_R011713_Scaffold51","Pad_R011742_Scaffold513","Pad_R011745_Scaffold513"
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
