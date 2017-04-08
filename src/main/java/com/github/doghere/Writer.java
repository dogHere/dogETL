package com.github.doghere;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;

/**
 * Created by dog on 4/6/17.
 */
public interface Writer<E> extends Ready,WorkHandler<E> {
}
