package com.github.doghere;



import com.github.doghere.util.Log;
import com.github.doghere.util.Status;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * This is a queue
 * Created by dogHere@tutamail.com on 3/11/17.
 */
public class Q<E> extends LinkedBlockingQueue<E> {


    private F f ;

    /**
     * Init with size.
     * @param size
     */
    public Q(int size){
        super(size);
    }

    /**
     * Init with size 3000 default.
     */
    public Q() {
        super(3000);
    }


    /**
     * To get f.
     * @return f
     */
    public F getF() {
        return f;
    }

    /**
     * To set f
     * @param f
     * @return self
     */
    public Q setF(F f) {
        this.f = f;
        return this;

    }


    /**
     * To fetch rows from queue,return table
     * @param size
     * @param m M
     * @return T
     * @throws InterruptedException
     */
    public T fetch(int size,M m) throws InterruptedException {
        return fetch(size,m,2000,5);
    }

    /**
     * To fetch rows from queue,return table.
     * @param size
     * @param m
     * @param maxTryTimes
     * @param timeout
     * @return
     * @throws InterruptedException
     */
    public T fetch(int size,M m,int maxTryTimes,int timeout) throws InterruptedException {

        T t = new T(size);
        t.setF(this.f);


        int put = 0;
        int tryTimes = 0;
        while (t.capacity() - t.position() >= 0) {

            if (put >= size) {
                m.stopReading();
                return t;
            }
            if (m.getStatus().equals(Status.READ_OUT)) {
                if (this.size() == 0) {
                    m.stopReading();
                    return t;

                } else {
                    Object r = this.take();
                    t.put(r);
                    put++;
                }

            }else if(m.getStatus().equals(Status.READING)){
                if (this.size() == 0) {
                    if(tryTimes>=maxTryTimes){
                        m.stopReading();
                    } else{
                        tryTimes++;
                        Log.getLogger().trace(" timeout " + tryTimes * timeout);
                        Thread.sleep(timeout);
                        continue;
                    }


                } else {
                    Object r = this.take();
                    t.put(r);
                    put++;
                }

            } else {
                synchronized (m){
                    m.wait();
                }
            }
        }
        m.stopReading();
        return t;
    }


}
