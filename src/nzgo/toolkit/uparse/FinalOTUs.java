package nzgo.toolkit.uparse;

import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.Sequence;
import nzgo.toolkit.core.io.SequenceFileIO;
import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.sequences.SequenceUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * the number of non-chimera OTUs after Uchime does not match
 * the difference between input OTUs and chimera OTUs,
 * so that we cannot use the non-chimera OTUs directly from Uchime.
 *
 * @author Walter Xie
 */
public class FinalOTUs {

    final Path otusPath; final Path chimerasPath;

    public FinalOTUs(Path otusPath, Path chimerasPath) {
        this.otusPath = otusPath;
        this.chimerasPath = chimerasPath;
    }

    public void rmChimeraOTUs(final Path finalOTUsPath) throws IOException, ImportException {
        List<Sequence> otus = SequenceFileIO.importNucleotideSequences(otusPath);
        List<Sequence> chimeras = SequenceFileIO.importNucleotideSequences(chimerasPath);

        List<Sequence> finalOTUs = SequenceUtil.removeAllFrom(chimeras, otus);

        MyLogger.info("\nRemoving " + chimeras.size() + " chimera OTUs from " +
                otus.size() + " OTUs, write " + finalOTUs.size() +
                " final OTUs to file " + finalOTUsPath.toString());

        SequenceFileIO.writeToFasta(finalOTUsPath, finalOTUs);

        otus.clear();
        chimeras.clear();
        finalOTUs.clear();
    }
}
