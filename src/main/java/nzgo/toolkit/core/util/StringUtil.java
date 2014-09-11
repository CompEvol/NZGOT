package nzgo.toolkit.core.util;

/**
 * ArrayUtil
 * @author Walter Xie
 */
public class StringUtil {

    public static String getStringExclude(String original, String regex) {
        return original.replaceAll(regex, "");
    }
}
