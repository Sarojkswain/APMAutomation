package com.ca.apm.systemtest.fld.util.networktrafficmonitor.jfreechart;

import org.apache.http.util.Args;
import org.jfree.data.xy.XYDataItem;

public abstract class BaseXYDataItemRowMapper implements RowMapper<XYDataItem> {

    private int indexX = -1;
    private int indexY = -1;

    protected BaseXYDataItemRowMapper() {}

    protected BaseXYDataItemRowMapper(int indexX, int indexY) {
        this.indexX = indexX;
        this.indexY = indexY;
//        Args.notNegative(indexX, "indexX");
//        Args.notNegative(indexY, "indexY");
    }

    @Override
    public XYDataItem mapRow(String[] row) {
        double x = Long.parseLong(row[indexX]);
        x = computeX(x);
        double y = Long.parseLong(row[indexY]);
        y = computeY(y);
        return new XYDataItem(x, y);
    }

    protected abstract double computeX(double x);

    protected abstract double computeY(double y);

    public int getIndexX() {
        return indexX;
    }

    public void setIndexX(int indexX) {
        this.indexX = indexX;
    }

    public int getIndexY() {
        return indexY;
    }

    public void setIndexY(int indexY) {
        this.indexY = indexY;
    }

}
