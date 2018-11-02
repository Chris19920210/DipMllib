package com.dip.corenlp;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import utils.MyPipeline;

import java.util.Properties;

public class MyCoreNLP extends StanfordCoreNLP implements MyPipeline<Annotation> {

    public MyCoreNLP() {
        super();
    }

    MyCoreNLP(Properties props) {
        super(props);
    }

    @Override
    public void dealWith(Annotation A) {
        this.annotate(A);
    }
}
