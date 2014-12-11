package nzgo.toolkit.core.io;

import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.Regex;
import nzgo.toolkit.core.naming.RegexFactory;
import nzgo.toolkit.core.naming.Separator;
import nzgo.toolkit.core.pipeline.Module;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Config FileIO
 * @author Walter Xie
 */
public class ConfigFileIO extends FileIO {

    /**
     * 2-column mapping file, 1st column is key, 2nd is value
     * @param twoColumnTSV
     * @param desc            description for 2-column mapping file
     * @return
     * @throws IOException
     */
    public static SortedMap<String, String> importTwoColumnTSV (Path twoColumnTSV, String desc) throws IOException {
        Module.validateFileName(twoColumnTSV.getFileName().toString(), "", NameSpace.SUFFIX_TSV, NameSpace.SUFFIX_TXT);

        SortedMap<String, String> twoColumnMap = new TreeMap<>();
        BufferedReader reader = getReader(twoColumnTSV, desc);

        String line = reader.readLine();
        while (line != null) {
            if (hasContent(line)) { // not comments or empty
                String[] items = lineParser.getSeparator(0).parse(line);
                if (items.length < 2)
                    throw new IllegalArgumentException("Invalid file format: " + desc + ", line = " + line);
                if (twoColumnMap.containsKey(items[0]))
                    throw new IllegalArgumentException("Find duplicate name in the 1st column : " + items[0]);

                twoColumnMap.put(items[0], items[1]);
            }

            line = reader.readLine();
        }
        reader.close();

        if (twoColumnMap.size() < 1)
            throw new IllegalArgumentException("It needs at least one valid row in " + twoColumnTSV);

        return twoColumnMap;
    }

    public static List<Regex> importRegex (Path regexTSV, RegexFactory.RegexType regexType) throws IOException {
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

    public static void writeTSVFileFromMap(Path configMapTSV, Map<String, String> map, String msg) throws IOException {
        BufferedWriter writer = getWriter(configMapTSV, msg);

//        writer.write("# \n");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            writer.write(entry.getKey() + "\t" + entry.getValue() + "\n");
        }

        writer.flush();
        writer.close();
    }

}
