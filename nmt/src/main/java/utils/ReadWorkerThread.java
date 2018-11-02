package utils;


import java.io.BufferedReader;

public class ReadWorkerThread<T> implements Runnable {
    private BufferedReader[] ins;
    private MyBlockingQueue<QueueElement<T>> read;
    private int batch;
    private MyFunctions.ThreeFunction<MyBlockingQueue<QueueElement<T>>, Integer, BufferedReader[]> processor;


    public ReadWorkerThread(MyBlockingQueue<QueueElement<T>> read,
                     int batch,
                     MyFunctions.ThreeFunction<MyBlockingQueue<QueueElement<T>>, Integer, BufferedReader[]> processor,
                     BufferedReader... ins) {
        this.ins = ins;
        this.read = read;
        this.batch = batch;
        this.processor = processor;
    }

    @Override
    public void run() {

        try {
            this.processor.apply(this.read, batch, ins);
            this.read.increment();
            for (BufferedReader in : ins) {
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
