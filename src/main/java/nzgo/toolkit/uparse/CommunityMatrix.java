package nzgo.toolkit.uparse;


import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.Sequence;
import nzgo.toolkit.core.io.SequenceFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.r.DataFrame;
import nzgo.toolkit.core.r.Utils;
import nzgo.toolkit.core.sequences.SequenceUtil;
import nzgo.toolkit.core.uparse.Parser;
import nzgo.toolkit.core.uparse.UPParser;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * @author Walter Xie
 */
public class CommunityMatrix {

    public static void removeChimeras(Path otusPath, Path chimerasPath, Path finalSeqPath) throws IOException, ImportException {
        List<Sequence> otus = SequenceFileIO.importNucleotideSequences(otusPath);
        List<Sequence> chimeras = SequenceFileIO.importNucleotideSequences(chimerasPath);

        List<Sequence> finalSequences = SequenceUtil.removeAllFrom(chimeras, otus);

        SequenceFileIO.writeToFasta(finalSeqPath, finalSequences);
    }



    //Main method
    public static void main(final String[] args) {
//        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        Path workDir = Paths.get(System.getProperty("user.home") + "/Projects/FishGutMicrobiomes/OTUs");
        MyLogger.info("\nWorking path = " + workDir);

//        Path otusPath = Paths.get(workDir.toString(), "otus97", "otus.fasta");
//        Path chimerasPath = Paths.get(workDir.toString(), "otus97", "chimeras.fasta");
//        Path finalSeqPath = Paths.get(workDir.toString(), "otus97", "16s.fasta");
//        try {
//            removeChimeras(otusPath, chimerasPath, finalSeqPath);
//        } catch (IOException | ImportException e) {
//            e.printStackTrace();
//        }

        DataFrame derep_uc = Utils.readTable(Paths.get(workDir.toString(), "qc", "derep.uc"));

        DataFrame out_up = Utils.readTable(Paths.get(workDir.toString(), "otus97", "out.up"));

//        Set<String> otus = new HashSet<>();
        // 1st col of up
        List<String> labels = out_up.getData(UPParser.QUERY_COLUMN_ID);
        // separated by _, such as 806rcbc67_3069;size=19037;
        Set<String> samples = Parser.getSamples(labels, "_.*;?size=\\d+;?");

    }


}
