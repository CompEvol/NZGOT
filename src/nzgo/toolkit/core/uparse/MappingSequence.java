package nzgo.toolkit.core.uparse;

import nzgo.toolkit.core.io.SequenceFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.naming.NameSpace;
import nzgo.toolkit.core.pipeline.Module;

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
    public static List<DereplicatedSequence> getDuplicateSequences(List<DereplicatedSequence> dereplicatedSequences, List<DereplicatedSequence> allSequences) {
        List<DereplicatedSequence> duplicateSequences = new ArrayList<>();
        for (DereplicatedSequence ds : dereplicatedSequences) {
            for (DereplicatedSequence as : allSequences) {
                if (ds.isIdenticalSequence(as)) {
                    duplicateSequences.add(as);
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
        Path inFastaFilePath = Module.inputValidFile(Paths.get(workPathString), inFastaFileName,
                "dereplicated sequences file", NameSpace.SUFFIX_FASTA);

        List<DereplicatedSequence> dereplicatedSequences = SequenceFileIO.importDereplicatedSequences(inFastaFilePath, true, false);

        inFastaFileName = "reads.fasta";
        inFastaFilePath = Module.inputValidFile(Paths.get(workPathString), inFastaFileName,
                "all sequences file", NameSpace.SUFFIX_FASTA);

        List<DereplicatedSequence> allSequences = SequenceFileIO.importDereplicatedSequences(inFastaFilePath, false, false);

        List<DereplicatedSequence> duplicateSequences = getDuplicateSequences(dereplicatedSequences, allSequences);

        Path otusPath = Paths.get(workPathString, "duplicateChimeras.fasta"); //
        SequenceFileIO.writeDereplicatedSequenceToFasta(otusPath, duplicateSequences);
    }

}
