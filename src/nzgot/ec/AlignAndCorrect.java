package nzgot.ec;

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


	public String[] getMatch() {

		matches = 0;
		mismatches = 0;
		gaps = 0;
		correctionDeletions = 0;
		correctionInsertions = 0;

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

				switch (prefix.type) {
				case ins_read_delete:
					gaps += 1;
				case match_delete:
//					node.codon(FType.match_delete, 0);
					Logger.getLogger().debug(scores.getCodonString(prefix.codon));
//					Logger.getLogger().debug(dna_read.getState(node.i).toString());
					correctionDeletions += 1; break;
				case ins_read_duplicate:
					gaps += 1;
				case match_duplicate:
					correctionInsertions += 1; break;
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
		
		String[] result = new String[]{ code.reverse().toString(),
				alignedRead.reverse().toString(), translatedRead.reverse().toString(), alignedRef.reverse().toString(),};

		return result;
		/*String corrected = result[1];
		return corrected;*/
	}




	/**
	 * Print the score, the F matrix, and the alignment
	 *
	 * @param out           output to print to
	 * @param msg           message printed at start
	 */
		public void doMatch(Output out, String msg) {
		out.println(msg + ":");
		out.println("Score = " + optimal.score);

		out.println("An optimal alignment:");
		String[] match = getMatch(	);
		for (String s : match) {
			out.println(s);
		}

		out.println("matchs=" + matches + " mismatchs=" + mismatches + " gaps=" + gaps);
		out.println("corrections(del)=" + correctionDeletions + " corrections(insert)=" + correctionInsertions);
		out.println("amino acid percent identity=" + Math.round((double) matches * 1000.0 / (match[0].length()/3.0)) / 10.0 + "%");

	}

}