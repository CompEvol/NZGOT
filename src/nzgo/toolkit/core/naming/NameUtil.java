package nzgo.toolkit.core.naming;

/**
 * NameUtil
 * @author Walter Xie
 */
public class NameUtil {

    public static boolean isNumber(String label) {
        for (char c : label.toCharArray()) {
            if (!Character.isDigit(c))
                return false;
        }
        return true;
    }

}
