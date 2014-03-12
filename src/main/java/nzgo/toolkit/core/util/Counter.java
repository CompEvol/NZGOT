package nzgo.toolkit.core.util;

/**
 * Element: basic class for naming, Comparable, Countable
 * @author Walter Xie
 */
public class Counter implements Comparable, Countable{
    protected int count = 0;

    public Counter(int count) {
        setCount(count);
    }

    @Override
    public void incrementCount(int step) {
        this.count = count + step;
    }

    @Override
    public void setCount(int count) {
        this.count = count;
    }

    public Integer getCount() {
        return count;
    }

    @Override
    public int compareTo(Object o) {
        return getCount().compareTo(((Counter) o).getCount());
    }

    public String toString() {
        return Integer.toString(getCount());
    }

}
