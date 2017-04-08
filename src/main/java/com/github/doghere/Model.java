package com.github.doghere;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by dog on 4/6/17.
 */
final public class Model<E> {
    
    private Reader<E> reader ;
    private Writer<E> []writer;
    private Factory<E> factory;
    private int ringBufferSize=2^10;

    public Factory<E> getFactory() {
        return factory;
    }

    public Model setFactory(Factory<E> factory) {
        this.factory = factory;
        return this;
    }


    public Model setRingBufferSize(int ringBufferSize) {
        this.ringBufferSize = ringBufferSize;
        return this;
    }

    public Model setReader(Reader<E> reader) {
        this.reader = reader;
        return this;
    }

    public Model setWriter(Writer<E> []writer) {
        this.writer = writer;
        return this;
    }

    public void start() {

        long startTime = System.currentTimeMillis();
        try {
            reader.before();
            for(Writer w:writer) w.before();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Executor executor = Executors.newCachedThreadPool();
        Disruptor<E> disruptor = new Disruptor<E>(getFactory(),
                ringBufferSize,
                executor,
                ProducerType.SINGLE,
                new BlockingWaitStrategy());
        disruptor.handleEventsWithWorkerPool(writer);
        disruptor.start();

        RingBuffer<E> ringBuffer = disruptor.getRingBuffer();
        reader.setRingBuffer(ringBuffer);


        for (long l = 0; reader.hasRemaining(); l++) {
            try {
                reader.read();
                if(l%3000==0) {
                    System.out.println("read rows:\t" + l +"\tcost "+(System.currentTimeMillis()-startTime));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        try {
            disruptor.shutdown();
            reader.after();
            for(Writer w:writer) w.after();
            long endTime = System.currentTimeMillis();
            System.out.println("cost "+(endTime-startTime));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
