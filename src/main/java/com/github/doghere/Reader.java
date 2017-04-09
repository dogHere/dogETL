package com.github.doghere;

import com.lmax.disruptor.*;

import java.sql.SQLException;

/**
 * Created by dog on 4/6/17.
 */
public interface Reader<E> extends Ready{

    public void read() throws Exception;
    public void setRingBuffer(RingBuffer ringBuffer);
    public boolean hasRemaining();

}
