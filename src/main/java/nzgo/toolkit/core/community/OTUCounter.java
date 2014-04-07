package nzgo.toolkit.core.community;

import nzgo.toolkit.core.util.Counter;
import nzgo.toolkit.core.util.Element;

/**
 * OTUCounter to generate CM faster, not store any information of reads
 * @author Walter Xie
 */
public class OTUCounter extends Element {

    public OTUCounter(String name) {
        super(name);
        addCounter(); // add 2nd counter for number of otu

        assert getCountersSize() == 2;
    }

    public Counter getReadsCounter() {
        return getCounter(OTUs.READS_COUNTER_ID);
    }

    public Counter getOTUsCounter() {
        return getCounter(OTUs.OTUS_COUNTER_ID);
    }

}