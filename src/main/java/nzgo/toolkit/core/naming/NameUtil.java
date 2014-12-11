package nzgo.toolkit.core.naming;

/**
 * NameUtil
 * @author Walter Xie
 */
public class NameUtil {

    /**
     *
     * @param fileName
     * @param fileNameExtension   contain dot, such as ".fasta" or ".csv" in nzgo.toolkit.core.naming.NameSpace
     * @return
     */
    public static boolean hasFileExtension(String fileName, String... fileNameExtension) {
        for (String fnex : fileNameExtension) {
            if (fileName.toLowerCase().endsWith(fnex.toLowerCase()))
                return true;
        }
        return false;
    }

    public static void validateFileExtension(String fileName, String... fileNameExtension) {
        if (!NameUtil.hasFileExtension(fileName, fileNameExtension))
            throw new IllegalArgumentException("Invalid file name extension : " + fileName);
    }

    public static String getNameNoExtension(String fileName) {
        if (fileName == null)
            return null;

        int dot = fileName.lastIndexOf(".");

        if (dot < 0)
            return fileName;

        return fileName.substring(0, dot);
    }

    public static String getSuffix (String fileName) {
        if (fileName == null)
            return null;

        int dot = fileName.lastIndexOf(".");

        if (dot < 0)
            return fileName;

        return fileName.substring(dot);
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

    public static boolean hasSameItem(String label1, int index1, String label2, int index2, String regex) {
        String[] items1 = label1.split(regex, -1);
        String[] items2 = label2.split(regex, -1);
        return index1 < items1.length && index2 < items2.length && items1[index1].contentEquals(items2[index2]);
    }

}
