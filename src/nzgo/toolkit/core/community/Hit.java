package nzgo.toolkit.core.community;

import jebl.util.Attributable;
import jebl.util.AttributableHelper;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Hit: Represents a query-target alignment.
 * For clustering, indicates the cluster assignment for the query.
 * http://drive5.com/usearch/manual/ucout.html
 * similar to BasicSequence with sequence string
 * @author Walter Xie
 */
public class Hit implements Attributable, Comparable{

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
            return compareTo( ((Hit) o).getIdentity() );
        else
            return name.compareTo(o.toString());
    }

    public int compareTo(Hit hit) {
        return Double.compare(this.identity, hit.getIdentity());
    }

    // Taxonomy
    // TODO

    // Attributable IMPLEMENTATION

    private AttributableHelper helper = null;

    public void setAttribute(String name, Object value) {
        if (helper == null) {
            helper = new AttributableHelper();
        }
        helper.setAttribute(name, value);
    }

    public Object getAttribute(String name) {
        if (helper == null) {
            return null;
        }
        return helper.getAttribute(name);
    }

    public void removeAttribute(String name) {
        if (helper != null) {
            helper.removeAttribute(name);
        }
    }

    public Set<String> getAttributeNames() {
        if (helper == null) {
            return Collections.emptySet();
        }
        return helper.getAttributeNames();
    }

    public Map<String, Object> getAttributeMap() {
        if (helper == null) {
            return Collections.emptyMap();
        }
        return helper.getAttributeMap();
    }

}
