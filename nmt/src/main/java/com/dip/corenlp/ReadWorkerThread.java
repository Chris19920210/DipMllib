package com.dip.corenlp;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

public class ReadWorkerThread implements Runnable {
    private BufferedReader in;
    private MyBlockingQueue<SentencePairs<String,String>> read;
    private int batch;


    public ReadWorkerThread(BufferedReader in, MyBlockingQueue<SentencePairs<String, String>> read, int batch){
        this.in=in;
        this.read=read;
        this.batch=batch;
    }

    @Override
    public void run(){
        String str;
        int counter = 0;
        String[] tmp;
        List<String> en = new ArrayList<>();
        List<String> zh = new ArrayList<>();
        try{
            while((str = in.readLine()) != null) {
                counter += 1;
                tmp = str.split("\t");
                en.add(tmp[0]);
                zh.add(tmp[1]);
                if (counter % batch == 0) {
                    read.add(new SentencePairs<>(String.join("\n", en), String.join("\n", zh)));
                    en.clear();
                    zh.clear();
                }
            }
            if(en.size() != 0){
                read.add(new SentencePairs<>(String.join("\n", en), String.join("\n", zh)));
                en.clear();
                zh.clear();
            }
            read.increment();
            in.close();
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
