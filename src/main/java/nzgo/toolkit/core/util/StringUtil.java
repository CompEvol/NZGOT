package nzgo.toolkit.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StringUtil
 * @author Walter Xie
 */
public class StringUtil {

    public static String getStringExclude(String original, String regex) {
        return original.replaceAll(regex, "");
    }

    public static String getRow(Object[] elements) {
        return getRow(elements, "\t");
    }

    public static String getRow(Object[] elements, String delimiter) {
        assert elements != null && elements.length > 1;
        String row = elements[0].toString();
        for (int i = 1; i < elements.length; i++) {
            row += delimiter + elements[i].toString();
        }
        row += "\n";
        return row;
    }

    public static boolean contains(String original, String[] keywords) {
        for (String keyw : keywords) {
            if (original.contains(keyw))
                return true;
        }
        return false;
    }

    /**
     * return substring between 2 given substring
     * @param wholeStr
     * @param subStr1
     * @param subStr2
     * @return
     */
    public static String substringBetween(String wholeStr, String subStr1, String subStr2) {
        Pattern pattern = Pattern.compile(subStr1 + "(.*?)" + subStr2);
        Matcher matcher = pattern.matcher(wholeStr);
        matcher.find();
//        while (matcher.find()) { //TODO multi-match
            return matcher.group(1);
//        }
    }


    public static List<String> matchedSubstring(String wholeStr, String patternStr) {
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(wholeStr);

        List<String> matchedList = new ArrayList<>();
        while(matcher.find()) {
            matchedList.add(matcher.group(1));
        }
        return matchedList;
    }
}
