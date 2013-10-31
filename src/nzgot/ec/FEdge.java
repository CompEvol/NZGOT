package nzgot.ec;

/**
 * @author Alexei Drummond
 */
public class FEdge {

    public FEdge(FNode prefix, FNode.FType type, int offset, int[] codon) {
        this.prefix = prefix;
        this.type = type;
        this.offset = offset;
        this.codon = codon;
    }

    FNode prefix;
    FNode.FType type;
    int offset;
    int[] codon;


}