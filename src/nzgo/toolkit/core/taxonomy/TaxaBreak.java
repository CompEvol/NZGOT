package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.taxonomy.parser.EFetchStAXParser;
import nzgo.toolkit.core.util.NameUtil;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Taxa Break
 * @author Walter Xie
 */
public class TaxaBreak {

    protected Rank rankToBreak;
    protected final Taxa taxa;
    protected Taxa taxaOnRank;

    public TaxaBreak(Taxa taxa, Rank rankToBreak) {
        this.taxa = taxa;
        setRankToBreak(rankToBreak);
    }

    public Rank getRankToBreak() {
        return rankToBreak;
    }

    public void setRankToBreak(Rank rankToBreak) {
        if (rankToBreak == null || rankToBreak == Rank.NO_RANK)
            throw new IllegalArgumentException("Please give a correct taxonomic rank !");
        this.rankToBreak = rankToBreak;
    }

    /**
     * create taxa break table by a given rank
     * @param workPath
     * @throws java.io.IOException
     * @throws javax.xml.stream.XMLStreamException
     */
    public void writeTaxaBreakTable(String workPath) throws IOException, XMLStreamException {
        List<String> taxaOnGivenRank = new ArrayList<>();
        MyLogger.info("\n" + taxa.size() + " Taxa extracted from tree tips labels : ");

        String outputFilePath = workPath + "taxaBreakTable.txt";
        BufferedWriter out = new BufferedWriter(new FileWriter(outputFilePath));
        // column head
        out.write("# taxa\tguess\t" + rankToBreak + "\n");
        for (Object name : taxa) {
            // 1st column
            out.write(name.toString());
            List<Taxon> taxonList = EFetchStAXParser.getTaxonByName(name.toString());
            // 2nd column to guess correct name
            // try prefix, such as Cotesia_ruficrus
            if (taxonList.size() < 1) {
                String prefix = NameUtil.getPrefix(name.toString(), "_");
                if (!prefix.contentEquals(name.toString())) {
                    taxonList = EFetchStAXParser.getTaxonByName(prefix);
                    out.write("\t" + prefix);
                    // 3rd column, or more if multi-result
                    for (Taxon taxon : taxonList) {
                        Taxon t = taxon.getParentTaxonOn(rankToBreak);
                        out.write("\t" + t);
                        if (t != null && !taxaOnGivenRank.contains(t.getScientificName())) {
                            taxaOnGivenRank.add(t.getScientificName());
                        }
                    }
                } else {
                    out.write("\t");
                }
            }
            // 2nd column empty
            // 3rd column, or more if multi-result
            for (Taxon taxon : taxonList) {
                Taxon t = taxon.getParentTaxonOn(rankToBreak);
                out.write("\t\t" + t);
                if (t != null && !taxaOnGivenRank.contains(t.getScientificName())) {
                    taxaOnGivenRank.add(t.getScientificName());
                }
            }

            out.write("\n");
        }

        out.flush();
        out.close();

        // 2nd output file for summary
        outputFilePath = workPath + "taxaBreakSummary.txt";
        out = new BufferedWriter(new FileWriter(outputFilePath));

        out.write("# " + taxa.size() + " taxa belong to " + taxaOnGivenRank.size() + " " + rankToBreak + "s\n");
        for (String t : taxaOnGivenRank) {
            out.write(t + "\n");
        }

        out.flush();
        out.close();

    }

}
