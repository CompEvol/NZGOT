package java.nzgot.cma.core;

import java.nzgot.core.util.BioObject;

/**
 * OTU
 * @author Walter Xie
 */
public class OTU extends BioObject {

    protected String refSeqId; // TODO convert to Sequence object

    public OTU(String name) {
        super(name);
    }

    public String getRefSeqId() {
        return refSeqId;
    }

    public void setRefSeqId(String refSeqId) {
        this.refSeqId = refSeqId;
    }
}
