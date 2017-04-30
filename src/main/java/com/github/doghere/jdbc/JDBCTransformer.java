package com.github.doghere.jdbc;

import com.github.doghere.Row;
import com.github.doghere.Transformer;
import com.lmax.disruptor.EventHandler;

/**
 * Created by dog on 4/30/17.
 */
public class JDBCTransformer implements Transformer<Row> {


    @Override
    public void onEvent(Row event, long sequence, boolean endOfBatch) throws Exception {

        System.out.print(sequence+"\t"+endOfBatch+"\t"+event);
    }
}
