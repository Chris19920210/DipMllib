package com.dip.corenlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProcessUtils {

    public static void SentencePairWriter(FileOutputStream outEn,FileOutputStream outZh, QueueElement<String> element){
        String[] result = element.get();
        try{
            outEn.write(result[0].getBytes("UTF-8"));
            outEn.write("\n".getBytes("UTF-8"));
            outZh.write(result[1].getBytes("UTF-8"));
            outZh.write("\n".getBytes("UTF-8"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static QueueElement<String> TrueCaseTokenizeProcessor(StanfordCoreNLP enPipeline,
                                                          StanfordCoreNLP zhPipeline,
                                                          QueueElement<String> element,
                                                          int batch,
                                                          String[] results){
        int counter = 0;
        List<String> result = new ArrayList<>();
        Annotation zhDocuments = new Annotation(element.get()[1]);
        zhPipeline.annotate(zhDocuments);
        List<CoreMap> sentenceTmp = zhDocuments.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence: sentenceTmp) {
            counter += 1;
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)){
                result.add(token.word());
            }
            results[counter - 1] = String.join(" ", result);
            result.clear();
        }
        String zhSentences;
        if(counter == batch){
            zhSentences = String.join("\n", results);
        } else {
            zhSentences = String.join("\n", Arrays.asList(results).subList(0, counter));
        }
        Annotation enDocuments = new Annotation(element.get()[0]);
        enPipeline.annotate(enDocuments);
        sentenceTmp = enDocuments.get(CoreAnnotations.SentencesAnnotation.class);
        counter = 0;
        for (CoreMap sentence:sentenceTmp) {
            counter += 1;
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)){
                result.add(token.get(CoreAnnotations.TrueCaseTextAnnotation.class));
            }
            results[counter - 1] = String.join(" ", result);
            result.clear();
        }
        String enSentences;
        if(counter == batch){
            enSentences = String.join("\n", results);
        } else {
            enSentences = String.join("\n", Arrays.asList(results).subList(0, counter));
        }
        return new SentencePairs(enSentences, zhSentences);
    }
}

