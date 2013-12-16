package nzgo.toolkit.ec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This represents a node in the dynamic programming table that holds an optimal score of an alignment between two
 * sequence prefixes as well as traceback links to recover an optimal alignment.
 *
 * @author Alexei Drummond
 * @author Walter Xie
 */
public class FNode {

    enum FType {
        match,
        match_duplicate,
        match_delete,
        ins_read,
        ins_read_duplicate,
        ins_read_delete,
        ins_ref
    }

    // the score of the optimal alignment up to this point
    double score;

    // the length of the prefix of the read sequence aligned
    int i;

    // the length of the reference sequence aligned
    int j;

    int prefixIndex = -1;

    FEdge optimalPrefix = null;

    //List<FEdge> prefixes;
    //List<Double> prefixScores;

    /**
     * @return the FNode that is an optimal prefix to this FNode
     */
    public FEdge prefix() {
        return optimalPrefix;
    }

    public void compute(int i, int j, FNode[][] F, FScore fscore) {

        this.i = i;
        this.j = j;

        if (i < 2 || j == 0) {
            score = 0;
            return;
        }

        List<FEdge> prefixes = new ArrayList<FEdge>();
        List<Double> prefixScores = new ArrayList<Double>();

        if (i >= 3) {
            prefixScores.add(extend(FType.match, 0, F, fscore, prefixes));
            prefixScores.add(extend(FType.ins_read, 0, F, fscore, prefixes));
        }

        prefixScores.add(extend(FType.ins_ref, 0, F, fscore, prefixes));

        if (i >= 4) {
            for (int offset = -3; offset <= 0; offset++) {
                prefixScores.add(extend(FType.match_delete, offset, F, fscore, prefixes));
                prefixScores.add(extend(FType.ins_read_delete, offset, F, fscore, prefixes));
            }
        }

        if (i >= 2) {
            for (int offset = -2; offset <= 0; offset++) {
                prefixScores.add(extend(FType.match_duplicate, offset, F, fscore, prefixes));
                prefixScores.add(extend(FType.ins_read_duplicate, offset, F, fscore, prefixes));
            }
        }

        score = Collections.max(prefixScores);

        for (int k = 0; k < prefixes.size(); k++) {
            if (prefixScores.get(k) == score) {
                optimalPrefix = prefixes.get(k);
                return;
            }
        }
    }

    /**
     * @param type
     * @param offset
     * @param F
     * @param score
     * @return the score of the extension
     */
    public double extend(FType type, int offset, FNode[][] F, FScore score, List<FEdge> prefixes) {

        int[] codon = codon(type, offset);
        int correctNucInSeq = correctNucInSeq(type, offset);

        switch (type) {
            case match:
                if (offset != 0) throw new IllegalArgumentException();
                prefixes.add(new FEdge(F[i - 3][j - 1], FType.match, offset, codon, correctNucInSeq));
                return F[i - 3][j - 1].score + score.matchScore(codon, j);
            case match_duplicate:
                prefixes.add(new FEdge(F[i - 2][j - 1], FType.match_duplicate, offset, codon, correctNucInSeq));
                return F[i - 2][j - 1].score + score.matchDuplicate(codon, j);
            case match_delete:
                prefixes.add(new FEdge(F[i - 4][j - 1], FType.match_delete, offset, codon, correctNucInSeq));
                return F[i - 4][j - 1].score + score.matchDelete(codon, j);
            case ins_read:
                if (offset != 0) throw new IllegalArgumentException();
                prefixes.add(new FEdge(F[i - 3][j], FType.ins_read, offset, codon, correctNucInSeq));
                return F[i - 3][j].score + score.insertCodon(codon);
            case ins_read_duplicate:
                prefixes.add(new FEdge(F[i - 2][j], FType.ins_read_duplicate, offset, codon, correctNucInSeq));
                return F[i - 2][j].score + score.insertCodonDuplicate(codon);
            case ins_read_delete:
                prefixes.add(new FEdge(F[i - 4][j], FType.ins_read_delete, offset, codon, correctNucInSeq));
                return F[i - 4][j].score + score.insertCodonDelete(codon);
            case ins_ref:
                if (offset != 0) throw new IllegalArgumentException();
                prefixes.add(new FEdge(F[i][j - 1], FType.ins_ref, offset, codon, correctNucInSeq));
                return F[i][j - 1].score + score.indelPenalty;
            default:
                throw new IllegalArgumentException();
        }
    }

    public int[] codon(FType type, int offset) {
        switch (type) {
            case match:
            case ins_read:
                if (offset != 0) throw new IllegalArgumentException();
                return new int[]{i - 2, i - 1, i};
            case match_duplicate:
            case ins_read_duplicate:
                switch (offset) {
                    case 0:
                        return new int[]{i - 1, i, i};
                    case -1:
                        return new int[]{i - 1, i - 1, i};
                    case -2:
                        return new int[]{i - 2, i - 1, i};
                    default:
                        throw new IllegalArgumentException();
                }
            case match_delete:
            case ins_read_delete:
                switch (offset) {
                    case 0:
                        return new int[]{i - 3, i - 2, i - 1};
                    case -1:
                        return new int[]{i - 3, i - 2, i};
                    case -2:
                        return new int[]{i - 3, i - 1, i};
                    case -3:
                        return new int[]{i - 2, i - 1, i};
                    default:
                        throw new IllegalArgumentException();
                }
            case ins_ref:
                return new int[]{0, 0, 0};
            default:
                throw new IllegalArgumentException();
        }

    }

    public int correctNucInSeq(FType type, int offset) {
        switch (type) {
            case match:
            case ins_read:
            case ins_ref:
                return -1;
            case match_duplicate:
            case ins_read_duplicate:
                switch (offset) {
                    case 0:
                        return i;
                    case -1:
                        return i - 1;
                    case -2:
                        return i - 2;
                    default:
                        throw new IllegalArgumentException();
                }
            case match_delete:
            case ins_read_delete:
                switch (offset) {
                    case 0:
                        return i;
                    case -1:
                        return i - 1;
                    case -2:
                        return i - 2;
                    case -3:
                        return i - 3;
                    default:
                        throw new IllegalArgumentException();
                }
            default:
                throw new IllegalArgumentException();
        }

    }


    public String toString() {
    	StringBuilder builder = new StringBuilder();
    	
    	builder.append("FNode(i=");
    	builder.append(i);
    	builder.append(", j=");
    	builder.append(j);
    	builder.append(", score=");
    	builder.append(score);
      	builder.append(")");
    	
    	return builder.toString();
    }
}
