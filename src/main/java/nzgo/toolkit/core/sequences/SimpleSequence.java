package nzgo.toolkit.core.sequences;


import nzgo.toolkit.core.util.Element;

/**
 *
 * @author Walter Xie
 */
// TODO add Hit ?
public class SimpleSequence extends Element {

    protected String sequence = null;

    public SimpleSequence(String name) {
        super(name);
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public boolean isIdenticalSequence(SimpleSequence ss) {
        // UPARSE change letter case somehow
        return this.getSequence().equalsIgnoreCase(ss.getSequence());
    }
}
