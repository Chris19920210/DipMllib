package com.dip.corenlp;

import utils.*;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BatchProcessOneLangMultipleThreads {
    private MyBlockingQueue<QueueElement<String>> read;
    private MyBlockingQueue<QueueElement<String>> write;
    private FileOutputStream out;
    private BufferedReader in;
    private int batch;
    private int numThreads;
    private Properties props = new Properties();
    private String lang;


    private BatchProcessOneLangMultipleThreads(String inputPath, String outputPath, int batch, int numThreads, String lang) {
        this.batch = batch;
        this.read = new MyBlockingQueue<>();
        this.write = new MyBlockingQueue<>();
        this.numThreads = numThreads;
        this.lang = lang;


        try {
            this.out = new FileOutputStream(new File(outputPath + "." + this.lang));
            this.in = new BufferedReader(new FileReader(inputPath));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (this.lang.equals("en")) {
            try {
                props.setProperty("annotators", "tokenize,ssplit,truecase");
                props.setProperty("ssplit.newlineIsSentenceBreak", "always");
                props.setProperty("ssplit.boundaryTokenRegex", "\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (this.lang.equals("zh")) {
            InputStream in = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("StanfordCoreNLP-chinese.properties");
            try {
                props.load(in);
                props.setProperty("annotators", "tokenize,ssplit");
                props.setProperty("ssplit.newlineIsSentenceBreak", "always");
                props.setProperty("ssplit.boundaryTokenRegex", "\n");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {
            System.out.println("Only support zh and en, no support for " + this.lang);
            System.exit(0);
        }

    }

    private void process() {

        Thread reader = new Thread(new ReadWorkerThread<>(read, batch, ProcessUtils::SingleSentenceReader, in));
        reader.start();

        Thread writer = new Thread(new WriteWorkerThread<>(write, numThreads, ProcessUtils::SingleSentenceWriter, out));
        writer.start();

        ExecutorService workers = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            MyCoreNLP pipeline = new MyCoreNLP(props);
            if (this.lang.equals("en")) {
                workers.execute(new ProcessWorkerThread(read, write, batch, ProcessUtils::EnTokenizeProcessor, pipeline));

            } else {
                workers.execute(new ProcessWorkerThread(read, write, batch, ProcessUtils::ZhTokenizeProcessor, pipeline));
            }
        }
        workers.shutdown();
    }

    public static void main(String[] args) {
        String inputPath = args[0];
        String outputPath = args[1];
        int batch = Integer.parseInt(args[2]);
        int numThreads = Integer.parseInt(args[3]);
        String lang = args[4];
        BatchProcessOneLangMultipleThreads batchProcessOneLangMultipleThreads = new BatchProcessOneLangMultipleThreads(inputPath, outputPath, batch, numThreads, lang);
        batchProcessOneLangMultipleThreads.process();
    }


}
