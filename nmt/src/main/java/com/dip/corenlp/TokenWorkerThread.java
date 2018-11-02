package com.dip.corenlp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import utils.MyBlockingQueue;
import utils.MyFunctions;


public class TokenWorkerThread implements Runnable {

    private final MyBlockingQueue<QueueElement<String>> read;
    private final MyBlockingQueue<QueueElement<String>> write;
    private Properties zhProps = new Properties();
    private Properties enProps = new Properties();
    private MyFunctions.FiveFunction<QueueElement<String>, Integer, String[], StanfordCoreNLP[], QueueElement<String>> processor;
    private int batch;
    private StanfordCoreNLP[] pipelines = new StanfordCoreNLP[2];

    private void loadChinese() {
        InputStream zhIn = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("StanfordCoreNLP-chinese.properties");
        try {
            zhProps.load(zhIn);
            zhProps.setProperty("annotators", "tokenize,ssplit");
            zhProps.setProperty("ssplit.newlineIsSentenceBreak", "always");
            zhProps.setProperty("ssplit.boundaryTokenRegex", "\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (zhIn != null) zhIn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void loadEnglish() {
        try {
            enProps.setProperty("annotators", "tokenize,ssplit,truecase");
            enProps.setProperty("ssplit.newlineIsSentenceBreak", "always");
            enProps.setProperty("ssplit.boundaryTokenRegex", "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    TokenWorkerThread(MyBlockingQueue<QueueElement<String>> read,
                      MyBlockingQueue<QueueElement<String>> write,
                      int batch,
                      MyFunctions.FiveFunction<QueueElement<String>, Integer, String[], StanfordCoreNLP[], QueueElement<String>> processor,
                      String lang) {
        this.read = read;
        this.write = write;
        switch (lang) {
            case "both":
                this.loadChinese();
                this.loadEnglish();
                pipelines[0] = new StanfordCoreNLP(enProps);
                pipelines[1] = new StanfordCoreNLP(zhProps);

            case "zh":
                this.loadChinese();
                pipelines[0] = new StanfordCoreNLP(zhProps);
            case "en":
                this.loadEnglish();
                pipelines[0] = new StanfordCoreNLP(enProps);
        }
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
