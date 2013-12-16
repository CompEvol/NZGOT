package nzgot.core.community;

import jebl.evolution.sequences.Sequence;

/**
 * @deprecated plan to be replaced by Target
 * A mapping class to keep the relationship
 * between OTU and reference sequence
 * @author Walter Xie
 */
public class Reference<E, T> {

    protected final E otu;
    protected T referenceSeq;
    protected double identity = -1;
    private boolean chooseWorst = false;

    public Reference(E otu, T referenceSeq) {
        this.otu = otu;
        this.referenceSeq = referenceSeq;
    }

    public Reference(E otu, T referenceSeq, double identity, boolean chooseWorst) {
        this.otu = otu;
        this.referenceSeq = referenceSeq;
        this.identity = identity;
        this.chooseWorst = chooseWorst;
    }

    public E getOtu() {
        return otu;
    }

    public T getReferenceSeq() {
        return referenceSeq;
    }

    public void setReferenceSeq(T referenceSeq) {
        this.referenceSeq = referenceSeq;
    }

    public void setBestReferenceSeq(T referenceSeq, double identity) {
        if (identity > this.identity) {
            this.identity = identity;
            this.referenceSeq = referenceSeq;
        }
    }

    public void setWorstReferenceSeq(T referenceSeq, double identity) {
        if (identity < this.identity) {
            this.identity = identity;
            this.referenceSeq = referenceSeq;
        }
    }

    public String toString() {
        if (referenceSeq instanceof Sequence) {
            return ((Sequence) referenceSeq).getTaxon().getName();
        } else {
            return referenceSeq.toString();
        }
    }
}
