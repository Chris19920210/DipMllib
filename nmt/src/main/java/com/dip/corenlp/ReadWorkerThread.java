package com.dip.corenlp;


import utils.MyBlockingQueue;

import java.io.BufferedReader;
import java.util.Arrays;

public class ReadWorkerThread implements Runnable {
    private BufferedReader in;
    private MyBlockingQueue<QueueElement<String>> read;
    private int batch;


    ReadWorkerThread(BufferedReader in, MyBlockingQueue<QueueElement<String>> read, int batch){
        this.in=in;
        this.read=read;
        this.batch=batch;
    }

    @Override
    public void run(){
        String str;
        int counter = 0;
        String[] tmp;
        String[] en = new String[batch];
        String[] zh = new String[batch];
        try{
            while((str = in.readLine()) != null) {
                counter += 1;
                tmp = str.split("\t");
                en[counter % batch - 1 < 0 ? batch - 1: counter % batch - 1] = tmp[0];
                zh[counter % batch - 1 < 0 ? batch - 1: counter % batch - 1] = tmp[1];
                if (counter % batch == 0) {
                    read.add(new SentencePairs(String.join("\n", en), String.join("\n", zh)));
                }
            }
            if(counter % batch != 0){
                read.add(new SentencePairs(String.join("\n", Arrays.asList(en).subList(0, counter % batch)),
                        String.join("\n", Arrays.asList(zh).subList(0, counter % batch))));
            }
            read.increment();
            in.close();
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
