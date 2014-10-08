package nzgo.toolkit.core.uparse.io;

import nzgo.toolkit.core.community.Community;
import nzgo.toolkit.core.community.OTU;
import nzgo.toolkit.core.community.OTUs;
import nzgo.toolkit.core.io.FileIO;
import nzgo.toolkit.core.io.SequenceFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.AssemblerUtil;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.naming.NameUtil;
import nzgo.toolkit.core.naming.SiteNameParser;
import nzgo.toolkit.core.taxonomy.Rank;
import nzgo.toolkit.core.taxonomy.Taxon;
import nzgo.toolkit.core.uparse.DereplicatedSequence;
import nzgo.toolkit.core.uparse.Parser;
import nzgo.toolkit.core.uparse.UCParser;
import nzgo.toolkit.core.uparse.UPParser;
import nzgo.toolkit.core.util.StringUtil;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Community Matrix FileIO
 * Not attempt to store reads as Sequence, but as String
 * both OTU and reference mapping file are uc format.
 * @author Walter Xie
 */
public class CommunityFileIO extends OTUsFileIO {

    public static final String COMMUNITY_MATRIX = "community_matrix";
    public static final int COMMUNITY_REPORT_COLUMN = 7;

    /**
     * 1st set siteType in siteNameParser, default to BY_PLOT
     * 2nd load reads into each OTU, and parse label to get sample array
     * 3rd set sample array, and calculate Alpha diversity for each OTU
     *
     * set hits into each OTU, if no OTU is uploaded, then create new OTUs from mapping file
     * e.g. S	17	300	*	*	*	*	*	HA5K40001BTFNL|IndirectSoil|3-H;size=177;	*
     * e.g. H	11	300	98.3	+	0	0	300M	G86XGG201B3O46|IndirectSoil|5-I;size=166;	G86XGG201B3YD9|IndirectSoil|5-N;size=248;
     * @param initCommunity
     * @param ucMappingFile
     * @param siteNameParser          used only for initCommunity, if null then ignore sites
     * @throws java.io.IOException
     * @throws IllegalArgumentException
     */
    public static TreeSet<String> importCommunityFromUCFile(OTUs initCommunity, File ucMappingFile, SiteNameParser siteNameParser) throws IOException, IllegalArgumentException {
        NameUtil.validateFileExtension(ucMappingFile.getName(), NameSpace.SUFFIX_UC);

        // this method has to count annotated size in OTU members
        initCommunity.setCountSizeAnnotation(true);

        boolean countSizeAnnotation = initCommunity.isCountSizeAnnotation();
        MyLogger.info(countSizeAnnotation ? "Sum up annotated size in OTU ... " : "Count reads in OTU ignoring size annotation ... ");

        TreeSet<String> sites = null;
        int total = 0;

        BufferedReader reader = FileIO.getReader(ucMappingFile, "OTUs and OTU mapping from");
        String line = reader.readLine();
        while (line != null) {
            // 2 columns: 1st -> read id, 2nd -> otu name
            String[] fields = FileIO.lineParser.getSeparator(0).parse(line);

            if (fields.length < 2) throw new IllegalArgumentException("Error: invalid mapping in the line: " + line);

            // important: Centroid is excluded from Hit list
            if (fields[UCParser.Record_Type_COLUMN_ID].contentEquals(UCParser.HIT) || fields[UCParser.Record_Type_COLUMN_ID].contentEquals(UCParser.Centroid)) {
                String otuName = Parser.getLabel(fields[UCParser.Target_Sequence_COLUMN_ID], initCommunity.removeSizeAnnotation);
                if (fields[UCParser.Record_Type_COLUMN_ID].contentEquals(UCParser.Centroid))
                    otuName = Parser.getLabel(fields[UCParser.Query_Sequence_COLUMN_ID], initCommunity.removeSizeAnnotation);

                if (!Parser.isNA(otuName)) {
                    String finalLabel = Parser.getLabel(fields[UCParser.Query_Sequence_COLUMN_ID], initCommunity.removeSizeAnnotation);
                    double identity = Parser.getIdentity(fields[UCParser.H_Identity_COLUMN_ID]);
                    // annotated size from dereplication
                    int annotatedSize = Parser.getAnnotatedSize(fields[UCParser.Query_Sequence_COLUMN_ID]);

                    if (initCommunity.containsUniqueElement(otuName)) {
                        OTU otu = initCommunity.getOTU(otuName);

                        if (otu == null) {
                            throw new IllegalArgumentException("Error: find an invalid OTU " + otuName +
                                    ", from the mapping file which does not exist in OTUs file !");
                        } else {
                            // add new member
                            DereplicatedSequence member = new DereplicatedSequence(finalLabel);
                            member.setAnnotatedSize(annotatedSize);
                            otu.addElement(member);

                            if (siteNameParser != null) {
                                if (sites == null) {
                                    sites = new TreeSet<>();
                                    MyLogger.info("Site type: " + siteNameParser.siteType);
                                }

                                // if by plot, then add plot to TreeSet, otherwise add subplot
                                String sampleLocation = siteNameParser.getSite(finalLabel);
                                sites.add(sampleLocation);
                            }
                        }
                    } else {
                        OTU otu = new OTU(otuName);
                        DereplicatedSequence representative = new DereplicatedSequence(finalLabel);
                        representative.setAnnotatedSize(annotatedSize);
                        otu.addElement(representative);

                        initCommunity.addUniqueElement(otu);
                    }

                    total++;
                }
            }

            line = reader.readLine();
        }

        reader.close();

        int[] sizes = initCommunity.getSizes();
        MyLogger.debug("Total valid lines = " + total);
        MyLogger.debug("OTU clustering get OTUs = " + sizes[0] + (countSizeAnnotation ? ", unique reads = " : ", reads = ") +
                sizes[1] + ", annotated reads = " + sizes[2]);

        return sites;
    }

    /**
     * take the annotated size from the unique sequence label to count, and save it in OTU
     *
     * 1st set siteType in siteNameParser, default to BY_PLOT
     * 2nd load reads into each OTU, and parse label to get sample array
     * 3rd set sample array, and calculate Alpha diversity for each OTU
     *
     * e.g.
     * IPCL6BO02G2MEU|18S-combined|5-I|18S-FLX;size=23;	otu	*	*	*
     * IPCL6BO02F6OAL|18S-combined|5-I|18S-FLX;size=23;	match	99.3	*	IPCL6BO02H63PX|18S-combined|4-N|18S-FLX
     * IPCL6BO02FK1D5|18S-combined|CM30C30-N|18S-FLX;size=23;	chimera	96.3	97.7	IPCL6BO02G7N0M|18S-combined|CM30C30-L|18S-FLX(1-68)+IPCL6BO02IQUGN|18S-combined|3-N|18S-FLX(69-300)
     *
     * @param initCommunity
     * @param upMappingFile
     * @param siteNameParser          used only for initCommunity, if null then ignore sites
     * @throws java.io.IOException
     * @throws IllegalArgumentException
     */
    public static TreeSet<String> importCommunityFromUPFile(Community initCommunity, Path upMappingFile, Path chimerasFile, SiteNameParser siteNameParser) throws IOException, IllegalArgumentException {
        NameUtil.validateFileExtension(upMappingFile.getFileName().toString(), NameSpace.SUFFIX_UP);

        List<DereplicatedSequence> chimeras = null;
        if (chimerasFile != null) {
            NameUtil.validateFileExtension(chimerasFile.getFileName().toString(), NameSpace.SUFFIX_FASTA);

            // load chimeras from label
            chimeras = SequenceFileIO.importDereplicatedSequences(chimerasFile, false, initCommunity.removeSizeAnnotation);
        }

        // TODO
        // this method has to count annotated size in OTU members
        initCommunity.setCountSizeAnnotation(true);
        boolean countSizeAnnotation = initCommunity.isCountSizeAnnotation();
        MyLogger.info(countSizeAnnotation ? "Sum up annotated size in OTU ... " : "Count reads in OTU ignoring size annotation ... ");

        TreeSet<String> sites = null;
        int total = 0;
        int chimerasInClustering = 0;
        int chimerasInClusteringAnnotatedSize = 0;

        BufferedReader reader = FileIO.getReader(upMappingFile, "OTUs and OTU mapping from");
        String line = reader.readLine();
        while (line != null) {
            // 5 columns: 1st -> query, 2nd -> classification
            String[] fields = FileIO.lineParser.getSeparator(0).parse(line);

            if (fields.length < 2) throw new IllegalArgumentException("Error: invalid mapping in the line: " + line);

            String querySeqLabel = fields[UPParser.QUERY_COLUMN_ID];
            int annotatedSize = Parser.getAnnotatedSize(querySeqLabel); // annotated size from dereplication
            String finalLabel = Parser.getLabel(querySeqLabel, initCommunity.removeSizeAnnotation);
            OTU otu;

            String classification = fields[UPParser.Classification_COLUMN_ID];
            switch (classification) {
                case UPParser.OTU:
                    otu = new OTU(finalLabel);

                    DereplicatedSequence representative = new DereplicatedSequence(finalLabel);
                    representative.setAnnotatedSize(annotatedSize);
                    otu.addElement(representative);
                    initCommunity.addUniqueElement(otu);
                    break;

                case UPParser.OTU_MEMBER:
                    String otuLabel = Parser.getLabel(fields[UPParser.OTU_COLUMN_ID], initCommunity.removeSizeAnnotation);
                    double identity = Parser.getIdentity(fields[UPParser.Identity_COLUMN_ID]);

                    otu = initCommunity.getOTU(otuLabel);

                    if (otu == null) {
                        throw new IllegalArgumentException("Cannot find OTU " + finalLabel + " from line : " + line);
                    } else {
                        // add new member
                        DereplicatedSequence member = new DereplicatedSequence(finalLabel);
                        member.setAnnotatedSize(annotatedSize);
                        otu.addElement(member);

                        if (siteNameParser != null) {
                            if (sites == null) {
                                sites = new TreeSet<>();
                                MyLogger.info("Site type: " + siteNameParser.siteType);
                            }

                            // if by plot, then add plot to TreeSet, otherwise add subplot
                            String sampleLocation = siteNameParser.getSite(finalLabel);
                            sites.add(sampleLocation);
                        }
                    }
                    break;

                case UPParser.CHIMERA:
                    chimerasInClustering ++;
                    if (initCommunity.isCountSizeAnnotation()) {
                        chimerasInClusteringAnnotatedSize += annotatedSize;
                    }
                    break;

                default:
                    throw new IllegalArgumentException("Invalid classification " + classification + " in line : " + line);
            }

            total++;

            line = reader.readLine();
        }

        reader.close();

        int[] sizes = initCommunity.getSizes();
        MyLogger.debug("Total valid lines = " + total);
        MyLogger.debug("OTU clustering get OTUs = " + sizes[0] + (countSizeAnnotation ? ", unique reads = " : ", reads = ") +
                sizes[1] + ", annotated reads = " + sizes[2]);
        MyLogger.debug("OTU clustering get chimeras = " + chimerasInClustering + ", annotated reads = " + chimerasInClusteringAnnotatedSize);
        initCommunity.chimerasInClustering = chimerasInClustering;
        initCommunity.chimerasInClusteringAnnotatedSize = chimerasInClusteringAnnotatedSize;

        if (chimeras != null) {
            int sizeRemoved = initCommunity.filterChimeras(chimeras);
            MyLogger.debug("Remove " + chimeras.size() + " chimeras OTU , annotated reads = " + sizeRemoved);

            sizes = initCommunity.getSizes();
            MyLogger.debug("After chimeras filtering get OTUs = " + sizes[0] + (countSizeAnnotation ? ", unique reads = " : ", reads = ") +
                    sizes[1] + ", annotated reads = " + sizes[2]);
        }

        return sites;
    }

    /**
     * write community matrix
     * A COLUMN PER SAMPLE AND A ROW FOR EACH SPECIES/OTU
     *
     * @param outCMFilePath
     * @param community
     * @param printTaxonomy      print the OTU's taxonomy
     * @param ranks              has to set printTaxonomy = true
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public static int[] writeCommunityMatrix(Path outCMFilePath, Community community, boolean printTaxonomy, Rank... ranks) throws IOException, IllegalArgumentException {

        BufferedWriter writer = FileIO.getWriter(outCMFilePath, "community matrix");

        for (String sample : community.getSites()) {
            writer.write("," + sample);
        }
        writer.write("\n");

        int uniqueTotal = 0;
        int total = 0;
        int otu1Read = 0;
        int otu2Reads = 0;
        for(Object o : community){
            OTU otu = (OTU) o;
            writer.write(otu.getName());

            // print readsPerSite column
            int rowsum = 0;
            if (otu.getReadsPerSite() != null) {
                for (int reads : otu.getReadsPerSite()) {
                    writer.write("," + reads);
                    rowsum += reads;
                }
            }

            // print Taxon column
            if (printTaxonomy && otu.hasTaxon()) {
                Taxon taxonLCA = otu.taxonLCA;
                writer.write("," + taxonLCA.getScientificName());
                if (taxonLCA != null && ranks != null) {
                    for (Rank rank : ranks) {
                        Taxon t = taxonLCA.getParentTaxonOn(rank);
//                        String str = ("no " + rank.toString()).toLowerCase();
                        writer.write("," + t.getScientificName());
                    }
                }
            }

            writer.write("\n");

            int size;
            int unique = otu.size(); // real size
            if (community.isCountSizeAnnotation()) {
                size = otu.getTotalAnnotatedSize(); // annotated size
            } else {
                size = otu.size(); // real size
            }

            if (size != rowsum)
                throw new RuntimeException("OTU " + otu + " size " + size + " not equal community matrix row sum " + rowsum);

            uniqueTotal += unique;
            total += size;
            if (size == 1) {
                otu1Read ++;
            } else if (size == 2) {
                otu2Reads ++;
            }

        }

        writer.flush();
        writer.close();

        MyLogger.info("\nCommunity Matrix " + community.getName() + ": " + community.size() + " OTUs, " +
                uniqueTotal + " unique sequences, " + total + " reads " +
                (community.isCountSizeAnnotation() ? "from size annotation, " : ", ") +
                otu1Read + " OTUs represented by 1 reads, " + otu2Reads + " OTUs represented by 2 reads,");
        MyLogger.info("OTU clustering removed " + community.chimerasInClustering + " chimeras unique sequences (" +
                community.chimerasInClusteringAnnotatedSize + " chimeras reads), " +
                community.getSites().length + " sites = " + Arrays.toString(community.getSites()));

        return new int[]{community.size(), uniqueTotal, total, otu1Read, otu2Reads, community.chimerasInClustering, community.chimerasInClusteringAnnotatedSize, community.getSites().length};
    }

    public static int[] writeCommunityMatrix(Path outCMFilePath, Community community) throws IOException, IllegalArgumentException {
        return writeCommunityMatrix(outCMFilePath, community, false);
    }

    /**
     * 2 columns: 1st -> reference sequence id, 2nd -> number of reads
     * last row is total reads
     * at least one OTU should have reference
     * @param outFileAndPath
     * @param otus
     * @throws IOException
     * @throws IllegalArgumentException
     */

    public static void writeRefReads(String outFileAndPath, OTUs otus) throws IOException, IllegalArgumentException {

        PrintStream out = new PrintStream(new FileOutputStream(outFileAndPath));

        MyLogger.info("\nGenerate report of how many reads map to reference sequence in the file: " + outFileAndPath);

        int total = 0;
        Map<String, Integer> readsCountMap = otus.getRefSeqReadsCountMap();
        for(Map.Entry<String, Integer> entry : readsCountMap.entrySet()){
            out.println(entry.getKey() + "\t" + entry.getValue());
            total += entry.getValue();
        }
        out.println("total\t" + total);
        out.flush();
        out.close();
    }

    /**
     * write Community Report, rowNames.size() == report.size()
     * @param reportFile
     * @param report
     * @param rowNames
     * @throws IOException
     */
    public static void writeCommunityReport(Path reportFile, List<int[]> report, List<String> rowNames) throws IOException {
        BufferedWriter writer = FileIO.getWriter(reportFile, "OTUs summary report");

        String[] colNames = new String[]{"Stage","OTUs","Unique","Reads","OTUs1Read","OTUs2Reads","UniqueChimeras","Chimeras"};
        writer.write(StringUtil.getRow(colNames));

        for (int i = 0; i < report.size(); i++) {
            int[] row = report.get(i);
            String rowName = "";
            if (rowNames != null) {
                assert rowNames.size() == report.size();
                rowName = rowNames.get(i);
            }
            writer.write(rowName);
            for (int j = 0; j < COMMUNITY_REPORT_COLUMN; j++) {
                writer.write("\t" + row[j]);
            }
            writer.write("\n");
        }

        writer.close();
    }

    /**
     * this method only works for specific directory structure described in WalterPipeline.txt
     * @param workDir
     * @param otuMappingFile
     * @param reportFileName
     * @param cmFileName
     * @param experiments
     * @param thresholds
     * @param thresholdWhoseOTUsToAppendSize    add size to sequence label to combine with BLAST taxonomy
     * @throws java.io.IOException
     */
    @Deprecated
    public static void reportCommunityByOTUThreshold(Path workDir, String otuMappingFile, String reportFileName, String cmFileName,
                                                     String[] experiments, int[] thresholds, int thresholdWhoseOTUsToAppendSize) throws IOException {

        for (String experiment : experiments) {
            // go into each gene folder
            Path workPath = Paths.get(workDir.toString(), experiment);
            MyLogger.info("\nWorking path = " + workPath);

            Path outFile = Paths.get(workPath.toString(), experiment + reportFileName);
            BufferedWriter writer = FileIO.getWriter(outFile, "OTUs summary report at " + experiment);

            writer.write("Threshold\tOTUs\tReads\tOTUs1Read\tOTUs2Reads\n");

            for (int thre : thresholds) {
                Path otusPath = Paths.get(workPath.toString(), "otus" + thre);

                File mappingFile = Paths.get(otusPath.toString(), otuMappingFile).toFile();
                SiteNameParser siteNameParser = new SiteNameParser();
                Community community = new Community(mappingFile, siteNameParser);

                Path outCMFilePath = Paths.get(otusPath.toString(), experiment + "_" + thre + cmFileName);
                int[] report = writeCommunityMatrix(outCMFilePath, community);
                writer.write(thre + "\t" + report[0] + "\t" + report[2] + "\t" + report[3] + "\t" + report[4] + "\n");

                if (thresholdWhoseOTUsToAppendSize == thre) { // 97
                    Path otusFile = Paths.get(otusPath.toString(), "otus.fasta");
                    AssemblerUtil.removeAnnotationAppendSizeToLabel(otusFile, community);
                }
            }

            writer.close();
        }
    }
}
