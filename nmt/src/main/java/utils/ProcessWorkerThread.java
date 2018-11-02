package utils;

import java.util.concurrent.TimeUnit;


public class ProcessWorkerThread implements Runnable {

    private final MyBlockingQueue<QueueElement<String>> read;
    private final MyBlockingQueue<QueueElement<String>> write;

    private MyFunctions.FiveFunction<QueueElement<String>, Integer, String[], MyPipeline[], QueueElement<String>> processor;
    private int batch;
    private MyPipeline[] pipelines;


    public ProcessWorkerThread(MyBlockingQueue<QueueElement<String>> read,
                        MyBlockingQueue<QueueElement<String>> write,
                        int batch,
                        MyFunctions.FiveFunction<QueueElement<String>, Integer, String[], MyPipeline[], QueueElement<String>> processor,
                        MyPipeline... pipelines) {
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
                QueueElement<String> processed = this.processor.apply(element, batch, results, pipelines);

                this.write.add(processed);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
