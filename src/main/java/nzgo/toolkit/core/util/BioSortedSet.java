package nzgo.toolkit.core.util;

import nzgo.toolkit.core.logger.MyLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * BioSortedSet
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
     * no exception to find duplication
     * @param e
     */
    public void addElement(E e) {
        add(e);
    }

    public void addElement(E e, boolean warnDuplicate) {
        if (!add(e) && warnDuplicate)
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
     * @param name
     * @return
     * @throws IllegalArgumentException
     */
    public E getUniqueElement(String name) throws IllegalArgumentException {
        for (E e : this) {
            if (e.toString().contentEquals(name))
                return e;
        }
        return null;
    }

    public boolean containsUniqueElement(String name) throws IllegalArgumentException {
        for (E e : this) {
            if (e.toString().contentEquals(name))
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
