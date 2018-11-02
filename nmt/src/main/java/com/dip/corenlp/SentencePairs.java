package com.dip.corenlp;

import utils.QueueElement;

public class SentencePairs implements QueueElement<String> {
    private final String[] ret = new String[2];

    SentencePairs(String enSentences, String zhSentences) {
        ret[0] = enSentences;
        ret[1] = zhSentences;

    }

    public String[] get() {
        return ret;
    }
}