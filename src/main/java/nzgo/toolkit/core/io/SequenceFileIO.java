package nzgo.toolkit.core.io;

import jebl.evolution.io.FastaExporter;
import jebl.evolution.io.FastaImporter;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import nzgo.toolkit.core.logger.MyLogger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Sequence FileIO: adapter of FastaImporter
 *
 * @author Walter Xie
 */
public class SequenceFileIO extends FileIO {

    public static List<Sequence> importNucleotideSequences (Path sequenceFile) throws IOException, ImportException {
        FastaImporter sequenceImporter = new FastaImporter(sequenceFile.toFile() , SequenceType.NUCLEOTIDE);

        return sequenceImporter.importSequences();
    }


    public static void writeFasta (Path sequenceFile, List<Sequence> sequences) throws IOException {
        MyLogger.info("\nCreating fasta ..." + sequenceFile);

        BufferedWriter writer = getWriter(sequenceFile, "fasta");

        FastaExporter sequenceExporter = new FastaExporter(writer);

        sequenceExporter.exportSequences(sequences);
    }
}
