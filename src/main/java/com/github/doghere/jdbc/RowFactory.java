package com.github.doghere.jdbc;

import com.github.doghere.Factory;

/**
 * Created by dog on 4/8/17.
 */
public class RowFactory implements Factory<Row> {
    @Override
    public Row newInstance() {
        return new Row();
    }
}
