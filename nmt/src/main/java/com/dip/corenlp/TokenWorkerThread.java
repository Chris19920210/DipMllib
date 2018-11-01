package com.dip.corenlp;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


public class TokenWorkerThread implements Runnable {

    private final MyBlockingQueue<SentencePairs<String,String>> read;
    private final MyBlockingQueue<SentencePairs<String,String>> write;
    private Properties zhProps = new Properties();
    private Properties enProps = new Properties();
    private StanfordCoreNLP zhPipeline;
    private StanfordCoreNLP enPipeline;

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

    public TokenWorkerThread(MyBlockingQueue<SentencePairs<String,String>> read, MyBlockingQueue<SentencePairs<String,String>> write){
        this.read = read;
        this.write = write;
        this.zhPipeline = new StanfordCoreNLP(zhProps);
        this.enPipeline = new StanfordCoreNLP(enProps);
    }



    @Override
    public void run() {
        String enSentences;
        String zhSentences;
        List<String> results = new ArrayList<>();
        List<String> result = new ArrayList<>();
        int counter = 0;
        System.out.println("Thread="+ Thread.currentThread().getName() + " is parsing...");
        while(true) {
            try{
                if(this.read.get() > 0 && this.read.isEmpty()){
                    this.write.increment();
                    break;
                }
                SentencePairs<String, String> SentencesPair = this.read.poll(2, TimeUnit.SECONDS);
                if(SentencesPair == null){
                    continue;
                }
                counter += 1;
                System.out.println("Thread="+ Thread.currentThread().getName() + " is parsing its "+ counter + " batch");

                enSentences = SentencesPair.enSentences;
                zhSentences = SentencesPair.zhSentences;

                Annotation zhDocuments = new Annotation(zhSentences);
                this.zhPipeline.annotate(zhDocuments);
                List<CoreMap> sentenceTmp = zhDocuments.get(CoreAnnotations.SentencesAnnotation.class);
                for (CoreMap sentence: sentenceTmp) {
                    for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)){
                        result.add(token.word());
                    }
                    results.add(String.join(" ", result));
                    result.clear();
                }
                zhSentences = String.join("\n", results);
                results.clear();
                Annotation enDocuments = new Annotation(enSentences);
                this.enPipeline.annotate(enDocuments);
                sentenceTmp = enDocuments.get(CoreAnnotations.SentencesAnnotation.class);
                for (CoreMap sentence:sentenceTmp) {
                    for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)){
                        result.add(token.get(CoreAnnotations.TrueCaseTextAnnotation.class));
                    }
                    results.add(String.join(" ", result));
                    result.clear();
                }
                enSentences = String.join("\n", results);
                this.write.add(new SentencePairs<>(enSentences, zhSentences));
                results.clear();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        }

}
