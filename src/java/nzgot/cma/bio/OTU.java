package awc.uoa.mes.tools.bio;

import java.util.ArrayList;
import java.util.List;

/**
 * OTU
 * @author Walter Xie
 */
public class OTU {

    protected String otuName;
    protected List<String> readsList = new ArrayList<String>();
    protected String refSeqId; // TODO convert to Sequence object

    public OTU(String otuName) {
        this.otuName = otuName;
    }

    public String getOTUName() {
        return otuName;
    }

    public void setOTUName(String otuName) {
        this.otuName = otuName;
    }

    public List<String> getReadsList() {
        return readsList;
    }

    public boolean addRead(String read) throws IllegalArgumentException {
        if (containsRead(read)) {
            throw new IllegalArgumentException("Error: find duplicate read (" + read + ") for OTU (" + otuName + ") !");
        } else {
            return this.readsList.add(read);
        }
    }

    public boolean containsRead(String read) {
        return this.readsList.contains(read);
    }

    public boolean removeRead(String read) {
        return this.readsList.remove(read);
    }

    public String getRefSeqId() {
        return refSeqId;
    }

    public void setRefSeqId(String refSeqId) {
        this.refSeqId = refSeqId;
    }

    public String toString() {
        return otuName;
    }
}
