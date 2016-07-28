package nzgo.toolkit.core.sequences;

import jebl.evolution.sequences.GeneticCode;

/**
 * Genetic Code Util
 * @author Walter Xie
 */
public class GeneticCodeUtil {

    public static String[] getGeneticCodeNames() {
        GeneticCode[] geneticCodesArray = GeneticCode.getGeneticCodesArray();
        String[] names = new String[geneticCodesArray.length];
        for (int i = 0; i < geneticCodesArray.length; i++) {
            names[i] = geneticCodesArray[i].getName();
        }
        return names;
    }

    public static void printGeneticCodes() {
        System.out.println("  Available genetic codes are :");
        System.out.println("  Name\tDescription\tCode Table\tNCBI Translation Table Number");
        for (GeneticCode geneticCodes : GeneticCode.getGeneticCodes()) {
            System.out.println("  " + geneticCodes.getName() + "\t" + geneticCodes.getDescription() +
                    "\t" + geneticCodes.getCodeTable() + "\t" + geneticCodes.getNcbiTranslationTableNumber());
        }
        System.out.println();
    }
}
