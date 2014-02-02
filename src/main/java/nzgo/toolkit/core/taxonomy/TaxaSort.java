package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.Separator;
import nzgo.toolkit.core.taxonomy.parser.EFetchStAXParser;
import nzgo.toolkit.core.util.Element;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Taxa Sort
 * @author Walter Xie
 */
public class TaxaSort {

    private final Separator errMsgSeparator = new Separator("\t"); // for multi-column error message
    protected final Taxa taxa;

    protected Rank rankToBreak;
    // the high level taxonomy of biological classification that all given taxa should belong to
    // e.g. new Taxon("Insecta", "50557");
    // or EFetchStAXParser.getTaxonById("50557");
    protected Taxon bioClass;
    private final Separator regexPrefixSeparator;

    public Map<String, String> taxaSortMap = new TreeMap<>();
    public Map<String, String> errors = new TreeMap<>();


    public TaxaSort(Taxa taxa, Rank rankToBreak, Taxon bioClass) {
        this(taxa, rankToBreak, "_", bioClass);
    }

    /**
     *
     * @param taxa
     * @param rankToBreak
     * @param regexPrefix
     * @param bioClass
     */
    public TaxaSort(Taxa taxa, Rank rankToBreak, String regexPrefix, Taxon bioClass) {
        this.taxa = taxa;
        setRankToBreak(rankToBreak);
        if (regexPrefix == null) {
            regexPrefixSeparator = null;
        } else {
            regexPrefixSeparator = new Separator(regexPrefix);
        }
        setBioClass(bioClass);
    }

    public Map<String, String> getTaxaSortMap() throws IOException, XMLStreamException {
        fillTaxaSortMap();
        return taxaSortMap;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    protected void fillTaxaSortMap() throws IOException, XMLStreamException {
        taxaSortMap.clear();
        errors.clear();

        for (Object name : taxa) {
            List<Taxon> taxonList = EFetchStAXParser.getTaxonByName(name.toString());

            // try prefix, such as Cotesia_ruficrus
            if (taxonList.size() < 1 && regexPrefixSeparator != null) {
                String prefix = regexPrefixSeparator.getPrefix(name.toString());
                if (!prefix.contentEquals(name.toString())) {
                    taxonList = EFetchStAXParser.getTaxonByName(prefix);

                    fillTaxon(name.toString(), taxonList, prefix);
                } else {
                    // no prefix
                    fillTaxon(name.toString(), taxonList, null);
                }

            } else {
                fillTaxon(name.toString(), taxonList, null);
            }
        }
    }

    protected void fillTaxon(String queryTaxon, List<Taxon> taxonList, String prefix) {

        if (taxonList.size() < 1) {
            String errMsg = appendPrefixMsg(Error.UNIDENTIFIED.toString(), prefix);
            errors.put(queryTaxon, errMsg);
            taxaSortMap.put(queryTaxon, Error.UNIDENTIFIED.toString());
            return;
        }

        Taxon taxon = taxonList.get(0);
        if (taxonList.size() > 1) {
            List<String> items = new ArrayList<>();
            items.add(taxon.getScientificName());
            for (int i = 1; i < taxonList.size(); i++) {
                if (!taxon.taxIdEquals(taxonList.get(i))) {
                    items.add(taxonList.get(i).getScientificName());
                }
            }
            if (items.size() > 1) {
                items.add(0, Error.MULTI_RESULT.toString());
                appendPrefixMsg(items, prefix);
                String errMsg = errMsgSeparator.getLabel(items);
                errors.put(queryTaxon, errMsg);
                taxaSortMap.put(queryTaxon, Error.UNIDENTIFIED.toString());
            } else {
                // if multi-result give a same taxon on the given rank, then it is still valid
                taxaSortMap.put(queryTaxon, taxon.getScientificName());
            }

        } else { // == 1
            if (taxon == null) {
                String errMsg = appendPrefixMsg(Error.UNIDENTIFIED.toString(), prefix);
                errors.put(queryTaxon, errMsg);
                taxaSortMap.put(queryTaxon, Error.UNIDENTIFIED.toString());

            } else if (!taxon.belongsTo(bioClass)) {
                // filter out taxon not belong to bioClass, but always true if bioClass == null
                String errMsg = appendPrefixMsg(Error.NOT_IN_BIO_CLASS.toString(), prefix);
                errors.put(queryTaxon, errMsg);
                taxaSortMap.put(queryTaxon, Error.UNIDENTIFIED.toString());

            } else if (taxon.getRank().compareTo(rankToBreak) > 0) {
                // taxon rank is higher than rankToBreak
                String errMsg = appendPrefixMsg(Error.HIGHER_RANK.toString(), prefix);
                errors.put(queryTaxon, errMsg);
                taxaSortMap.put(queryTaxon, taxon.getScientificName());
            } else {
                // normal cases
                Taxon t = taxon.getParentTaxonOn(rankToBreak);
                if (t == null) {
                    String errMsg = appendPrefixMsg(Error.NO_RANK.toString(), prefix);
                    errors.put(queryTaxon, errMsg);
                    taxaSortMap.put(queryTaxon, Error.UNIDENTIFIED.toString());
                } else {
                    taxaSortMap.put(queryTaxon, t.getScientificName());
                }
            }
        }
    }

    public String appendPrefixMsg(String msg, String prefix) {
        if (prefix != null) {
            List<String> items = new ArrayList<>();
            items.add(msg);
            items.add(Error.PREFIX.toString());
            items.add(prefix);
            return errMsgSeparator.getLabel(items);
        }
        return msg;
    }

    public void appendPrefixMsg(List<String> items, String prefix) {
        if (prefix != null) {
            items.add(Error.PREFIX.toString());
            items.add(prefix);
        }
    }

    public Taxa<String> getTaxaOnRank() {
        Taxa<String> taxaOnRank = new Taxa<>();
        for (String queryTaxon : taxaSortMap.keySet()) {
            String taxonOnRank = taxaSortMap.get(queryTaxon);
            if (!taxaOnRank.contains(taxonOnRank)) {
                taxaOnRank.add(taxonOnRank);
            }
        }
        return taxaOnRank;
    }

    /**
     * create taxa sort table by a given rank
     * @param workPath
     * @throws java.io.IOException
     * @throws javax.xml.stream.XMLStreamException
     */
    public void writeTaxaSortTable(String workPath) throws IOException, XMLStreamException {
        fillTaxaSortMap();

        MyLogger.info("\n" + taxa.size() + " Taxa extracted from tree tips labels : ");

        String outputFilePath = workPath + "taxaSortTable" + NameSpace.SUFFIX_TSV;
        BufferedWriter out = new BufferedWriter(new FileWriter(outputFilePath));
        // column head
        out.write("# count\ttaxa\t" + rankToBreak + "\terror\n");
        for (Object name : taxa) {
            // 1st column count
            if (name instanceof Element)
                out.write(((Element) name).getCount() + "\t");
            // 2nd column taxa
            out.write(name.toString());

            // 3rd column to guess correct name
            out.write("\t");
            if (taxaSortMap.containsKey(name.toString()))  {
                out.write(taxaSortMap.get(name.toString()));
            }

            // 4th column error detail, may be multi columns separated by \t
            if (errors.size() > 1) {
                out.write("\t");
                if (errors.containsKey(name.toString())) {
                    out.write(errors.get(name.toString()));
                }
            }

            out.write("\n");
        }

        out.flush();
        out.close();

        // 2nd output file for summary
        outputFilePath = workPath + "taxaSortSummary" + NameSpace.SUFFIX_TSV;
        out = new BufferedWriter(new FileWriter(outputFilePath));

        Taxa<String> taxaOnRank = getTaxaOnRank();
        out.write("# " + taxa.size() + " taxa belong to " + taxaOnRank.size() + " " + rankToBreak + "s\n");
        for (String t : taxaOnRank) {
            out.write(t + "\n");
        }

        out.flush();
        out.close();

    }

    public Rank getRankToBreak() {
        return rankToBreak;
    }

    public void setRankToBreak(Rank rankToBreak) {
        if (rankToBreak == null || rankToBreak == Rank.NO_RANK)
            throw new IllegalArgumentException("Please give a correct taxonomic rank !");
        this.rankToBreak = rankToBreak;
    }

    public Taxon getBioClass() {
        return bioClass;
    }

    public void setBioClass(Taxon bioClass) {
        this.bioClass = bioClass;
    }


//    protected void writeParentTaxon(String queryTaxon, List<Taxon> taxonList, BufferedWriter out) throws IOException {
//        for (Taxon taxon : taxonList) {
//            // filter out taxon not belong to bioClass, but always true if bioClass == null
//            if (taxon.belongsTo(bioClass)) {
//                Taxon t = taxon.getParentTaxonOn(rankToBreak);
//                out.write("\t" + t);
//                if (t != null) {
//                    taxaSortMap.put(queryTaxon, t.getScientificName());
//                } else {
//                    taxaSortMap.put(queryTaxon, OTHER);
//                }
//            }
//        }
//    }

    public boolean isError(String type) {
        for (Error e : Error.class.getEnumConstants()) {
            if (e.toString().equalsIgnoreCase(type))
                return true;
        }
        return false;
    }

    public static enum Error {
        UNIDENTIFIED ("unidentified", "unidentified taxon"),
        MULTI_RESULT ("multi-result", "multi-result for a given taxon name"),
        PREFIX       ("prefix", "use the prefix of taxon name"),
        NO_RANK      (Rank.NO_RANK.toString(), "no taxonomy at the given rank"),
        HIGHER_RANK      ("higher rank", "taxon rank is higher than the given rank"),
        NOT_IN_BIO_CLASS ("not in", "taxon does not belong to the given biological classification"),
        ERROR        ("error", "error"),
        OTHER        ("other", "other");

        private String type;
        private String msg;

        private Error(String type, String msg) {
            this.type = type;
            this.msg = msg;
        }

        public String getMsg() {
            return msg;
        }

        @Override
        public String toString() {
            return type;
        }

        public static String[] valuesToString() {
            return Arrays.copyOf(TaxaSort.Error.values(), TaxaSort.Error.values().length, String[].class);
        }

    }


}
