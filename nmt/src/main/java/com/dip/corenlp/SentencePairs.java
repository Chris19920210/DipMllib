package com.dip.corenlp;

public class SentencePairs implements QueueElement<String> {
    private final String enSentences;
    private final String zhSentences;

    SentencePairs(String enSentences, String zhSentences) {
        this.enSentences = enSentences;
        this.zhSentences = zhSentences;
    }

    public String[] get() {
        String[] ret = new String[2];
        ret[0] = enSentences;
        ret[1] = zhSentences;
        return ret;
    }
}