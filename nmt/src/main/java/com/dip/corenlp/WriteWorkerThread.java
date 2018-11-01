package com.dip.corenlp;


import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class WriteWorkerThread implements Runnable {
    private MyBlockingQueue<SentencePairs<String,String>> read;
    private FileOutputStream out;
    private int batch;

    public WriteWorkerThread(FileOutputStream out, MyBlockingQueue<SentencePairs<String,String>> read, int batch){
        this.out = out;
        this.read = read;
    }

    @Override
    public void run() {
        String enSentences;
        String zhSentences;
        int counter = 0;
        List<String> results = new ArrayList<>();
        while(true){
            try{
                SentencePairs<String,String> pair = read.take();
                enSentences = pair.enSentences;
                zhSentences = pair.zhSentences;
                if(enSentences.equals("F") && zhSentences.equals("F")){
                    out.close();
                    break;
                }
                String[] en = enSentences.split("\n");
                String[] zh = zhSentences.split("\n");

                for(int i = 0;  i < en.length; i++) {
                    counter += 1;
                    String line = en[i] + "\t" + zh[i];
                    results.add(line);
                    if(counter % batch == 0){
                        out.write(String.join("\n", results).getBytes("UTF-8"));
                        out.write("\n".getBytes("UTF-8"));
                        results.clear();
                        System.out.println("Batch=" + counter/batch);
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        try {
            if(results.size() != 0){
                out.write(String.join("\n", results).getBytes("UTF-8"));
                out.flush();
                results.clear();
                System.out.println("Done, with total records="+counter);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
