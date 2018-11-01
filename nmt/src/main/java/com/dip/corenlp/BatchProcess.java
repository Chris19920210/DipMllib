package com.dip.corenlp;


import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BatchProcess {
    private Properties zhProps = new Properties();
    private Properties enProps = new Properties();
    private StanfordCoreNLP zhPipeline;
    private StanfordCoreNLP enPipeline;
    private int batch;
    private FileOutputStream outZh;
    private FileOutputStream outEn;
    private BufferedReader in;


    private BatchProcess(String inputPath, String outputPath, int batch) {
        this.batch = batch;
        try{
            this.outZh = new FileOutputStream(new File(outputPath + ".zh"));
            this.outEn = new FileOutputStream(new File(outputPath + ".en"));
            this.in = new BufferedReader(new FileReader(inputPath));
            this.enPipeline = new StanfordCoreNLP(enProps);
            this.zhPipeline = new StanfordCoreNLP(zhProps);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    {
        InputStream zhIn = BatchProcess.class.getResourceAsStream("/StanfordCoreNLP-chinese.properties");
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



    private void tokenWriter(List<String> en, List<String> zh){
    try{
        List<String> result = new ArrayList<>();
        Annotation enDocuments = new Annotation(String.join("\n", en));
        this.enPipeline.annotate(enDocuments);
        List<CoreMap> sentenceTmp = enDocuments.get(CoreAnnotations.SentencesAnnotation.class);
        en.clear();
        for (CoreMap sentence:sentenceTmp) {
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)){
                result.add(token.get(CoreAnnotations.TrueCaseTextAnnotation.class));
            }
            en.add(String.join(" ", result));
            result.clear();
        }
        outEn.write(String.join("\n", en).getBytes("UTF-8"));
        outEn.write("\n".getBytes("UTF-8"));
        en.clear();
        Annotation zhDocuments = new Annotation(String.join("\n", zh));
        this.zhPipeline.annotate(zhDocuments);
        sentenceTmp = zhDocuments.get(CoreAnnotations.SentencesAnnotation.class);
        zh.clear();
        for (CoreMap sentence: sentenceTmp) {
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)){
                result.add(token.word());
            }
            zh.add(String.join(" ", result));
            result.clear();
        }
        outZh.write(String.join("\n", zh).getBytes("UTF-8"));
        outZh.write("\n".getBytes("UTF-8"));
        zh.clear();
        } catch(Exception e) {
        e.printStackTrace();
    }
}

    private void process() throws Exception {
        String str;
        int counter = 0;
        try {
            ArrayList<String> en = new ArrayList<>(batch);
            ArrayList<String> zh = new ArrayList<>(batch);
            while ((str = in.readLine()) != null) {
                counter += 1;
                String[] tmp = str.split("\t");
                en.add(tmp[0]);
                zh.add(tmp[1]);
                if (counter % batch == 0) {
                    tokenWriter(en, zh);
                    System.out.println("Batch=" + counter/batch);
                }
            }
            if (en.size() != 0) {
                tokenWriter(en, zh);
            }
            in.close();
            outZh.flush();
            outEn.flush();
            System.out.println("Done, with total records="+counter);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main (String[] args) throws Exception{
        String inputPath = args[0];
        String outputPath = args[1];
        int batch = Integer.parseInt(args[2]);
        BatchProcess batchProcess = new BatchProcess(inputPath,outputPath,batch);
        batchProcess.process();
    }
}
