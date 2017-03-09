package com.github.doghere.util;

import java.nio.InvalidMarkException;

/**
 * Created by dog on 12/19/16.
 * <p>
 * A buffer like nio buffer.
 *
 * @author dogHere@tutamail.com
 */
public class Buffer {


    //    private Object []rows;
    // Invariants: mark <= position <= limit <= capacity
    private int mark = -1;
    private int position = 0;
    private int limit;
    private int capacity;


    public Buffer(int mark, int pos, int lim, int cap) {       // package-private
        if (cap < 0)
            throw new IllegalArgumentException("Negative capacity: " + cap);
        this.capacity = cap;
        limit(lim);
        position(pos);
        if (mark >= 0) {
            if (mark > pos)
                throw new IllegalArgumentException("mark > position: ("
                        + mark + " > " + pos + ")");
            this.mark = mark;
        }

//        this.rows = new Object[cap];
    }

    public Buffer(int cap) {
        this(-1, 0, cap, cap);
    }

    public static Buffer allocate(int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException();
        return new Buffer(-1, 0, 0, capacity);
    }

    public synchronized void down() {
        if (position > 0) {
            position--;
        }
    }


    public synchronized void grow() {
        if (hasRemaining()) {
            position++;
        }
    }

    public final int capacity() {
        return capacity;
    }

    public final int position() {
        return position;
    }

    public final Buffer position(int newPosition) {
        if ((newPosition > limit) || (newPosition < 0))
            throw new IllegalArgumentException();
        position = newPosition;
        if (mark > position) mark = -1;
        return this;
    }

    public final int limit() {
        return limit;
    }

    public final Buffer limit(int newLimit) {
        if ((newLimit > capacity) || (newLimit < 0))
            throw new IllegalArgumentException();
        limit = newLimit;
        if (position > limit) position = limit;
        if (mark > limit) mark = -1;
        return this;
    }

    public final Buffer mark() {
        mark = position;
        return this;
    }

    public final Buffer reset() {
        int m = mark;
        if (m < 0)
            throw new InvalidMarkException();
        position = m;
        return this;
    }

    public final Buffer clear() {
        position = 0;
        limit = capacity;
        mark = -1;
        return this;
    }

    public final Buffer flip() {
        limit = position;
        position = 0;
        mark = -1;
        return this;
    }

    public final Buffer rewind() {
        position = 0;
        mark = -1;
        return this;
    }

    public Buffer extend(int size){
        this.capacity+=size;
        this.limit = capacity;
        return this;
    }

    public final int remaining() {
        return limit - position;
    }

    public final boolean hasRemaining() {
        return position < limit;
    }

    public boolean isReadOnly() {
        return false;
    }


}
