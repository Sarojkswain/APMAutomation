package com.ca.apm.systemtest.fld.util.networktrafficmonitor.jfreechart;

import org.apache.http.util.Args;

public class ValueDivisionXYDataItemRowMapperImpl extends BaseXYDataItemRowMapper {

    private double factorX = 1;
    private double factorY = 1;

    protected ValueDivisionXYDataItemRowMapperImpl() {
        super();
    }

    public ValueDivisionXYDataItemRowMapperImpl(double factorX, double factorY) {
        super();
        this.factorX = factorX;
        this.factorY = factorY;
        Args.check(factorX != 0, "zero value not allowed: factorX");
        Args.check(factorY != 0, "zero value not allowed: factorY");
    }

    public ValueDivisionXYDataItemRowMapperImpl(int indexX, int indexY, double factorX,
        double factorY) {
        super(indexX, indexY);
        this.factorX = factorX;
        this.factorY = factorY;
        Args.check(factorX != 0, "zero value not allowed: factorX");
        Args.check(factorY != 0, "zero value not allowed: factorY");
    }

    @Override
    protected double computeX(double x) {
        return x / factorX;
    }

    @Override
    protected double computeY(double y) {
        return y / factorY;
    }

    public double getFactorX() {
        return factorX;
    }

    public void setFactorX(double factorX) {
        this.factorX = factorX;
    }

    public double getFactorY() {
        return factorY;
    }

    public void setFactorY(double factorY) {
        this.factorY = factorY;
    }

}
