package nzgo.toolkit.core.community;

import nzgo.toolkit.core.util.BioSortedSet;

/**
 * Target is similar to the subject in BLAST
 * elementsSet contains Hits
 * @author Walter Xie
 */
public class Target<E> extends BioSortedSet<E> {
    protected E bestHit;

    public Target(String name) {
        super(name);
    }

    public E getBestHit() {
        if (bestHit == null)
            bestHit = findBestHit();
        return bestHit;
    }

    public E findBestHit() {
        double maxIdent = 0;
        E maxE = null;
        for (E e : this) {
            Hit hit = (Hit) e;
            if (maxIdent < hit.getIdentity()) {
                maxIdent = hit.getIdentity();
                maxE = e;
            }
        }
        return maxE;
    }

}
