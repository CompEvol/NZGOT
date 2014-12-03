package nzgo.toolkit.core.ncbi.submission;

import nzgo.toolkit.core.io.FileIO;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Source Modifiers Table for Barcode Submission Tool
 * http://www.ncbi.nlm.nih.gov/WebSub/html/help/source-table.html
 *
 * @author Walter Xie
 */
public class SourceModifiersTable {

    protected List<String> Sequence_ID = new ArrayList<>();
    protected List<String> Collected_by = new ArrayList<>();
    protected List<String> Collection_date = new ArrayList<>();
    protected List<String> Country = new ArrayList<>();
    protected List<String> Identified_by = new ArrayList<>();
    protected List<String> Isolate = new ArrayList<>();
    protected List<String> Lat_Lon = new ArrayList<>();
    protected List<String> Specimen_voucher = new ArrayList<>();

    public SourceModifiersTable() {
    }

    public void addValue(String sequence_ID, String specimen_voucher) {
        Sequence_ID.add(sequence_ID);
        Specimen_voucher.add(specimen_voucher);
    }

    public void addValue(String sequence_ID, String specimen_voucher, String collected_by, String collection_date,
                         String country, String identified_by, String isolate, String lat_lon) {
        addValue(sequence_ID, specimen_voucher);
        Collected_by.add(collected_by);
        Collection_date.add(collection_date);
        Country.add(country);
        Identified_by.add(identified_by);
        Isolate.add(isolate);
        Lat_Lon.add(lat_lon);
    }

    public void outputTwoTable(Path outPath) throws IOException {
        if (Sequence_ID.size() != Specimen_voucher.size())
            throw new RuntimeException("Sequence_ID column number of elements does not equal to Specimen_voucher column !");

        PrintStream out = FileIO.getPrintStream(outPath, "Sample Two-Column Source Modifiers Table");

        //head
        out.println("Sequence_ID\tSpecimen_voucher");

        for (int i = 0; i < Sequence_ID.size(); i++) {
            out.println(Sequence_ID.get(i) + "\t" + Specimen_voucher.get(i));
        }

        out.flush();
        out.close();
    }

    public void outputTable(Path outPath) throws IOException {
        if (Sequence_ID.size() != Specimen_voucher.size()) //TODO more columns
            throw new RuntimeException("Sequence_ID column number of elements does not equal to Specimen_voucher column !");

        PrintStream out = FileIO.getPrintStream(outPath, "Sample Source Modifiers Table");

        //head
        out.println("Sequence_ID\tCollected_by\tCollection_date\tCountry\tIdentified_by\tIsolate\tLat_Lon\tSpecimen_voucher");

        for (int i = 0; i < Sequence_ID.size(); i++) {
            out.println(Sequence_ID.get(i) + "\t" + Collected_by.get(i) + "\t" + Collection_date.get(i) + "\t" +
                    Country.get(i) + "\t" + Identified_by.get(i) + "\t" + Isolate.get(i) + "\t" +
                    Lat_Lon.get(i) + "\t" + Specimen_voucher.get(i));
        }

        out.flush();
        out.close();
    }
}
