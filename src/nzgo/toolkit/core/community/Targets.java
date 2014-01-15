package nzgo.toolkit.core.community;


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
        return super.containsUniqueElement(targetName);
    }

    public E getTarget(String targetName) {
        return super.getUniqueElement(targetName);
    }

}
