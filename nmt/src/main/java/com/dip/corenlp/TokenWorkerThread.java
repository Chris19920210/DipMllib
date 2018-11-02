package com.dip.corenlp;

import java.util.concurrent.TimeUnit;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import utils.MyBlockingQueue;
import utils.MyFunctions;
import utils.QueueElement;


public class TokenWorkerThread implements Runnable {

    private final MyBlockingQueue<QueueElement<String>> read;
    private final MyBlockingQueue<QueueElement<String>> write;

    private MyFunctions.FiveFunction<QueueElement<String>, Integer, String[], StanfordCoreNLP[], QueueElement<String>> processor;
    private int batch;
    private StanfordCoreNLP[] pipelines;


    TokenWorkerThread(MyBlockingQueue<QueueElement<String>> read,
                      MyBlockingQueue<QueueElement<String>> write,
                      int batch,
                      MyFunctions.FiveFunction<QueueElement<String>, Integer, String[], StanfordCoreNLP[], QueueElement<String>> processor,
                      StanfordCoreNLP... pipelines) {
        this.read = read;
        this.write = write;
        this.pipelines = pipelines;
        this.processor = processor;
        this.batch = batch;
    }


    @Override
    public void run() {
        String[] results = new String[batch];
        int counter = 0;
        System.out.println("Thread=" + Thread.currentThread().getName() + " is parsing...");
        while (true) {
            try {
                if (this.read.get() > 0 && this.read.isEmpty()) {
                    this.write.increment();
                    break;
                }
                QueueElement<String> element = this.read.poll(2, TimeUnit.SECONDS);
                if (element == null) {
                    continue;
                }
                counter += 1;
                System.out.println("Thread=" + Thread.currentThread().getName() + " is parsing its " + counter + " batch");
                if (pipelines.length != 2) {
                    System.out.println(pipelines.length);
                }
                QueueElement<String> processed = this.processor.apply(element, batch, results, pipelines);

                this.write.add(processed);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
