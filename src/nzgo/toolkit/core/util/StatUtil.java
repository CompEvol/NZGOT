package nzgo.toolkit.core.util;

/**
 * StatUtil
 * @author Walter Xie
 */
public class StatUtil {
    //TODO generic?
    public static double min(double number1, double number2) {
        return number1 < number2 ? number1 : number2;
    }

    public static double max(double number1, double number2) {
        return number1 > number2 ? number1 : number2;
    }

    public static int min(int number1, int number2) {
        return number1 < number2 ? number1 : number2;
    }

    public static int max(int number1, int number2) {
        return number1 > number2 ? number1 : number2;
    }
}
