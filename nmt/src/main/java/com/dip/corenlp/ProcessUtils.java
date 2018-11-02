package com.dip.corenlp;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import utils.MyBlockingQueue;
import utils.QueueElement;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProcessUtils {
    public static void SentencePairReader(
            MyBlockingQueue<QueueElement<String>> read,
            int batch,
            BufferedReader[] ins) {
        String str;
        int counter = 0;
        String[] tmp;
        String[] en = new String[batch];
        String[] zh = new String[batch];
        try {
            while ((str = ins[0].readLine()) != null) {
                counter += 1;
                tmp = str.split("\t");
                en[counter % batch - 1 < 0 ? batch - 1 : counter % batch - 1] = tmp[0];
                zh[counter % batch - 1 < 0 ? batch - 1 : counter % batch - 1] = tmp[1];
                if (counter % batch == 0) {
                    read.add(new SentencePairs(String.join("\n", en), String.join("\n", zh)));
                }
            }
            if (counter % batch != 0) {
                read.add(new SentencePairs(String.join("\n", Arrays.asList(en).subList(0, counter % batch)),
                        String.join("\n", Arrays.asList(zh).subList(0, counter % batch))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SentencePairWriter(QueueElement<String> element, FileOutputStream[] outs) {
        String[] result = element.get();
        try {
            outs[0].write(result[0].getBytes("UTF-8"));
            outs[0].write("\n".getBytes("UTF-8"));
            outs[1].write(result[1].getBytes("UTF-8"));
            outs[1].write("\n".getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static QueueElement<String> TrueCaseTokenizeProcessor(QueueElement<String> element,
                                                                 int batch,
                                                                 String[] results,
                                                                 StanfordCoreNLP[] pipelines) {
        int counter = 0;
        List<String> result = new ArrayList<>();
        Annotation zhDocuments = new Annotation(element.get()[1]);
        pipelines[1].annotate(zhDocuments);
        List<CoreMap> sentenceTmp = zhDocuments.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentenceTmp) {
            counter += 1;
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                result.add(token.word());
            }
            results[counter - 1] = String.join(" ", result);
            result.clear();
        }
        String zhSentences;
        if (counter == batch) {
            zhSentences = String.join("\n", results);
        } else {
            zhSentences = String.join("\n", Arrays.asList(results).subList(0, counter));
        }
        Annotation enDocuments = new Annotation(element.get()[0]);
        pipelines[0].annotate(enDocuments);
        sentenceTmp = enDocuments.get(CoreAnnotations.SentencesAnnotation.class);
        counter = 0;
        for (CoreMap sentence : sentenceTmp) {
            counter += 1;
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                result.add(token.get(CoreAnnotations.TrueCaseTextAnnotation.class));
            }
            results[counter - 1] = String.join(" ", result);
            result.clear();
        }
        String enSentences;
        if (counter == batch) {
            enSentences = String.join("\n", results);
        } else {
            enSentences = String.join("\n", Arrays.asList(results).subList(0, counter));
        }
        return new SentencePairs(enSentences, zhSentences);
    }
}

