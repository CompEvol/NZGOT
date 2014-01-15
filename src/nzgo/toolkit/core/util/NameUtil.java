package nzgo.toolkit.core.util;

/**
 * NameUtil
 * @author Walter Xie
 */
public class NameUtil {

    public static String getPrefix(String name, String separator) {
        int index = name.indexOf(separator);
        if (index > 0)
            return name.substring(0, index);
        return name;
    }

    public static boolean isNumber(String label) {
        for (char c : label.toCharArray()) {
            if (!Character.isDigit(c))
                return false;
        }
        return true;
    }

}
