package com.dip.corenlp;


import utils.*;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BatchProcessMultipleThread {
    private MyBlockingQueue<QueueElement<String>> read;
    private MyBlockingQueue<QueueElement<String>> write;
    private FileOutputStream outZh;
    private FileOutputStream outEn;
    private BufferedReader in;
    private int batch;
    private int numThreads;
    private Properties zhProps = new Properties();
    private Properties enProps = new Properties();

    {
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

        try {
            enProps.setProperty("annotators", "tokenize,ssplit,truecase");
            enProps.setProperty("ssplit.newlineIsSentenceBreak", "always");
            enProps.setProperty("ssplit.boundaryTokenRegex", "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BatchProcessMultipleThread(String inputPath, String outputPath, int batch, int numThreads) {
        this.batch = batch;
        this.read = new MyBlockingQueue<>();
        this.write = new MyBlockingQueue<>();
        this.numThreads = numThreads;

        try {
            this.outZh = new FileOutputStream(new File(outputPath + ".zh"));
            this.outEn = new FileOutputStream(new File(outputPath + ".en"));
            this.in = new BufferedReader(new FileReader(inputPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void process() {

        Thread reader = new Thread(new ReadWorkerThread<>(read, batch, ProcessUtils::SentencePairReader, in));
        reader.start();

        Thread writer = new Thread(new WriteWorkerThread<>(write, numThreads, ProcessUtils::SentencePairWriter, outEn, outZh));
        writer.start();

        ExecutorService workers = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            MyCoreNLP enPipeline = new MyCoreNLP(enProps);
            MyCoreNLP zhPipeline = new MyCoreNLP(zhProps);
            workers.execute(new ProcessWorkerThread(read, write, batch, ProcessUtils::TrueCaseTokenizeProcessor, enPipeline, zhPipeline));
        }
        workers.shutdown();
    }

    public static void main(String[] args) {
        String inputPath = args[0];
        String outputPath = args[1];
        int batch = Integer.parseInt(args[2]);
        int numThreads = Integer.parseInt(args[3]);
        BatchProcessMultipleThread batchProcessMultipleThread = new BatchProcessMultipleThread(inputPath, outputPath, batch, numThreads);
        batchProcessMultipleThread.process();
    }
}
