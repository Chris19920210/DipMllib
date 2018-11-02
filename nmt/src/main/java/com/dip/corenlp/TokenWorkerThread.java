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
    private StanfordCoreNLP zhPipeline;
    private StanfordCoreNLP enPipeline;
    private MyFunctions.SixFunction<StanfordCoreNLP,
            StanfordCoreNLP, QueueElement<String>, Integer,String[], QueueElement<String>> processor;
    private int batch;

    {
        InputStream zhIn = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("StanfordCoreNLP-chinese.properties");
        try {
            zhProps.load(zhIn);
            zhProps.setProperty("annotators", "tokenize,ssplit");
            zhProps.setProperty("ssplit.newlineIsSentenceBreak","always");
            zhProps.setProperty("ssplit.boundaryTokenRegex", "\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try{
                if(zhIn != null) zhIn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            enProps.setProperty("annotators", "tokenize,ssplit,truecase");
            enProps.setProperty("ssplit.newlineIsSentenceBreak","always");
            enProps.setProperty("ssplit.boundaryTokenRegex", "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    TokenWorkerThread(MyBlockingQueue<QueueElement<String>> read,
                             MyBlockingQueue<QueueElement<String>> write,
                             int batch,
                             MyFunctions.SixFunction<StanfordCoreNLP,
                                     StanfordCoreNLP,
                                     QueueElement<String>,
                                     Integer,
                                     String[],
                                     QueueElement<String>> processor){
        this.read = read;
        this.write = write;
        this.zhPipeline = new StanfordCoreNLP(zhProps);
        this.enPipeline = new StanfordCoreNLP(enProps);
        this.processor = processor;
        this.batch = batch;
    }



    @Override
    public void run() {
        String[] results = new String[batch];
        int counter = 0;
        System.out.println("Thread="+ Thread.currentThread().getName() + " is parsing...");
        while(true) {
            try{
                if(this.read.get() > 0 && this.read.isEmpty()){
                    this.write.increment();
                    break;
                }
                QueueElement<String> element = this.read.poll(2, TimeUnit.SECONDS);
                if(element == null){
                    continue;
                }
                counter += 1;
                System.out.println("Thread="+ Thread.currentThread().getName() + " is parsing its "+ counter + " batch");

                QueueElement<String> processed = this.processor.apply(this.enPipeline,
                        this.zhPipeline, element, batch, results);

                this.write.add(processed);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        }

}
