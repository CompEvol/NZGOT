package nzgo.toolkit.core.uc;


/**
 *
 * @author Walter Xie
 */
// TODO Merge to OTU and HIT
public class TargetSequence extends DereplicatedSequence {

    protected double identity = 100;

    public TargetSequence(String name) {
        super(name);
    }

    public double getIdentity() {
        return identity;
    }

    public void setIdentity(double identity) {
        this.identity = identity;
    }
}
