package com.dip.corenlp;


import java.io.FileOutputStream;


public class WriteWorkerThread implements Runnable {
    private MyBlockingQueue<SentencePairs<String,String>> read;
    private FileOutputStream outZh;
    private FileOutputStream outEn;
    private int numThreads;

    public WriteWorkerThread(FileOutputStream outZh, FileOutputStream outEn, MyBlockingQueue<SentencePairs<String,String>> read, int numThreads){
        this.outZh = outZh;
        this.outEn = outEn;
        this.read = read;
        this.numThreads = numThreads;
    }


    @Override
    public void run() {
        String enSentences;
        String zhSentences;
        int counter = 0;
        while(true){
            try{
                if(this.read.get() == numThreads && this.read.isEmpty()){
                    break;
                }
                if(this.read.get() < numThreads && this.read.isEmpty() ){
                    Thread.sleep(1000);
                    continue;
                }

                counter += 1;
                SentencePairs<String,String> pair = this.read.take();

                enSentences = pair.enSentences;
                zhSentences = pair.zhSentences;
                this.outEn.write(enSentences.getBytes("UTF-8"));
                this.outEn.write("\n".getBytes("UTF-8"));
                this.outZh.write(zhSentences.getBytes("UTF-8"));
                this.outZh.write("\n".getBytes("UTF-8"));
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
