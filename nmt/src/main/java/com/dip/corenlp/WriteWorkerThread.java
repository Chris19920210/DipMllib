package com.dip.corenlp;


import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class WriteWorkerThread implements Runnable {
    private MyBlockingQueue<SentencePairs<String,String>> read;
    private FileOutputStream outZh;
    private FileOutputStream outEn;
    private int batch;

    public WriteWorkerThread(FileOutputStream outZh, FileOutputStream outEn, MyBlockingQueue<SentencePairs<String,String>> read, int batch){
        this.outZh = outZh;
        this.outEn = outEn;
        this.read = read;
        this.batch = batch;
    }



    @Override
    public void run() {
        String enSentences;
        String zhSentences;
        int counter = 0;
        List<String> enResults = new ArrayList<>(batch);
        List<String> zhResults = new ArrayList<>(batch);
        while(true){
            try{
                if(this.read.getLastElement() && this.read.isEmpty()){
                    break;
                }
                SentencePairs<String,String> pair = read.take();
                enSentences = pair.enSentences;
                zhSentences = pair.zhSentences;

                String[] en = enSentences.split("\n");
                String[] zh = zhSentences.split("\n");

                for(int i = 0;  i < en.length; i++) {
                    counter += 1;
                    enResults.add(en[i]);
                    zhResults.add(zh[i]);
                    if(counter % batch == 0){
                        outEn.write(String.join("\n", enResults).getBytes("UTF-8"));
                        outEn.write("\n".getBytes("UTF-8"));
                        enResults.clear();
                        outZh.write(String.join("\n", enResults).getBytes("UTF-8"));
                        outZh.write("\n".getBytes("UTF-8"));
                        zhResults.clear();
                        System.out.println("Batch=" + counter/batch);
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        try {
            if(enResults.size() != 0){
                outEn.write(String.join("\n", enResults).getBytes("UTF-8"));
                outEn.write("\n".getBytes("UTF-8"));
                outEn.flush();
                enResults.clear();
                outZh.write(String.join("\n", enResults).getBytes("UTF-8"));
                outZh.write("\n".getBytes("UTF-8"));
                outZh.flush();
                zhResults.clear();
                System.out.println("Done, with total records="+counter);
            }
        } catch(Exception e){
            e.printStackTrace();
        }

    }
}
