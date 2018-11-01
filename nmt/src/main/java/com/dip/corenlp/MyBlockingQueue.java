package com.dip.corenlp;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;


public class MyBlockingQueue<E> extends LinkedBlockingDeque<E> {
    private AtomicInteger count;


    public MyBlockingQueue()
    {
        super();
        count = new AtomicInteger(0);
    }
    public MyBlockingQueue(int capacity)
    {
        super(capacity);
        count = new AtomicInteger(0);
    }

    public void increment(){
        count.incrementAndGet();
    }

    public int get(){
        return count.get();
    }


}
