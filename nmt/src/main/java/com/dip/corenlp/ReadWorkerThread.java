package com.dip.corenlp;


import utils.MyBlockingQueue;
import utils.MyFunctions;

import java.io.BufferedReader;

public class ReadWorkerThread implements Runnable {
    private BufferedReader[] ins;
    private MyBlockingQueue<QueueElement<String>> read;
    private int batch;
    private MyFunctions.ThreeFunction<MyBlockingQueue<QueueElement<String>>, Integer, BufferedReader[]> processor;


    ReadWorkerThread(MyBlockingQueue<QueueElement<String>> read,
                     int batch,
                     MyFunctions.ThreeFunction<MyBlockingQueue<QueueElement<String>>, Integer, BufferedReader[]> processor,
                     BufferedReader... ins) {
        this.ins = ins;
        this.read = read;
        this.batch = batch;
        this.processor = processor;
    }

    @Override
    public void run() {

        try {
            this.processor.apply(this.read, batch, ins);
            this.read.increment();
            for (BufferedReader in : ins) {
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
