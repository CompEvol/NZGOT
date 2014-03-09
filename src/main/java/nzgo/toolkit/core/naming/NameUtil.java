package nzgo.toolkit.core.naming;

/**
 * NameUtil
 * @author Walter Xie
 */
public class NameUtil {

    public static String getNameWithoutExtension (String fileName) {
        if (fileName == null)
            return null;

        int dot = fileName.lastIndexOf(".");

        if (dot < 0)
            return fileName;

        return fileName.substring(0, dot);
    }

    public static String getPrefix(String label, String separator) {
        int index = label.indexOf(separator);
        if (index > 0)
            return label.substring(0, index);
        return label;
    }

    public static boolean isNumber(String label) {
        for (char c : label.toCharArray()) {
            if (!Character.isDigit(c))
                return false;
        }
        return true;
    }

    public static boolean isEmptyNull(String item) {
        return item == null || "null".equalsIgnoreCase(item) || item.trim().isEmpty();
    }

    public static String getWordCharacters(String name) {
        return name.replaceAll("\\W", "");
    }

    public static boolean endsWith(String name, String[] suffixes) {
        for (String suffix : suffixes) {
            if (name.endsWith(suffix))
                return true;
        }
        return false;
    }
}
