package com.github.doghere.json;

import com.github.doghere.Each;
import com.github.doghere.Row;
import com.github.doghere.Writer;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * Created by dog on 4/17/17.
 * //// TODO: 4/30/17 json writer 
 */
public class JSONWriter implements Writer<Row>,Each<Row> {
    @Override
    public void dealEach(Row row) throws Exception {

    }

    @Override
    public void setDisruptor(Disruptor<Row> disruptor) {

    }

    @Override
    public void before() throws Exception {

    }

    @Override
    public void after() throws Exception {

    }

    @Override
    public void onEvent(Row row) throws Exception {

    }
}
