package com.dip.corenlp;


import utils.MyBlockingQueue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
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
            workers.execute(new TokenWorkerThread(read, write, batch, ProcessUtils::TrueCaseTokenizeProcessor));
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
