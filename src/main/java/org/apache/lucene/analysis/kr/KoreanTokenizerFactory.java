package org.apache.lucene.analysis.kr;

import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.Version;
import org.apache.solr.analysis.BaseTokenizerFactory;

public class KoreanTokenizerFactory extends BaseTokenizerFactory {
	public Tokenizer create(Reader input) {
		return new KoreanTokenizer(Version.LUCENE_32, input);
	}
}
