package nzgo.toolkit.core.uparse;

import nzgo.toolkit.core.io.SequenceFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.pipeline.Module;
import nzgo.toolkit.core.sequences.SimpleSequence;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Walter Xie
 */
public class MappingSequence {

    /**
     * retrieve duplicate sequences from all sequences given dereplicated sequences
     * @param dereplicatedSequences
     * @param allSequences
     * @return
     */
    public static List<SimpleSequence> getDuplicateSequences(List<SimpleSequence> dereplicatedSequences, List<SimpleSequence> allSequences) {
        List<SimpleSequence> duplicateSequences = new ArrayList<>();
        for (SimpleSequence ds : dereplicatedSequences) {
            for (SimpleSequence ss : allSequences) {
                if (ds.isIdenticalSequence(ss)) {
                    duplicateSequences.add(ss);
                    break;
                }
            }
        }
        MyLogger.debug("dereplicatedSequences = " + dereplicatedSequences.size() + ", allSequences = " +
                allSequences.size() + ", duplicateSequences = " + duplicateSequences.size());
        return duplicateSequences;
    }

    // main
    public static void main(String[] args) throws IOException {
        if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");

        String workPathString = args[0];
        MyLogger.info("\nWorking path = " + workPathString);

        String inFastaFileName = "chimeras.fasta";
        Path inFastaFilePath = Module.validateInputFile(Paths.get(workPathString), inFastaFileName,
                new String[]{NameSpace.SUFFIX_FASTA}, "dereplicated sequences file");

        List<SimpleSequence> dereplicatedSequences = SequenceFileIO.importSimpleSequences(inFastaFilePath, true);

        inFastaFileName = "reads.fasta";
        inFastaFilePath = Module.validateInputFile(Paths.get(workPathString), inFastaFileName,
                new String[]{NameSpace.SUFFIX_FASTA}, "all sequences file");

        List<SimpleSequence> allSequences = SequenceFileIO.importSimpleSequences(inFastaFilePath, false);

        List<SimpleSequence> duplicateSequences = getDuplicateSequences(dereplicatedSequences, allSequences);

        Path otusPath = Paths.get(workPathString, "duplicateChimeras.fasta"); //
        SequenceFileIO.writeSimpleSequenceToFasta(otusPath, duplicateSequences);
    }

}
