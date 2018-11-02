package com.dip.corenlp;


import utils.MyBlockingQueue;
import utils.MyFunctions;

import java.io.FileOutputStream;
import java.util.concurrent.TimeUnit;



public class WriteWorkerThread implements Runnable {
    private MyBlockingQueue<QueueElement<String>> read;
    private FileOutputStream outZh;
    private FileOutputStream outEn;
    private int numThreads;
    private MyFunctions.ThreeFunction<FileOutputStream,FileOutputStream, QueueElement<String>> processor;

    WriteWorkerThread(FileOutputStream outZh, FileOutputStream outEn, MyBlockingQueue<QueueElement<String>> read,
                             int numThreads, MyFunctions.ThreeFunction<FileOutputStream,FileOutputStream, QueueElement<String>> processor){
        this.outZh = outZh;
        this.outEn = outEn;
        this.read = read;
        this.numThreads = numThreads;
        this.processor = processor;
    }


    @Override
    public void run() {
        int counter = 0;
        while(true){
            try{
                if(this.read.get() == numThreads && this.read.isEmpty()){
                    break;
                }

                QueueElement<String> pair = this.read.poll(1, TimeUnit.SECONDS);
                if(pair == null) {
                    continue;
                }

                counter += 1;
                this.processor.apply(outEn, outZh, pair);
                System.out.println("Batch=" + counter);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        try {
            System.out.println("Done");
            this.outEn.flush();
            this.outZh.flush();
        } catch(Exception e){
            e.printStackTrace();
        }

    }
}
