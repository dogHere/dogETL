package com.github.doghere;

import com.github.doghere.Factory;
import com.github.doghere.Row;

/**
 * Created by dog on 4/8/17.
 */
public class RowFactory implements Factory<Row> {
    @Override
    public Row newInstance() {
        return new Row();
    }
}
