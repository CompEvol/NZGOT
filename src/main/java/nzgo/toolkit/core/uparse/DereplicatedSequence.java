package nzgo.toolkit.core.uparse;


import nzgo.toolkit.core.sequences.SimpleSequence;

/**
 * Dereplication is the removal of duplicated sequences
 * http://www.drive5.com/usearch/manual/dereplication.html
 * @author Walter Xie
 */
// TODO add Hit ?
public class DereplicatedSequence extends SimpleSequence {

    // size = duplicated sequences removed + itself
    // use counter0 as annotatedSize

    public DereplicatedSequence(String name) {
        super(name);
        int annotatedSize = UCParser.getAnnotatedSize(name);
        if (annotatedSize > 0) {
            setName(UCParser.getLabelNoSizeAnnotation(name));
            setAnnotatedSize(annotatedSize);
        }
    }

    public int getAnnotatedSize() {
        return super.getCounter().getCount();
    }

    public void setAnnotatedSize(int annotatedSize) {
        super.getCounter().setCount(annotatedSize);
    }

}
