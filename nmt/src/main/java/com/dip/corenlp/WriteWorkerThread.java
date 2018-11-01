package com.dip.corenlp;


import java.io.FileOutputStream;


public class WriteWorkerThread implements Runnable {
    private MyBlockingQueue<SentencePairs<String,String>> read;
    private FileOutputStream outZh;
    private FileOutputStream outEn;

    public WriteWorkerThread(FileOutputStream outZh, FileOutputStream outEn, MyBlockingQueue<SentencePairs<String,String>> read){
        this.outZh = outZh;
        this.outEn = outEn;
        this.read = read;
    }


    @Override
    public void run() {
        String enSentences;
        String zhSentences;
        int counter = 0;
        while(true){
            try{
                if(this.read.getLastElement() && this.read.isEmpty()){
                    break;
                }
                counter += 1;
                SentencePairs<String,String> pair = read.take();

                enSentences = pair.enSentences;
                zhSentences = pair.zhSentences;
                outEn.write(enSentences.getBytes("UTF-8"));
                outEn.write("\n".getBytes("UTF-8"));
                outZh.write(zhSentences.getBytes("UTF-8"));
                outZh.write("\n".getBytes("UTF-8"));
                System.out.println("Batch=" + counter);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        try {
            System.out.println("Done");
            outEn.flush();
            outZh.flush();
        } catch(Exception e){
            e.printStackTrace();
        }

    }
}
