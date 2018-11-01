package com.dip.corenlp;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;


public class MyBlockingQueue<E> extends LinkedBlockingDeque<E> {
    private AtomicInteger count = new AtomicInteger(0);


    public MyBlockingQueue()
    {
        super();
    }
    public MyBlockingQueue(int capacity)
    {
        super(capacity);
    }

    public void increment(){
        count.incrementAndGet();
    }

    public int get(){
        return count.get();
    }


}
