package nzgo.toolkit.core.r;

import nzgo.toolkit.core.logger.MyLogger;
import nzgo.toolkit.core.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * resemble R data frame, only string type
 *
 * Note: use Java index to start from 0, not R index
 *
 * @author Walter Xie
 */
public class DataFrame<T> {

    protected List<List<T>> data = new ArrayList<>();
    protected List<String> colNames = new ArrayList<>();
    protected List<String> rowNames = new ArrayList<>();


    public DataFrame() { }

    public DataFrame(final List<String> colNames) {
        this.colNames = colNames;
        for (int i=0; i<colNames.size(); i++)
            this.data.add(new ArrayList<>());
    }

    public DataFrame(final List<String> colNames, final List<List<T>> data) {
        this(colNames);
        setData(data);
    }

    public int ncol() {
        return data.size();
    }

    public int nrow() {
        if (data.size() == 0) return 0;
        return data.get(0).size();
    }

    public List<String> getColNames() {
        return colNames;
    }

    // Note: use Java index to start from 0
    public String getColName(int col) {
        assert col >= 0 && col < ncol();
        return colNames.get(col);
    }
    public void setColNames(List<String> colNames) {
        this.colNames.clear();
        this.colNames.addAll(colNames);
    }

    public List<String> getRowNames() {
        return rowNames;
    }

    public void setRowNames(List<String> rowNames) {
        this.rowNames.clear();
        this.rowNames.addAll(rowNames);
    }

    public List<List<T>> getData() {
        return data;
    }

    // Note: use Java index to start from 0
    public List<T> getData(int col) {
        assert col >= 0 && col < ncol();
        return data.get(col);
    }

    // Note: use Java index to start from 0
    public void removeCol(int col) {
        assert col >= 0 && col < ncol();
        MyLogger.debug("Remove " + col + " column " + getColName(col));
        colNames.remove(col);
        data.remove(col);
    }


    // set data after set col names
    public void setData(List<List<T>> data) {
        assert data.size() == colNames.size();
        this.data.clear();
        this.data.addAll(data);
        // add row names if empty
        if (rowNames.size() < 1)
            setRowNames(StringUtil.getNames("", 0, nrow()));
    }

    public void appendRow(T[] row) {
        assert row.length == colNames.size();
        rowNames.add(Integer.toString(nrow() + 1));
        for (int i = 0; i < data.size(); i++) {
            List<T> colData = data.get(i);
            colData.add(row[i]);
            assert rowNames.size() == colData.size();
        }
    }
}
