package nzgot.core.community;

import jebl.util.Attributable;
import jebl.util.AttributableHelper;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Query Sequence
 * @author Walter Xie
 */
public class QuerySequence implements Attributable, Comparable{

    protected String name;
    protected double identity = 0;

    public QuerySequence(String name) {
        setName(name);
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
        return name.compareTo(o.toString());
    }

    public int compareTo(QuerySequence querySequence) {
        return Double.compare(this.identity, querySequence.getIdentity());
    }

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