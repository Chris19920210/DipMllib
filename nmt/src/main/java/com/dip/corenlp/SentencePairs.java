package com.dip.corenlp;

public class SentencePairs<A, B> {
    public final A enSentences;
    public final B zhSentences;

    public SentencePairs(A enSentences, B zhSentences) {
        this.enSentences = enSentences;
        this.zhSentences = zhSentences;
    }
}