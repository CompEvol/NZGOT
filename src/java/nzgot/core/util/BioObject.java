package java.nzgot.core.util;

import java.util.HashSet;
import java.util.Set;

/**
 * BioObject
 * @author Walter Xie
 */
public class BioObject<E> {

    public Set<E> elementsSet = new HashSet<E>(); // elements are unique

    protected String name;

    public BioObject(String name) {
        this.name = name;
    }

    /**
     * add a unique element to the set, if duplication found, then throw Exception
     * @param e
     * @throws IllegalArgumentException
     */
    public void addUniqueElement(E e) throws IllegalArgumentException {
        if (!elementsSet.add(e))
            throw new IllegalArgumentException("Error: find duplicate " +
                    e.toString() + " in " + getName() + " !");
    }

    /**
     * given name to find the element matching this name in the set
     * @param name
     * @return
     * @throws IllegalArgumentException
     */
    public E getUniqueElement(String name) throws IllegalArgumentException {
        for (E e : elementsSet) {
            if (e.toString().equals(name)) return e;
        }
        return null;
    }

    public String getName() {
        if (name == null ) setName("Unknown");
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return getName();
    }


}
