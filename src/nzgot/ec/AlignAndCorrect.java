package nzgot.ec;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jebl.evolution.align.Output;
import jebl.evolution.align.scores.Blosum45;
import jebl.evolution.align.scores.Scores;
import jebl.evolution.sequences.AminoAcidState;
import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.taxa.Taxon;
import nzgot.core.logger.Logger;

/**
 * @author Alexei Drummond
 * @author Walter Xie
 * @author Thomas Hummel
 */
public class AlignAndCorrect {

    FScore scores;
    FNode[][] nodes;
    int m, n;

    FNode optimal = null;

    Sequence dna_read;
    Sequence aa_ref;

    int matches = 0;
    int mismatches = 0;
    int gaps = 0;
    int correctionDeletions = 0;
    int correctionInsertions = 0;
    
    //run getMatch() first to get all counts
    public int[] correctionCounts = new int[Correction.correctionHeader.length];
    
    /**
     * @param sub match score matrix
     * @param d   indel penalty
     * @param c   correction penalty
     */
    public AlignAndCorrect(Scores sub, float d, float c, float stopCodonPenalty, myGeneticCode geneticCode) {

        scores = new FScore(geneticCode, d, c, stopCodonPenalty, new Blosum45());
    }

    /**
     * First sequence is interpreted as nucleotide read and the second sequence is interpreted as amino acid reference
     *
     * @param read
     * @param aa
     */
    public void prepareAlignment(String read, String aa) {

        dna_read = new BasicSequence(SequenceType.NUCLEOTIDE, Taxon.getTaxon("read"), read.toUpperCase());
        aa_ref = new BasicSequence(SequenceType.AMINO_ACID, Taxon.getTaxon("aa_reference"), aa.toUpperCase());

        scores.read = dna_read;
        scores.ref = aa_ref;

        m = dna_read.getLength();
        n = aa_ref.getLength();

        //System.out.println("Nucleotide read of length      :" + m);
        //System.out.println("Amino acid reference of length :" + n);

        //first time
        // running this alignment. Create all new matrices.
        if (nodes == null) {
            nodes = new FNode[m + 1][n + 1];
        }

        //alignment already been run and existing matrix is big enough to reuse.
        else if (dna_read.getLength() <= m && aa_ref.getLength() <= n) {
            this.m = dna_read.getLength();
            this.n = aa_ref.getLength();
        }

        //alignment already been run but matrices not big enough for new alignment.
        //create all new matrices.
        else {
            this.m = dna_read.getLength();
            this.n = aa_ref.getLength();
            nodes = new FNode[m + 1][n + 1];
        }
    }

    /**
     *
     */
    public void doAlignment(String read, String aminoAcid) {

        prepareAlignment(read, aminoAcid);

        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {

                if (nodes[i][j] == null) {
                    nodes[i][j] = new FNode();
                }
                nodes[i][j].compute(i, j, nodes, scores);
            }
        }

        // Find maximal score on right-hand and bottom borders
        int maxi = -1, maxj = -1;
        double maxval = Double.NEGATIVE_INFINITY;
        for (int i = 0; i <= m; i++) {
            if (maxval < nodes[i][n].score) {
                maxi = i;
                maxval = nodes[i][n].score;
            }
        }
        for (int j = 0; j <= n; j++) {
            if (maxval < nodes[m][j].score) {
                maxj = j;
                maxval = nodes[m][j].score;
            }
        }

        //System.out.println("Maximum score = " + maxval);
        Logger.getLogger().debug("Maximum score = " + maxval);
        if (maxj != -1) {            // the maximum score was F[m][maxj]
            optimal = nodes[m][maxj];
        } else {                       // the maximum score was F[maxi][n]
            optimal = nodes[maxi][n];
        }
        Logger.getLogger().debug("Optimal found at " + optimal.i + ", " + optimal.j);
        //System.out.println("Optimal found at " + optimal.i + ", " + optimal.j);

    }
     
    public int[] getCorrectionCount() {
    	 return correctionCounts;
     }

    public String[] getMatch() {

        matches = 0;
        mismatches = 0;
        gaps = 0;
        correctionDeletions = 0;
        correctionInsertions = 0;
        
//        int[] correctionCounts = new int[Correction.correctionHeader.length];

        StringBuilder alignedRead = new StringBuilder();
        StringBuilder alignedRef = new StringBuilder();
        StringBuilder translatedRead = new StringBuilder();
        StringBuilder code = new StringBuilder();

        FNode node = optimal;
        AminoAcidState[] translation = new AminoAcidState[1];

        while (node.i > 0 && node.j > 0) {

            FEdge prefix = node.prefix();

            if (prefix == null) {
                return null;
                //System.out.println("finished at node " + node);
            } else {
                String nuc = null;
                switch (prefix.type) {
                    case ins_read_delete:
                        gaps += 1;
                    case match_delete:
                        Logger.getLogger().debug(scores.getCodonString(prefix.codon));
                        correctionDeletions += 1;
                        nuc = scores.getCorrectNuc(prefix);
                        Correction.count(correctionCounts, nuc, prefix.type);
                        break;
                    case ins_read_duplicate:
                        gaps += 1;
                    case match_duplicate:
                        correctionInsertions += 1;
                        nuc = scores.getCorrectNuc(prefix);
                        Correction.count(correctionCounts, nuc, prefix.type);
                        break;
                    case ins_read:
                    case ins_ref:
                        gaps += 1;
                }

                // output aligned read
                if (prefix.type != FNode.FType.ins_ref) {
                    alignedRead.append(scores.getCodonString(prefix.codon).reverse());

                    translation = scores.getTranslation(prefix.codon, translation);
                    if (translation.length == 1) {
                        translatedRead.append(" ").append(translation[0].getCode()).append(" ");
                    } else {
                        translatedRead.append(" ? ");
                    }

                } else {
                    alignedRead.append("---");
                    translatedRead.append(" - ");
                }

                // output reference amino acid
                if (prefix.type != FNode.FType.ins_read &&
                        prefix.type != FNode.FType.ins_read_duplicate &&
                        prefix.type != FNode.FType.ins_read_delete) {

                    AminoAcidState ref_state = (AminoAcidState) aa_ref.getState(node.j - 1);
                    alignedRef.append(" ").append(ref_state).append(" ");

                    if (translation != null && translation[0].equals(ref_state)) {
                        matches += 1;
                    } else {
                        mismatches += 1;
                    }
                } else {
                    alignedRef.append(" - ");
                }

                switch (prefix.type) {
                    case match:
                        code.append(" . ");
                        break;

                    case match_delete:
                    case ins_read_delete:
                        code.append("c- ");
                        break;
                    case match_duplicate:
                    case ins_read_duplicate:
                        code.append("c+ ");
                        break;
                    default: code.append("   ");
                }

                node = prefix.prefix;
            }
        }

        Logger.getLogger().debug("Del: " + correctionDeletions);
        Logger.getLogger().debug("Ins: " + correctionInsertions);

        String[] result = new String[]{ code.reverse().toString(), alignedRead.reverse().toString(),
                translatedRead.reverse().toString(), alignedRef.reverse().toString(),
                Correction.toString(correctionCounts)
        };

        return result;
    }
    
    /**
     * randomly insert and delete nucleotides
     *
     * @return random corrected sequence
     */
    public String getRandomCorrection() {
    	//TODO only affect homopolymers
    	
    	Random rand = new Random();
    	int[] count = correctionCounts;
    	char nuc;
    	StringBuilder string = new StringBuilder();
    	
    	
    	char[] seq = dna_read.getString().toCharArray();
    	List<Character> charList = new ArrayList<Character>();
    	
    	for(int i=0; i<seq.length; i++) {
    		charList.add(seq[i]);
    	}
    	
    	
    	while (count[0] > 0 || count[1] > 0 || count[2] > 0 || count[3] > 0) {
    		int rnd = rand.nextInt(charList.size());
    		nuc = charList.get(rnd);
    		switch (nuc) {
    		case 'A': 
    			if(count[0] > 0) {
    				charList.remove(rnd);
    				count[0]--;
    				break;
    			}
    		case 'C':
    			if(count[1] > 0) {
    				charList.remove(rnd);
    				count[1]--;
    				break;
    			}
    		case 'G':
    			if(count[2] > 0) {
    				charList.remove(rnd);
    				count[2]--;
    				break;
    			}
    		case 'T':
    			if(count[3] > 0) {
    				charList.remove(rnd);
    				count[3]--;
    				break;
    			}
    		default: break;
    		}
    	}
    	while (count[4] > 0 || count[5] > 0 || count[6] > 0 || count[7] > 0) {
    		int rnd = rand.nextInt(charList.size());
    		nuc = charList.get(rnd);
    		switch (nuc) {
    		case 'A':
    			if(count[4] > 0) {
    				charList.add(rnd+1, nuc);
    				count[4]--;
    				break;
    			}
    		case 'C':
    			if(count[5] > 0) {
    				charList.add(rnd+1, nuc);
    				count[5]--;
    				break;
    			}
    		case 'G':
    			if(count[6] > 0) {
    				charList.add(rnd+1, nuc);
    				count[6]--;
    				break;
    			}
    		case 'T':
    			if(count[7] > 0) {
    				charList.add(rnd+1, nuc);
    				count[7]--;
    				break;
    			}
    		default: break;
    		}
    	}
       	for(int i=0; i < charList.size(); i++) {
       		string.append(charList.get(i));
       	}
    	// random corrected sequence is not in frame!       	
       	return string.toString();
    }



    /**
     * Print the score, the F matrix, and the alignment
     *
     * @param out           output to print to
     * @param msg           message printed at start
     */
    public void doMatch(Output out, String msg, String[] match) {
        out.println(msg + ":");
        out.println("Score = " + optimal.score);

        out.println("An optimal alignment:");

        for (String s : match) {
            out.println(s);
        }

        out.println("matchs=" + matches + " mismatchs=" + mismatches + " gaps=" + gaps);
        out.println("corrections(del)=" + correctionDeletions + " corrections(insert)=" + correctionInsertions);
        out.println("amino acid percent identity=" + Math.round((double) matches * 1000.0 / (match[0].length()/3.0)) / 10.0 + "%");
    }

}