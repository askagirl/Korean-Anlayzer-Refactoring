package org.apache.lucene.analysis.kr;


/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

/**
 * Filters {@link StandardTokenizer} with {@link StandardFilter}, {@link
 * LowerCaseFilter} and {@link StopFilter}, using a list of English stop words.
 *
 * @version $Id: KoreanAnalyzer.java,v 1.4 2009/12/22 01:48:56 smlee0818 Exp $
 */
public final class KoreanAnalyzer extends Analyzer {
	  private Set<?> stopSet;
//	 private final CharArraySet stopSet;

	  private boolean bigrammable = true;

	  private boolean hasOrigin = true;

	  /** An array containing some common English words that are usually not
	  useful for searching. */
	  //	  public static final String[] STOP_WORDS = StopAnalyzer.ENGLISH_STOP_WORDS;
	  //	  public static final String[] KOR_STOP_WORDS = new String[]{"이","그","저","것","수","등","들"};
	  public static final Set<?> STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
	  /** An unmodifiable set containing some common Korean words that are not usually useful
	    for searching.*/
	  public static final Set<?> KOR_STOP_WORDS_SET;
	  private final Version matchVersion;

	  public static final String DIC_ENCODING = "UTF-8";
	  /** Default maximum allowed token length */
	    public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;


	  static {
		  final List<String> korStopWords = Arrays.asList("이","그","저","것","수","등","들");
		  final CharArraySet stopSet = new CharArraySet(Version.LUCENE_32, korStopWords.size(), false);
		  stopSet.addAll(korStopWords);
		  KOR_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);
	  }


/*	public KoreanAnalyzer() {
	    this(Version.LUCENE_30, STOP_WORDS_SET);
	}
*/
	public KoreanAnalyzer(Version matchVersion) {
	    this(matchVersion, STOP_WORDS_SET);
	}
	public KoreanAnalyzer(Version matchVersion, Set<?> stopWords) {
		final CharArraySet stopSets = new CharArraySet(matchVersion, stopWords.size(), false);
		stopSets.addAll(KOR_STOP_WORDS_SET);
		stopSets.addAll(stopWords);

		this.stopSet = stopSets;
	    this.matchVersion = matchVersion;
	}

  /** Builds an analyzer with the stop words from the given file.
   * @see WordlistLoader#getWordSet(File)
   */
	public KoreanAnalyzer(Version matchVersion, File stopwords) throws IOException {
		this(matchVersion, KoreanWordlistLoader.getWordSet(stopwords));
	}

  /** Builds an analyzer with the stop words from the given file.
   * @see WordlistLoader#getWordSet(File)
   */
	public KoreanAnalyzer(Version matchVersion, File stopwords, String encoding) throws IOException {
		this(matchVersion, KoreanWordlistLoader.getWordSet(stopwords, encoding));
	}

	/** Builds an analyzer with the stop words from the given reader.
	 * @see WordlistLoader#getWordSet(Reader)
	*/
	public KoreanAnalyzer(Version matchVersion, Reader stopwords) throws IOException {
		this(matchVersion, KoreanWordlistLoader.getWordSet(stopwords));
	}

	public TokenStream tokenStream(String fieldName, Reader reader) {
		TokenStream result = null;
		KoreanTokenizer tokenStream = new KoreanTokenizer(matchVersion, reader);
		result = tokenStream;
		result = new KoreanFilter(tokenStream, bigrammable, hasOrigin);
	    result = new LowerCaseFilter(matchVersion, result);
	    result = new StopFilter(matchVersion, result, stopSet);
	    return result;
	}

	/**
	 * determine whether the bigram index term is returned or not if a input word is failed to analysis
	 * If true is set, the bigram index term is returned. If false is set, the bigram index term is not returned.
	 * @param is
	 */
	public void setBigrammable(boolean is) {
		bigrammable = is;
	}

	/**
	 * determin whether the original term is returned or not if a input word is analyzed morphically.
	 * @param has
	 */
	public void setHasOrigin(boolean has) {
		hasOrigin = has;
	}
}
