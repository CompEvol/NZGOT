package nzgo.toolkit.core.uparse;


import nzgo.toolkit.core.util.Element;

/**
 * Dereplication is the removal of duplicated sequences
 * http://www.drive5.com/usearch/manual/dereplication.html
 * @author Walter Xie
 */
// TODO add Hit ?
public class DereplicatedSequence extends Element {

    protected String sequence = null;

    public DereplicatedSequence(String name) {
        super(name);
        int annotatedSize = Parser.getAnnotatedSizeInt(name);
        if (annotatedSize > 0) {
            setName(Parser.getLabelNoSizeAnnotation(name));
            setAnnotatedSize(annotatedSize);
        }
    }

    public int getAnnotatedSize() {
        return super.getCounter().getCount();
    }

    public void setAnnotatedSize(int annotatedSize) {
        super.getCounter().setCount(annotatedSize);
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public boolean isIdenticalSequence(DereplicatedSequence ss) {
        // UPARSE change letter case somehow
        return this.getSequence().equalsIgnoreCase(ss.getSequence());
    }

}
