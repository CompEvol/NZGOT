package nzgo.toolkit.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Element: basic class for naming, Comparable, Countable
 * @author Walter Xie
 */
public class Element implements Comparable{
    public static final String DEFAULT_NAME = "Unknown";

    protected String name;
    protected List<Counter> counters = new ArrayList<>();

    public Element() {
        addCounterStartFrom(0);
    }

    public Element(String name) {
        setName(name);
        addCounterStartFrom(0);
    }

    public Element(String name, int numOfCounters) {
        setName(name);
        for (int i=0; i < numOfCounters; i++) {
            addCounterStartFrom(0);
        }
    }

    public String getName() {
        if (name == null ) setName(DEFAULT_NAME);
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCountersSize() {
        return counters.size();
    }

    public void addCounter() {
        addCounterStartFrom(0);
    }

    /**
     * add counter started from initCount
     * @param initCount
     */
    public void addCounterStartFrom(int initCount) {
        Counter counter = new Counter(initCount);
        counters.add(counter);
    }

    public Counter getCounter() {
        return getCounter(0);
    }

    public Counter getCounter(int counterId) {
        if (counterId >= counters.size())
            throw new IllegalArgumentException("Invalid counter index, counterId = " + counterId + ", counters = " + counters);
        return counters.get(counterId);
    }

    public void setCounter(int counterId, Counter counter) {
        counters.set(counterId, counter);
    }

    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(Object o) {
        return toString().compareTo(o.toString());
    }

    @Override
    public boolean equals(Object obj) {
        return (toString().equalsIgnoreCase(obj.toString()));
    }

}
