package nzgo.toolkit.core.community;

/**
 * Hit: Represents a query-target alignment.
 * For clustering, indicates the cluster assignment for the query.
 * http://drive5.com/usearch/manual/ucout.html
 * similar to BasicSequence with sequence string
 * @author Walter Xie
 */
public class Hit implements Comparable{

    protected String name;
    protected double identity = 0;

    public Hit(String name) {
        setName(name);
    }

    public Hit(String name, double identity) {
        setName(name);
        setIdentity(identity);
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

    public double getIdentity() {
        return identity;
    }

    public void setIdentity(double identity) {
        this.identity = identity;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof Hit)
            return compareTo((Hit) o);
        else
            return name.compareTo(o.toString());
    }

    public int compareTo(Hit hit) {
        return Double.compare(this.identity, hit.getIdentity());
    }
}
