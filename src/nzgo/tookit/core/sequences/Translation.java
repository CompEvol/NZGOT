package nzgo.tookit.core.sequences;

import jebl.evolution.sequences.Sequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Translation relationship class, such as translation errors
 * @author Walter Xie
 */
public class Translation {

    public int frame;
    public Sequence nucleotide;
    public Sequence aminoAcid;

    public Error translationError;

    public Translation(Sequence nucleotide, int frame) {
        this.frame = frame;
        this.nucleotide = nucleotide;
    }

    public Translation(Sequence nucleotide, Error translationError) {
        this.nucleotide = nucleotide;
        this.translationError = translationError;
    }

    public Translation(Sequence nucleotide, List<Integer> multiFrame) {
        this(nucleotide, Error.MULTI_FRAME);
        this.getTranslationError().multiFrame.addAll(multiFrame);
    }

    public enum Error {
        MULTI_FRAME ("The sequence can be translated to multi-frame"),
        NO_FRAME   ("The sequence cannot be translated");

        private final String msg;
        Error(String msg) {
            this.msg = msg;
        }

        public List<Integer> multiFrame = new ArrayList<>();

        public String toString() {
            String m = msg;
            for (int i=0; i<multiFrame.size(); i++) {
                if (i == 0) m += ": frame " + multiFrame.get(i);
                m += " & " + multiFrame.get(i);
            }

            return m;
        }
    }

    public boolean isTranslatable() {
        return (frame == 1 || frame == 2 || frame == 3);
    }

    public int getFrame() {
        if (frame < 1 || frame > 3) throw new IllegalArgumentException("Get an illegal frame : " + frame);
        return frame;
    }

//    public void setFrame(int frame) {
//        if (frame < 1 || frame > 3) throw new IllegalArgumentException("Cannot set an illegal frame : " + frame);
//        this.frame = frame;
//    }

    public Sequence getNucleotide() {
        if (nucleotide == null) throw new IllegalArgumentException("Nucleotide sequence is not initialised !");
        return nucleotide;
    }

//    public void setNucleotide(Sequence nucleotide) {
//        this.nucleotide = nucleotide;
//    }

    public Sequence getAminoAcid() {
        if (aminoAcid == null) throw new IllegalArgumentException("Amino Acid sequence is not initialised !");
        return aminoAcid;
    }

    public void setAminoAcid(Sequence aminoAcid) {
        this.aminoAcid = aminoAcid;
    }

    public Error getTranslationError() {
        if (translationError == null) throw new IllegalArgumentException("Translation error is not initialised !");
        return translationError;
    }

//    public void setTranslationError(Error translationError) {
//        this.translationError = translationError;
//    }
}
