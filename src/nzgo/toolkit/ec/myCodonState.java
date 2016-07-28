package nzgo.toolkit.ec;



/*
 * CodonState.java
 *
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

/**
 * As of 2007-07-30, instances of this class are only constructed for non-ambigous
 * nucleotide triplets - see {@link jebl.evolution.sequences.Codons}.
 * 
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id: CodonState.java 810 2007-10-12 00:40:45Z twobeers $
 */
public final class myCodonState extends State {
    myCodonState(String name, String stateCode, int index) {
        super(name, stateCode, index);
    }

    myCodonState(String name, String stateCode, int index, myCodonState[] ambiguities) {
        super(name, stateCode, index, ambiguities);
    }

    public boolean isGap() {
		return this == myCodons.GAP_STATE;
	}
}
