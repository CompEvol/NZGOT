package nzgo.toolkit.core.io;

import nzgo.toolkit.core.naming.Regex;
import nzgo.toolkit.core.naming.RegexFactory;
import nzgo.toolkit.core.naming.RegexType;
import nzgo.toolkit.core.naming.Separator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Config FileIO
 * @author Walter Xie
 */
public class ConfigFileIO extends FileIO {

    public static List<Regex> importRegex (Path regexTSV, RegexType regexType) throws IOException {
        List<Regex> regexList = new ArrayList<>();
        RegexFactory regexFactory = new RegexFactory(regexType);

        BufferedReader reader = getReader(regexTSV, "customized " + regexType.toString() + "s");

        Separator lineSeparator = new Separator("\t");
        String line = reader.readLine();
        while (line != null) {
            if (hasContent(line)) { // not comments or empty
                String[] items = lineSeparator.parse(line);

                Regex regex = regexFactory.getRegex(items[0]);
                if (items.length > 1) {
                    regexFactory.setValue(regex, items[1]);
                }
                regexList.add(regex);
            }

            line = reader.readLine();
        }
        reader.close();

        if (regexList.size() < 1)
            throw new IllegalArgumentException("It needs at least one " + regexType.toString() + " !");

        return regexList;
    }
//
//    public static List<Separator> importSeparators (Path separatorsTSV) throws IOException {
//        List<Separator> separators = new ArrayList<>();
//        BufferedReader reader = getReader(separatorsTSV, "customized separators");
//
//        Separator lineSeparator = new Separator("\t");
//        String line = reader.readLine();
//        while (line != null) {
//            if (hasContent(line)) { // not comments or empty
//                String[] items = lineSeparator.parse(line);
//
//                Separator separator = new Separator(items[0]);
//                if (items.length > 1) {
////                    if (!NameUtil.isNumber(items[1]))
////                        throw new IllegalArgumentException("The 2nd column is not integer of line : " + line);
//                    int splitIndex = Integer.parseInt(items[1]);
//                    separator.setSplitIndex(splitIndex);
//
//                }
//                separators.add(separator);
//            }
//
//            line = reader.readLine();
//        }
//        reader.close();
//
//        if (separators.size() < 1)
//            throw new IllegalArgumentException("It needs at least one separator !");
//
//        return separators;
//    }
//
//    public static List<Matcher> importMatchers (Path matchersTSV) throws IOException {
//        List<Matcher> matchers = new ArrayList<>();
//        BufferedReader reader = getReader(matchersTSV, "customized matchers");
//
//        Separator lineSeparator = new Separator("\t");
//        String line = reader.readLine();
//        while (line != null) {
//            if (hasContent(line)) { // not comments or empty
//                String[] items = lineSeparator.parse(line);
//
//                Matcher matcher = new Matcher(items[0]);
//                if (items.length > 1) {
//                    matcher.setName(items[1]);
//                }
//                matchers.add(matcher);
//            }
//
//            line = reader.readLine();
//        }
//        reader.close();
//
//        if (matchers.size() < 1)
//            throw new IllegalArgumentException("It needs at least one matcher !");
//
//        return matchers;
//    }

    public static Map<String, String> importPreTaxaTraits (Path traitsMapTSV) throws IOException {
        Map<String, String> preTaxaTraits = new TreeMap<>();
        BufferedReader reader = getReader(traitsMapTSV, "pre-defined taxa traits mapping");

        Separator lineSeparator = new Separator("\t");
        String line = reader.readLine();
        while (line != null) {
            if (hasContent(line)) { // not comments or empty
                String[] items = lineSeparator.parse(line);
                if (items.length < 2)
                    throw new IllegalArgumentException("Invalid file format for taxa traits mapping, line : " + line);
                if (preTaxaTraits.containsKey(items[0]))
                    throw new IllegalArgumentException("Find duplicate name for leaf node : " + items[0]);

                preTaxaTraits.put(items[0], items[1]);
            }

            line = reader.readLine();
        }
        reader.close();

        if (preTaxaTraits.size() < 1)
            throw new IllegalArgumentException("It needs at least one separator !");

        return preTaxaTraits;
    }

    public static void writeTaxaTraits (Path traitsMapTSV, String[][] taxaTraits) throws IOException {
        BufferedWriter writer = getWriter(traitsMapTSV, "taxa traits map");

//        writer.write("# \n");
        for (int i = 0; i < taxaTraits.length; i++) {
            writer.write(taxaTraits[i][0] + "\t" + taxaTraits[i][1] + "\n");
        }

        writer.flush();
        writer.close();

    }


}
