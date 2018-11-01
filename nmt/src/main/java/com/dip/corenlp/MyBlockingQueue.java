package com.dip.corenlp;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;


public class MyBlockingQueue<E> extends LinkedBlockingDeque<E> {
    private boolean finish;
    private boolean lastElement;

    public MyBlockingQueue()
    {
        super();
        this.finish = false;
        this.lastElement = false;
    }
    public MyBlockingQueue(int capacity)
    {
        super(capacity);
        this.finish = false;
        this.lastElement = false;
    }

    public void setLastElement(boolean lastElement) {
        this.lastElement = lastElement;
    }

    public boolean getLastElement(){
        return this.lastElement;
    }


}
