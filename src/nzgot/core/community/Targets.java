package nzgot.core.community;


/**
 * the set to keep all Targets
 * elementsSet contains Target
 * @author Walter Xie
 */
public class Targets<E> extends OTUs<E> {

    public Targets(String name) {
        super(name);
    }

    public boolean containsTarget(String targetName) {
        return super.containsOTU(targetName);
    }

    public E getTarget(String targetName) {
        return super.getOTU(targetName);
    }

}
