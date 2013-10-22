package awc.uoa.mes.tools.bio;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * OTU
 * @author Walter Xie
 */
public class CommunityMatrix {

    protected String name;
    protected Map<String, OTU> otusMap = new HashMap<String, OTU>();
    protected File otusFile;
    protected File otuMappingFile;
    protected File referenceMappingFile; // Sanger sequence for reference

    public CommunityMatrix(File otusFile, File otuMappingFile) {
        this.otusFile = otusFile;
        setName(otusFile.getName());
        try {
            importOTUs(otusFile);
            importOTUMapping(otuMappingFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void importOTUs (File otusFile) throws IOException, IllegalArgumentException {
        BufferedReader reader = new BufferedReader(new FileReader(otusFile));

        System.out.println("\nImport OTUs from file: " + otusFile);

        OTU otu = null;
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith(">")) {
//                line.replaceAll("size=", "");
                // the current label only contains otu name
                String otuName = line.substring(1);
                otu = new OTU(otuName);

                addOTU(otu);

            } else {
                // TODO add ref sequence
            }

            line = reader.readLine();
        }

        reader.close();
    }

    public void importOTUMapping (File otuMappingFile) throws IOException, IllegalArgumentException {
        int indexRead = 0;
        int indexOTUName = 1;

        BufferedReader reader = new BufferedReader(new FileReader(otuMappingFile));

        System.out.println("\nImport OTU mapping (to reads) file: " + otuMappingFile);

        String line = reader.readLine();
        while (line != null) {
            // 2 columns: 1st -> read id, 2nd -> otu name
            String[] fields = line.split("\t", -1);

            if (fields.length < 2) throw new IllegalArgumentException("Error: invalid mapping in the line: " + line);

            if (containsOTU(fields[indexOTUName])) {
                OTU otu = getOTU(fields[indexOTUName]);

                otu.addRead(fields[indexRead]);

            } else {
                throw new IllegalArgumentException("Error: find an invalid OTU (" + fields[1] + ") not existing in OTUs file !");
            }

            line = reader.readLine();
        }

        reader.close();
    }

    public void importReferenceMappingFile (File referenceMappingFile) throws IOException, IllegalArgumentException {
        int indexIdentity = 0;
        int indexOTUName = 1;
        int indexRefSeq = 2;

        BufferedReader reader = new BufferedReader(new FileReader(referenceMappingFile));

        System.out.println("\nImport reference sequence mapping (to OTU) file: " + referenceMappingFile);

        String line = reader.readLine();
        while (line != null) {
            // 3 columns: 1st -> identity %, 2nd -> otu name, 3rd -> reference sequence id
            String[] fields = line.split("\t", -1);

            if (fields.length < 3) throw new IllegalArgumentException("Error: invalid mapping in the line: " + line);

            if (containsOTU(fields[indexOTUName])) {
                OTU otu = getOTU(fields[indexOTUName]);

                otu.setRefSeqId(fields[indexRefSeq]);

            } else {
                throw new IllegalArgumentException("Error: find an invalid OTU (" + fields[1] + ") not existing in OTUs file !");
            }

            line = reader.readLine();
        }

        reader.close();
    }

    /**
     * 2 columns: 1st -> reference sequence id, 2nd -> number of reads
     * last row is total reads
     * depend on importOTUs, importOTUMapping, importReferenceMappingFile
     * @param outFileAndPath
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public void writeRefReads(String outFileAndPath) throws IOException, IllegalArgumentException {
        PrintStream out = new PrintStream(new FileOutputStream(outFileAndPath));

        System.out.println("\nGenerate report of how many reads map to reference sequence in the file: " + outFileAndPath);

        int total = 0;
        for(Map.Entry<String, OTU> entry : otusMap.entrySet()){
            OTU otu = entry.getValue();
            if (!otu.getOTUName().equalsIgnoreCase(entry.getKey())) {
                throw new IllegalArgumentException("Error: find an invalid OTU having different names : " +
                        entry.getValue() + ", " + otu.getOTUName());
            } else if (otu.getRefSeqId() != null) {
                int reads = otu.getReadsList().size();
                out.println(otu.getRefSeqId() + "\t" + reads);
                total += reads;
            }
        }
        out.println("total\t" + total);
        out.flush();
        out.close();
    }

    public static boolean isOTUsFile(String fileName) {
        return fileName.startsWith(NameSpace.PREFIX_OTUS_RELABELED) && fileName.endsWith(".fasta");
    }

    public static boolean isOTUMappingFile(String fileName) {
        return fileName.startsWith(NameSpace.PREFIX_OTU_MAPPING) && fileName.endsWith(".m8");
    }

    public static boolean isReferenceMappingFile(String fileName) {
        return fileName.startsWith(NameSpace.PREFIX_OTU_REFERENCE) && fileName.endsWith(".m8");
    }

    public void setReferenceMappingFile(File referenceMappingFile) {
        this.referenceMappingFile = referenceMappingFile;
    }

    public void setOtuMappingFile(File otuMappingFile) {
        this.otuMappingFile = otuMappingFile;
    }

    public OTU getOTU(String otuName) {
        return otusMap.get(otuName);
    }

    public OTU addOTU(OTU otu) throws IllegalArgumentException {
        if (containsOTU(otu.getOTUName())) {
            throw new IllegalArgumentException("Error: find duplicate OTU (" + otu + ") in the file: " + otusFile + " !");
        } else {
            return otusMap.put(otu.getOTUName(), otu);
        }
    }

    public OTU removeOTU(String otuName) {
        return otusMap.remove(otuName);
    }

    public boolean containsOTU(String otuName) {
        return otusMap.containsKey(otuName);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
