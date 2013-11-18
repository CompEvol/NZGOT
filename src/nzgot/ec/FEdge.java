package nzgot.ec;

/**
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class FEdge {

    public FEdge(FNode prefix, FNode.FType type, int offset, int[] codon, int correctNucInSeq) {
        this.prefix = prefix;
        this.type = type;
        this.offset = offset;
        this.codon = codon;
        this.correctNucInSeq = correctNucInSeq;
    }

    FNode prefix;
    FNode.FType type;
    int offset;
    int[] codon;

    final int correctNucInSeq;
}