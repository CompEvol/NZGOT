package nzgo.toolkit.core.taxonomy;

import nzgo.toolkit.core.community.OTU;
import nzgo.toolkit.core.community.OTUs;
import nzgo.toolkit.core.io.TaxonomyFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.Separator;
import nzgo.toolkit.core.taxonomy.parser.EFetchStAXParser;
import nzgo.toolkit.core.util.Element;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Taxonomy Assignment
 * @author Walter Xie
 */
public class TaxonomicAssignment {

    protected final OTUs<OTU> otus;

    // the high level taxonomy of biological classification that all given taxa should belong to
    // e.g. new Taxon("Insecta", "50557"); or TaxonomyUtil.getTaxonByeFetch("50557");
    protected Taxon bioClass;

    protected Rank[] ranksToBreak;

    protected TaxonSet<Taxon> taxonomySet;

    /**
     * otus should be set Taxon already
     * @param otus
     */
    public TaxonomicAssignment(OTUs<OTU> otus) {
        this.otus = otus;
        assignTaxonomy();
    }

    public TaxonomicAssignment(OTUs<OTU> otus, Rank... ranksToBreak) {
        this(otus);
        setRankToBreak(ranksToBreak);
    }

    //TODO bug: keep adding
    public TaxonSet<Taxon> getTaxonomyOn(Rank rankToBreak) {
        if (taxonomySet == null)
            throw new IllegalArgumentException("Taxonomy set is not assigned ! Please run assignTaxonomy() first !");

        TaxonSet<Taxon> taxonSetOnRank = new TaxonSet<>();
        for (Taxon taxon : taxonomySet) {
            Taxon taxonToBreak = taxon.getParentTaxonOn(rankToBreak);

            if (taxonToBreak == null)
                throw new IllegalArgumentException("Taxon " + taxon.getScientificName() +
                        "(" + taxon.getRank() + ") has no parent on rank " + rankToBreak);

            if (taxonSetOnRank.containsTaxon(taxonToBreak.toString())) {
                Taxon taxonAssigned = taxonSetOnRank.getTaxon(taxonToBreak.toString());
                taxonAssigned.getCounter(OTUs.READS_COUNTER_ID).incrementCount(taxon.getCounter(OTUs.READS_COUNTER_ID).getCount());
                taxonAssigned.getCounter(OTUs.OTUS_COUNTER_ID).incrementCount(taxon.getCounter(OTUs.OTUS_COUNTER_ID).getCount());
            } else if (taxon.isSameAs(taxonToBreak)) {
                taxonSetOnRank.addTaxon(taxon);
            } else {
                if (taxonToBreak.getCountersSize() < 2)
                    taxonToBreak.addCounter(); // add 2nd counter for number of otu
                taxonToBreak.getCounter(OTUs.READS_COUNTER_ID).setCount(taxon.getCounter(OTUs.READS_COUNTER_ID).getCount());
                taxonToBreak.getCounter(OTUs.OTUS_COUNTER_ID).setCount(taxon.getCounter(OTUs.OTUS_COUNTER_ID).getCount());
                taxonSetOnRank.addTaxon(taxonToBreak);
            }
        }

        return taxonSetOnRank;
    }

    protected void assignTaxonomy() {
        taxonomySet = otus.getTaxonomy();
    }

    /**
     * multi-output-files of Taxonomy Assignment
     * @param workPath
     * @throws IOException
     */
    public void writeTaxonomyAssignment(String workPath) throws IOException {
        Path outTAFilePath = Paths.get(workPath, TaxonomyFileIO.TAXONOMY_ASSIGNMENT + ".tsv");
        // assignment of overall
        TaxonomyFileIO.writeTaxonomyAssignment(outTAFilePath, taxonomySet, null);

        for (Rank rank : ranksToBreak) {
            // assignment of given rank
            outTAFilePath = Paths.get(workPath, TaxonomyFileIO.TAXONOMY_ASSIGNMENT + "_" + rank + ".tsv");

            TaxonSet<Taxon> taxonSetOnRank = getTaxonomyOn(rank);
            TaxonomyFileIO.writeTaxonomyAssignment(outTAFilePath, taxonSetOnRank, rank);
        }
    }




    public void setRankToBreak(Rank... ranksToBreak) {
        this.ranksToBreak = ranksToBreak;
//        for (Rank rank : ranksToBreak) {
//            if (rank == null || rank == Rank.NO_RANK)
//                throw new IllegalArgumentException("Please give a correct taxonomic rank !");
//            this.ranksToBreak = rank;
//        }
    }

    public Taxon getBioClass() {
        return bioClass;
    }

    public void setBioClass(Taxon bioClass) {
        this.bioClass = bioClass;
    }







    /************* wait to tidy up ***************/

    private Separator regexPrefixSeparator;
    private Separator errMsgSeparator = new Separator("\t"); // for multi-column error message

    // key is query Taxon, value is either Error.? or Taxon assigned on certain condition
    // such as given a rank, or given a biology classification
    @Deprecated
    public Map<String, String> taxaAssignementMap = new TreeMap<>();
    public Map<String, String> errors = new TreeMap<>();

    @Deprecated
    public TaxonomicAssignment(TaxonSet taxonomySet, Rank rankToBreak, Taxon bioClass) {
        this(taxonomySet, rankToBreak, "_", bioClass);
    }

    /**
     *
     * @param taxonomySet
     * @param rankToBreak
     * @param regexPrefix
     * @param bioClass
     */
    @Deprecated
    public TaxonomicAssignment(TaxonSet taxonomySet, Rank rankToBreak, String regexPrefix, Taxon bioClass) {
        otus = null;
        this.taxonomySet = taxonomySet;
        if (regexPrefix == null) {
            regexPrefixSeparator = null;
        } else {
            regexPrefixSeparator = new Separator(regexPrefix);
        }
        setBioClass(bioClass);
    }

    public Map<String, String> getTaxaAssignementMap() throws IOException, XMLStreamException {
        fillTaxaSortMap();
        return taxaAssignementMap;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    protected void fillTaxaSortMap() throws IOException, XMLStreamException {
        taxaAssignementMap.clear();
        errors.clear();

        for (Object name : taxonomySet) {
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
            taxaAssignementMap.put(queryTaxon, Error.UNIDENTIFIED.toString());
            return;
        }

        Taxon taxon = taxonList.get(0);
        if (taxonList.size() > 1) {
            List<String> items = new ArrayList<>();
            items.add(taxon.getScientificName());
            for (int i = 1; i < taxonList.size(); i++) {
                if (!taxon.isSameAs(taxonList.get(i))) {
                    items.add(taxonList.get(i).getScientificName());
                }
            }
            if (items.size() > 1) {
                items.add(0, Error.MULTI_RESULT.toString());
                appendPrefixMsg(items, prefix);
                String errMsg = errMsgSeparator.getLabel(items);
                errors.put(queryTaxon, errMsg);
                taxaAssignementMap.put(queryTaxon, Error.UNIDENTIFIED.toString());
            } else {
                // if multi-result give a same taxon on the given rank, then it is still valid
                taxaAssignementMap.put(queryTaxon, taxon.getScientificName());
            }

        } else { // == 1
            if (taxon == null) {
                String errMsg = appendPrefixMsg(Error.UNIDENTIFIED.toString(), prefix);
                errors.put(queryTaxon, errMsg);
                taxaAssignementMap.put(queryTaxon, Error.UNIDENTIFIED.toString());

            } else if (!taxon.belongsTo(bioClass)) {
                // filter out taxon not belong to bioClass, but always true if bioClass == null
                String errMsg = appendPrefixMsg(Error.NOT_IN_BIO_CLASS.toString(), prefix);
                errors.put(queryTaxon, errMsg);
                taxaAssignementMap.put(queryTaxon, Error.UNIDENTIFIED.toString());

            } else if (taxon.getRank().compareTo(ranksToBreak[0]) > 0) {
                // taxon rank is higher than rankToBreak
                String errMsg = appendPrefixMsg(Error.HIGHER_RANK.toString(), prefix);
                errors.put(queryTaxon, errMsg);
                taxaAssignementMap.put(queryTaxon, taxon.getScientificName());
            } else {
                // normal cases
                Taxon t = taxon.getParentTaxonOn(ranksToBreak[0]);
                if (t == null) {
                    String errMsg = appendPrefixMsg(Error.NO_RANK.toString(), prefix);
                    errors.put(queryTaxon, errMsg);
                    taxaAssignementMap.put(queryTaxon, Error.UNIDENTIFIED.toString());
                } else {
                    taxaAssignementMap.put(queryTaxon, t.getScientificName());
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

    public TaxonSet<String> getTaxaOnRank() {
        TaxonSet<String> taxonSetOnRank = new TaxonSet<>();
        for (String queryTaxon : taxaAssignementMap.keySet()) {
            String taxonOnRank = taxaAssignementMap.get(queryTaxon);
            if (!taxonSetOnRank.contains(taxonOnRank)) {
                taxonSetOnRank.add(taxonOnRank);
            }
        }
        return taxonSetOnRank;
    }

    /**
     * create taxa sort table by a given rank
     * @param workPath
     * @throws java.io.IOException
     * @throws javax.xml.stream.XMLStreamException
     */
    public void writeTaxaSortTable(String workPath) throws IOException, XMLStreamException {
        fillTaxaSortMap();

        MyLogger.info("\n" + taxonomySet.size() + " Taxa extracted from tree tips labels : ");

        String outputFilePath = workPath + "taxaSortTable" + NameSpace.SUFFIX_TSV;
        BufferedWriter out = new BufferedWriter(new FileWriter(outputFilePath));
        // column head
        out.write("# count\ttaxa\t" + ranksToBreak + "\terror\n");
        for (Object name : taxonomySet) {
            // 1st column count
            if (name instanceof Element)
                out.write(((Element) name).getCounter().getCount() + "\t");
            // 2nd column taxa
            out.write(name.toString());

            // 3rd column to guess correct name
            out.write("\t");
            if (taxaAssignementMap.containsKey(name.toString()))  {
                out.write(taxaAssignementMap.get(name.toString()));
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

        TaxonSet<String> taxonSetOnRank = getTaxaOnRank();
        out.write("# " + taxonomySet.size() + " taxa belong to " + taxonSetOnRank.size() + " " + ranksToBreak + "s\n");
        for (String t : taxonSetOnRank) {
            out.write(t + "\n");
        }

        out.flush();
        out.close();

    }

//    protected void writeParentTaxon(String queryTaxon, List<Taxon> taxonList, BufferedWriter out) throws IOException {
//        for (Taxon taxon : taxonList) {
//            // filter out taxon not belong to bioClass, but always true if bioClass == null
//            if (taxon.belongsTo(bioClass)) {
//                Taxon t = taxon.getParentTaxonOn(rankToBreak);
//                out.write("\t" + t);
//                if (t != null) {
//                    taxaAssignementMap.put(queryTaxon, t.getScientificName());
//                } else {
//                    taxaAssignementMap.put(queryTaxon, OTHER);
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
            return Arrays.copyOf(TaxonomicAssignment.Error.values(), TaxonomicAssignment.Error.values().length, String[].class);
        }

    }


}
