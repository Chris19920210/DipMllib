package utils;


import java.io.FileOutputStream;
import java.util.concurrent.TimeUnit;


public class WriteWorkerThread<T> implements Runnable {
    private MyBlockingQueue<QueueElement<T>> read;
    private int numThreads;
    private FileOutputStream[] outs;
    private MyFunctions.TwoFunction<QueueElement<T>, FileOutputStream[]> processor;

    public WriteWorkerThread(MyBlockingQueue<QueueElement<T>> read,
                      int numThreads,
                      MyFunctions.TwoFunction<QueueElement<T>, FileOutputStream[]> processor
            , FileOutputStream... outs) {
        this.outs = outs;
        this.read = read;
        this.numThreads = numThreads;
        this.processor = processor;
    }


    @Override
    public void run() {
        int counter = 0;
        while (true) {
            try {
                if (this.read.get() == numThreads && this.read.isEmpty()) {
                    break;
                }

                QueueElement<T> pair = this.read.poll(1, TimeUnit.SECONDS);
                if (pair == null) {
                    continue;
                }

                counter += 1;
                this.processor.apply(pair, outs);
                System.out.println("Batch=" + counter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            System.out.println("Done");
            for (FileOutputStream o : outs) {
                o.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
