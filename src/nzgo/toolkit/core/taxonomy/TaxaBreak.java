package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameParser;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.taxonomy.parser.EFetchStAXParser;
import nzgo.toolkit.core.util.Element;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Taxa Break
 * @author Walter Xie
 */
public class TaxaBreak {

    public static final String OTHER = "Unidentified"; //"Other";
    public static final String MULTI_RESULT = "Multi-result";
    public static final String ERROR = "Error"; // not belong to bioClassification

    protected final Taxa taxa;

    protected Rank rankToBreak;
    protected Taxon bioClassification;
    //    protected Taxa<String> taxaOnRank = new Taxa<>();
    protected boolean checkPrefix = true;

    public Map<String, String> taxaBreakMap = new TreeMap<>();
    public Map<String, String> guessTaxaMap = new TreeMap<>();


    public TaxaBreak(Taxa taxa, Rank rankToBreak) {
        this.taxa = taxa;
        setRankToBreak(rankToBreak);
    }

    public void setCheckPrefix(boolean checkPrefix) {
        this.checkPrefix = checkPrefix;
    }

    public Rank getRankToBreak() {
        return rankToBreak;
    }

    public void setRankToBreak(Rank rankToBreak) {
        if (rankToBreak == null || rankToBreak == Rank.NO_RANK)
            throw new IllegalArgumentException("Please give a correct taxonomic rank !");
        this.rankToBreak = rankToBreak;
    }

    public Taxon getBioClassification() {
        return bioClassification;
    }

    public void setBioClassification(Taxon bioClassification) {
        this.bioClassification = bioClassification;
    }

    public void getTaxaBreakMap() throws IOException, XMLStreamException {
        taxaBreakMap.clear();

        for (Object name : taxa) {
            List<Taxon> taxonList = EFetchStAXParser.getTaxonByName(name.toString());

            // try prefix, such as Cotesia_ruficrus
            if (taxonList.size() < 1) {
                if (checkPrefix) {
                    String prefix = NameParser.getPrefix(name.toString(), "_");
                    if (!prefix.contentEquals(name.toString())) {
                        taxonList = EFetchStAXParser.getTaxonByName(prefix);
                        guessTaxaMap.put(name.toString(), prefix);
                        putTaxaBreakMap(name.toString(), taxonList);
                    }
                }
            } else {
                putTaxaBreakMap(name.toString(), taxonList);
            }
        }
    }

    protected void putTaxaBreakMap(String queryTaxon, List<Taxon> taxonList) {
        if (taxonList.size() < 1) {
            taxaBreakMap.put(queryTaxon, OTHER);
            return;
        }

        Taxon taxon = taxonList.get(0);

        if (taxonList.size() > 1) {
            for (int i = 1; i < taxonList.size(); i++) {
                if (!taxon.taxIdEquals(taxonList.get(i))) {
                    taxaBreakMap.put(queryTaxon, MULTI_RESULT);
                }
            }
        }

        // filter out taxon not belong to bioClassification, but always true if bioClassification == null
        if (taxon != null && taxon.belongsTo(bioClassification)) {
            Taxon t = taxon.getParentTaxonOn(rankToBreak);
            if (t != null) {
                taxaBreakMap.put(queryTaxon, t.getScientificName());
            } else {
                taxaBreakMap.put(queryTaxon, ERROR);
            }
        }
    }

    public Taxa<String> getTaxaOnRank() {
        Taxa<String> taxaOnRank = new Taxa<>();
        for (String queryTaxon : taxaBreakMap.keySet()) {
            String taxonOnRank = taxaBreakMap.get(queryTaxon);
            if (!taxaOnRank.contains(taxonOnRank)) {
                taxaOnRank.add(taxonOnRank);
            }
        }
        return taxaOnRank;
    }

    /**
     * create taxa break table by a given rank
     * @param workPath
     * @throws java.io.IOException
     * @throws javax.xml.stream.XMLStreamException
     */
    public void writeTaxaBreakTable(String workPath) throws IOException, XMLStreamException {
        MyLogger.info("\n" + taxa.size() + " Taxa extracted from tree tips labels : ");

        String outputFilePath = workPath + "taxaBreakTable" + NameSpace.POSTFIX_TSV;
        BufferedWriter out = new BufferedWriter(new FileWriter(outputFilePath));
        // column head
        out.write("# count\ttaxa\tguess\t" + rankToBreak + "\n");
        for (Object name : taxa) {
            // 1st column
            if (name instanceof Element)
                out.write(((Element) name).getCount() + "\t");
            // 2nd column
            out.write(name.toString());

            // 3rd column to guess correct name
            if (checkPrefix) {
                out.write("\t");
                if (guessTaxaMap.containsKey(name.toString())) {
                    out.write(guessTaxaMap.get(name.toString()));
                }
            }

            // 4th column, or more if multi-result
            out.write("\t");
            if (taxaBreakMap.containsKey(name.toString()))  {
                out.write(taxaBreakMap.get(name.toString()));
            }
            out.write("\n");
        }

        out.flush();
        out.close();

        // 2nd output file for summary
        outputFilePath = workPath + "taxaBreakSummary" + NameSpace.POSTFIX_TSV;
        out = new BufferedWriter(new FileWriter(outputFilePath));

        Taxa<String> taxaOnRank = getTaxaOnRank();
        out.write("# " + taxa.size() + " taxa belong to " + taxaOnRank.size() + " " + rankToBreak + "s\n");
        for (String t : taxaOnRank) {
            out.write(t + "\n");
        }

        out.flush();
        out.close();

    }

//    protected void writeParentTaxon(String queryTaxon, List<Taxon> taxonList, BufferedWriter out) throws IOException {
//        for (Taxon taxon : taxonList) {
//            // filter out taxon not belong to bioClassification, but always true if bioClassification == null
//            if (taxon.belongsTo(bioClassification)) {
//                Taxon t = taxon.getParentTaxonOn(rankToBreak);
//                out.write("\t" + t);
//                if (t != null) {
//                    taxaBreakMap.put(queryTaxon, t.getScientificName());
//                } else {
//                    taxaBreakMap.put(queryTaxon, OTHER);
//                }
//            }
//        }
//    }

}
