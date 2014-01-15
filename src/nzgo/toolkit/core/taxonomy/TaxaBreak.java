package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.taxonomy.parser.EFetchStAXParser;
import nzgo.toolkit.core.util.Element;
import nzgo.toolkit.core.util.NameSpace;
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

    protected final Taxa taxa;

    protected Rank rankToBreak;
    protected Taxon bioClassification;
    //    protected Taxa taxaOnRank; // TODO equivalent List taxaOnGivenRank
    protected List<String> taxaOnGivenRank = new ArrayList<>();

    public TaxaBreak(Taxa taxa, Rank rankToBreak) {
        this.taxa = taxa;
        setRankToBreak(rankToBreak);
    }

    public List<String> getTaxaOnGivenRank() {
        return taxaOnGivenRank;
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

    /**
     * create taxa break table by a given rank
     * @param workPath
     * @throws java.io.IOException
     * @throws javax.xml.stream.XMLStreamException
     */
    public void writeTaxaBreakTable(String workPath) throws IOException, XMLStreamException {
        taxaOnGivenRank.clear();
        MyLogger.info("\n" + taxa.size() + " Taxa extracted from tree tips labels : ");

        String outputFilePath = workPath + "taxaBreakTable" + NameSpace.POSTFIX_TSV;
        BufferedWriter out = new BufferedWriter(new FileWriter(outputFilePath));
        // column head
        out.write("# count\ttaxa\tguess\t" + rankToBreak + "\n");
        for (Object name : taxa) {
            // 0th column
            if (name instanceof Element)
                out.write(((Element) name).getCount() + "\t");
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
                    writeParentTaxon(taxonList, taxaOnGivenRank, out);
                } else {
                    out.write("\t");
                }
            } else {
                // 2nd column empty
                // 3rd column, or more if multi-result
                out.write("\t");
                writeParentTaxon(taxonList, taxaOnGivenRank, out);
            }
            out.write("\n");
        }

        out.flush();
        out.close();

        // 2nd output file for summary
        outputFilePath = workPath + "taxaBreakSummary" + NameSpace.POSTFIX_TSV;
        out = new BufferedWriter(new FileWriter(outputFilePath));

        out.write("# " + taxa.size() + " taxa belong to " + taxaOnGivenRank.size() + " " + rankToBreak + "s\n");
        for (String t : taxaOnGivenRank) {
            out.write(t + "\n");
        }

        out.flush();
        out.close();

    }

    protected void writeParentTaxon(List<Taxon> taxonList, List<String> taxaOnGivenRank, BufferedWriter out) throws IOException {
        for (Taxon taxon : taxonList) {
            // filter out taxon not belong to bioClassification, but always true if bioClassification == null
            if (taxon.belongsTo(bioClassification)) {
                Taxon t = taxon.getParentTaxonOn(rankToBreak);
                out.write("\t" + t);
                if (t != null && !taxaOnGivenRank.contains(t.getScientificName())) {
                    taxaOnGivenRank.add(t.getScientificName());
                }
            }
        }
    }

}
