package org.apache.lucene.analysis.kr;

import org.apache.lucene.analysis.TokenStream;
import org.apache.solr.analysis.BaseTokenFilterFactory;

public class KoreanFilterFactory extends BaseTokenFilterFactory {
    public TokenStream create(TokenStream input) {
        return new KoreanFilter(input);
    }
}
