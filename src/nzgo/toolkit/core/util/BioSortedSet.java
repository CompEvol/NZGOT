package nzgo.toolkit.core.util;

import nzgo.toolkit.core.logger.MyLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * BioSortedSet for most of bioinformatics concept
 * use add(E e) from super class, no warning no exception
 * use addElement(E e), no exception
 * use addUniqueElement(E e), throw exception if duplicate
 * @author Walter Xie
 */
public class BioSortedSet<E> extends TreeSet<E> implements Comparable<E>{

//    public SortedSet<E> elementsSet = new TreeSet<>(); // elements are unique, and sorted

    protected String name;

    public BioSortedSet() {
        super();
    }

    public BioSortedSet(String name) {
        this();
        setName(name);
    }

    public BioSortedSet(Collection<? extends E> c) {
        super(c);
    }

    /**
     * symmetric difference between this set and secondSet
     * @param secondSet
     * @return
     */
    public BioSortedSet<E> symmetricDiff(final BioSortedSet<E> secondSet) {
        BioSortedSet<E> symmetricDiff = new BioSortedSet<E>();
        symmetricDiff.addAll(this);
        symmetricDiff.addAll(secondSet);
        BioSortedSet<E> tmp = new BioSortedSet<E>(this);
        tmp.retainAll(secondSet);
        symmetricDiff.removeAll(tmp);
        return symmetricDiff;
    }

    /**
     * no exception to find duplication, but log warning
     * use add no warning no exception
     * @param e
     */
    public void addElement(E e) {
        if (!add(e))
            MyLogger.warn("find duplicate " + e.toString() + " in " + getName() + " !");
    }

    /**
     * add a unique element to the set, if duplication found, then throw Exception
     * @param e
     * @throws IllegalArgumentException
     */
    public void addUniqueElement(E e) throws IllegalArgumentException {
        if (!add(e))
            throw new IllegalArgumentException("Error: find duplicate " +
                    e.toString() + " in " + getName() + " !");
    }

    /**
     * given name to find the element matching this name in the set
     * @param name   Ignore Case
     * @return
     * @throws IllegalArgumentException
     */
    public E getUniqueElement(String name) throws IllegalArgumentException {
        for (E e : this) {
            if (e.toString().equalsIgnoreCase(name))
                return e;
        }
        return null;
    }

    public boolean containsUniqueElement(String name) throws IllegalArgumentException {
        for (E e : this) {
            if (e.toString().equalsIgnoreCase(name))
                return true;
        }
        return false;
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

    public String elementsToString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        int i = 0;
        for (E e : this) {
            if (i > 0)
                stringBuilder.append(",");
            stringBuilder.append(e);
            i++;
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public List<E> toList() {
        return new ArrayList<E>(this);
    }

    public boolean equalsIgnoreCase(String name) {
        return this.name.equalsIgnoreCase(name);
    }

    @Override
    public int compareTo(E o) {
        return name.compareTo(o.toString());
    }
}
