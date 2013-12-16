package nzgo.tookit.ec;

/*
 * Codons.java
 *
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */


import jebl.evolution.sequences.NucleotideState;
import jebl.evolution.sequences.Nucleotides;

import java.util.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id: Codons.java 744 2007-07-30 02:57:11Z twobeers $
 */
public final class myCodons{
	public static final String NAME = "codon";

    public static final int CANONICAL_STATE_COUNT = 64;
    public static final int STATE_COUNT = 66;

    public static final myCodonState[] CANONICAL_STATES;
    public static final myCodonState[] STATES;

    // This bit of static code creates the 64 canonical codon states
    static {
        CANONICAL_STATES = new myCodonState[CANONICAL_STATE_COUNT];
        char[] nucs = new char[] { 'A', 'C', 'G', 'T' };
        int x = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    String code = "" + nucs[i] + nucs[j] + nucs[k];
                    CANONICAL_STATES[x] = new myCodonState(code, code, x);
                    x++;
                }
            }
        }

    }

    public static final myCodonState UNKNOWN_STATE = new myCodonState("?", "???", 64, CANONICAL_STATES);
    public static final myCodonState GAP_STATE = new myCodonState("-", "---", 65, CANONICAL_STATES);

    public static int getStateCount() { return STATE_COUNT; }

    public static List<State> getStates() { return Collections.unmodifiableList(Arrays.asList((State[])STATES)); }

    public static int getCanonicalStateCount() { return CANONICAL_STATE_COUNT; }

    public static List<State> getCanonicalStates() { return Collections.unmodifiableList(Arrays.asList((State[])CANONICAL_STATES)); }

	public static myCodonState getState(NucleotideState nucleotide1, NucleotideState nucleotide2, NucleotideState nucleotide3) {
		if (nucleotide1.isGap() && nucleotide2.isGap() && nucleotide3.isGap()) {
			return GAP_STATE;
		}

		if (nucleotide1.isAmbiguous() || nucleotide2.isAmbiguous() || nucleotide3.isAmbiguous()) {
			return UNKNOWN_STATE;
		}

	    String code = nucleotide1.getCode() + nucleotide2.getCode() + nucleotide3.getCode();
	    return statesByCode.get(code);
	}

    /**
     * Gets the state object for the given code. Returns null if the code is illegal
     * or contains ambiguous nucleotides.
     * @param code a three-character string of non-ambiguous nucleotides in uppercase
     * @return the state
     */
    public static myCodonState getState(String code) {
        //code=code.toUpperCase().replace('U','T');
        return statesByCode.get(code);
	}

	public static myCodonState getState(int index) {
	    return STATES[index];
	}

	public static myCodonState getUnknownState() { return UNKNOWN_STATE; }

	public static myCodonState getGapState() { return GAP_STATE; }

	public static boolean isUnknown(myCodonState state) { return state == UNKNOWN_STATE; }

	public static boolean isGap(myCodonState state) { return state == GAP_STATE; }

	public static NucleotideState[] toNucleotides(myCodonState state) {
		NucleotideState[] nucs = new NucleotideState[3];
		String code = state.getCode();
		nucs[0] = Nucleotides.getState(code.charAt(0));
		nucs[1] = Nucleotides.getState(code.charAt(1));
		nucs[2] = Nucleotides.getState(code.charAt(2));
		return nucs;
	}

	public static myCodonState[] toStateArray(String sequenceString) {
		int n = sequenceString.length() / 3;
		myCodonState[] seq = new myCodonState[n];
		for (int i = 0; i < n; i++) {
			seq[i] = getState(sequenceString.substring(i * 3, (i * 3) + 3));
		}
		return seq;
	}

	public static myCodonState[] toStateArray(byte[] indexArray) {
		myCodonState[] seq = new myCodonState[indexArray.length];
	    for (int i = 0; i < seq.length; i++) {
	        seq[i] = getState(indexArray[i]);
	    }
	    return seq;
	}

    // Contains 64 mappings, one for each triplet of non-ambiguous amino acids
    private static final Map<String, myCodonState> statesByCode;

    // now create the complete codon state array
    static {
        STATES = new myCodonState[STATE_COUNT];
        for (int i = 0; i < 64; i++) {
            STATES[i] = CANONICAL_STATES[i];
        }
        STATES[64] = UNKNOWN_STATE;
        STATES[65] = GAP_STATE;

        statesByCode = new HashMap<String, myCodonState>();
        for (int i = 0; i < STATES.length; i++) {
            statesByCode.put(STATES[i].getCode(), STATES[i]);
        }
    }

}
