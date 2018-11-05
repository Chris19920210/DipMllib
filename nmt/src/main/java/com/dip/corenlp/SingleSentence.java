package com.dip.corenlp;

import utils.QueueElement;

public class SingleSentence implements QueueElement<String> {
    private final String[] ret = new String[1];

    SingleSentence(String sentence) {
        this.ret[0] = sentence;
    }

    @Override
    public String[] get() {
        return ret;
    }
}
